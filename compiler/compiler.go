// Package compiler compiles TextMapper grammars.
package compiler

import (
	"context"
	"fmt"
	"log"
	"regexp"
	"sort"
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
	Compat    bool
	CheckOnly bool // set to true, if the caller is interested in compilation errors only
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

	lexer := newLexerCompiler(opts, resolver, params.Compat, &s)
	lexer.compile(file)

	// Resolve terminal references.
	opts.resolve(resolver)

	c := newCompiler(file, opts.out, lexer.out, resolver, params, &s)
	c.compileParser(file)

	tpl := strings.TrimPrefix(file.Child(selector.Templates).Text(), "%%")
	if params.Compat {
		c.out.CustomTemplates = parseInGrammarTemplates(tpl)
	} else {
		c.out.CustomTemplates = tpl
	}
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

func (c *compiler) compileParser(file ast.File) {
	p, ok := file.Parser()
	if !ok || !c.out.Options.GenParser {
		// Lexer-only grammar.
		return
	}

	loader := newSyntaxLoader(c.resolver, c.Status)
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
		tokens := c.out.Lexer.MappedTokens
		switch {
		case len(loader.mapping) > 0:
			// Prefer the injection mapping from the grammar.
			if len(tokens) > 0 {
				c.Errorf(file.Header(), "reportTokens are ignored in the presence of %%inject directives")
			}
			if len(c.out.Options.ReportTokens) > 0 {
				c.Errorf(file.Header(), "reporting nodes via lexer rules is ignored in the presence of %%inject directives")
			}
			tokens = loader.mapping

		case len(tokens) > 0 && len(c.out.Options.ReportTokens) > 0:
			m := make(map[int]int)
			for _, t := range c.out.Options.ReportTokens {
				m[t] = 1
			}
			for _, t := range tokens {
				if m[t.Token] == 0 {
					c.Errorf(c.out.Syms[t.Token].Origin, "token %v is reported as %v but is not mentioned in the reportTokens clause", c.out.Syms[t.Token].Name, t)
				}
				m[t.Token] = 2
			}
			for _, t := range c.out.Options.ReportTokens {
				if m[t] != 2 {
					c.Errorf(file.Header(), "token %v is found in reportTokens but not in the lexer", c.out.Syms[t].Name)
				}
			}
		case len(tokens) == 0:
			for _, t := range c.out.Options.ReportTokens {
				name := ident.Produce(c.out.Syms[t].Name, ident.CamelCase)
				tokens = append(tokens, syntax.RangeToken{Token: t, Name: name})
			}
		}
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
		c.out.Lexer.MappedTokens = tokens

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
	if c.params.Compat {
		// Note: prefer original terminal names over IDs.
		source.Terminals = nil
		for _, sym := range c.out.Syms {
			source.Terminals = append(source.Terminals, sym.Name)
		}
	}
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
	addSyntheticInputs(source, c.params.Compat)

	// Prepare the model for code generation.
	c.resolver.addNonterms(source)
	c.out.Syms = c.resolver.Syms

	opts := genOptions{
		compat:        c.params.Compat,
		expectSR:      loader.expectSR,
		expectRR:      loader.expectRR,
		syms:          c.out.Syms,
		optimize:      c.out.Options.OptimizeTables && !c.params.CheckOnly,
		defaultReduce: c.out.Options.DefaultReduce,
	}
	if err := generateTables(source, c.out.Parser, opts, file); err != nil {
		c.AddError(err)
	}
}

type genOptions struct {
	compat   bool
	expectRR int
	expectSR int
	syms     []grammar.Symbol

	optimize      bool
	defaultReduce bool
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

			var innerReports int
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
					innerReports = len(report)
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

			if last := len(report) - 1; last >= 0 && report[last].Start == 0 && report[last].End == numRefs &&
				// Note: in compatibility note we don't promote -> from inside parentheses.
				(!opts.compat || len(report) > innerReports) {

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

	if opts.compat {
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

	lopts := lalr.Options{
		Optimize:      opts.optimize,
		DefaultReduce: opts.defaultReduce,
	}
	tables, err := lalr.Compile(g, lopts)
	if err != nil {
		return err
	}

	out.Rules = rules
	out.Tables = tables
	out.Inputs = source.Inputs
	out.Nonterms = source.Nonterms
	out.NumTerminals = len(source.Terminals)
	return nil
}

var tplMap = map[string]string{
	"go_lexer.stateVars":            "stateVars",
	"go_lexer.initStateVars":        "initStateVars",
	"go_lexer.onAfterNext":          "onAfterNext",
	"go_lexer.onBeforeNext":         "onBeforeNext",
	"go_lexer.onAfterLexer":         "onAfterLexer",
	"go_lexer.onBeforeLexer":        "onBeforeLexer",
	"go_parser.stateVars":           "parserVars",
	"go_parser.initStateVars":       "initParserVars",
	"go_parser.setupLookaheadLexer": "setupLookaheadLexer",
	"go_parser.onBeforeIgnore":      "onBeforeIgnore",
	"go_parser.onAfterParser":       "onAfterParser",
	"go_parser.customReportNext":    "customReportNext",
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
