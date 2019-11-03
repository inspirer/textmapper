package grammar

import (
	"fmt"
	"regexp"
	"sort"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/tm-go/lex"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/container"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"github.com/inspirer/textmapper/tm-parsers/tm/selector"
)

// Compile validates and compiles grammar files.
func Compile(file ast.File) (*Grammar, error) {
	targetLang, _ := file.Header().Target()
	c := &compiler{
		file: file,
		out: &Grammar{
			Name:       file.Header().Name().Text(),
			TargetLang: targetLang.Text(),
			Lexer:      &Lexer{},
			Options: &Options{
				TokenLine: true,
			},
		},
		syms:       make(map[string]int),
		ids:        make(map[string]string),
		codeRule:   make(map[symRule]int),
		codeAction: make(map[symAction]int),
		params:     make(map[string]int),
		nonterms:   make(map[string]int),
	}
	c.parseOptions()
	c.compileLexer()
	c.compileParser()

	c.resolveOptions()

	tpl := strings.TrimPrefix(file.Child(selector.Templates).Text(), "%%")
	c.out.CustomTemplates = parseInGrammarTemplates(tpl)
	return c.out, c.s.Err()
}

type compiler struct {
	file ast.File
	out  *Grammar
	s    status.Status

	syms map[string]int
	ids  map[string]string // ID -> name

	// Lexer
	conds        map[string]int
	inclusiveSC  []int
	patterns     []*patterns // to keep track of unused patterns
	classRules   []*lex.Rule
	rules        []*lex.Rule
	codeRule     map[symRule]int   // -> index in c.out.RuleToken
	codeAction   map[symAction]int // -> index in c.out.Actions
	reportTokens map[string]bool
	reportList   []ast.Identifier

	// Parser
	source   *syntax.Model
	params   map[string]int // -> index in source.Params
	nonterms map[string]int // -> index in source.Nonterms
}

type symAction struct {
	code  string
	space bool
}

type symRule struct {
	code  string
	space bool
	class bool
	sym   int
}

var noSpace = ast.LexemeAttribute{}

func (c *compiler) compileLexer() {
	out := c.out.Lexer

	eoi := c.addToken(Eoi, "EOI", ast.RawType{}, noSpace, nil)
	out.InvalidToken = c.addToken(InvalidToken, "INVALID_TOKEN", ast.RawType{}, noSpace, nil)

	c.collectStartConds()
	lexer, _ := c.file.Lexer()
	c.traverseLexer(lexer.LexerPart(), c.inclusiveSC, nil /*parent patterns*/)
	c.resolveTokenComments()
	c.resolveClasses()
	c.out.NumTokens = len(c.out.Syms)

	inline := c.canInlineRules()
	if !inline {
		// Prepend EOI and InvalidToken to the rule token array to simplify handling of -1 and -2
		// actions.
		out.RuleToken = append(out.RuleToken, 0, 0)
		copy(out.RuleToken[2:], out.RuleToken)
		out.RuleToken[0] = out.InvalidToken
		out.RuleToken[1] = eoi
	}

	var err error
	out.Tables, err = lex.Compile(c.rules)
	c.s.AddError(err)

	if inline {
		// There is at most one action per token, so it is possible to use tokens IDs as actions.
		// We need to swap EOI and InvalidToken actions.
		// TODO simplify
		start := out.Tables.ActionStart()
		for i, val := range out.Tables.Dfa {
			if val == start {
				out.Tables.Dfa[i] = start - 1
			} else if val == start-1 {
				out.Tables.Dfa[i] = start
			} else if val < start {
				out.Tables.Dfa[i] = start - out.RuleToken[start-val-2]
			}
		}
		for i, val := range out.Tables.Backtrack {
			switch val.Action {
			case 0:
				out.Tables.Backtrack[i].Action = out.InvalidToken
			case 1:
				out.Tables.Backtrack[i].Action = eoi
			default:
				out.Tables.Backtrack[i].Action = out.RuleToken[val.Action-2]
			}
		}
		for i, val := range out.ClassActions {
			out.ClassActions[i].Action = out.RuleToken[val.Action]
			for k, v := range val.Custom {
				val.Custom[k] = out.RuleToken[v]
			}
		}
		for i, val := range out.Actions {
			out.Actions[i].Action = out.RuleToken[val.Action]
		}
		out.RuleToken = nil
	}

	for _, p := range c.patterns {
		for name, unused := range p.unused {
			c.errorf(unused, "unused pattern '%v'", name)
		}
	}
}

