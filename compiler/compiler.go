// Package compiler compiles TextMapper grammars.
package compiler

import (
	"context"
	"fmt"
	"log"
	"sort"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/ident"
)

// Params control the grammar compilation process.
type Params struct {
	CheckOnly    bool // set to true, if the caller is interested in compilation errors only
	Verbose      bool // set to true for more verbose errors
	DebugTables  bool // set to true to get generated tables with embedded debug info
	CollectStats bool // set to true to collect more statistics about the LALR algorithm execution
}

// Compile validates and compiles grammar files.
func Compile(ctx context.Context, path, content string, params Params) (*grammar.Grammar, error) {
	tree, err := ast.Parse(ctx, path, content, tm.StopOnFirstError)
	if err != nil {
		return nil, err
	}
	file := ast.File{Node: tree.Root()}

	var s status.Status

	opts := newOptionsParser(&s)
	opts.parseFrom(file)

	resolver := newResolver(&s)

	lexer := newLexerCompiler(opts.out, resolver, &s)
	lexer.compile(file)

	c := newCompiler(file, opts.out, lexer.out, resolver, params, &s)
	c.compileParser(file)

	c.out.CustomTemplates = strings.TrimPrefix(file.Child(selector.Templates).Text(), "%%")
	return c.out, s.Err()
}

type compiler struct {
	out      *grammar.Grammar
	resolver *resolver
	params   Params
	*status.Status
}

