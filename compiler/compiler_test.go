package compiler

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/grammar/grammar"
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
			want: [][]string{{`$1[KWA_list]`}},
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
	var collect func(e *syntax.Expr)
	collected := make([]map[int]syntax.ArgRef, 0)
	collect = func(e *syntax.Expr) {
		if e.CmdArgs != nil && e.CmdArgs.ArgRefs != nil {
			collected = append(collected, e.CmdArgs.ArgRefs)
		}
	}

	e.ForEach(-1, collect)
	var ret []string
	for _, argRefs := range collected {
		ret = append(ret, serializeArgRefs(argRefs, grammar))
	}
	return "[" + strings.Join(ret, " ") + "]"
}
