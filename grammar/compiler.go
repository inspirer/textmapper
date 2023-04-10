package grammar

import (
	"fmt"
	"log"
	"regexp"
	"sort"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/lex"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/ident"
)

// Compile validates and compiles grammar files.
func Compile(file ast.File, compat bool) (*Grammar, error) {
	c := newCompiler(file, compat)
	c.parseOptions()
	c.compileLexer()

	c.resolveOptions()
	c.compileParser()

	tpl := strings.TrimPrefix(file.Child(selector.Templates).Text(), "%%")
	c.out.CustomTemplates = parseInGrammarTemplates(tpl)
	return c.out, c.s.Err()
}

type compiler struct {
	file   ast.File
	out    *Grammar
	s      status.Status
	compat bool

	syms map[string]int
	ids  map[string]string // ID -> name

	// Lexer
	conds        map[string]int
	inclusiveSC  []int
	patterns     []*patterns // to keep track of unused patterns
	classRules   []*lex.Rule
	rules        []*lex.Rule
	codeRule     map[symRule]int   // -> index in c.out.Lexer.RuleToken
	codeAction   map[symAction]int // -> index in c.out.Lexer.Actions
	reportTokens map[string]bool
	reportList   []ast.Identifier

	// Parser
	source    *syntax.Model
	namedSets map[string]int // -> index in source.Sets
	asserts   []assert
	params    map[string]int // -> index in source.Params
	nonterms  map[string]int // -> index in source.Nonterms
	cats      map[string]int // -> index in source.Cats
	paramPerm []int          // for parameter permutations
	expectSR  int
	expectRR  int
	rhsPos    int // Counter for positional index of a reference in the current rule.
	rhsNames  map[string]int
}

func newCompiler(file ast.File, compat bool) *compiler {
	targetLang, _ := file.Header().Target()
	return &compiler{
		file: file,
		out: &Grammar{
			Name:       file.Header().Name().Text(),
			TargetLang: targetLang.Text(),
			Lexer:      &Lexer{},
			Parser:     &Parser{},
			Options: &Options{
				TokenLine: true,
			},
		},
		compat:     compat,
		syms:       make(map[string]int),
		ids:        make(map[string]string),
		codeRule:   make(map[symRule]int),
		codeAction: make(map[symAction]int),
		namedSets:  make(map[string]int),
		params:     make(map[string]int),
		nonterms:   make(map[string]int),
		cats:       make(map[string]int),
	}
}

