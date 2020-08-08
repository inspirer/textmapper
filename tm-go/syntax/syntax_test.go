package syntax_test

import (
	"fmt"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/dump"
)

var simplifyTests = []struct {
	input *syntax.Expr
	want  *syntax.Expr
}{
	{
		input: &syntax.Expr{
			Kind: syntax.Sequence,
			Sub: []*syntax.Expr{
				{Kind: syntax.Sequence, Sub: []*syntax.Expr{{Kind: syntax.Empty}}},
				{Kind: syntax.Choice},
				{Kind: syntax.Empty},
			},
		},
		want: &syntax.Expr{Kind: syntax.Empty},
	},
}

func TestSimplify(t *testing.T) {
	for _, tc := range simplifyTests {
		got := syntax.Simplify(tc.input)
		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("Simplify(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}

func TestLexer(t *testing.T) {
	input := `A -> a B c(b .foo|C)* {as}; B -> Q<T="true"> %prec z; C -> set(B); %input C;`
	l := lexer{input: input}
	var got []string
	for l.next() != EOI {
		got = append(got, l.text())
	}
	if l.err != nil {
		t.Fatalf("lexer(%v) failed with %v", input, l.err)
	}

	want := []string{"A", "->", "a", "B", "c", "(", "b", ".", "foo", "|", "C", ")", "*", "{as}", ";",
		"B", "->", "Q", "<", "T", "=", `"true"`, ">", "%", "prec", "z", ";",
		"C", "->", "set", "(", "B", ")", ";",
		"%", "input", "C", ";"}
	if diff := dump.Diff(want, got); diff != "" {
		t.Errorf("next(%v) produced diff (-want +got):\n%s", input, diff)
	}

	// Test failures.
	l = lexer{input: `A - > g;`}
	for l.next() != EOI {
	}
	wantErr := fmt.Errorf("unexpected input: %s", "A ▶- > g;")
	if diff := dump.Diff(wantErr, l.err); diff != "" {
		t.Errorf("lexer.err(%v) produced diff (-want +got):\n%s", l.input, diff)
	}
}

type token uint16

const (
	EOI token = 0

	ID token = iota + 256
	CODE
	LITERAL
	PLUSEQ
	LOOKAHEAD
	ARROW
)

// lexer is a simplified lexer for the textual representation of syntax.Model, supporting
// a subset of the Textmapper language tokens.
type lexer struct {
	input      string
	tokenStart int
	offset     int
	err        error
}

func (l *lexer) text() string {
	return l.input[l.tokenStart:l.offset]
}

func (l *lexer) next() token {
restart:
	l.tokenStart = l.offset
	if l.offset == len(l.input) {
		return EOI
	}
	start := l.offset
	s := l.input[start:]
	ch := s[0]
	if ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' {
		for ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' {
			l.offset++
			if l.offset >= len(l.input) {
				break
			}
			ch = l.input[l.offset]
		}
		return ID
	}
	switch ch {
	case ' ', '\t', '\n', '\r':
		l.offset++
		goto restart
	case '-':
		if strings.HasPrefix(s, "->") {
			l.offset += 2
			return ARROW
		}
	case ';', '(', ')', '|', '&', '?', '*', '+', '<', '>', ',', '=', '$', '%', '.':
		if strings.HasPrefix(s, "+=") {
			l.offset += 2
			return PLUSEQ
		}
		if strings.HasPrefix(s, "(?=") {
			l.offset += 3
			return LOOKAHEAD
		}
		l.offset++
		return token(ch)
	case '{':
		if i := strings.IndexByte(s, '}'); i >= 0 {
			l.offset += i + 1
			return CODE
		}
	case '"':
		if i := strings.IndexByte(s[1:], '"'); i >= 0 {
			l.offset += i + 2
			return LITERAL
		}
	}

	// Unexpected input.
	l.err = fmt.Errorf("unexpected input: %v", l.input[:l.tokenStart]+"▶"+l.input[l.tokenStart:])
	l.offset = len(l.input)
	return EOI
}