func newCompiler(file ast.File, opts *grammar.Options, lexer *grammar.Lexer, resolver *resolver, params Params, s *status.Status) *compiler {
	targetLang, _ := file.Header().Target()
	return &compiler{
		out: &grammar.Grammar{
			Name:       file.Header().Name().Text(),
			TargetLang: targetLang.Text(),
			Lexer:      lexer,
			Parser:     &grammar.Parser{},
			Options:    opts,
			Syms:       resolver.Syms,
			NumTokens:  resolver.NumTokens,
		},
		resolver: resolver,
		params:   params,
		Status:   s,
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
	sym   int
}

func addSyntheticInputs(m *syntax.Model) {
	seen := make(map[lalr.Sym]bool)
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
}

// longestPhrase computes the length of the longest string (in tokens) that can be
// matched by a given nonterminal.
//
// It returns false if the length is unbounded.
func longestPhrase(m *syntax.Model, expr *syntax.Expr, memo map[int]int) (int, bool) {
	switch expr.Kind {
	case syntax.Empty, syntax.StateMarker, syntax.Command, syntax.Lookahead:
		return 0, true
	case syntax.Set:
		return 1, true
	case syntax.List:
		// Lists are always unbounded.
		return 0, false
	case syntax.Assign, syntax.Append, syntax.Arrow, syntax.Prec, syntax.Optional:
		return longestPhrase(m, expr.Sub[0], memo)
	case syntax.Choice:
		var ret int
		for _, c := range expr.Sub {
			val, ok := longestPhrase(m, c, memo)
			if !ok {
				return 0, false
			}
			ret = max(ret, val)
		}
		return ret, true
	case syntax.Sequence:
		var ret int
		for _, c := range expr.Sub {
			val, ok := longestPhrase(m, c, memo)
			if !ok {
				return 0, false
			}
			ret += val
		}
		return ret, true
	case syntax.Reference:
		if expr.Symbol < len(m.Terminals) {
			return 1, true
		}

		// Nonterminal reference.
		val, ok := memo[expr.Symbol]
		if ok {
			// Recursive definitions are unbounded.
			return val, val > 0
		}
		memo[expr.Symbol] = 0 // computing
		val, ok = longestPhrase(m, m.Nonterms[expr.Symbol-len(m.Terminals)].Value, memo)
		memo[expr.Symbol] = val
		return val, ok
	default:
		log.Fatal("invariant failure")
		return 0, false
	}
}

func checkLookaheads(m *syntax.Model, maxSize int) error {
	var s status.Status
	seen := make(map[int]int)
	m.ForEach(syntax.Lookahead, func(_ *syntax.Nonterm, expr *syntax.Expr) {
		for _, sub := range expr.Sub {
			if sub.Kind == syntax.LookaheadNot {
				sub = sub.Sub[0]
			}
			if sub.Kind == syntax.Reference && sub.Symbol >= len(m.Terminals) {
				length, checked := seen[sub.Symbol]
				if !checked {
					max, ok := longestPhrase(m, sub, make(map[int]int))
					length = max
					if !ok {
						length = -1
					}
					seen[sub.Symbol] = length
				}
				name := m.Nonterms[sub.Symbol-len(m.Terminals)].Name
				switch {
				case length == -1:
					s.Errorf(sub.Origin, "lookahead for %v is unbounded", name)
				case length > maxSize:
					s.Errorf(sub.Origin, "lookahead for %v is too long (%v tokens)", name, length)
				}
			}
		}
	})
	return s.Err()
}

func checkSyntaxes(m *syntax.Model, opts *grammar.Options) error {
	var s status.Status
	disabled := make(map[string]bool)
	for _, kind := range opts.DisableSyntax {
		disabled[kind] = true
	}
	var visit func(e *syntax.Expr, top bool)
	visit = func(e *syntax.Expr, top bool) {
		if disabled["NestedChoice"] && e.Kind == syntax.Choice && !top {
			s.Errorf(e.Origin, "parenthesized Choice operator is not supported")
		}
		var kind = e.Kind.GoString()
		if disabled[kind] {
			s.Errorf(e.Origin, "syntax %v is not supported", kind)
		}
		for _, sub := range e.Sub {
			visit(sub, false)
		}
	}
	for _, nt := range m.Nonterms {
		if len(nt.Params) > 0 && disabled["Templates"] {
			s.Errorf(nt.Origin, "templates are not supported")
		}
		visit(nt.Value, true)
	}
	return s.Err()
}

func (c *compiler) compileParser(file ast.File) {
	p, ok := file.Parser()
	if !ok || !c.out.Options.GenParser {
		// Lexer-only grammar.
		return
	}
	target, _ := file.Header().Target()
	loader := newSyntaxLoader(c.resolver, target.Text(), c.out.Options, c.Status)
	loader.load(p, file.Header())
	if c.Err() != nil {
		// Parsing errors cause inconsistencies inside c.source. Aborting.
		return
	}
	c.out.Sets = loader.sets
	c.out.Parser.Prec = loader.prec

	source := loader.out

	if len(c.out.Options.DisableSyntax) > 0 {
		err := checkSyntaxes(source, c.out.Options)
		if err != nil {
			c.AddError(err)
			return
		}
	}

	if err := syntax.PropagateLookaheads(source); err != nil {
		c.AddError(err)
		return
	}

	if err := syntax.Instantiate(source); err != nil {
		c.AddError(err)
		return
	}

	if c.out.Options.EventBased {
		tokens := loader.mapping
		opts := syntax.TypeOptions{
			EventFields: c.out.Options.EventFields,
			GenSelector: c.out.Options.GenSelector,
			ExtraTypes:  c.out.Options.ExtraTypes,
		}
		types, err := syntax.ExtractTypes(source, tokens, opts)
		if err != nil {
			c.AddError(err)
			return
		}
		c.out.Parser.Types = types
		c.out.Parser.MappedTokens = tokens

		seenFlags := make(map[string]bool)
		for _, t := range tokens {
			for _, f := range t.Flags {
				if !seenFlags[f] {
					seenFlags[f] = true
					c.out.Lexer.UsedFlags = append(c.out.Lexer.UsedFlags, f)
				}
			}
		}
		sort.Strings(c.out.Lexer.UsedFlags)
	}

	if c.out.Options.MaxLookahead > 0 {
		err := checkLookaheads(source, c.out.Options.MaxLookahead)
		if err != nil {
			c.AddError(err)
			return
		}
	}

	if err := syntax.Expand(source, loader.expandOpts); err != nil {
		c.AddError(err)
		return
	}

	// Use instantiated nonterminal names to describe sets in generated code.
	old := source.Terminals
	for _, set := range c.out.Sets {
		in := source.Sets[loader.namedSets[set.Name]]
		set.Expr = "set(" + in.String(source) + ")"
	}
	source.Terminals = old

	if err := syntax.ResolveSets(source); err != nil {
		c.AddError(err)
		return
	}

	if errSym, ok := c.resolver.syms["error"]; ok {
		if index, ok := loader.namedSets["afterErr"]; ok {
			// Non-empty "afterErr" set turns on error recovery.
			c.out.Parser.IsRecovering = len(source.Sets[index].Sub) > 0
			c.out.Parser.ErrorSymbol = errSym
		}
	}

	// Export computed named sets for code generation.
	for _, set := range c.out.Sets {
		in := source.Sets[loader.namedSets[set.Name]]
		for _, term := range in.Sub {
			set.Terminals = append(set.Terminals, term.Symbol)
		}
	}

	// Introduce synthetic inputs for runtime lookaheads.
	addSyntheticInputs(source)

	// Prepare the model for code generation.
	c.resolver.addNonterms(source)
	c.out.Syms = c.resolver.Syms

	var lookahead int
	if la, ok := p.Lookahead(); ok {
		lookahead, _ = strconv.Atoi(la.Text())
		const maxLookahead = 8
		if lookahead < 1 || lookahead > maxLookahead {
			c.Errorf(la, "lookahead value of %v is out of the [1, %v] range", lookahead, maxLookahead)
			lookahead = 1
		}
	}

	opts := genOptions{
		expectSR: loader.expectSR,
		expectRR: loader.expectRR,
		lalrOpts: lalr.Options{
			Lookahead:     lookahead,
			Optimize:      c.out.Options.OptimizeTables && !c.params.CheckOnly,
			DefaultReduce: c.out.Options.DefaultReduce,
			Debug:         c.params.DebugTables,
			CollectStats:  c.params.CollectStats,
			Verbose:       c.params.Verbose,
		},
	}
	if err := generateTables(source, c.out, opts, file); err != nil {
		c.AddError(err)
	}
}

type genOptions struct {
	expectRR int
	expectSR int

	lalrOpts lalr.Options
}

func generateTables(source *syntax.Model, out *grammar.Grammar, opts genOptions, origin status.SourceNode) error {
	var s status.Status
	parser := out.Parser
	g := &lalr.Grammar{
		Terminals:  len(source.Terminals),
		Precedence: parser.Prec,
		ExpectSR:   opts.expectSR,
		ExpectRR:   opts.expectRR,
		Origin:     origin,
	}
	for _, sym := range out.Syms {
		g.Symbols = append(g.Symbols, sym.Name)
	}
	inputs := make(map[lalr.Input]int32)
	for _, inp := range source.Inputs {
		out := lalr.Input{
			Nonterminal: lalr.Sym(g.Terminals + inp.Nonterm),
			Eoi:         !inp.NoEoi,
		}
		inputs[out] = int32(len(g.Inputs))
		g.Inputs = append(g.Inputs, out)
	}
	markers := make(map[string]int)
	types := make(map[string]int)
	cats := make(map[string]bool)
	seenFlags := make(map[string]bool)
	if parser.Types != nil {
		for i, t := range parser.Types.RangeTypes {
			types[t.Name] = i
		}
		for _, t := range parser.Types.Categories {
			cats[t.Name] = true
		}
	}

	// The very first action is a no-op.
	parser.Actions = append(parser.Actions, grammar.SemanticAction{})
	var rules []*grammar.Rule
	midrule := newCommandExtractor(source, len(out.Syms))
	for self, nt := range source.Nonterms {
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
				LHS:    lalr.Sym(g.Terminals + self),
				Type:   -1,
				Origin: nt.Origin,
			}
			g.Lookaheads = append(g.Lookaheads, la)
			g.Rules = append(g.Rules, rule)
			rules = append(rules, &grammar.Rule{Rule: rule, Value: nt.Value})
			continue
		}

		if nt.Value.Kind != syntax.Choice {
			log.Fatalf("%v is not properly instantiated: %v", nt.Name, nt.Value)
		}
		for _, expr := range nt.Value.Sub {
			pure := expr
			for pure.Kind == syntax.Arrow && len(pure.Sub) == 1 {
				pure = pure.Sub[0]
			}

			rule := lalr.Rule{
				LHS:    lalr.Sym(g.Terminals + self),
				Type:   -1,
				Origin: pure.Origin, // without arrows
			}
			exprWithPrec := expr
			if expr.Kind == syntax.Prec {
				rule.Precedence = lalr.Sym(expr.Symbol)
				expr = expr.Sub[0]
			}
			var report []grammar.Range
			var command string
			var args *syntax.CmdArgs
			var numRefs int
			actualPos := make(map[int]int) // $i inside a semantic action -> index in rule.RHS
			cmdOrigin := expr.Origin

			var traverse func(expr *syntax.Expr)
			traverse = func(expr *syntax.Expr) {
				switch expr.Kind {
				case syntax.Arrow:
					start := numRefs
					for _, sub := range expr.Sub {
						traverse(sub)
					}
					if cats[expr.Name] {
						// Categories are used during the grammar analysis only and don't need
						// to be reported.
						break
					}
					if t, ok := types[expr.Name]; ok { // !ok for categories
						end := numRefs
						report = append(report, grammar.Range{
							Start: start,
							End:   end,
							Type:  t,
							Flags: expr.ArrowFlags,
						})
						for _, f := range expr.ArrowFlags {
							if !seenFlags[f] {
								seenFlags[f] = true
								parser.UsedFlags = append(parser.UsedFlags, f)
							}
						}
					}
				case syntax.Sequence, syntax.Assign, syntax.Append:
					for _, sub := range expr.Sub {
						traverse(sub)
					}
				case syntax.Reference:
					if command != "" {
						// We have a pending mid-rule command. Extract it into an nullable nonterminal.
						var vars *grammar.ActionVars
						if args != nil {
							vars = &grammar.ActionVars{CmdArgs: *args, Remap: actualPos}
							for _, r := range rule.RHS {
								if r.IsStateMarker() {
									s.Errorf(origin, "mixing mid-rule actions with state markers is not supported")
								} else {
									vars.SymRefCount++
								}
							}
							addTypes(vars, out.Syms)
						}
						cmdNT := midrule.extract(nt, command, vars, cmdOrigin)
						rule.RHS = append(rule.RHS, cmdNT)
						numRefs++

						// Reset the command.
						command = ""
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
					cmdOrigin = expr.Origin
				}
			}
			traverse(expr)
			sort.Strings(parser.UsedFlags)

			if last := len(report) - 1; last >= 0 && report[last].Start == 0 && report[last].End == numRefs {
				// Promote to the rule default.
				rule.Type = report[last].Type
				rule.Flags = report[last].Flags
				report = report[:last]
			}
			for _, r := range report {
				if r.Start == r.End && r.Start == numRefs {
					s.Errorf(expr.Origin, "reporting empty ranges at the end of a rule is not allowed")
				}
			}
			if len(report) > 0 || command != "" {
				// TODO reuse existing actions
				act := grammar.SemanticAction{
					Report: report,
					Code:   command,
					Origin: cmdOrigin,
				}
				if args != nil {
					act.Vars = &grammar.ActionVars{CmdArgs: *args, Remap: actualPos}
					for _, r := range rule.RHS {
						if !r.IsStateMarker() {
							act.Vars.SymRefCount++
						}
					}
					addTypes(act.Vars, out.Syms)
					act.Vars.LHSType = out.Syms[rule.LHS].Type
				}
				rule.Action = len(parser.Actions)
				parser.Actions = append(parser.Actions, act)
			}
			g.Rules = append(g.Rules, rule)
			rules = append(rules, &grammar.Rule{Rule: rule, Value: exprWithPrec})
		}
	}
	if s.Err() != nil {
		// Parsing errors cause inconsistencies inside c.source. Aborting.
		return s.Err()
	}

	parser.Rules = rules
	parser.Inputs = source.Inputs
	parser.Nonterms = source.Nonterms
	parser.NumTerminals = len(source.Terminals)
	midrule.finalize(out, g)

	tables, err := lalr.Compile(g, opts.lalrOpts)
	parser.Tables = tables
	return err
}