type assert struct {
	index int // in source.Sets
	empty bool
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
	allowBacktracking := !c.out.Options.NonBacktracking
	out.Tables, err = lex.Compile(c.rules, allowBacktracking)
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
		rawType = strings.TrimSuffix(strings.TrimPrefix(t.Text(), "{"), "}")
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
		id = ident.Produce(name, ident.UpperCase)
	}
	if prev, exists := c.ids[id]; exists {
		c.errorf(n, "%v and %v get the same ID in generated code", name, prev)
	}

	sym := Symbol{
		Index:  len(c.out.Syms),
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

func (c *compiler) addNonterms(m *syntax.Model) {
	// TODO error is also nullable - make it so!
	nullable := syntax.Nullable(m)
	nonterms := m.Nonterms

	for _, nt := range nonterms {
		name := nt.Name
		if _, ok := c.syms[name]; ok {
			// TODO come up with a better error message
			c.errorf(nt.Origin, "duplicate name %v", name)
		}
		id := ident.Produce(name, ident.CamelCase)
		if prev, exists := c.ids[id]; exists {
			c.errorf(nt.Origin, "%v and %v get the same ID in generated code", name, prev)
		}
		index := len(c.out.Syms)
		sym := Symbol{
			Index:     index,
			ID:        id,
			Name:      name,
			Type:      nt.Type,
			CanBeNull: nullable.Get(index),
			Origin:    nt.Origin,
		}
		c.syms[name] = sym.Index
		c.ids[id] = name
		c.out.Syms = append(c.out.Syms, sym)
	}
}

func addSyntheticInputs(m *syntax.Model, compat bool) {
	seen := make(map[lalr.Sym]bool)
	size := len(m.Inputs)
	for _, inp := range m.Inputs {
		if inp.NoEoi {
			seen[lalr.Sym(inp.Nonterm+len(m.Terminals))] = true
		}
	}
	for _, nt := range m.Nonterms {
		if nt.Value.Kind != syntax.Lookahead {
			continue
		}
		for _, sub := range nt.Value.Sub {
			if sub.Kind == syntax.LookaheadNot {
				sub = sub.Sub[0]
			}
			if sub.Kind != syntax.Reference || sub.Symbol < len(m.Terminals) {
				log.Fatalf("%v is not properly instantiated: %v", nt.Name, sub)
			}
			key := lalr.Sym(sub.Symbol)
			if !seen[key] {
				m.Inputs = append(m.Inputs, syntax.Input{
					Nonterm:   sub.Symbol - len(m.Terminals),
					NoEoi:     true,
					Synthetic: true,
				})
				seen[key] = true
			}
		}
	}
	if compat {
		// Textmapper Java puts synthetic inputs before user ones.
		m.Inputs = append(m.Inputs[size:], m.Inputs[:size]...)
	}
}

func (c *compiler) addLexerAction(cmd ast.Command, space, class ast.LexemeAttribute, sym int, comment string) int {
	if !cmd.IsValid() && !space.IsValid() && !class.IsValid() && !c.compat {
		if sym == int(lex.EOI) {
			return -1
		} else if sym == c.out.InvalidToken {
			return -2
		}
	}

	out := c.out.Lexer
	key := symRule{code: cmd.Text(), space: space.IsValid(), class: class.IsValid(), sym: sym}
	if a, ok := c.codeRule[key]; ok && !c.compat {
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
		set:    make(map[string]*lex.Pattern),
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

			pattern, err := parsePattern(name, pat)
			if err != nil {
				c.s.AddError(err)
				continue
			}
			rule := &lex.Rule{
				Pattern:         pattern,
				StartConditions: defaultSCs,
				Resolver:        ps,
				Origin:          p,
			}
			comment := fmt.Sprintf("%v: /%v/", name, rule.Pattern.Text)

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
		val, _ := r.Pattern.RE.Constant()
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
	tables, err := lex.Compile(rewritten, true /*allowBacktracking*/)
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
		val, isConst := r.Pattern.RE.Constant()
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
			c.errorf(r.Origin, "%v must be applicable in the same set of start conditions as %v", r.Pattern.Name, class.Pattern.Name)

			// Fixing the problem for now and keep going.
			r.StartConditions = class.StartConditions
		}

		// Move the rule under its class rule.
		c.out.ClassActions[classRule].Custom[val] = r.Action
	}

	for i, r := range c.classRules {
		if len(c.out.ClassActions[i].Custom) == 0 {
			c.errorf(r.Origin, "class rule without specializations '%v'", r.Pattern.Name)
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

func (c *compiler) parseExpr(e ast.Expression, defaultVal any) any {
	switch e := e.(type) {
	case *ast.Array:
		if _, ok := defaultVal.([]string); ok {
			var ret []string
			for _, el := range e.Expression() {
				lit, ok := el.(*ast.StringLiteral)
				if !ok {
					c.errorf(el.TmNode(), "string is expected")
					continue
				}
				s, err := strconv.Unquote(lit.Text())
				if err != nil {
					c.errorf(el.TmNode(), "cannot parse string literal: %v", err)
					continue
				}
				ret = append(ret, s)
			}
			return ret
		}
		if _, ok := defaultVal.([]syntax.ExtraType); ok {
			var ret []syntax.ExtraType
			for _, el := range e.Expression() {
				lit, ok := el.(*ast.StringLiteral)
				if !ok {
					c.errorf(el.TmNode(), "string is expected")
					continue
				}
				s, err := strconv.Unquote(lit.Text())
				if err != nil {
					c.errorf(el.TmNode(), "cannot parse string literal: %v", err)
					continue
				}
				ids := strings.Split(s, "->")
				for i, id := range ids {
					id := strings.TrimSpace(id)
					if !ident.IsValid(id) {
						c.errorf(el.TmNode(), "%v is not a valid identifier", id)
						ids = nil
						break
					}
					ids[i] = id
				}
				if len(ids) > 0 {
					out := syntax.ExtraType{
						Name:       ids[0],
						Implements: ids[1:],
						Origin:     el.TmNode(),
					}
					ret = append(ret, out)
				}
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
	case []syntax.ExtraType:
		c.errorf(e.TmNode(), `list of strings with names is expected. E.g. ["Foo", "Bar -> Expr"]`)
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

type nontermImpl struct {
	nonterm int // in source.Nonterms
	def     ast.Nonterm
}

func (c *compiler) collectNonterms(p ast.ParserSection) []nontermImpl {
	var ret []nontermImpl
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
			id := ident.Produce(name, ident.CamelCase)
			if prev, exists := c.ids[id]; exists {
				c.errorf(nonterm.Name(), "%v and %v get the same ID in generated code", name, prev)
			}
			if ann, ok := nonterm.Annotations(); ok {
				c.errorf(ann.TmNode(), "unsupported syntax")
			}

			nt := &syntax.Nonterm{
				Name:   name,
				Origin: nonterm.Name(),
			}
			if t, ok := nonterm.NontermType(); ok {
				if rt, _ := t.(*ast.RawType); rt != nil {
					nt.Type = strings.TrimSuffix(strings.TrimPrefix(rt.Text(), "{"), "}")
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
						if !c.source.Params[i].Lookahead {
							nt.Params = append(nt.Params, i)
						} else {
							c.errorf(param, "lookahead parameters cannot be declared '%v'", name)
						}
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
			c.nonterms[name] = len(c.source.Nonterms)
			ret = append(ret, nontermImpl{len(c.source.Nonterms), *nonterm})
			c.source.Nonterms = append(c.source.Nonterms, nt)
		}
	}
	return ret
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
				_, noeoi := ref.NoEoi()
				c.source.Inputs = append(c.source.Inputs, syntax.Input{Nonterm: nonterm, NoEoi: noeoi})
			}
		}
	}

	if len(c.source.Inputs) > 0 {
		return
	}

	input, found := c.nonterms["input"]
	if found && len(c.source.Nonterms[input].Params) > 0 {
		c.errorf(c.source.Nonterms[input].Origin, "the 'input' nonterminal cannot be parametrized")
		found = false
	}
	if !found {
		c.errorf(c.file.Header(), "the grammar does not specify an input nonterminal, use '%%input' to declare one")
		return
	}
	c.source.Inputs = append(c.source.Inputs, syntax.Input{Nonterm: input})
}

func (c *compiler) collectDirectives(p ast.ParserSection) {
	precTerms := container.NewBitSet(c.out.NumTokens)
	var seenSR, seenRR bool

	for _, part := range p.GrammarPart() {
		switch part := part.(type) {
		case *ast.DirectiveAssert:
			set := c.convertSet(part.RhsSet().Expr(), nil /*nonterm*/)
			index := len(c.source.Sets)
			c.source.Sets = append(c.source.Sets, set)
			_, empty := part.Empty()
			c.asserts = append(c.asserts, assert{index: index, empty: empty})
		case *ast.DirectiveInterface:
			for _, id := range part.Ids() {
				text := id.Text()
				if _, exists := c.cats[text]; exists {
					c.errorf(id, "duplicate interface declaration of '%v'", text)
					continue
				}
				index := len(c.source.Cats)
				c.source.Cats = append(c.source.Cats, text)
				c.cats[text] = index
			}
		case *ast.DirectivePrio:
			var assoc lalr.Associativity
			switch part.Assoc().Text() {
			case "left":
				assoc = lalr.Left
			case "right":
				assoc = lalr.Right
			case "nonassoc":
				assoc = lalr.NonAssoc
			default:
				c.errorf(part, "unsupported associativity")
				continue
			}
			prec := lalr.Precedence{Associativity: assoc}
			for _, ref := range part.Symbols() {
				name := ref.Name()
				sym, ok := c.syms[name.Text()]
				if !ok || sym >= c.out.NumTokens {
					c.errorf(name, "unresolved reference '%v'", name.Text())
					continue
				}
				if precTerms.Get(sym) {
					c.errorf(name, "second precedence rule for '%v'", name.Text())
					continue
				}
				precTerms.Set(sym)
				prec.Terminals = append(prec.Terminals, lalr.Sym(sym))
			}
			if len(prec.Terminals) > 0 {
				c.out.Prec = append(c.out.Prec, prec)
			}
		case *ast.DirectiveExpect:
			if seenSR {
				c.errorf(part, "duplicate %%expect directive")
				continue
			}
			seenSR = true
			c.expectSR, _ = strconv.Atoi(part.Child(selector.IntegerLiteral).Text())
		case *ast.DirectiveExpectRR:
			if seenRR {
				c.errorf(part, "duplicate %%expect-rr directive")
				continue
			}
			seenRR = true
			c.expectRR, _ = strconv.Atoi(part.Child(selector.IntegerLiteral).Text())
		case *ast.DirectiveSet:
			name := part.Name()
			if name.Text() == "afterErr" {
				c.errorf(name, "'afterErr' is reserved for built-in error recovery")
				continue
			}
			if _, ok := c.namedSets[name.Text()]; ok {
				c.errorf(name, "redeclaration of token set '%v'", name.Text())
				continue
			}

			set := c.convertSet(part.RhsSet().Expr(), nil /*nonterm*/)
			c.namedSets[name.Text()] = len(c.source.Sets)
			c.out.Sets = append(c.out.Sets, &NamedSet{
				Name: name.Text(),
				Expr: part.RhsSet().Text(), // Note: this gets replaced later with instantiated names
			})
			c.source.Sets = append(c.source.Sets, set)
		}
	}
}

// TODO remove the second parameter and disallow templating sets
func (c *compiler) convertSet(expr ast.SetExpression, nonterm *syntax.Nonterm) *syntax.TokenSet {
	switch expr := expr.(type) {
	case *ast.SetAnd:
		return &syntax.TokenSet{
			Kind:   syntax.Intersection,
			Sub:    []*syntax.TokenSet{c.convertSet(expr.Left(), nonterm), c.convertSet(expr.Right(), nonterm)},
			Origin: expr,
		}
	case *ast.SetComplement:
		return &syntax.TokenSet{
			Kind:   syntax.Complement,
			Sub:    []*syntax.TokenSet{c.convertSet(expr.Inner(), nonterm)},
			Origin: expr,
		}
	case *ast.SetCompound:
		return c.convertSet(expr.Inner(), nonterm)
	case *ast.SetOr:
		return &syntax.TokenSet{
			Kind:   syntax.Union,
			Sub:    []*syntax.TokenSet{c.convertSet(expr.Left(), nonterm), c.convertSet(expr.Right(), nonterm)},
			Origin: expr,
		}
	case *ast.SetSymbol:
		ret := &syntax.TokenSet{Kind: syntax.Any}
		if op, ok := expr.Operator(); ok {
			switch op.Text() {
			case "first":
				ret.Kind = syntax.First
			case "last":
				ret.Kind = syntax.Last
			case "precede":
				ret.Kind = syntax.Precede
			case "follow":
				ret.Kind = syntax.Follow
			default:
				c.errorf(op, "operator must be one of: first, last, precede or follow")
			}
		}
		ret.Symbol, ret.Args = c.resolveRef(expr.Symbol(), nonterm)
		return ret
	default:
		c.errorf(expr.TmNode(), "syntax error")
		return &syntax.TokenSet{} // == eoi
	}
}

func (c *compiler) pred(ref ast.ParamRef, nonterm *syntax.Nonterm, op syntax.PredicateOp, val string, origin ast.PredicateExpression) *syntax.Predicate {
	param, ok := c.resolveParam(ref, nonterm)
	if !ok {
		return nil
	}
	return &syntax.Predicate{
		Op:     op,
		Param:  param,
		Value:  val,
		Origin: origin.TmNode(),
	}
}

func (c *compiler) predNot(pred *syntax.Predicate, origin ast.PredicateExpression) *syntax.Predicate {
	if pred == nil {
		return nil
	}
	return &syntax.Predicate{
		Op:     syntax.Not,
		Sub:    []*syntax.Predicate{pred},
		Origin: origin.TmNode(),
	}
}

func (c *compiler) predLiteral(ref ast.ParamRef, nonterm *syntax.Nonterm, literal ast.Literal, origin ast.PredicateExpression) *syntax.Predicate {
	lit, ok := literal.(*ast.StringLiteral)
	if !ok {
		c.errorf(literal.TmNode(), "string is expected")
		return nil
	}
	val, err := strconv.Unquote(lit.Text())
	if err != nil {
		c.errorf(lit, "cannot parse string literal: %v", err)
		return nil
	}
	return c.pred(ref, nonterm, syntax.Equals, val, origin)
}

func (c *compiler) predList(op syntax.PredicateOp, list []ast.PredicateExpression, nonterm *syntax.Nonterm, origin ast.PredicateExpression) *syntax.Predicate {
	var out []*syntax.Predicate
	for _, expr := range list {
		if p := c.convertPredicate(expr, nonterm); p != nil {
			out = append(out, p)
		}
	}
	if len(out) == 0 {
		return nil
	}
	return &syntax.Predicate{
		Op:     op,
		Sub:    out,
		Origin: origin.TmNode(),
	}
}

func (c *compiler) convertPredicate(expr ast.PredicateExpression, nonterm *syntax.Nonterm) *syntax.Predicate {
	switch expr := expr.(type) {
	case *ast.PredicateOr:
		return c.predList(syntax.Or, []ast.PredicateExpression{expr.Left(), expr.Right()}, nonterm, expr)
	case *ast.PredicateAnd:
		return c.predList(syntax.And, []ast.PredicateExpression{expr.Left(), expr.Right()}, nonterm, expr)
	case *ast.ParamRef:
		return c.pred(*expr, nonterm, syntax.Equals, "true", expr)
	case *ast.PredicateNot:
		return c.predNot(c.pred(expr.ParamRef(), nonterm, syntax.Equals, "true", expr), expr)
	case *ast.PredicateEq:
		return c.predLiteral(expr.ParamRef(), nonterm, expr.Literal(), expr)
	case *ast.PredicateNotEq:
		return c.predNot(c.predLiteral(expr.ParamRef(), nonterm, expr.Literal(), expr), expr)
	default:
		c.errorf(expr.TmNode(), "syntax error")
		return nil
	}
}

func (c *compiler) resolveParam(ref ast.ParamRef, nonterm *syntax.Nonterm) (int, bool) {
	name := ref.Identifier().Text()
	for _, p := range nonterm.Params {
		if name == c.source.Params[p].Name {
			return p, true
		}
	}

	if p, ok := c.params[name]; ok && c.source.Params[p].Lookahead {
		// Lookahead parameters don't have to be declared.
		return p, true
	}

	c.errorf(ref.Identifier(), "unresolved parameter reference '%v' (in %v)", name, nonterm.Name)
	return 0, false
}

func (c *compiler) instantiateOpt(name string, origin ast.Symref) (int, bool) {
	nt := &syntax.Nonterm{
		Name:   name,
		Origin: origin,
	}

	var ref *syntax.Expr
	target := name[:len(name)-3]
	if index, ok := c.syms[target]; ok {
		nt.Type = c.out.Syms[index].Type
		ref = &syntax.Expr{Kind: syntax.Reference, Symbol: index, Origin: origin, Model: c.source}
	} else if nonterm, ok := c.nonterms[target]; ok {
		nt.Type = c.source.Nonterms[nonterm].Type
		nt.Params = c.source.Nonterms[nonterm].Params
		ref = &syntax.Expr{Kind: syntax.Reference, Symbol: c.out.NumTokens + nonterm, Origin: origin, Model: c.source}
		for _, param := range nt.Params {
			ref.Args = append(ref.Args, syntax.Arg{Param: param, TakeFrom: param})
		}
	} else {
		// Unresolved.
		return 0, false
	}
	nt.Value = &syntax.Expr{Kind: syntax.Optional, Sub: []*syntax.Expr{ref}, Origin: origin}

	c.nonterms[name] = len(c.source.Nonterms)
	index := c.out.NumTokens + len(c.source.Nonterms)
	c.source.Nonterms = append(c.source.Nonterms, nt)
	return index, true
}

func (c *compiler) resolveRef(ref ast.Symref, nonterm *syntax.Nonterm) (int, []syntax.Arg) {
	name := ref.Name()
	text := name.Text()
	index, ok := c.syms[text]
	if !ok {
		index, ok = c.nonterms[text]
		if ok {
			index += c.out.NumTokens
		}
	}
	if !ok && len(text) > 3 && strings.HasSuffix(text, "opt") {
		index, ok = c.instantiateOpt(text, ref)
	}
	if !ok {
		c.errorf(name, "unresolved reference '%v'", text)
		return 0, nil // == eoi
	}

	if index < c.out.NumTokens {
		if args, ok := ref.Args(); ok {
			c.errorf(args, "terminals cannot be parametrized")
		}
		return index, nil
	}

	target := c.source.Nonterms[index-c.out.NumTokens]
	required := container.NewBitSet(len(c.source.Params))
	populated := container.NewBitSet(len(c.source.Params))
	for _, p := range target.Params {
		required.Set(p)
	}
	var args []syntax.Arg
	if arguments, ok := ref.Args(); ok {
		for _, arg := range arguments.ArgList() {
			var ref ast.ParamRef
			out := syntax.Arg{Origin: arg.TmNode()}
			switch arg := arg.(type) {
			case *ast.ArgumentFalse:
				ref = arg.Name()
				out.Value = "false"
			case *ast.ArgumentTrue:
				ref = arg.Name()
				out.Value = "true"
			case *ast.ArgumentVal:
				ref = arg.Name()
				if val, ok := arg.Val(); ok {
					switch val := val.(type) {
					case *ast.BooleanLiteral:
						out.Value = val.Text()
					case *ast.ParamRef:
						if nonterm == nil {
							c.errorf(val.Identifier(), "unresolved parameter reference '%v'", val.Identifier().Text())
							continue
						}
						out.TakeFrom, ok = c.resolveParam(*val, nonterm)
						if !ok {
							continue
						}
					default:
						c.errorf(val.TmNode(), "unsupported value")
						continue
					}
					break
				}
				if nonterm == nil {
					c.errorf(ref, "missing value")
					continue
				}
				// Note: matching by name enables value propagation between inline parameters.
				out.TakeFrom, ok = c.resolveParam(ref, nonterm)
				if !ok {
					continue
				}
			default:
				c.errorf(arg.TmNode(), "syntax error")
				continue
			}
			param, ok := c.resolveParam(ref, target)
			if !ok {
				continue
			}
			if populated.Get(param) {
				c.errorf(ref, "second argument for '%v'", c.source.Params[param].Name)
				continue
			}
			populated.Set(param)
			required.Clear(param)
			out.Param = param
			out.Origin = ref
			args = append(args, out)
		}
	}

	var uninitialized []string
	for _, p := range required.Slice(nil) {
		param := c.source.Params[p]
		if nonterm != nil {
			var found bool
			for _, from := range nonterm.Params {
				// Note: matching by name enables value propagation between inline parameters.
				if param.Name == c.source.Params[from].Name {
					args = append(args, syntax.Arg{
						Param:    p,
						TakeFrom: from,
					})
					found = true
					break
				}
			}
			if found {
				continue
			}
		}
		if param.DefaultValue != "" {
			args = append(args, syntax.Arg{
				Param: p,
				Value: param.DefaultValue,
			})
			continue
		}
		uninitialized = append(uninitialized, param.Name)
	}
	if len(uninitialized) > 0 {
		c.errorf(ref.Name(), "uninitialized parameters: %v", strings.Join(uninitialized, ", "))
	}
	c.sortArgs(target.Params, args)
	return index, args
}

func (c *compiler) sortArgs(params []int, args []syntax.Arg) {
	if len(args) < 2 {
		return
	}
	pos := c.paramPerm
	for i := range pos {
		pos[i] = -1
	}
	for i, param := range params {
		pos[param] = i
	}
	e := len(params)
	for i, index := range pos {
		if index == -1 {
			pos[i] = e
			e++
		}
	}
	sort.Slice(args, func(i, j int) bool { return pos[args[i].Param] < pos[args[j].Param] })
}

func (c *compiler) convertReportClause(n ast.ReportClause) report {
	action := n.Action().Text()
	if len(action) == 0 {
		return report{}
	}
	var flags []string
	for _, id := range n.Flags() {
		flags = append(flags, id.Text())
	}
	if len(flags) > 0 && c.isSelector(action) {
		c.errorf(n.Action(), "selector clauses cannot be used together with flags")
		flags = nil
	}
	ret := report{
		node: &syntax.Expr{Kind: syntax.Arrow, Name: action, ArrowFlags: flags, Origin: n},
	}
	if c.isSelector(action) {
		ret.selector = ret.node
		ret.node = nil
	}
	if as, ok := n.ReportAs(); ok {
		if ret.selector != nil {
			c.errorf(as, "reporting a selector 'as' some other node is not supported")
			return ret
		}
		if !c.isSelector(as.Identifier().Text()) {
			c.errorf(as, "'as' expects a selector")
			return ret
		}
		ret.selector = &syntax.Expr{Kind: syntax.Arrow, Name: as.Identifier().Text(), Origin: as}
	}
	return ret
}

func (c *compiler) convertSeparator(sep ast.ListSeparator) *syntax.Expr {
	var subs []*syntax.Expr
	for _, ref := range sep.Separator() {
		sym, _ := c.resolveRef(ref, nil /*nonterm*/)
		if sym >= c.out.NumTokens {
			c.errorf(ref, "separators must be terminals")
			continue
		}
		expr := &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Origin: ref, Model: c.source}
		subs = append(subs, expr)
	}
	switch len(subs) {
	case 0:
		return &syntax.Expr{Kind: syntax.Empty, Origin: sep}
	case 1:
		return subs[0]
	}
	return &syntax.Expr{
		Kind:   syntax.Sequence,
		Sub:    subs,
		Origin: sep,
	}
}

func (c *compiler) allocatePos() int {
	ret := c.rhsPos
	c.rhsPos++
	return ret
}

func (c *compiler) pushName(name string, pos int) {
	if c.rhsNames == nil {
		c.rhsNames = make(map[string]int)
	}
	var index int
	if _, ok := c.rhsNames[name+"#0"]; ok {
		for {
			index++
			if _, ok := c.rhsNames[fmt.Sprintf("%v#%v", name, index)]; !ok {
				break
			}
		}
	} else if val, ok := c.rhsNames[name]; ok {
		c.rhsNames[name+"#0"] = val
		delete(c.rhsNames, name)
		index = 1
	}
	if index > 0 {
		name = fmt.Sprintf("%v#%v", name, index)
	}
	c.rhsNames[name] = pos
}

func (c *compiler) convertPart(p ast.RhsPart, nonterm *syntax.Nonterm) *syntax.Expr {
	switch p := p.(type) {
	case *ast.Command:
		args := &syntax.CmdArgs{MaxPos: c.rhsPos}
		if len(c.rhsNames) > 0 {
			// Only names and references preceding the command are available to its code.
			// Note: the list below can include entities from a different alternative but
			// they'll be automatically filtered later on.
			args.Names = make(map[string]int)
			for k, v := range c.rhsNames {
				args.Names[k] = v
			}
		}
		text := p.Text()
		return &syntax.Expr{Kind: syntax.Command, Name: text, CmdArgs: args, Origin: p}
	case *ast.RhsAssignment:
		// Ignore any names within the assigned expression.
		old := c.rhsNames
		c.rhsNames = nil
		inner := c.convertPart(p.Inner(), nonterm)
		c.rhsNames = old

		name := p.Id().Text()
		if inner.Pos > 0 {
			c.pushName(name, inner.Pos)
		}
		subs := []*syntax.Expr{inner}
		return &syntax.Expr{Kind: syntax.Assign, Name: name, Sub: subs, Origin: p}
	case *ast.RhsPlusAssignment:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.Append, Name: p.Id().Text(), Sub: subs, Origin: p}
	case *ast.RhsCast:
		// TODO implement
	case *ast.RhsLookahead:
		var subs []*syntax.Expr
		for _, pred := range p.Predicates() {
			sym, args := c.resolveRef(pred.Symref(), nonterm)
			if sym < c.out.NumTokens {
				c.errorf(pred.Symref(), "lookahead expressions do not support terminals")
				continue
			}
			expr := &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args, Origin: pred.Symref(), Model: c.source}
			if _, not := pred.Not(); not {
				expr = &syntax.Expr{Kind: syntax.LookaheadNot, Sub: []*syntax.Expr{expr}, Origin: pred}
			}
			subs = append(subs, expr)
		}
		if len(subs) == 0 {
			return &syntax.Expr{Kind: syntax.Empty, Origin: p}
		}
		return &syntax.Expr{Kind: syntax.Lookahead, Sub: subs, Origin: p}
	case *ast.RhsNested:
		return c.convertRules(p.Rule0(), nonterm, report{} /*defaultReport*/, false /*topLevel*/, p)
	case *ast.RhsOptional:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.Optional, Sub: subs, Origin: p}
	case *ast.RhsPlusList:
		seq := c.convertSequence(p.RuleParts(), nonterm, p)
		subs := []*syntax.Expr{seq}
		if sep := c.convertSeparator(p.ListSeparator()); sep.Kind != syntax.Empty {
			subs = []*syntax.Expr{seq, sep}
		}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, ListFlags: syntax.OneOrMore, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsStarList:
		seq := c.convertSequence(p.RuleParts(), nonterm, p)
		subs := []*syntax.Expr{seq}
		if sep := c.convertSeparator(p.ListSeparator()); sep.Kind != syntax.Empty {
			subs = []*syntax.Expr{seq, sep}
		}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsPlusQuantifier:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, ListFlags: syntax.OneOrMore, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsStarQuantifier:
		subs := []*syntax.Expr{c.convertPart(p.Inner(), nonterm)}
		return &syntax.Expr{Kind: syntax.List, Sub: subs, Pos: c.allocatePos(), Origin: p}
	case *ast.RhsSet:
		set := c.convertSet(p.Expr(), nonterm)
		index := len(c.source.Sets)
		c.source.Sets = append(c.source.Sets, set)
		return &syntax.Expr{Kind: syntax.Set, Pos: c.allocatePos(), SetIndex: index, Origin: p, Model: c.source}
	case *ast.RhsSymbol:
		sym, args := c.resolveRef(p.Reference(), nonterm)
		c.pushName(p.Reference().Name().Text(), c.rhsPos)
		return &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args, Pos: c.allocatePos(), Origin: p, Model: c.source}
	case *ast.StateMarker:
		return &syntax.Expr{Kind: syntax.StateMarker, Name: p.Name().Text(), Origin: p}
	case *ast.SyntaxProblem:
		c.errorf(p, "syntax error")
		return &syntax.Expr{Kind: syntax.Empty, Origin: p}
	}
	c.errorf(p.TmNode(), "unsupported syntax (%T)", p)
	return &syntax.Expr{Kind: syntax.Empty, Origin: p.TmNode()}
}

