package parser

import (
	"bytes"
	"fmt"
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

func TestBuilder(t *testing.T) {
	for _, tc := range builderTests {
		source := tc.ranges[len(tc.ranges)-1]
		b := newBuilder("test", source)
		for _, r := range tc.ranges {
			i := strings.Index(source, r)
			if i == -1 {
				t.Fatalf("%v not found in %q", r, source)
			}
			b.addNode(testType, i, i+len(r))
		}

		if err := b.status.Err(); err != nil {
			t.Fatalf("builder failed with %v", err)
		}
		b.file.parsed = b.chunks

		var buf bytes.Buffer
		serialize(b.file.root(), &buf)
		got := buf.String()
		if got != tc.want {
			t.Errorf("builder returned %v, want: %v", got, tc.want)
		}
	}
}

const testInput = `
language abc(go);
lang = "abc"

:: lexer
eoi:        /%%.*(\r?\n)?/
whitespace: /[\n\r\t ]+/                      (space)

qqq = /q1/

'q': /{qqq}/

:: parser

%input a;

a : 'q'+ ;
`

func TestParser(t *testing.T) {
	file, err := Parse("file1", testInput)
	if err != nil {
		t.Errorf("cannot parse %q: %v", testInput, err)
	}

	var buf bytes.Buffer
	for _, lp := range file.Lexer().LexerPart() {
		switch lp := lp.(type) {
		case *ast.Lexeme:
			fmt.Fprintf(&buf, "token %v\n", lp.Name().Text())
		}
	}
	// TODO check "buf"
	t.Log(string(buf.Bytes()))
}
