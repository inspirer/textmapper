package compiler

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/diff"
)

var testFiles = []string{
	"lexer.tmerr",
	"opts.tmerr",
	"opts_ok.tmerr",
	"parser.tmerr",
	"parser_confl.tmerr",
	"noinput.tmerr",
	"inline_input.tmerr",
	"templ_input.tmerr",
	"badinput.tmerr",
	"backtrack.tmerr",
	"set.tmerr",
	"set2.tmerr",
	"conflict1.tmerr",
	"lr0.tmerr",
	"greedy.tmerr",
	"inject.tmerr",
	"flexmode.tmerr",
	"max_la.tmerr",
	"disabled_syntax.tmerr",
	"expansion_limit.tmerr",
}

func TestErrors(t *testing.T) {
	ctx := context.Background()
	for _, file := range testFiles {
		content, err := os.ReadFile(filepath.Join("testdata", file))
		if err != nil {
			t.Errorf("cannot read %v: %v", file, err)
			continue
		}

		inp := string(content)
		pt := parsertest.New(t, file, inp)

		var want []string
		for _, line := range strings.Split(inp, "\n") {
			const prefix = "# err: "
			switch {
			case strings.HasPrefix(line, prefix):
				want = append(want, line[len(prefix):])
			case line == "# err:":
				want = append(want, "")
			}
		}

		var got []string
		_, err = Compile(ctx, file, pt.Source(), Params{})
		if err != nil {
			s := status.FromError(err)
			s.Sort()
			for _, e := range s {
				pt.Consume(t, e.Origin.Offset, e.Origin.EndOffset)
				got = append(got, e.Msg)
			}
		}
		pt.Done(t, nil)
		if diff := diff.LineDiff(strings.Join(want, "\n"), strings.Join(got, "\n")); diff != "" {
			t.Errorf("%v: errors diff\n--- want\n+++ got\n%v\n", file, diff)
			break
		}
	}
}

var modelFiles = []string{
	"model1.tm",
}

func TestSourceModel(t *testing.T) {
	ctx := context.Background()
	for _, file := range modelFiles {
		content, err := os.ReadFile(filepath.Join("testdata", file))
		if err != nil {
			t.Errorf("cannot read %v: %v", file, err)
			continue
		}

		tree, err := ast.Parse(ctx, file, string(content), tm.StopOnFirstError)
		if err != nil {
			t.Errorf("%v: parsing failed with %v", file, err)
			continue
		}
		file := ast.File{Node: tree.Root()}

		var s status.Status

		opts := newOptionsParser(&s)
		opts.parseFrom(file)

		resolver := newResolver(&s)

		lexer := newLexerCompiler(opts.out, resolver, &s)
		lexer.compile(file)

		c := newCompiler(file, opts.out, lexer.out, resolver, Params{}, &s)
		c.compileParser(file)

		if s.Err() != nil {
			t.Errorf("compilation failure %v", s.Err())
			continue
		}

		want := strings.TrimPrefix(tree.Root().Child(selector.Templates).Text(), "%%")
		var b strings.Builder
		b.WriteString("\n\n")
		for _, nt := range c.out.Parser.Nonterms {
			writeNonterm(nt, &b)
		}
		got := b.String()

		if diff := diff.LineDiff(want, got); diff != "" {
			t.Errorf("The in-file syntax model differs from the compiled one.\n--- %v\n+++ %v (compiled)\n%v", file, file, diff)
		}
	}
}

func writeNonterm(nt *syntax.Nonterm, b *strings.Builder) {
	b.WriteString(nt.Name)
	if len(nt.Params) > 0 {
		fmt.Fprintf(b, "<%v unsubstituted parameters>", len(nt.Params))
	}
	b.WriteString(" :\n")
	if nt.Value.Kind == syntax.Choice {
		for i, sub := range nt.Value.Sub {
			if i > 0 {
				fmt.Fprintf(b, "| %v\n", sub)
			} else {
				fmt.Fprintf(b, "  %v\n", sub)
			}
		}
		b.WriteString(";\n\n")
	} else {
		fmt.Fprintf(b, "  %v ;\n\n", nt.Value)
	}
}

