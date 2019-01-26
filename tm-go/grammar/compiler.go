package grammar

import (
	"github.com/inspirer/textmapper/tm-go/lex"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"sort"
	"strconv"
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
		ids:        make(map[string]int),
		codeAction: make(map[symCode]int),
	}
	c.compileLexer()
	c.parseOptions()
	return c.out, c.s.Err()
}

type compiler struct {
	file ast.File
	out  *Grammar
	s    status.Status

	syms        map[string]int
	ids         map[string]int
	conds       map[string]int
	inclusiveSC []int
	patterns    []*patterns // to keep track of unused patterns
	classRules  []*lex.Rule
	rules       []*lex.Rule
	codeAction  map[symCode]int
}

type symCode struct {
	code  string
	space bool
	class bool
	sym   int
}

func (c *compiler) compileLexer() {
	eoi := c.addToken(Eoi, "EOI", ast.RawType{}, nil)
	c.out.InvalidToken = c.addToken(InvalidToken, "INVALID_TOKEN", ast.RawType{}, nil)

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
		c.out.RuleToken = append(c.out.RuleToken, 0, 0)
		copy(c.out.RuleToken[2:], c.out.RuleToken)
		c.out.RuleToken[0] = c.out.InvalidToken
		c.out.RuleToken[1] = eoi
	}

	var err error
	c.out.Tables, err = lex.Compile(c.rules)
	c.s.AddError(err)

	if inline {
		// There is at most one action per token, so it is possible to use tokens IDs as actions.
		// We need to swap EOI and InvalidToken actions.
		// TODO simplify
		start := c.out.Tables.ActionStart()
		for i, val := range c.out.Tables.Dfa {
			if val == start {
				c.out.Tables.Dfa[i] = start - 1
			} else if val == start-1 {
				c.out.Tables.Dfa[i] = start
			} else if val < start {
				c.out.Tables.Dfa[i] = start - c.out.RuleToken[start-val-2]
			}
		}
		for i, val := range c.out.Tables.Backtrack {
			c.out.Tables.Backtrack[i].Action = c.out.RuleToken[val.Action-2]
		}
		for i, val := range c.out.ClassActions {
			c.out.ClassActions[i].Action = c.out.RuleToken[val.Action]
			for k, v := range val.Custom {
				val.Custom[k] = c.out.RuleToken[v]
			}
		}
		for i, val := range c.out.Actions {
			c.out.Actions[i].Action = c.out.RuleToken[val.Action]
		}
		c.out.RuleToken = nil
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
	for _, a := range c.out.Actions {
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
			c.errorf(n, "redeclaration of %v", name)
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

func (c *compiler) addToken(name, id string, t ast.RawType, n status.SourceNode) int {
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
		return sym.Index
	}
	if id == "" {
		id = SymbolID(name, UpperCase)
	}
	if i, exists := c.ids[id]; exists {
		prev := c.out.Syms[i].Name
		c.errorf(n, "%v and %v get the same ID in generated code", name, prev)
	}

	sym := Symbol{
		Index:  len(c.syms),
		Type:   rawType,
		ID:     id,
		Name:   name,
		Origin: n,
	}
	c.syms[name] = sym.Index
	c.ids[id] = sym.Index
	c.out.Syms = append(c.out.Syms, sym)
	return sym.Index
}

func (c *compiler) addLexerAction(cmd ast.Command, space, class ast.LexemeAttribute, sym int) int {
	if !cmd.IsValid() && !space.IsValid() && !class.IsValid() {
		if sym == int(lex.EOI) {
			return -1
		} else if sym == c.out.InvalidToken {
			return -2
		}
	}

	key := symCode{code: cmd.Text(), space: space.IsValid(), class: class.IsValid(), sym: sym}
	if a, ok := c.codeAction[key]; ok {
		return a
	}
	a := len(c.out.RuleToken)
	c.out.RuleToken = append(c.out.RuleToken, sym)
	c.codeAction[key] = a

	if !cmd.IsValid() && !space.IsValid() {
		return a
	}
	act := SemanticAction{Action: a, Code: key.code, Space: space.IsValid()}
	if cmd.IsValid() {
		act.Origin = cmd
	} else {
		act.Origin = space
	}
	c.out.Actions = append(c.out.Actions, act)
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
			tok := c.addToken(p.Name().Text(), "" /*id*/, rawType, p.Name())
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
				OriginName:      p.Name().Text(),
			}

			if prio, ok := p.Priority(); ok {
				rule.Precedence, _ = strconv.Atoi(prio.Text())
			}
			if sc, ok := p.StartConditions(); ok {
				rule.StartConditions = c.resolveSC(sc)
			}

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

			cmd, _ := p.Command()
			rule.Action = c.addLexerAction(cmd, space, class, tok)
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

func (c *compiler) parseExpr(e ast.Expression, defaultVal interface{}) interface{} {
	switch e := e.(type) {
	case *ast.Array:
		if _, ok := defaultVal.([]int); ok {
			var ret []int
			for _, el := range e.Expression() {
				if ref, ok := el.(*ast.Symref); ok {
					id := ref.Name().Text()
					sym, ok := c.syms[id]
					if !ok {
						c.errorf(el.TmNode(), "unresolved reference '%v'", id)
						continue
					}
					// TODO correctly resolve templated nonterminals
					ret = append(ret, sym)
					continue
				}
				c.errorf(el.TmNode(), "symbol reference is expected")
			}
			return ret
		}
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
		case "eventFields":
			opts.EventFields = c.parseExpr(kv.Value(), opts.EventFields).(bool)
		case "eventAST":
			opts.EventAST = c.parseExpr(kv.Value(), opts.EventAST).(bool)
		case "reportTokens":
			opts.ReportTokens = c.parseExpr(kv.Value(), opts.ReportTokens).([]int)
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
		return status.Errorf(np.Name(), "redeclaration of %v", name)
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
