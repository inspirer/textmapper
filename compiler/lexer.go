package compiler

import (
	"fmt"
	"log"
	"sort"
	"strconv"
	"strings"
	"unicode"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/lex"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/ident"
)

var noSpace = ast.LexemeAttribute{}

type lexerCompiler struct {
	opts     *grammar.Options
	resolver *resolver
	out      *grammar.Lexer
	*status.Status

	conds       map[string]int
	inclusiveSC []int
	patterns    []*patterns // to keep track of unused patterns
	classRules  []*lex.Rule
	rules       []*lex.Rule
	codeRule    map[symRule]int   // -> index in c.out.Lexer.RuleToken
	codeAction  map[symAction]int // -> index in c.out.Lexer.Actions
	injected    map[string]bool
}

func newLexerCompiler(opts *grammar.Options, resolver *resolver, s *status.Status) *lexerCompiler {
	return &lexerCompiler{
		opts:     opts,
		resolver: resolver,
		out:      new(grammar.Lexer),
		Status:   s,

		codeRule:   make(map[symRule]int),
		codeAction: make(map[symAction]int),
		injected:   make(map[string]bool),
	}
}

func (c *lexerCompiler) compile(file ast.File) {
	out := c.out
	lexer, _ := file.Lexer()
	if c.opts.FlexMode {
		c.parseFlexDeclarations(lexer)
		return
	}

	eoi := c.resolver.addToken(grammar.Eoi, "" /*id*/, ast.RawType{}, noSpace, nil)
	out.InvalidToken = c.resolver.addToken(grammar.InvalidToken, "" /*id*/, ast.RawType{}, noSpace, nil)

	c.addDefaultAction(out.InvalidToken)
	c.addDefaultAction(eoi)

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
	}

	c.collectStartConds(file)
	c.traverseLexer(lexer.LexerPart(), c.inclusiveSC, nil /*parent patterns*/)
	c.resolveTokenComments()
	c.resolveClasses()

	inline := c.canInlineRules()

	var err error
	allowBacktracking := !c.opts.NonBacktracking
	out.Tables, err = lex.Compile(c.rules, c.opts.ScanBytes, allowBacktracking)
	c.AddError(err)

	if inline {
		// There is at most one action per token, so it is possible to use tokens IDs as actions.
		// TODO simplify
		start := out.Tables.ActionStart()
		for i, val := range out.Tables.Dfa {
			if val <= start {
				out.Tables.Dfa[i] = start - out.RuleToken[start-val]
			}
		}
		for i, val := range out.Tables.Backtrack {
			out.Tables.Backtrack[i].Action = out.RuleToken[val.Action]
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
	for _, e := range c.out.RuleToken[2:] {
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

func (c *lexerCompiler) addDefaultAction(sym int) {
	key := symRule{sym: sym}
	if _, ok := c.codeRule[key]; ok {
		log.Fatal("internal error")
	}
	a := len(c.out.RuleToken)
	c.out.RuleToken = append(c.out.RuleToken, sym)
	// TODO remove this condition (will better merge invalid token rules)
	if sym == 0 {
		c.codeRule[key] = a
	}
}

func (c *lexerCompiler) addLexerAction(cmd ast.Command, space, class ast.LexemeAttribute, sym int, comment string) int {
	out := c.out
	key := symRule{code: cmd.Text(), space: space.IsValid(), sym: sym}
	if ret, ok := c.codeRule[key]; ok && !class.IsValid() {
		if ca, ok := c.codeAction[symAction{key.code, key.space}]; ok && comment != "" {
			out.Actions[ca].Comments = append(out.Actions[ca].Comments, comment)
		}
		return ret
	}
	ret := len(out.RuleToken)
	out.RuleToken = append(out.RuleToken, sym)
	if !class.IsValid() {
		// Never merge (class) rules, even if they seem identical.
		c.codeRule[key] = ret
	}

	if !cmd.IsValid() && !space.IsValid() {
		return ret
	}
	act := grammar.SemanticAction{Action: ret, Code: key.code, Space: space.IsValid()}
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
	return ret
}

func (c *lexerCompiler) traverseLexer(parts []ast.LexerPart, defaultSCs []int, p *patterns) {
	inClause := p != nil
	ps := &patterns{
		parent: p,
		set:    make(map[string]*lex.Pattern),
		unused: make(map[string]status.SourceNode),
	}
	c.patterns = append(c.patterns, ps)

	reOpts := lex.CharsetOptions{ScanBytes: c.opts.ScanBytes, Fold: c.opts.CaseInsensitive}
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
			var id string
			if lid, ok := p.LexemeId(); ok {
				id = lid.Identifier().Text()
				if strings.ContainsFunc(id, unicode.IsLower) {
					id = ident.Produce(id, ident.UpperCase)
				}
			}
			tok := c.resolver.addToken(name, id, rawType, space, p.Name())

			pat, ok := p.Pattern()
			if !ok {
				break
			}

			pattern, err := parsePattern(name, pat, reOpts)
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
			c.AddError(ps.add(p, reOpts))
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

func (c *lexerCompiler) resolveTokenComments() {
	comments := make(map[int]string)
	for _, r := range c.rules {
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
		fork.Action = index + 1 // 0 is reserved for InvalidToken
		rewritten = append(rewritten, fork)
	}
	tables, err := lex.Compile(rewritten, c.opts.ScanBytes, true /*allowBacktracking*/)
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
			if size == len(val) && result > 0 {
				classRule = result - 1
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

var emptyRE = lex.MustParse("", lex.CharsetOptions{})

func (p *patterns) add(np *ast.NamedPattern, opts lex.CharsetOptions) error {
	name := np.Name().Text()
	if _, exists := p.set[name]; exists {
		return status.Errorf(np.Name(), "redeclaration of '%v'", name)
	}

	pattern, err := parsePattern(name, np.Pattern(), opts)
	p.set[name] = pattern
	p.unused[name] = np.Name()
	return err
}

func parsePattern(name string, p ast.Pattern, opts lex.CharsetOptions) (*lex.Pattern, error) {
	text := p.Text()
	text = text[1 : len(text)-1]
	ret := &lex.Pattern{Name: name, Text: text, RE: emptyRE, Origin: p}
	re, err := lex.ParseRegexp(text, opts)
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

func (c *lexerCompiler) parseFlexDeclarations(lexer ast.LexerSection) {
	c.resolver.addToken(grammar.Eoi, "" /*id*/, ast.RawType{}, noSpace, nil)
	error := c.resolver.addToken(grammar.Error, "YYerror", ast.RawType{}, noSpace, nil)
	c.out.InvalidToken = c.resolver.addToken(grammar.InvalidToken, "" /*id*/, ast.RawType{}, noSpace, nil)

	c.resolver.Syms[error].FlexID = 256
	c.resolver.Syms[c.out.InvalidToken].FlexID = 257
	next := 258

	reOpts := lex.CharsetOptions{ScanBytes: c.opts.ScanBytes, Fold: c.opts.CaseInsensitive}
	for _, p := range lexer.LexerPart() {
		switch p := p.(type) {
		case *ast.Lexeme:
			rawType, _ := p.RawType()
			var space ast.LexemeAttribute
			if attrs, ok := p.Attrs(); ok {
				switch name := attrs.LexemeAttribute().Text(); name {
				case "space":
					space = attrs.LexemeAttribute()
				default:
					c.Errorf(attrs.LexemeAttribute(), "unsupported attribute (flex mode)")
				}
			}
			if prio, ok := p.Priority(); ok {
				c.Errorf(prio, "priorities are not supported in flex mode")
			}
			if sc, ok := p.StartConditions(); ok {
				c.Errorf(sc, "start conditions are not supported in flex mode")
			}
			if cmd, ok := p.Command(); ok {
				c.Errorf(cmd, "commands are not supported in flex mode")
			}

			name := p.Name().Text()
			if _, exists := c.resolver.syms[name]; exists {
				c.Errorf(p.Name(), "redeclaration of '%v'", name)
				continue
			}
			var id string
			if lid, ok := p.LexemeId(); ok {
				id = lid.Identifier().Text()
				if strings.ContainsFunc(id, unicode.IsLower) {
					id = ident.Produce(id, ident.UpperCase)
				}
			}
			tok := c.resolver.addToken(name, id, rawType, space, p.Name())

			// Flex mode allows only ASCII characters as patterns. All other tokens should be
			// simply declared (without patterns).

			var ch byte
			if pat, ok := p.Pattern(); ok {
				pattern, err := parsePattern(name, pat, reOpts)
				if err != nil {
					c.AddError(err)
					continue
				}

				val, ok := pattern.RE.Constant()
				if !ok || len(val) != 1 || val[0] < ' ' || val[0] > '~' {
					c.Errorf(pattern.Origin, "only individual ASCII characters are allowed as patterns in flex mode")
					continue
				}
				ch = val[0]
			}

			var flexID int
			if ch != 0 {
				flexID = int(ch)
			} else {
				flexID = next
				next++
			}
			c.resolver.Syms[tok].FlexID = flexID

			var comment string
			if c := p.Next(selector.Any); c.Type() == tm.Comment && strings.HasPrefix(c.Text(), "//") && strings.Count(p.Tree().Text()[p.Endoffset():c.Offset()], "\n") == 0 {
				// This is a trailing comment on the same line.
				comment = strings.TrimSpace(strings.TrimPrefix(c.Text(), "//"))
			}
			c.resolver.Syms[tok].Comment = comment

		case *ast.NamedPattern:
			c.Errorf(p, "named patterns are not supported in flex mode")
		case *ast.ExclusiveStartConds, *ast.InclusiveStartConds, *ast.StartConditionsScope:
			c.Errorf(p.TmNode(), "start conditions are not supported in flex mode")
		case *ast.SyntaxProblem, *ast.DirectiveBrackets:
			c.Errorf(p.TmNode(), "syntax error")
		}
	}
}