func (c *compiler) convertSequence(parts []ast.RhsPart, nonterm *syntax.Nonterm, origin status.SourceNode) *syntax.Expr {
	var subs []*syntax.Expr
	for _, p := range parts {
		subs = append(subs, c.convertPart(p, nonterm))
	}
	switch len(subs) {
	case 0:
		return &syntax.Expr{Kind: syntax.Empty, Origin: origin}
	case 1:
		return subs[0]
	}
	return &syntax.Expr{
		Kind:   syntax.Sequence,
		Sub:    subs,
		Origin: origin,
	}
}

type report struct {
	node     *syntax.Expr
	selector *syntax.Expr
}

func (r report) withDefault(def report) report {
	if r.node == nil && r.selector == nil {
		return def
	}
	if r.selector == nil && def.selector != nil {
		return report{r.node, def.selector}
	}
	return r
}

func (r report) apply(expr *syntax.Expr) *syntax.Expr {
	if r.node != nil {
		e := *r.node
		e.Sub = []*syntax.Expr{expr}
		expr = &e
	}
	if r.selector != nil {
		e := *r.selector
		e.Sub = []*syntax.Expr{expr}
		expr = &e
	}
	return expr
}

func (c *compiler) isSelector(name string) bool {
	_, ok := c.cats[name]
	return ok
}