// addTypes updates the `Types` field of the given action variables using the type information
// from the given symbols `syms`.
func addTypes(vars *grammar.ActionVars, syms []grammar.Symbol) {
	vars.Types = make(map[int]string)
	for _, ref := range vars.CmdArgs.ArgRefs {
		if ref.Symbol < len(syms) {
			vars.Types[ref.Pos] = syms[ref.Symbol].Type
		} else {
			// No types for extracted commands.
			vars.Types[ref.Pos] = ""
		}
	}
}

type commandExtractor struct {
	baseSyms  int
	takenName map[string]bool
	index     map[commandKey]lalr.Sym
	prev      *syntax.Nonterm
	counter   int

	// Delayed output.
	syms    []grammar.Symbol
	midrule []*syntax.Nonterm
	actions []grammar.SemanticAction
	rules   []lalr.Rule
}

type commandKey struct {
	command    string
	varsDigest string
}

func newCommandExtractor(m *syntax.Model, baseSyms int) *commandExtractor {
	taken := make(map[string]bool)
	for _, t := range m.Terminals {
		taken[t.Name] = true
	}
	for _, p := range m.Params {
		taken[p.Name] = true
	}
	for _, nt := range m.Nonterms {
		taken[nt.Name] = true
	}
	return &commandExtractor{takenName: taken, index: make(map[commandKey]lalr.Sym), baseSyms: baseSyms}
}