// canInlineRules decides whether we can replace rule ids directly with token ids.
func (c *compiler) canInlineRules() bool {
	// Note: the first two actions are reserved for InvalidToken and EOI respectively.
	seen := container.NewBitSet(c.out.NumTokens)
	for _, e := range c.out.RuleToken {
		if e < 2 || seen.Get(e) {
			return false
		}
		seen.Set(e)
	}
	// TODO inline rules with actions
	for _, a := range c.out.Lexer.Actions {
		if a.Code != "" {
			return false
		}
	}
	return true
}

func (c *compiler) collectStartConds() {
	conds := make(map[string]bool)
	var names []string

	insert := func(n *ast.Node, excl bool) {
		name := n.Text()
		if _, exists := conds[name]; exists {
			c.errorf(n, "redeclaration of '%v'", name)
			return
		}
		conds[name] = excl
		names = append(names, name)
	}
	lexer, _ := c.file.Lexer()
	for _, p := range lexer.LexerPart() {
		switch p := p.(type) {
		case *ast.ExclusiveStartConds:
			for _, s := range p.States() {
				insert(s.Node, true)
			}
		case *ast.InclusiveStartConds:
			for _, s := range p.States() {
				insert(s.Node, false)
			}
		}
	}

	if _, exists := conds[lex.Initial]; !exists {
		names = append(names, lex.Initial)
		copy(names[1:], names)
		names[0] = lex.Initial
	}

	c.out.StartConditions = names
	c.conds = make(map[string]int)
	for i, name := range names {
		c.conds[name] = i
		if !conds[name] {
			c.inclusiveSC = append(c.inclusiveSC, i)
		}
	}
}

func (c *compiler) resolveSC(sc ast.StartConditions) []int {
	var ret []int
	refs := sc.Stateref()
	if len(refs) == 0 {
		// <*>
		for i := range c.out.StartConditions {
			ret = append(ret, i)
		}
		return ret
	}
	for _, ref := range refs {
		name := ref.Name().Text()
		if val, ok := c.conds[name]; ok {
			ret = append(ret, val)
			continue
		}
		c.errorf(ref.Name(), "unresolved reference %v", name)
	}
	sort.Ints(ret)
	return ret
}

func (c *compiler) addToken(name, id string, t ast.RawType, space ast.LexemeAttribute, n status.SourceNode) int {
	var rawType string
	if t.IsValid() {
		rawType = t.Text()
	}
	if i, exists := c.syms[name]; exists {
		sym := c.out.Syms[i]
		if sym.Type != rawType {
			anchor := n
			if t.IsValid() {
				anchor = t
			}
			c.errorf(anchor, "terminal type redeclaration for %v, was %v", name, sym.PrettyType())
		}
		if sym.Space != space.IsValid() {
			anchor := n
			if space.IsValid() {
				anchor = space
			}
			c.errorf(anchor, "%v is declared as both a space and non-space terminal", name)
		}
		return sym.Index
	}
	if id == "" {
		id = SymbolID(name, UpperCase)
	}
	if prev, exists := c.ids[id]; exists {
		c.errorf(n, "%v and %v get the same ID in generated code", name, prev)
	}

	sym := Symbol{
		Index:  len(c.syms),
		ID:     id,
		Name:   name,
		Type:   rawType,
		Space:  space.IsValid(),
		Origin: n,
	}
	c.syms[name] = sym.Index
	c.ids[id] = name
	c.out.Syms = append(c.out.Syms, sym)
	return sym.Index
}

func (c *compiler) addLexerAction(cmd ast.Command, space, class ast.LexemeAttribute, sym int, comment string) int {
	if !cmd.IsValid() && !space.IsValid() && !class.IsValid() {
		if sym == int(lex.EOI) {
			return -1
		} else if sym == c.out.InvalidToken {
			return -2
		}
	}

	out := c.out.Lexer
	key := symRule{code: cmd.Text(), space: space.IsValid(), class: class.IsValid(), sym: sym}
	if a, ok := c.codeRule[key]; ok {
		if ca, ok := c.codeAction[symAction{key.code, key.space}]; ok && comment != "" {
			out.Actions[ca].Comments = append(out.Actions[ca].Comments, comment)
		}
		return a
	}
	a := len(out.RuleToken)
	out.RuleToken = append(out.RuleToken, sym)
	c.codeRule[key] = a

	if !cmd.IsValid() && !space.IsValid() {
		return a
	}
	act := SemanticAction{Action: a, Code: key.code, Space: space.IsValid()}
	if comment != "" {
		act.Comments = append(act.Comments, comment)
	}
	if cmd.IsValid() {
		act.Origin = cmd
	} else {
		act.Origin = space
	}
	c.codeAction[symAction{key.code, key.space}] = len(out.Actions)
	out.Actions = append(out.Actions, act)
	return a
}