func (c *compiler) convertRules(rules []ast.Rule0, nonterm *syntax.Nonterm, defaultReport report, topLevel bool, origin status.SourceNode) *syntax.Expr {
	var subs []*syntax.Expr
	for _, rule0 := range rules {
		rule, ok := rule0.(*ast.Rule)
		if !ok {
			c.errorf(rule0.TmNode(), "syntax error")
			continue
		}

		if topLevel {
			// Counting of RHS symbols does not restart for inline alternatives.
			c.rhsPos = 1
			c.rhsNames = nil
		}
		expr := c.convertSequence(rule.RhsPart(), nonterm, rule)
		clause, _ := rule.ReportClause()
		expr = c.convertReportClause(clause).withDefault(defaultReport).apply(expr)
		if suffix, ok := rule.RhsSuffix(); ok {
			switch suffix.Name().Text() {
			case "prec":
				sym, _ := c.resolveRef(suffix.Symref(), nonterm)
				if sym < c.out.NumTokens {
					expr = &syntax.Expr{Kind: syntax.Prec, Symbol: sym, Sub: []*syntax.Expr{expr}, Model: c.source, Origin: suffix}
				} else {
					c.errorf(suffix.Symref(), "terminal is expected")
				}
			default:
				c.errorf(suffix, "unsupported syntax")
			}
		}
		if pred, ok := rule.Predicate(); ok {
			p := c.convertPredicate(pred.PredicateExpression(), nonterm)
			if p != nil {
				expr = &syntax.Expr{Kind: syntax.Conditional, Predicate: p, Sub: []*syntax.Expr{expr}, Model: c.source, Origin: pred}
			}
		}

		subs = append(subs, expr)
	}
	switch len(subs) {
	case 0:
		return &syntax.Expr{Kind: syntax.Empty, Origin: origin}
	case 1:
		return subs[0]
	}
	return &syntax.Expr{
		Kind:   syntax.Choice,
		Sub:    subs,
		Origin: origin,
	}
}

