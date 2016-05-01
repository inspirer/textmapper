package js_test

import (
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/js"
)

const jsExample = `
  const a = 15;
  var b = 7
  var e = "aa"
  for (; b < a; b++) { }
  var c = (function() {})()
`

func PanicOnError(line, offset, len int, msg string) {
	panic(fmt.Sprintf("%d, %d: %s", line, offset, msg))
}

func testLexer(input []byte, t *testing.T) {
	l := new(js.Lexer)
	l.Init(input, PanicOnError)
	spacesRE := regexp.MustCompile(`^\s+$`)

	next := l.Next()
	offset := 0
	for next != js.EOI {
		s, e := l.Pos()
		if s > offset && !spacesRE.Match(input[offset:s]) {
			t.Errorf("Spaces expected: %s", input[offset:s])
		}
		offset = e
		token := string(input[s:e])
		switch next {
		case js.LBRACE, js.RBRACE, js.LBRACK, js.RBRACK,
			js.COLON, js.COMMA, js.NULL, js.TRUE, js.FALSE:
			if token != next.String() {
				t.Errorf("Bad token %v: %s", next, token)
			}
		case js.STRINGLITERAL:
			if !strings.HasPrefix(token, `"`) || !strings.HasSuffix(token, `"`) {
				t.Errorf("Bad string literal: %s", token)
			}
		}
		next = l.Next()
	}
}

func TestLexerExample(t *testing.T) {
	testLexer([]byte(jsExample), t)
}

func BenchmarkLexer(b *testing.B) {
	l := new(js.Lexer)
	for i := 0; i < b.N; i++ {
		l.Init([]byte(jsExample), PanicOnError)
		next := l.Next()
		for next != js.EOI {
			next = l.Next()
		}
	}
	b.SetBytes(int64(len(jsExample)))
}