func (c *compiler) traverseLexer(parts []ast.LexerPart, defaultSCs []int, p *patterns) {
	inClause := p != nil
	ps := &patterns{
		parent: p,
		set:    make(map[string]*lex.Regexp),
		unused: make(map[string]status.SourceNode),
	}
	c.patterns = append(c.patterns, ps)

	for _, p := range parts {
		switch p := p.(type) {
		case *ast.Lexeme:
			rawType, _ := p.RawType()
			var space, class ast.LexemeAttribute
			if attrs, ok := p.Attrs(); ok {
				switch name := attrs.LexemeAttribute().Text(); name {
				case "class":
					class = attrs.LexemeAttribute()
				case "space":
					space = attrs.LexemeAttribute()
				default:
					c.errorf(attrs.LexemeAttribute(), "unsupported attribute")
				}
			}

			name := p.Name().Text()
			tok := c.addToken(name, "" /*id*/, rawType, space, p.Name())
			pat, ok := p.Pattern()
			if !ok {
				break
			}

			re, err := parsePattern(pat)
			c.s.AddError(err)
			rule := &lex.Rule{
				RE:              re,
				StartConditions: defaultSCs,
				Resolver:        ps,
				Origin:          p,
				OriginName:      name,
				RegexpText:      pat.Text(),
			}
			comment := fmt.Sprintf("%v: %v", name, rule.RegexpText)

			if prio, ok := p.Priority(); ok {
				rule.Precedence, _ = strconv.Atoi(prio.Text())
			}
			if sc, ok := p.StartConditions(); ok {
				rule.StartConditions = c.resolveSC(sc)
			}

			cmd, _ := p.Command()
			if c.reportTokens[name] && space.IsValid() {
				// This token needs to be reported from the lexer to appear in the AST. It will be ignored
				// in the parser.
				space = noSpace
			}
			rule.Action = c.addLexerAction(cmd, space, class, tok, comment)
			if class.IsValid() {
				c.classRules = append(c.classRules, rule)
			} else {
				c.rules = append(c.rules, rule)
			}
		case *ast.NamedPattern:
			c.s.AddError(ps.add(p))
		case *ast.StartConditionsScope:
			newDefaults := c.resolveSC(p.StartConditions())
			c.traverseLexer(p.LexerPart(), newDefaults, ps)
		case *ast.SyntaxProblem, *ast.DirectiveBrackets:
			c.errorf(p.TmNode(), "syntax error")
		case *ast.ExclusiveStartConds, *ast.InclusiveStartConds:
			if inClause {
				// %s and %x are not allowed inside start condition clauses.
				c.errorf(p.TmNode(), "syntax error")
			}
		}
	}
}

func (c *compiler) resolveTokenComments() {
	comments := make(map[int]string)
	for _, r := range c.rules {
		if r.Action < 0 {
			continue
		}
		tok := c.out.RuleToken[r.Action]
		val, _ := r.RE.Constant()
		if old, ok := comments[tok]; ok && val != old {
			comments[tok] = ""
			continue
		}
		comments[tok] = val
	}
	for tok, val := range comments {
		c.out.Syms[tok].Comment = val
	}
}

func (c *compiler) resolveClasses() {
	if len(c.classRules) == 0 {
		return
	}

	var rewritten []*lex.Rule
	for index, r := range c.classRules {
		fork := new(lex.Rule)
		*fork = *r
		fork.Action = index
		rewritten = append(rewritten, fork)
	}
	tables, err := lex.Compile(rewritten)
	c.s.AddError(err)
	if err != nil {
		// Pretend that these class rules do not exist in the grammar and keep going.
		return
	}

	// Pre-create class actions.
	for _, r := range c.classRules {
		ca := ClassAction{
			Action: r.Action,
			Custom: make(map[string]int),
		}
		c.out.ClassActions = append(c.out.ClassActions, ca)
	}

	out := c.rules[:0]
	for _, r := range c.rules {
		val, isConst := r.RE.Constant()
		if !isConst {
			out = append(out, r)
			continue
		}

		classRule := -1
		for _, start := range r.StartConditions {
			if start >= len(tables.StateMap) {
				// This start condition is not covered by class rules.
				continue
			}
			size, result := tables.Scan(start, val)
			if size == len(val) && result >= 0 {
				classRule = result
				break
			}
		}
		if classRule == -1 {
			out = append(out, r)
			continue
		}
		class := c.classRules[classRule]

		if !container.SliceEqual(class.StartConditions, r.StartConditions) {
			c.errorf(r.Origin, "%v must be applicable in the same set of start conditions as %v", r.OriginName, class.OriginName)

			// Fixing the problem for now and keep going.
			r.StartConditions = class.StartConditions
		}

		// Move the rule under its class rule.
		c.out.ClassActions[classRule].Custom[val] = r.Action
	}

	for i, r := range c.classRules {
		if len(c.out.ClassActions[i].Custom) == 0 {
			c.errorf(r.Origin, "class rule without specializations '%v'", r.OriginName)
		}
	}

	c.rules = append(out, c.classRules...)
	c.classRules = nil
}