var debugFiles = []string{
	"debug.tm",
}

func TestDebugInfo(t *testing.T) {
	ctx := context.Background()
	for _, file := range debugFiles {
		content, err := os.ReadFile(filepath.Join("testdata", file))
		if err != nil {
			t.Errorf("cannot read %v: %v", file, err)
			continue
		}

		tree, err := ast.Parse(ctx, file, string(content), tm.StopOnFirstError)
		if err != nil {
			t.Errorf("%v: parsing failed with %v", file, err)
			continue
		}

		g, err := Compile(ctx, file, string(content), Params{DebugTables: true})
		if err != nil {
			t.Errorf("%v: compilation failed with %v", file, err)
			continue
		}

		want := strings.TrimPrefix(tree.Root().Child(selector.Templates).Text(), "%%")
		var b strings.Builder
		b.WriteString("\n\n")
		for _, info := range g.Parser.Tables.DebugInfo {
			b.WriteString(info)
			b.WriteByte('\n')
		}
		got := b.String()

		if diff := diff.LineDiff(want, got); diff != "" {
			t.Errorf("The in-file debug info does not match the produced one.\n--- %v\n+++ %v (produced)\n%v", file, file, diff)
			t.Logf("Run (cd compiler/testdata; go run ../../cmd/textmapper/*.go debug --tables %v >> %v) to regenerate.", file, file)
		}
	}
}

