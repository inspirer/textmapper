package compiler

import (
	"fmt"
	"sort"
	"strconv"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/lex"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/container"
)

var noSpace = ast.LexemeAttribute{}

type lexerCompiler struct {
	options  *optionsParser
	resolver *resolver
	compat   bool
	out      *grammar.Lexer
	*status.Status

	conds       map[string]int
	inclusiveSC []int
	patterns    []*patterns // to keep track of unused patterns
	classRules  []*lex.Rule
	rules       []*lex.Rule
	codeRule    map[symRule]int   // -> index in c.out.Lexer.RuleToken
	codeAction  map[symAction]int // -> index in c.out.Lexer.Actions
	mapping     map[int]syntax.RangeToken
	injected    map[string]bool
}

func newLexerCompiler(options *optionsParser, resolver *resolver, compat bool, s *status.Status) *lexerCompiler {
	return &lexerCompiler{
		options:  options,
		resolver: resolver,
		compat:   compat,
		out:      new(grammar.Lexer),
		Status:   s,

		codeRule:   make(map[symRule]int),
		codeAction: make(map[symAction]int),
		mapping:    make(map[int]syntax.RangeToken),
		injected:   make(map[string]bool),
	}
}

func (c *lexerCompiler) compile(file ast.File) {
	out := c.out

	eoi := c.resolver.addToken(grammar.Eoi, "EOI", ast.RawType{}, noSpace, nil)
	out.InvalidToken = c.resolver.addToken(grammar.InvalidToken, "INVALID_TOKEN", ast.RawType{}, noSpace, nil)

	if p, ok := file.Parser(); ok {
		// Some (space) tokens may be injected into the AST by the parser. We should
		// return them from the lexer despite their space attribute.
		for _, part := range p.GrammarPart() {
			if part, ok := part.(*ast.DirectiveInject); ok {
				if name := part.Symref().Name().Text(); name != "" {
					c.injected[name] = true
				}
			}
		}

		// TODO remove this
		for _, reported := range c.options.reportList {
			c.injected[reported.Text()] = true
		}
	}

	c.collectStartConds(file)
	lexer, _ := file.Lexer()
	c.traverseLexer(lexer.LexerPart(), c.inclusiveSC, nil /*parent patterns*/)
	c.resolveTokenComments()
	c.resolveClasses()

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
	allowBacktracking := !c.options.out.NonBacktracking
	out.Tables, err = lex.Compile(c.rules, allowBacktracking)
	c.AddError(err)

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
			c.Errorf(unused, "unused pattern '%v'", name)
		}
	}
}