func (c *compiler) errorf(n status.SourceNode, format string, a ...interface{}) {
	c.s.Errorf(n, format, a...)
}

func (c *compiler) parseTokenList(e ast.Expression) []ast.Identifier {
	if arr, ok := e.(*ast.Array); ok {
		var ret []ast.Identifier
		for _, el := range arr.Expression() {
			if ref, ok := el.(*ast.Symref); ok {
				if args, ok := ref.Args(); ok {
					c.errorf(args, "terminals cannot be templated")
					continue
				}
				ret = append(ret, ref.Name())
				continue
			}
			c.errorf(el.TmNode(), "symbol reference is expected")
		}
		return ret
	}
	c.errorf(e.TmNode(), "list of symbols is expected")
	return nil
}

func (c *compiler) parseExpr(e ast.Expression, defaultVal interface{}) interface{} {
	switch e := e.(type) {
	case *ast.Array:
		if _, ok := defaultVal.([]string); ok {
			var ret []string
			for _, el := range e.Expression() {
				if lit, ok := el.(*ast.StringLiteral); ok {
					s, err := strconv.Unquote(lit.Text())
					if err != nil {
						c.errorf(el.TmNode(), "cannot parse string literal: %v", err)
						continue
					}
					ret = append(ret, s)
					continue
				}
				c.errorf(el.TmNode(), "string is expected")
			}
			return ret
		}
	case *ast.BooleanLiteral:
		if _, ok := defaultVal.(bool); ok {
			return e.Text() == "true"
		}
	case *ast.StringLiteral:
		if _, ok := defaultVal.(string); ok {
			s, err := strconv.Unquote(e.Text())
			if err != nil {
				c.errorf(e, "cannot parse string literal: %v", err)
				return defaultVal
			}
			return s
		}
	}
	switch defaultVal.(type) {
	case []int:
		c.errorf(e.TmNode(), "list of symbols is expected")
	default:
		c.errorf(e.TmNode(), "%T is expected", defaultVal)
	}
	return defaultVal
}

func (c *compiler) collectParams(p ast.ParserSection) {
	for _, part := range p.GrammarPart() {
		if param, ok := part.(*ast.TemplateParam); ok {
			name := param.Name().Text()
			if _, exists := c.params[name]; exists {
				c.errorf(param.Name(), "redeclaration of '%v'", name)
				continue
			}
			if _, exists := c.syms[name]; exists {
				c.errorf(param.Name(), "template parameters cannot be named after terminals '%v'", name)
				continue
			}
			p := syntax.Param{Name: name, Origin: param.Name()}
			if val, ok := param.ParamValue(); ok {
				switch val := val.(type) {
				case *ast.BooleanLiteral:
					p.DefaultValue = val.Text()
				default:
					c.errorf(val.TmNode(), "unsupported default value")
				}
			}
			if mod, ok := param.Modifier(); ok {
				if mod.Text() == "lookahead" {
					p.Lookahead = true
				} else {
					c.errorf(mod, "unsupported syntax")
				}
			}
			c.params[name] = len(c.source.Params)
			c.source.Params = append(c.source.Params, p)
		}
	}
}