func TestArgRef(t *testing.T) {
	header := `
		language medium(cc);

		namespace = "medium"

		:: lexer

		KW_A: /a/
		KW_B: /b/
		KW_C: /c/
		KW_D: /d/
		',': /,/

		:: parser

		%input Z;

		a {std::string}: KW_A;
		b {int}: KW_B;
		c {int*}: KW_C;
		d {double}: KW_D;
	`

	testCases := []struct {
		input string
		// If not provided, default to the start symbol "Z".
		symbol    string
		want      [][]string
		wantMulti [][][]string
	}{
		// Section: Optional.
		{
			input: "Z: a? {};",
			want:  [][]string{{`$1[a]?`}},
		},
		{
			input: `Z: c b?;`,
			// No arg refs are collected because there are no semantic actions.
			want: [][]string{},
		},
		{
			input: `Z: (a b)? {};`,
			want:  [][]string{{`$1[a]?`, `$2[b]?`}},
		},
		// Mid rule.
		{
			input: `Z: b? {} c;`,
			// Only b is collected because c is after the mid rule.
			want: [][]string{{`$1[b]?`}},
		},
		// Mid rule and semantic action.
		{
			input: `Z: b? {} c {};`,
			// For the mid rule, only b is collected; for the semantic action, both b and c are collected.
			want: [][]string{{`$1[b]?`}, {`$1[b]?`, `$2[c]`}},
		},
		// Duplicate symbol names.
		{
			input: `Z: (a a)? {};`,
			want:  [][]string{{`$1[a]?`, `$2[a]?`}},
		},
		// Duplicate symbol names.
		{
			input: `Z: a? a {};`,
			want:  [][]string{{`$1[a]?`, `$2[a]`}},
		},
		// Optional terminal.
		{
			input: `Z: KW_A? {};`,
			want:  [][]string{{`$1[KW_A]?`}},
		},
		// With state marker.
		{
			input: `Z: a? .my_state b {};`,
			want:  [][]string{{`$1[a]?`, `$2[b]`}},
		},
		// Section: List
		{
			input: `Z: a+ {};`,
			want:  [][]string{{`$1[a_list]`}},
		},
		{
			input: `Z: a* {};`,
			want:  [][]string{{`$1[a_optlist]`}},
		},
		// List with separator.
		{
			input: `Z: (a separator ',')+ {};`,
			want:  [][]string{{`$1[a_list_Comma_separated]`}},
		},
		// List with separator.
		{
			input: `Z: (a separator ',')* {};`,
			want:  [][]string{{`$1[a_list_Comma_separatedopt]`}},
		},
		// List of terminals.
		{
			input: `Z: KW_A+ {};`,
			want:  [][]string{{`$1[KWA_list]`}},
		},
		// Semantic action inside list.
		{
			input:  `Z: ( a {} )+ {};`,
			symbol: "a_list",
			want:   [][]string{{`$1[a]`}},
		},
		// Section: Alternating group.
		{
			input: `Z: (a | b) {};`,
			want:  [][]string{{`$1[a]?`, `$2[b]?`}},
		},
		{
			// Commands in alternating groups.
			// cmd1 only has access to b, and cmd2 only has access to c. cmd has access to all a, b, and
			// c.
			input: `Z: a ( b {cmd1} | c {cmd2} ) {cmd3};`,
			wantMulti: [][][]string{
				// cmd1
				{{`$2[b]?`}, {`$1[a]`, `$2[b]?`, `$3[c]?`}},
				// cmd2
				{{`$3[c]?`}, {`$1[a]`, `$2[b]?`, `$3[c]?`}},
			},
		},
		// Terminals in alternating group.
		{
			input: `Z: (KW_A | KW_B) {};`,
			want:  [][]string{{`$1[KW_A]?`, `$2[KW_B]?`}},
		},
		// Section: Nested syntax extensions.
		{
			input: `Z: (a? | b) {};`,
			want:  [][]string{{`$1[a]?`, `$2[b]?`}},
		},
		{
			input: `Z: (a+ | b*) {};`,
			want:  [][]string{{`$1[a_list]?`, `$2[b_optlist]?`}},
		},
		{
			// Nested choice inside a list
			input: `Z: ( a {cmd1} | b {cmd2} )+ {cmd3};`,
			want:  [][]string{{`$1[Z$1]`}},
		},
		{
			// List inside nested choice
			input: `Z: (a+ {cmd1} | (b{cmd2})* ) {};`,
			wantMulti: [][][]string{
				// (a+ {cmd1}) {};
				{{`$1[a_list]?`}, {`$1[a_list]?`, `$2[b_optlist]?`}},
				// b{cmd2}* {};
				{{`$1[a_list]?`, `$2[b_optlist]?`}},
			},
		},
		{
			input: `Z: (a? b)+ {};`,
			want:  [][]string{{`$1[Z$1]`}},
		},
		{
			input: `Z: (a | b)* {};`,
			want:  [][]string{{`$1[Z$1]`}},
		},
		// Section: Set.
		{
			input: `Z: set(KW_A | KW_B) {};`,
			want:  [][]string{{`$1[setof_KW_A_or_KW_B]`}},
		},
	}

	for _, tc := range testCases {
		input := header + tc.input
		parsed, err := parseToGrammar(input)

		if err != nil {
			t.Fatalf("cannot parse %q: %v", input, err)
		}

		nts := parsed.Parser.Nonterms

		var sym string
		if tc.symbol != "" {
			sym = tc.symbol
		} else {
			sym = "Z"
		}
		nt := getNt(nts, sym)
		if nt == nil {
			t.Fatalf("cannot find the start symbol Z")
		}
		rules := []*syntax.Expr{nt.Value}
		if nt.Value.Kind == syntax.Choice {
			rules = nt.Value.Sub
		}

		for i, rule := range rules {
			got := gotArgRefs(rule, parsed)
			var want string
			if tc.wantMulti != nil {
				want = fmt.Sprintf("%+v", tc.wantMulti[i])
			} else {
				want = fmt.Sprintf("%+v", tc.want)
			}
			if got != want {
				t.Errorf("got %v, want %v for input %q", got, want, tc.input)
			}
		}
	}
}