func (e *commandExtractor) extract(n *syntax.Nonterm, command string, vars *grammar.ActionVars, cmdOrigin status.SourceNode) lalr.Sym {
	key := commandKey{command, vars.String()}
	if sym, ok := e.index[key]; ok {
		return sym
	}

	if n != e.prev {
		e.prev = n
		e.counter = 0
	}

	// Extract this middle-rule command into a nonterminal.
	var name string
	for {
		e.counter++
		name = fmt.Sprintf("%s$%v", n.Name, e.counter)
		if _, ok := e.takenName[name]; !ok {
			break
		}
	}
	e.takenName[name] = true
	var args *syntax.CmdArgs
	if vars != nil {
		args = new(syntax.CmdArgs)
		*args = vars.CmdArgs

		// Give a hint to the code generator that this rule's rhs starts
		// earlier in the stack.
		args.Delta = -vars.SymRefCount

		// Make a copy.
		copy := *vars
		copy.CmdArgs = *args
		vars = &copy
	}

	// Update the syntax model.
	nt := &syntax.Nonterm{
		Name: name,
		Value: &syntax.Expr{Kind: syntax.Choice, Sub: []*syntax.Expr{
			{Kind: syntax.Command, Name: command, CmdArgs: args, Origin: cmdOrigin},
		}},
		Origin: cmdOrigin,
	}
	ntID := e.baseSyms + len(e.syms)
	e.syms = append(e.syms, grammar.Symbol{
		Index:  ntID,
		Name:   name,
		ID:     ident.Produce(name, ident.CamelCase),
		Origin: cmdOrigin,
	})
	e.midrule = append(e.midrule, nt)
	e.index[key] = lalr.Sym(ntID)

	// Ingest the new rule into the LALR grammar.
	rule := lalr.Rule{
		LHS:    lalr.Sym(ntID),
		Type:   -1,
		Origin: nt.Origin,
	}
	act := grammar.SemanticAction{
		Code:   command,
		Vars:   vars,
		Origin: cmdOrigin,
	}
	rule.Action = len(e.actions)
	e.actions = append(e.actions, act)

	e.rules = append(e.rules, rule)
	return lalr.Sym(ntID)
}

func (e *commandExtractor) finalize(out *grammar.Grammar, g *lalr.Grammar) {
	out.Syms = append(out.Syms, e.syms...)
	for _, sym := range e.syms {
		g.Symbols = append(g.Symbols, sym.Name)
	}
	out.Parser.Nonterms = append(out.Parser.Nonterms, e.midrule...)

	base := len(out.Parser.Actions)
	out.Parser.Actions = append(out.Parser.Actions, e.actions...)
	for i, rule := range e.rules {
		e.rules[i].Action = base + rule.Action
	}
	g.Rules = append(g.Rules, e.rules...)

	for i := range e.midrule {
		out.Parser.Rules = append(out.Parser.Rules, &grammar.Rule{Rule: e.rules[i], Value: e.midrule[i].Value})
	}
}