func (c *compiler) collectNonterms(p ast.ParserSection) {
	for _, part := range p.GrammarPart() {
		if nonterm, ok := part.(*ast.Nonterm); ok {
			name := nonterm.Name().Text()
			if _, exists := c.syms[name]; exists {
				c.errorf(nonterm.Name(), "redeclaration of '%v'", name)
				continue
			}
			if _, exists := c.nonterms[name]; exists {
				c.errorf(nonterm.Name(), "redeclaration of '%v'", name)
				continue
			}
			if _, exists := c.params[name]; exists {
				c.errorf(nonterm.Name(), "redeclaration of a template parameter '%v'", name)
				continue
			}
			id := SymbolID(name, CamelCase)
			if prev, exists := c.ids[id]; exists {
				c.errorf(nonterm.Name(), "%v and %v get the same ID in generated code", name, prev)
			}
			if ann, ok := nonterm.Annotations(); ok {
				c.errorf(ann.TmNode(), "unsupported syntax")
			}

			nt := syntax.Nonterm{
				Name:   name,
				Origin: nonterm,
			}
			if t, ok := nonterm.NontermType(); ok {
				rt, _ := t.(*ast.RawType)
				if rt != nil {
					nt.Type = rt.Text()
				} else {
					c.errorf(t.TmNode(), "unsupported syntax")
				}
			}

			var seen map[string]bool
			params, _ := nonterm.Params()
			for _, param := range params.List() {
				if seen == nil {
					seen = make(map[string]bool)
				}
				switch param := param.(type) {
				case *ast.ParamRef:
					name := param.Identifier().Text()
					if seen[name] {
						c.errorf(param, "duplicate parameter reference '%v'", name)
						continue
					}
					seen[name] = true
					if i, ok := c.params[name]; ok {
						nt.Params = append(nt.Params, i)
						continue
					}
					c.errorf(param, "unresolved parameter reference '%v'", name)
				case *ast.InlineParameter:
					name := param.Name().Text()
					if _, exists := c.params[name]; exists {
						c.errorf(param.Name().TmNode(), "redeclaration of '%v'", name)
						continue
					}
					p := syntax.Param{Name: name, Origin: param.Name().TmNode()}
					if val, ok := param.ParamValue(); ok {
						switch val := val.(type) {
						case *ast.BooleanLiteral:
							p.DefaultValue = val.Text()
						default:
							c.errorf(val.TmNode(), "unsupported default value")
						}
					}
					nt.Params = append(nt.Params, len(c.source.Params))
					c.source.Params = append(c.source.Params, p)
				}
			}
			c.ids[id] = name
			c.nonterms[name] = len(c.source.Nonterms)
			c.source.Nonterms = append(c.source.Nonterms, nt)
		}
	}
}

func (c *compiler) collectInputs(p ast.ParserSection) {
	for _, part := range p.GrammarPart() {
		if input, ok := part.(*ast.DirectiveInput); ok {
			for _, ref := range input.InputRefs() {
				name := ref.Reference().Name()
				nonterm, found := c.nonterms[name.Text()]
				if !found {
					c.errorf(name, "unresolved nonterminal '%v'", name.Text())
					continue
				}
				if len(c.source.Nonterms[nonterm].Params) > 0 {
					c.errorf(name, "input nonterminals cannot be parametrized")
				}
				// TODO support no-eoi inputs
				c.source.Inputs = append(c.source.Inputs, syntax.Input{Symbol: nonterm})
			}
		}
	}

	if len(c.source.Inputs) > 0 {
		return
	}

}

func (c *compiler) compileParser() {
	p, ok := c.file.Parser()
	if !ok {
		return
	}
	c.source = new(syntax.Model)
	for _, sym := range c.out.Syms {
		c.source.Terminals = append(c.source.Terminals, sym.Name)
	}
	c.collectParams(p)
	c.collectNonterms(p)
	c.collectInputs(p)
	// TODO
}