// A convenience function to parse a grammar string and return the corresponding model.
func parseToGrammar(content string) (*grammar.Grammar, error) {
	ctx := context.Background()
	filename := "test.tm"
	_, err := ast.Parse(ctx, filename, content, tm.StopOnFirstError)
	if err != nil {
		return nil, fmt.Errorf("%v: parsing failed with %v", filename, err)
	}

	return Compile(ctx, filename, content, Params{DebugTables: true})
}

func getNt(nts []*syntax.Nonterm, name string) *syntax.Nonterm {
	for _, nt := range nts {
		if nt.Name == name {
			return nt
		}
	}
	return nil
}

func serializeArgRef(ref syntax.ArgRef, grammar *grammar.Grammar) string {
	ret := fmt.Sprintf("$%v[%v]", ref.Pos, grammar.Syms[ref.Symbol].Name)
	if ref.Optional {
		ret += "?"
	}
	return ret
}

func serializeArgRefs(refs map[int]syntax.ArgRef, grammar *grammar.Grammar) string {
	var keys []int
	for k := range refs {
		keys = append(keys, k)
	}
	sort.Ints(keys)

	var ret []string
	for _, pos := range keys {
		ret = append(ret, serializeArgRef(refs[pos], grammar))
	}
	return "[" + strings.Join(ret, " ") + "]"
}

func gotArgRefs(e *syntax.Expr, grammar *grammar.Grammar) string {
	var collected []map[int]syntax.ArgRef
	e.ForEach(syntax.Command, func(e *syntax.Expr) {
		if e.CmdArgs != nil && e.CmdArgs.ArgRefs != nil {
			collected = append(collected, e.CmdArgs.ArgRefs)
		}
	})

	var ret []string
	for _, argRefs := range collected {
		ret = append(ret, serializeArgRefs(argRefs, grammar))
	}
	return "[" + strings.Join(ret, " ") + "]"
}

