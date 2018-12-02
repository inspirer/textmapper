package ast

import (
	"bytes"
	"fmt"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/tm"
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

func serialize(n *Node, b *bytes.Buffer) {
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

		tree, err := b.build()
		if err != nil {
			t.Fatalf("builder failed with %v", b.err)
		}

		var buf bytes.Buffer
		serialize(tree.Root().Child(selector.Any), &buf)
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
	tree, err := Parse("file1", testInput, tm.StopOnFirstError)
	if err != nil {
		t.Errorf("cannot parse %q: %v", testInput, err)
	}

	var buf bytes.Buffer
	file := File{tree.Root()}
	lexer, _ := file.Lexer()
	for _, lp := range lexer.LexerPart() {
		switch lp := lp.(type) {
		case *Lexeme:
			fmt.Fprintf(&buf, "token %v\n", lp.Name().Text())
		}
	}
	// TODO check "buf"
	t.Log(string(buf.Bytes()))
}
