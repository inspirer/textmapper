package ast_test

import (
	"bytes"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"github.com/inspirer/textmapper/tm-parsers/tm/selector"
)

const (
	testType = tm.Header
)

var builderTests = []struct {
	want   string
	ranges []string
}{
	{"(((1)(2))(3))", []string{
		"1", "2", "12", "3", "123",
	}},
	{"(((1)(2))((3))((4)(5)))", []string{
		"1", "2", "12", "3", "3", "4", "5", "45", "12345",
	}},
	{"(((1)(2))((3))((4)(5)))", []string{
		"1", "2", "12", "4", "5", "3", "3", "45", "12345",
	}},
}

func serialize(n ast.Node, b *bytes.Buffer) {
	b.Write([]byte("("))
	defer b.Write([]byte(")"))

	children := n.Children(selector.Any)
	text := n.Text()
	offset := n.Offset()
	for _, c := range children {
		b.Write([]byte(text[offset-n.Offset() : c.Offset()-n.Offset()]))
		offset = c.Endoffset()
		serialize(c, b)
	}
	b.Write([]byte(text[offset-n.Offset():]))
}

func TestNodeBuilder(t *testing.T) {
	for _, tc := range builderTests {
		source := tc.ranges[len(tc.ranges)-1]
		b := ast.NewBuilder(source)
		for _, r := range tc.ranges {
			i := strings.Index(source, r)
			if i == -1 {
				t.Fatalf("%v not found in %q", r, source)
			}
			b.Add(testType, i, i+len(r))
		}
		n, err := b.Root()
		if err != nil {
			t.Fatalf("builder.Root() returned %v", err)
		}

		var buf bytes.Buffer
		serialize(n, &buf)
		got := buf.String()
		if got != tc.want {
			t.Errorf("builder returned %v, want: %v", got, tc.want)
		}
	}
}