// canInlineRules decides whether we can replace rule ids directly with token ids.
func (c *lexerCompiler) canInlineRules() bool {
	// Note: the first two actions are reserved for InvalidToken and EOI respectively.
	seen := container.NewBitSet(c.resolver.NumTokens)
	for _, e := range c.out.RuleToken {
		if e < 2 || seen.Get(e) {
			// Explicit rules for InvalidToken or EOI cannot be inlined.
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

func (c *lexerCompiler) collectStartConds(file ast.File) {
	conds := make(map[string]bool)
	var names []string

	insert := func(n *ast.Node, excl bool) {
		name := n.Text()
		if _, exists := conds[name]; exists {
			c.Errorf(n, "redeclaration of '%v'", name)
			return
		}
		conds[name] = excl
		names = append(names, name)
	}
	lexer, _ := file.Lexer()
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

func (c *lexerCompiler) resolveSC(sc ast.StartConditions) []int {
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
		c.Errorf(ref.Name(), "unresolved reference %v", name)
	}
	sort.Ints(ret)
	return ret
}

func (c *lexerCompiler) addLexerAction(cmd ast.Command, space, class ast.LexemeAttribute, sym int, comment string) int {
	if !cmd.IsValid() && !space.IsValid() && !class.IsValid() && !c.compat {
		if sym == int(lex.EOI) {
			return -1
		}
		// Note: -2 is a dedicated action for implicitly discovered invalid tokens which
		// triggers backtracking. Here we handle an explicitly declared invalid token, so
		// we need a separate action.
	}

	out := c.out
	key := symRule{code: cmd.Text(), space: space.IsValid(), sym: sym}
	if a, ok := c.codeRule[key]; ok && !class.IsValid() && !c.compat {
		if ca, ok := c.codeAction[symAction{key.code, key.space}]; ok && comment != "" {
			out.Actions[ca].Comments = append(out.Actions[ca].Comments, comment)
		}
		return a
	}
	a := len(out.RuleToken)
	out.RuleToken = append(out.RuleToken, sym)
	if !class.IsValid() {
		// Never merge (class) rules, even if they seem identical.
		c.codeRule[key] = a
	}

	if !cmd.IsValid() && !space.IsValid() {
		return a
	}
	act := grammar.SemanticAction{Action: a, Code: key.code, Space: space.IsValid()}
	if comment != "" {
		act.Comments = append(act.Comments, comment)
	}
	if cmd.IsValid() {
		act.Origin = cmd
	} else {
		act.Origin = space
	}
	if !class.IsValid() {
		// Never merge (class) rules, even if they seem identical.
		c.codeAction[symAction{key.code, key.space}] = len(out.Actions)
	}
	out.Actions = append(out.Actions, act)
	return a
}

func (c *lexerCompiler) traverseLexer(parts []ast.LexerPart, defaultSCs []int, p *patterns) {
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
					c.Errorf(attrs.LexemeAttribute(), "unsupported attribute")
				}
			}

			name := p.Name().Text()
			tok := c.resolver.addToken(name, "" /*id*/, rawType, space, p.Name())

			// Handle token mappings.
			rt := c.resolveMapping(tok, p)
			prev, prevOK := c.mapping[tok]
			switch {
			case prevOK:
				if rt.Name != prev.Name || !eq(rt.Flags, prev.Flags) {
					c.Errorf(p.Name(), "inconsistent token mapping for %v: was %v", name, prev)
				}
			case rt.Name != "":
				c.out.MappedTokens = append(c.out.MappedTokens, rt)
				fallthrough
			default:
				c.mapping[tok] = rt
			}

			pat, ok := p.Pattern()
			if !ok {
				break
			}

			pattern, err := parsePattern(name, pat)
			if err != nil {
				c.AddError(err)
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
			if c.injected[name] && space.IsValid() {
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
			c.AddError(ps.add(p))
		case *ast.StartConditionsScope:
			newDefaults := c.resolveSC(p.StartConditions())
			c.traverseLexer(p.LexerPart(), newDefaults, ps)
		case *ast.SyntaxProblem, *ast.DirectiveBrackets:
			c.Errorf(p.TmNode(), "syntax error")
		case *ast.ExclusiveStartConds, *ast.InclusiveStartConds:
			if inClause {
				// %s and %x are not allowed inside start condition clauses.
				c.Errorf(p.TmNode(), "syntax error")
			}
		}
	}
}

func (c *lexerCompiler) resolveMapping(tok int, lexeme *ast.Lexeme) syntax.RangeToken {
	n, ok := lexeme.ReportClause()
	if !ok {
		return syntax.RangeToken{Token: tok}
	}

	name := n.Action().Text()
	if len(name) == 0 {
		return syntax.RangeToken{Token: tok}
	}
	var flags []string
	for _, id := range n.Flags() {
		flags = append(flags, id.Text())
	}
	if as, ok := n.ReportAs(); ok {
		c.Errorf(as, "reporting terminals 'as' some category is not supported")
	}
	return syntax.RangeToken{Token: tok, Name: name, Flags: flags}
}

func (c *lexerCompiler) resolveTokenComments() {
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
		c.resolver.Syms[tok].Comment = val
	}
}

func (c *lexerCompiler) resolveClasses() {
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
		ca := grammar.ClassAction{
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
			c.Errorf(r.Origin, "%v must be applicable in the same set of start conditions as %v", r.Pattern.Name, class.Pattern.Name)

			// Fixing the problem for now and keep going.
			r.StartConditions = class.StartConditions
		}

		// Move the rule under its class rule.
		c.out.ClassActions[classRule].Custom[val] = r.Action
	}

	for i, r := range c.classRules {
		if len(c.out.ClassActions[i].Custom) == 0 {
			c.Errorf(r.Origin, "class rule without specializations '%v'", r.Pattern.Name)
		}
	}

	c.rules = append(out, c.classRules...)
	c.classRules = nil
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

func eq(a, b []string) bool {
	// TODO replace with slices.Equal
	if len(a) != len(b) {
		return false
	}
	for i, v := range a {
		if v != b[i] {
			return false
		}
	}
	return true
}