func (c *compiler) compileParser() {
	p, ok := c.file.Parser()
	if !ok {
		return
	}
	c.source = new(syntax.Model)
	for _, sym := range c.out.Syms {
		c.source.Terminals = append(c.source.Terminals, sym.ID)
	}
	c.collectParams(p)
	nonterms := c.collectNonterms(p)
	c.paramPerm = make([]int, len(c.source.Params))

	c.collectInputs(p)
	c.collectDirectives(p)

	if errSym, ok := c.syms["error"]; ok {
		// %generate afterErr = set(follow error);
		const name = "afterErr"
		c.namedSets[name] = len(c.source.Sets)
		c.out.Sets = append(c.out.Sets, &NamedSet{
			Name: name,
			Expr: "set(follow error)",
		})
		c.source.Sets = append(c.source.Sets, &syntax.TokenSet{
			Kind:   syntax.Follow,
			Symbol: errSym,
		})
	}

	for _, nt := range nonterms {
		clause, _ := nt.def.ReportClause()
		defaultReport := c.convertReportClause(clause)
		expr := c.convertRules(nt.def.Rule0(), c.source.Nonterms[nt.nonterm], defaultReport, true /*topLevel*/, nt.def)
		c.source.Nonterms[nt.nonterm].Value = expr
	}

	if c.s.Err() != nil {
		// Parsing errors cause inconsistencies inside c.source. Aborting.
		return
	}

	if err := syntax.PropagateLookaheads(c.source); err != nil {
		c.s.AddError(err)
		return
	}

	if err := syntax.Instantiate(c.source); err != nil {
		c.s.AddError(err)
		return
	}

	if c.out.Options.EventBased {
		var tokens []syntax.RangeToken
		for _, t := range c.out.Options.ReportTokens {
			name := ident.Produce(c.out.Syms[t].Name, ident.CamelCase)
			tokens = append(tokens, syntax.RangeToken{Token: t, Name: name})
		}

		opts := syntax.TypeOptions{
			EventFields: c.out.Options.EventFields,
			GenSelector: c.out.Options.GenSelector,
			ExtraTypes:  c.out.Options.ExtraTypes,
		}
		types, err := syntax.ExtractTypes(c.source, tokens, opts)
		if err != nil {
			c.s.AddError(err)
			return
		}
		c.out.Types = types
		c.out.MappedTokens = tokens
	}

	if err := syntax.Expand(c.source); err != nil {
		c.s.AddError(err)
		return
	}

	// Use instantiated nonterminal names to describe sets in generated code.
	old := c.source.Terminals
	if c.compat {
		// Note: prefer original terminal names over IDs.
		c.source.Terminals = nil
		for _, sym := range c.out.Syms {
			c.source.Terminals = append(c.source.Terminals, sym.Name)
		}
	}
	for _, set := range c.out.Sets {
		in := c.source.Sets[c.namedSets[set.Name]]
		set.Expr = "set(" + in.String(c.source) + ")"
	}
	c.source.Terminals = old

	if err := syntax.ResolveSets(c.source); err != nil {
		c.s.AddError(err)
		return
	}

	if errSym, ok := c.syms["error"]; ok {
		if index, ok := c.namedSets["afterErr"]; ok {
			// Non-empty "afterErr" set turns on error recovery.
			c.out.Parser.IsRecovering = len(c.source.Sets[index].Sub) > 0
			c.out.Parser.ErrorSymbol = errSym
		}
	}

	// Export computed named sets for code generation.
	for _, set := range c.out.Sets {
		in := c.source.Sets[c.namedSets[set.Name]]
		for _, term := range in.Sub {
			set.Terminals = append(set.Terminals, term.Symbol)
		}
	}

	// Introduce synthetic inputs for runtime lookaheads.
	addSyntheticInputs(c.source, c.compat)

	// Prepare the model for code generation.
	c.addNonterms(c.source)
	if !c.generateTables() {
		return
	}

	out := c.out.Parser
	out.Inputs = c.source.Inputs
	out.Nonterms = c.source.Nonterms
	out.NumTerminals = len(c.source.Terminals)
}

