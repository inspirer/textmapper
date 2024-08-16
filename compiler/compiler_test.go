package compiler

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"testing"

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