func (c *compiler) parseOptions() {
	opts := c.out.Options
	seen := make(map[string]int)
	for _, opt := range c.file.Options() {
		kv, ok := opt.(*ast.KeyValue)
		if !ok {
			continue
		}
		name := kv.Key().Text()
		if line, ok := seen[name]; ok {
			c.errorf(kv.Key(), "reinitialization of '%v', previously declared on line %v", name, line)
		}
		line, _ := kv.Key().LineColumn()
		seen[name] = line
		switch name {
		case "package":
			opts.Package = c.parseExpr(kv.Value(), opts.Package).(string)
		case "genCopyright":
			opts.Copyright = c.parseExpr(kv.Value(), opts.Copyright).(bool)
		case "tokenLine":
			opts.TokenLine = c.parseExpr(kv.Value(), opts.TokenLine).(bool)
		case "tokenLineOffset":
			opts.TokenLineOffset = c.parseExpr(kv.Value(), opts.TokenLineOffset).(bool)
		case "cancellable":
			opts.Cancellable = c.parseExpr(kv.Value(), opts.Cancellable).(bool)
		case "recursiveLookaheads":
			opts.RecursiveLookaheads = c.parseExpr(kv.Value(), opts.RecursiveLookaheads).(bool)
		case "eventBased":
			opts.EventBased = c.parseExpr(kv.Value(), opts.EventBased).(bool)
		case "debugParser":
			opts.DebugParser = c.parseExpr(kv.Value(), opts.DebugParser).(bool)
		case "eventFields":
			opts.EventFields = c.parseExpr(kv.Value(), opts.EventFields).(bool)
		case "eventAST":
			opts.EventAST = c.parseExpr(kv.Value(), opts.EventAST).(bool)
		case "reportTokens":
			c.reportList = c.parseTokenList(kv.Value())
			c.reportTokens = make(map[string]bool)
			for _, id := range c.reportList {
				c.reportTokens[id.Text()] = true
			}
		case "extraTypes":
			opts.ExtraTypes = c.parseExpr(kv.Value(), opts.ExtraTypes).([]string)
		case "fileNode":
			opts.FileNode = c.parseExpr(kv.Value(), opts.FileNode).(string)
		case "lang":
			// This option often occurs in existing grammars. Ignore it.
			c.parseExpr(kv.Value(), "")
		default:
			c.errorf(kv.Key(), "unknown option '%v'", name)
		}
	}
}

func (c *compiler) resolveOptions() {
	opts := c.out.Options
	opts.ReportTokens = make([]int, 0, len(c.reportList))
	for _, id := range c.reportList {
		sym, ok := c.syms[id.Text()]
		if !ok {
			c.errorf(id, "unresolved reference '%v'", id.Text())
			continue
		}
		opts.ReportTokens = append(opts.ReportTokens, sym)
	}
}

type patterns struct {
	parent *patterns
	set    map[string]*lex.Regexp
	unused map[string]status.SourceNode
}

func (p *patterns) Resolve(name string) *lex.Regexp {
	if v, ok := p.set[name]; ok {
		delete(p.unused, name)
		return v
	}
	if p.parent != nil {
		return p.parent.Resolve(name)
	}
	return nil
}

var emptyRE = lex.MustParse("")

func (p *patterns) add(np *ast.NamedPattern) error {
	name := np.Name().Text()
	if _, exists := p.set[name]; exists {
		return status.Errorf(np.Name(), "redeclaration of '%v'", name)
	}

	re, err := parsePattern(np.Pattern())
	p.set[name] = re
	p.unused[name] = np.Name()
	return err
}

func parsePattern(p ast.Pattern) (*lex.Regexp, error) {
	text := p.Text()
	text = text[1 : len(text)-1]
	re, err := lex.ParseRegexp(text)
	if err != nil {
		rng := p.SourceRange()
		err := err.(lex.ParseError)
		if err.Offset <= err.EndOffset && err.EndOffset <= len(text) && err.Offset < len(text) {
			if err.Offset < err.EndOffset {
				rng.EndOffset = rng.Offset + err.EndOffset + 1
			} else {
				rng.EndOffset--
			}
			rng.Offset += err.Offset + 1
			rng.Column += err.Offset + 1
		}
		return emptyRE, &status.Error{Origin: rng, Msg: err.Error()}
	}
	return re, nil
}

var tplMap = map[string]string{
	"go_lexer.stateVars":     "stateVars",
	"go_lexer.initStateVars": "initStateVars",
	"go_lexer.onAfterNext":   "onAfterNext",
	"go_lexer.onBeforeNext":  "onBeforeNext",
}

var tplRE = regexp.MustCompile(`(?s)\${template ([\w.]+)(-?)}(.*?)\${end}`)

// parseInGrammarTemplates converts old Textmapper templates into Go ones.
// TODO get rid of this function after deleting the Java implementation
func parseInGrammarTemplates(templates string) string {
	const start = "${template newTemplates-}"
	const end = "${end}"

	var buf strings.Builder
	if i := strings.Index(templates, start); i >= 0 {
		templates := templates[i+len(start):]
		i = strings.Index(templates, end)
		if i >= 0 {
			buf.WriteString(templates[:i])
		}
	}

	for _, match := range tplRE.FindAllStringSubmatch(templates, -1) {
		name, content := match[1], match[3]
		if name, ok := tplMap[name]; ok {
			fmt.Fprintf(&buf, `{{define "%v"}}%v{{- end}}`, name, content)
		}
	}
	return buf.String()
}