func (c *compiler) generateTables() bool {
	g := &lalr.Grammar{
		Terminals:  len(c.source.Terminals),
		Precedence: c.out.Prec,
		ExpectSR:   c.expectSR,
		ExpectRR:   c.expectRR,
		Origin:     c.file,
	}
	for _, sym := range c.out.Syms {
		g.Symbols = append(g.Symbols, sym.Name)
	}
	inputs := make(map[lalr.Input]int32)
	for _, inp := range c.source.Inputs {
		out := lalr.Input{
			Nonterminal: lalr.Sym(g.Terminals + inp.Nonterm),
			Eoi:         !inp.NoEoi,
		}
		inputs[out] = int32(len(g.Inputs))
		g.Inputs = append(g.Inputs, out)
	}
	markers := make(map[string]int)
	types := make(map[string]int)
	if c.out.Types != nil {
		for i, t := range c.out.Types.RangeTypes {
			types[t.Name] = i
		}
	}

	// The very first action is a no-op.
	c.out.Parser.Actions = append(c.out.Parser.Actions, SemanticAction{})

	var rules []*Rule
	for self, nt := range c.source.Nonterms {
		if nt.Value.Kind == syntax.Lookahead {
			la := lalr.Lookahead{
				Nonterminal: lalr.Sym(g.Terminals + self),
				Origin:      nt.Origin,
			}
			for _, sub := range nt.Value.Sub {
				negated := sub.Kind == syntax.LookaheadNot
				if negated {
					sub = sub.Sub[0]
				}
				inp, ok := inputs[lalr.Input{Nonterminal: lalr.Sym(sub.Symbol)}]
				if !ok {
					log.Fatalf("%v is not properly instantiated: %v", nt.Name, sub)
				}
				pred := lalr.Predicate{
					Negated: negated,
					Input:   inp,
				}
				la.Predicates = append(la.Predicates, pred)
			}

			rule := lalr.Rule{
				LHS:        lalr.Sym(g.Terminals + self),
				Type:       -1,
				Origin:     nt.Origin,
				OriginName: nt.Name,
			}
			g.Lookaheads = append(g.Lookaheads, la)
			g.Rules = append(g.Rules, rule)
			rules = append(rules, &Rule{Rule: rule, Value: nt.Value})
			continue
		}

		if nt.Value.Kind != syntax.Choice {
			log.Fatalf("%v is not properly instantiated: %v", nt.Name, nt.Value)
		}
		for _, expr := range nt.Value.Sub {
			rule := lalr.Rule{
				LHS:        lalr.Sym(g.Terminals + self),
				Type:       -1,
				Origin:     expr.Origin,
				OriginName: nt.Name,
			}
			exprWithPrec := expr
			if expr.Kind == syntax.Prec {
				rule.Precedence = lalr.Sym(expr.Symbol)
				expr = expr.Sub[0]
			}
			var report []Range
			var command string
			var args *syntax.CmdArgs
			var numRefs int
			actualPos := make(map[int]int) // $i inside a semantic action -> index in rule.RHS
			origin := expr.Origin

			var innerReports int
			var traverse func(expr *syntax.Expr)
			traverse = func(expr *syntax.Expr) {
				switch expr.Kind {
				case syntax.Arrow:
					start := numRefs
					for _, sub := range expr.Sub {
						traverse(sub)
					}
					if c.isSelector(expr.Name) {
						// Categories are used during the grammar analysis only and don't need
						// to be reported.
						break
					}
					if t, ok := types[expr.Name]; ok { // !ok for categories
						end := numRefs
						report = append(report, Range{start, end, t, expr.ArrowFlags})
						if len(expr.ArrowFlags) != 0 {
							c.out.Parser.UsesFlags = true
						}
					}
				case syntax.Sequence, syntax.Assign, syntax.Append:
					for _, sub := range expr.Sub {
						traverse(sub)
					}
					innerReports = len(report)
				case syntax.Reference:
					if command != "" {
						// TODO This command needs to be extracted into a dedicated nonterminal.
						c.s.Errorf(origin, "commands must be placed at the end of a rule")
					}
					if expr.Pos > 0 {
						actualPos[expr.Pos] = numRefs
					}
					rule.RHS = append(rule.RHS, lalr.Sym(expr.Symbol))
					numRefs++
				case syntax.StateMarker:
					if i, ok := markers[expr.Name]; ok {
						rule.RHS = append(rule.RHS, lalr.Marker(i))
						return
					}
					i := len(g.Markers)
					markers[expr.Name] = i
					g.Markers = append(g.Markers, expr.Name)
					rule.RHS = append(rule.RHS, lalr.Marker(i))
				case syntax.Command:
					// Note: those are end-of-rule commands and typically there is at most one.
					command += expr.Name
					args = expr.CmdArgs // It is okay to override the args - the new ones are more permissive.
					origin = expr.Origin
				}
			}
			traverse(expr)

			if last := len(report) - 1; last >= 0 && report[last].Start == 0 && report[last].End == numRefs &&
				// Note: in compatibility note we don't promote -> from inside parentheses.
				(!c.compat || len(report) > innerReports) {

				// Promote to the rule default.
				rule.Type = report[last].Type
				rule.Flags = report[last].Flags
				report = report[:last]
			}
			if len(report) > 0 || command != "" {
				// TODO reuse existing actions
				act := SemanticAction{
					Report: report,
					Code:   command,
					Origin: origin,
				}
				if args != nil {
					act.Vars = &ActionVars{CmdArgs: *args, Remap: actualPos}
					for _, r := range rule.RHS {
						if r.IsStateMarker() {
							continue
						}
						act.Vars.Types = append(act.Vars.Types, c.out.Syms[r].Type)
					}
				}
				rule.Action = len(c.out.Parser.Actions)
				c.out.Parser.Actions = append(c.out.Parser.Actions, act)
			}
			g.Rules = append(g.Rules, rule)
			rules = append(rules, &Rule{Rule: rule, Value: exprWithPrec})
		}
	}
	if c.s.Err() != nil {
		// Parsing errors cause inconsistencies inside c.source. Aborting.
		return false
	}

	if c.compat {
		// Sort g.Markers.
		perm := make([]int, len(g.Markers))
		sort.Strings(g.Markers)
		for i, val := range g.Markers {
			perm[markers[val]] = i
		}
		for _, rule := range g.Rules {
			for i, val := range rule.RHS {
				if val.IsStateMarker() {
					rule.RHS[i] = lalr.Marker(perm[val.AsMarker()])
				}
			}
		}
	}

	tables, err := lalr.Compile(g)
	if err != nil {
		c.s.AddError(err)
		return false
	}

	c.out.Parser.Rules = rules
	c.out.Parser.Tables = tables
	return true
}

