// Package compiler compiles TextMapper grammars.
package compiler

import (
	"context"
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
)

// Params control the grammar compilation process.
type Params struct {
	CheckOnly   bool // set to true, if the caller is interested in compilation errors only
	Verbose     bool // set to true for more verbose errors
	DebugTables bool // set to true to get generated tables with embedded debug info
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

func (c *compiler) compileParser(file ast.File) {
	p, ok := file.Parser()
	if !ok || !c.out.Options.GenParser {
		// Lexer-only grammar.
		return
	}

	loader := newSyntaxLoader(c.resolver, c.out.Options.NoEmptyRules, c.Status)
	loader.load(p, file.Header())
	if c.Err() != nil {
		// Parsing errors cause inconsistencies inside c.source. Aborting.
		return
	}
	c.out.Sets = loader.sets
	c.out.Parser.Prec = loader.prec

	source := loader.out
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

	if err := syntax.Expand(source); err != nil {
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

	opts := genOptions{
		expectSR: loader.expectSR,
		expectRR: loader.expectRR,
		syms:     c.out.Syms,
		lalrOpts: lalr.Options{
			Optimize:      c.out.Options.OptimizeTables && !c.params.CheckOnly,
			DefaultReduce: c.out.Options.DefaultReduce,
			Debug:         c.params.DebugTables,
		},
	}
	if err := generateTables(source, c.out.Parser, opts, file); err != nil {
		c.AddError(err)
	}
}

type genOptions struct {
	expectRR int
	expectSR int
	syms     []grammar.Symbol

	lalrOpts lalr.Options
}

func generateTables(source *syntax.Model, out *grammar.Parser, opts genOptions, origin status.SourceNode) error {
	var s status.Status
	g := &lalr.Grammar{
		Terminals:  len(source.Terminals),
		Precedence: out.Prec,
		ExpectSR:   opts.expectSR,
		ExpectRR:   opts.expectRR,
		Origin:     origin,
	}
	for _, sym := range opts.syms {
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
	if out.Types != nil {
		for i, t := range out.Types.RangeTypes {
			types[t.Name] = i
		}
		for _, t := range out.Types.Categories {
			cats[t.Name] = true
		}
	}

	// The very first action is a no-op.
	out.Actions = append(out.Actions, grammar.SemanticAction{})

	var rules []*grammar.Rule
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
				LHS:        lalr.Sym(g.Terminals + self),
				Type:       -1,
				Origin:     nt.Origin,
				OriginName: nt.Name,
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
			var report []grammar.Range
			var command string
			var args *syntax.CmdArgs
			var numRefs int
			actualPos := make(map[int]int) // $i inside a semantic action -> index in rule.RHS
			origin := expr.Origin

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
								out.UsedFlags = append(out.UsedFlags, f)
							}
						}
					}
				case syntax.Sequence, syntax.Assign, syntax.Append:
					for _, sub := range expr.Sub {
						traverse(sub)
					}
				case syntax.Reference:
					if command != "" {
						// TODO This command needs to be extracted into a dedicated nonterminal.
						s.Errorf(origin, "commands must be placed at the end of a rule")
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
			sort.Strings(out.UsedFlags)

			if last := len(report) - 1; last >= 0 && report[last].Start == 0 && report[last].End == numRefs {

				// Promote to the rule default.
				rule.Type = report[last].Type
				rule.Flags = report[last].Flags
				report = report[:last]
			}
			if len(report) > 0 || command != "" {
				// TODO reuse existing actions
				act := grammar.SemanticAction{
					Report: report,
					Code:   command,
					Origin: origin,
				}
				if args != nil {
					act.Vars = &grammar.ActionVars{CmdArgs: *args, Remap: actualPos}
					for _, r := range rule.RHS {
						if r.IsStateMarker() {
							continue
						}
						act.Vars.Types = append(act.Vars.Types, opts.syms[r].Type)
					}
					act.Vars.LHSType = opts.syms[rule.LHS].Type
				}
				rule.Action = len(out.Actions)
				out.Actions = append(out.Actions, act)
			}
			g.Rules = append(g.Rules, rule)
			rules = append(rules, &grammar.Rule{Rule: rule, Value: exprWithPrec})
		}
	}
	if s.Err() != nil {
		// Parsing errors cause inconsistencies inside c.source. Aborting.
		return s.Err()
	}

	out.Rules = rules
	out.Inputs = source.Inputs
	out.Nonterms = source.Nonterms
	out.NumTerminals = len(source.Terminals)

	tables, err := lalr.Compile(g, opts.lalrOpts)
	out.Tables = tables
	return err
}