func TestMinimizeDFA(t *testing.T) {
	// Helper to extract symbol names
	symbolNames := func(syms []grammar.Symbol) []string {
		var names []string
		for _, s := range syms {
			names = append(names, s.Name)
		}
		return names
	}

	printTransitions := func(name string, tables *lalr.Tables, symbols []string) string {
		var b strings.Builder
		fmt.Fprintf(&b, "Transitions for %s (States: %v):\n", name, tables.NumStates)
		for i := 0; i < len(tables.Goto)-1; i++ {
			min := tables.Goto[i]
			max := tables.Goto[i+1]
			symbolName := "UNKNOWN"
			if i < len(symbols) {
				symbolName = symbols[i]
			}
			for i := min; i < max; i += 2 {
				from := tables.FromTo[i]
				to := tables.FromTo[i+1]
				fmt.Fprintf(&b, "  State %d -> State %d on symbol '%s'\n", from, to, symbolName)
			}
		}
		return b.String()
	}

	testCases := []struct {
		name    string
		grammar string
		wantOff string
		wantOn  string
	}{
		{
			name: "grammarSet",
			grammar: `
language test_set(go);
%v
:: lexer

a: /a/
b: /b/
c: /c/
d: /d/

:: parser

input: a (set(~(d | eoi | invalid_token)))* d;
`,
			wantOff: `Transitions for grammarSet (minimize OFF) (States: 10):
  State 8 -> State 9 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 2 -> State 3 on symbol 'a'
  State 2 -> State 4 on symbol 'b'
  State 2 -> State 5 on symbol 'c'
  State 2 -> State 6 on symbol 'd'
  State 0 -> State 8 on symbol 'input'
  State 2 -> State 7 on symbol 'setof_not_D_or_EOI_or_INVALID_TOKEN'
  State 1 -> State 2 on symbol 'setof_not_D_or_EOI_or_INVALID_TOKEN_optlist'
`,
			wantOn: `Transitions for grammarSet (minimize ON) (States: 8):
  State 6 -> State 7 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 2 -> State 3 on symbol 'a'
  State 2 -> State 3 on symbol 'b'
  State 2 -> State 3 on symbol 'c'
  State 2 -> State 4 on symbol 'd'
  State 0 -> State 6 on symbol 'input'
  State 2 -> State 5 on symbol 'setof_not_D_or_EOI_or_INVALID_TOKEN'
  State 1 -> State 2 on symbol 'setof_not_D_or_EOI_or_INVALID_TOKEN_optlist'
`,
		},
		{
			name: "grammarChoice",
			grammar: `
language test_choice(go);
%v
:: lexer

a: /a/
b: /b/
c: /c/
d: /d/

:: parser

input: a c d | b c d;
`,
			wantOff: `Transitions for grammarChoice (minimize OFF) (States: 9):
  State 7 -> State 8 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 0 -> State 2 on symbol 'b'
  State 1 -> State 3 on symbol 'c'
  State 2 -> State 4 on symbol 'c'
  State 3 -> State 5 on symbol 'd'
  State 4 -> State 6 on symbol 'd'
  State 0 -> State 7 on symbol 'input'
`,
			wantOn: `Transitions for grammarChoice (minimize ON) (States: 6):
  State 4 -> State 5 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 0 -> State 1 on symbol 'b'
  State 1 -> State 2 on symbol 'c'
  State 2 -> State 3 on symbol 'd'
  State 0 -> State 4 on symbol 'input'
`,
		},
		{
			name: "lookaheadReduce",
			grammar: `
language lookahead_reduce(go);
%v
:: lexer
a: /a/
b: /b/
c: /c/
d: /d/
x: /x/
y: /y/

:: parser
input: a R1 c | a R2 d ;
R1: x y ;
R2: x y ;`,
			wantOff: `Transitions for lookaheadReduce (minimize OFF) (States: 10):
  State 8 -> State 9 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 3 -> State 6 on symbol 'c'
  State 4 -> State 7 on symbol 'd'
  State 1 -> State 2 on symbol 'x'
  State 2 -> State 5 on symbol 'y'
  State 0 -> State 8 on symbol 'input'
  State 1 -> State 3 on symbol 'R1'
  State 1 -> State 4 on symbol 'R2'
`,
			wantOn: `Transitions for lookaheadReduce (minimize ON) (States: 9):
  State 7 -> State 8 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 3 -> State 6 on symbol 'c'
  State 4 -> State 6 on symbol 'd'
  State 1 -> State 2 on symbol 'x'
  State 2 -> State 5 on symbol 'y'
  State 0 -> State 7 on symbol 'input'
  State 1 -> State 3 on symbol 'R1'
  State 1 -> State 4 on symbol 'R2'
`,
		},
		{
			name: "emptyGrammar",
			grammar: `
language test_empty(go);
%v
:: lexer
a: /a/

:: parser
input: ;
`,
			wantOff: `Transitions for emptyGrammar (minimize OFF) (States: 3):
  State 1 -> State 2 on symbol 'eoi'
  State 0 -> State 1 on symbol 'input'
`,
			wantOn: `Transitions for emptyGrammar (minimize ON) (States: 3):
  State 1 -> State 2 on symbol 'eoi'
  State 0 -> State 1 on symbol 'input'
`,
		},
		{
			name: "diffType",
			grammar: `
language diff_type(go);
eventBased = true
%v
:: lexer
a: /a/
b: /b/
:: parser
input: a -> Type1 | b -> Type2 ;
Type1: ;
Type2: ;
`,
			wantOff: `Transitions for diffType (minimize OFF) (States: 5):
  State 3 -> State 4 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 0 -> State 2 on symbol 'b'
  State 0 -> State 3 on symbol 'input'
`,
			wantOn: `Transitions for diffType (minimize ON) (States: 5):
  State 3 -> State 4 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 0 -> State 2 on symbol 'b'
  State 0 -> State 3 on symbol 'input'
`,
		},
		{
			name: "diffFlags",
			grammar: `
language diff_flags(go);
eventBased = true
%v
:: lexer
a: /a/
b: /b/
:: parser
input: a -> input/F1 | b -> input/F2 ;
`,
			wantOff: `Transitions for diffFlags (minimize OFF) (States: 5):
  State 3 -> State 4 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 0 -> State 2 on symbol 'b'
  State 0 -> State 3 on symbol 'input'
`,
			wantOn: `Transitions for diffFlags (minimize ON) (States: 5):
  State 3 -> State 4 on symbol 'eoi'
  State 0 -> State 1 on symbol 'a'
  State 0 -> State 2 on symbol 'b'
  State 0 -> State 3 on symbol 'input'
`,
		},
		{
			name: "lookaheadCollision",
			grammar: `
language lookahead_collision(go);
%v
maxLookahead = 2

:: lexer
a: /a/
b: /b/

:: parser
%%expect-rr 2;

input : a R1 | b R2 ;
R1 : (?= S1) | ;
R2 : (?= S2) | ;
S1 : a ;
S2 : a ;
`,
			wantOff: `Transitions for lookaheadCollision (minimize OFF) (States: 15):
  State 11 -> State 14 on symbol 'eoi'
  State 0 -> State 3 on symbol 'a'
  State 1 -> State 5 on symbol 'a'
  State 2 -> State 6 on symbol 'a'
  State 0 -> State 4 on symbol 'b'
  State 0 -> State 11 on symbol 'input'
  State 3 -> State 7 on symbol 'R1'
  State 3 -> State 8 on symbol 'lookahead_S1'
  State 4 -> State 9 on symbol 'R2'
  State 4 -> State 10 on symbol 'lookahead_S2'
  State 1 -> State 12 on symbol 'S1'
  State 2 -> State 13 on symbol 'S2'
`,
			wantOn: `Transitions for lookaheadCollision (minimize ON) (States: 12):
  State 10 -> State 11 on symbol 'eoi'
  State 0 -> State 3 on symbol 'a'
  State 1 -> State 5 on symbol 'a'
  State 2 -> State 6 on symbol 'a'
  State 0 -> State 4 on symbol 'b'
  State 0 -> State 10 on symbol 'input'
  State 3 -> State 7 on symbol 'R1'
  State 3 -> State 8 on symbol 'lookahead_S1'
  State 4 -> State 7 on symbol 'R2'
  State 4 -> State 9 on symbol 'lookahead_S2'
  State 1 -> State 11 on symbol 'S1'
  State 2 -> State 11 on symbol 'S2'
`,
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			compOn, err := Compile(t.Context(), fmt.Sprintf("%s.tm", tc.name), fmt.Sprintf(tc.grammar, "minimizeDFA = true"), Params{})
			if err != nil {
				t.Fatalf("Compile error with DFA minimization on: %v", err)
			}

			parserOn := compOn.Parser
			if parserOn == nil {
				t.Fatalf("Parser is nil")
			}

			compOff, err := Compile(t.Context(), fmt.Sprintf("%s.tm", tc.name), fmt.Sprintf(tc.grammar, "minimizeDFA = false"), Params{})
			if err != nil {
				t.Fatalf("Compile error with DFA minimization off: %v", err)
			}

			parserOff := compOff.Parser
			if parserOff == nil {
				t.Fatalf("Parser is nil")
			}

			var nameOff, nameOn string
			nameOff = tc.name + " (minimize OFF)"
			nameOn = tc.name + " (minimize ON)"

			offOutput := printTransitions(nameOff, parserOff.Tables, symbolNames(compOff.Syms))
			onOutput := printTransitions(nameOn, parserOn.Tables, symbolNames(compOn.Syms))

			if offOutput != tc.wantOff {
				t.Errorf("DFA minimization OFF mismatch:\n--- want\n+++ got\n%v\n%v", tc.wantOff, offOutput)
			}

			if onOutput != tc.wantOn {
				t.Errorf("DFA minimization ON mismatch:\n--- want\n+++ got\n%v\n%v", tc.wantOn, onOutput)
			}
		})
	}
}