func (c *compiler) parseOptions() {
	opts := c.out.Options
	seen := make(map[string]int)
	for _, opt := range c.file.Options() {
		name := opt.Key().Text()
		if line, ok := seen[name]; ok {
			c.errorf(opt.Key(), "reinitialization of '%v', previously declared on line %v", name, line)
		}
		line, _ := opt.Key().LineColumn()
		seen[name] = line
		switch name {
		case "package":
			opts.Package = c.parseExpr(opt.Value(), opts.Package).(string)
		case "genCopyright":
			opts.Copyright = c.parseExpr(opt.Value(), opts.Copyright).(bool)
		case "tokenLine":
			opts.TokenLine = c.parseExpr(opt.Value(), opts.TokenLine).(bool)
		case "tokenLineOffset":
			opts.TokenLineOffset = c.parseExpr(opt.Value(), opts.TokenLineOffset).(bool)
		case "tokenColumn":
			opts.TokenColumn = c.parseExpr(opt.Value(), opts.TokenColumn).(bool)
		case "nonBacktracking":
			opts.NonBacktracking = c.parseExpr(opt.Value(), opts.NonBacktracking).(bool)
		case "cancellable":
			opts.Cancellable = c.parseExpr(opt.Value(), opts.Cancellable).(bool)
		case "writeBison":
			opts.WriteBison = c.parseExpr(opt.Value(), opts.WriteBison).(bool)
		case "recursiveLookaheads":
			opts.RecursiveLookaheads = c.parseExpr(opt.Value(), opts.RecursiveLookaheads).(bool)
		case "eventBased":
			opts.EventBased = c.parseExpr(opt.Value(), opts.EventBased).(bool)
		case "genSelector":
			opts.GenSelector = c.parseExpr(opt.Value(), opts.GenSelector).(bool)
		case "fixWhitespace":
			opts.FixWhitespace = c.parseExpr(opt.Value(), opts.FixWhitespace).(bool)
		case "debugParser":
			opts.DebugParser = c.parseExpr(opt.Value(), opts.DebugParser).(bool)
		case "eventFields":
			opts.EventFields = c.parseExpr(opt.Value(), opts.EventFields).(bool)
		case "eventAST":
			opts.EventAST = c.parseExpr(opt.Value(), opts.EventAST).(bool)
		case "reportTokens":
			c.reportList = c.parseTokenList(opt.Value())
			c.reportTokens = make(map[string]bool)
			for _, id := range c.reportList {
				c.reportTokens[id.Text()] = true
			}
		case "extraTypes":
			opts.ExtraTypes = c.parseExpr(opt.Value(), opts.ExtraTypes).([]syntax.ExtraType)
		case "customImpl":
			opts.CustomImpl = c.parseExpr(opt.Value(), opts.CustomImpl).([]string)
		case "fileNode":
			opts.FileNode = c.parseExpr(opt.Value(), opts.FileNode).(string)
		case "lang":
			// This option often occurs in existing grammars. Ignore it.
			c.parseExpr(opt.Value(), "")
		default:
			c.errorf(opt.Key(), "unknown option '%v'", name)
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
	set    map[string]*lex.Pattern
	unused map[string]status.SourceNode
}

func (p *patterns) Resolve(name string) *lex.Pattern {
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

	pattern, err := parsePattern(name, np.Pattern())
	p.set[name] = pattern
	p.unused[name] = np.Name()
	return err
}

func parsePattern(name string, p ast.Pattern) (*lex.Pattern, error) {
	text := p.Text()
	text = text[1 : len(text)-1]
	ret := &lex.Pattern{Name: name, Text: text, RE: emptyRE, Origin: p}
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
		return ret, &status.Error{Origin: rng, Msg: err.Error()}
	}
	ret.RE = re
	return ret, nil
}

var tplMap = map[string]string{
	"go_lexer.stateVars":            "stateVars",
	"go_lexer.initStateVars":        "initStateVars",
	"go_lexer.onAfterNext":          "onAfterNext",
	"go_lexer.onBeforeNext":         "onBeforeNext",
	"go_parser.stateVars":           "parserVars",
	"go_parser.initStateVars":       "initParserVars",
	"go_parser.setupLookaheadLexer": "setupLookaheadLexer",
	"go_parser.onBeforeIgnore":      "onBeforeIgnore",
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
			if match[2] == "-" {
				content = trimLeadingNL(content)
			}
			const callBase = `${call base-}`
			if i := strings.Index(content, callBase); i >= 0 {
				content = content[:i] + trimLeadingNL(content[i+len(callBase):])
			}
			fmt.Fprintf(&buf, `{{define "%v"}}%v{{end}}`, name, content)
		}
	}
	return buf.String()
}

func trimLeadingNL(s string) string {
	return strings.TrimPrefix(strings.TrimPrefix(s, "\r"), "\n")
}
