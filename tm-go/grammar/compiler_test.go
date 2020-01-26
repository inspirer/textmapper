package grammar

import (
	"fmt"
	"io/ioutil"
	"path/filepath"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/diff"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"github.com/inspirer/textmapper/tm-parsers/tm/selector"
)

var testFiles = []string{
	"lexer.tmerr",
	"opts.tmerr",
	"opts_ok.tmerr",
	"parser.tmerr",
	"noinput.tmerr",
	"badinput.tmerr",
}

func TestErrors(t *testing.T) {
	for _, file := range testFiles {
		content, err := ioutil.ReadFile(filepath.Join("testdata", file))
		if err != nil {
			t.Errorf("cannot read %v: %v", file, err)
			continue
		}

		for _, compat := range []bool{true, false} {
			inp := string(content)
			pt := parsertest.New(t, fmt.Sprintf("error,compat=%v", compat), inp)
			tree, err := ast.Parse(file, pt.Source(), tm.StopOnFirstError)
			if err != nil {
				t.Errorf("parsing failed with %v\n%v", err, inp)
				continue
			}

			var want []string
			for _, line := range strings.Split(inp, "\n") {
				const prefix = "# err: "
				if strings.HasPrefix(line, prefix) {
					want = append(want, line[len(prefix):])
				}
			}

			_, err = Compile(ast.File{Node: tree.Root()}, compat)
			if err != nil {
				s := status.FromError(err)
				s.Sort()
				for _, e := range s {
					pt.Consume(t, e.Origin.Offset, e.Origin.EndOffset)
					if len(want) == 0 {
						t.Errorf("%v (compat=%v): unexpected error at line %v: %v", file, compat, e.Origin.Line, e.Msg)
						continue
					}
					if want[0] != e.Msg {
						t.Errorf("%v (compat=%v): unexpected error at line %v: %v, want: %v", file, compat, e.Origin.Line, e.Msg, want[0])
					}
					want = want[1:]
				}
			}
			if len(want) != 0 {
				t.Errorf("%v (compat=%v): not reported errors:\n%v", file, compat, want)
			}
			pt.Done(t, nil)
		}
	}
}

var modelFiles = []string{
	"model1.tm",
}

func TestSourceModel(t *testing.T) {
	for _, file := range modelFiles {
		content, err := ioutil.ReadFile(filepath.Join("testdata", file))
		if err != nil {
			t.Errorf("cannot read %v: %v", file, err)
			continue
		}

		tree, err := ast.Parse(file, string(content), tm.StopOnFirstError)
		if err != nil {
			t.Errorf("%v: parsing failed with %v", file, err)
			continue
		}

		c := newCompiler(ast.File{Node: tree.Root()}, false /*compat*/)
		c.compileLexer()
		c.compileParser()
		if c.s.Err() != nil {
			t.Errorf("compilation failure %v", c.s.Err())
			continue
		}

		want := strings.TrimPrefix(tree.Root().Child(selector.Templates).Text(), "%%")
		var b strings.Builder
		b.WriteString("\n\n")
		for _, nt := range c.source.Nonterms {
			writeNonterm(nt, c.source, &b)
		}
		got := b.String()

		if diff := diff.LineDiff(want, got); diff != "" {
			t.Errorf("The in-file syntax model differs from the compiled one.\n--- %v\n+++ %v (compiled)\n%v", file, file, diff)
		}
	}
}

func writeNonterm(nt syntax.Nonterm, m *syntax.Model, b *strings.Builder) {
	b.WriteString(nt.Name)
	if len(nt.Params) > 0 {
		b.WriteByte('<')
		for i, p := range nt.Params {
			if i > 0 {
				b.WriteString(", ")
			}
			b.WriteString(m.Params[p].Name)
		}
		b.WriteByte('>')
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
