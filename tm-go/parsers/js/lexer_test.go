package js_test

import (
	"fmt"
	"regexp"
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/js"
)

type jsLexerTestCase struct {
	input    string
	expected []js.Token
}

var jsLexerTests = []jsLexerTestCase{
	{`var c = (function() {})() // comment
	`, []js.Token{
		js.VAR, js.IDENTIFIER, js.ASSIGN, js.LPAREN, js.FUNCTION, js.LPAREN, js.RPAREN,
		js.LBRACE, js.RBRACE, js.RPAREN, js.LPAREN, js.RPAREN,
	}},
	{`var c = /abc/ // comment
	`, []js.Token{
		js.VAR, js.IDENTIFIER, js.ASSIGN, js.REGULAREXPRESSIONLITERAL,
	}},
	{`var c = 1/2;`,
		[]js.Token{
			js.VAR, js.IDENTIFIER, js.ASSIGN, js.NUMERICLITERAL, js.DIV, js.NUMERICLITERAL, js.SEMICOLON,
		}},
	{`1/2;`, []js.Token{
		js.NUMERICLITERAL, js.DIV, js.NUMERICLITERAL, js.SEMICOLON,
	}},
	{`1 / /* comment */ /aa/.lastIndex`, []js.Token{
		js.NUMERICLITERAL, js.DIV, js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
	}},
	{`/aa/.lastIndex /* comment */ / 1`, []js.Token{
		js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER, js.DIV, js.NUMERICLITERAL,
	}},
	{`++/aa/`, []js.Token{
		js.PLUSPLUS, js.REGULAREXPRESSIONLITERAL,
	}},
	{`b--
	  / 3`, []js.Token{
		js.IDENTIFIER, js.MINUSMINUS, js.DIV, js.NUMERICLITERAL,
	}},
	{`(--
	  /aa/.lastIndex)`, []js.Token{
		js.LPAREN, js.MINUSMINUS, js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER, js.RPAREN,
	}},
	{`b
	  --/aaa/.lastIndex`, []js.Token{
		js.IDENTIFIER, js.MINUSMINUS, js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
	}},
	{`b /= --/aaa/.lastIndex`, []js.Token{
		js.IDENTIFIER, js.DIVASSIGN, js.MINUSMINUS, js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
	}},
	{`(a) == /aaa/.lastIndex`, []js.Token{
		js.LPAREN, js.IDENTIFIER, js.RPAREN, js.ASSIGNASSIGN,
		js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
	}},
	{`(a) / /aaa/.lastIndex`, []js.Token{
		js.LPAREN, js.IDENTIFIER, js.RPAREN, js.DIV,
		js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
	}},
	{`[a] / /aaa/.lastIndex`, []js.Token{
		js.LBRACK, js.IDENTIFIER, js.RBRACK, js.DIV,
		js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
	}},
	{"  `aa q`  ", []js.Token{
		js.NOSUBSTITUTIONTEMPLATE,
	}},
	{"`aa ${ /a/.lastIndex / 1 }q`", []js.Token{
		js.TEMPLATEHEAD, js.REGULAREXPRESSIONLITERAL, js.DOT, js.IDENTIFIER,
		js.DIV, js.NUMERICLITERAL, js.TEMPLATETAIL,
	}},
	{"`aa ${ 'aa' }q${ /aaa/ } `", []js.Token{
		js.TEMPLATEHEAD, js.STRINGLITERAL, js.TEMPLATEMIDDLE,
		js.REGULAREXPRESSIONLITERAL, js.TEMPLATETAIL,
	}},
	{"print`aa ${ 'aa' }q${ /aaa/ } `", []js.Token{
		js.IDENTIFIER, js.TEMPLATEHEAD, js.STRINGLITERAL, js.TEMPLATEMIDDLE,
		js.REGULAREXPRESSIONLITERAL, js.TEMPLATETAIL,
	}},
	{`typeof /aaa/`, []js.Token{
		js.TYPEOF, js.REGULAREXPRESSIONLITERAL,
	}},
	{`let /`, []js.Token{
		js.LET, js.DIV,
	}},
	{"`a` /", []js.Token{
		js.NOSUBSTITUTIONTEMPLATE, js.DIV,
	}},
	{"`${a}` /", []js.Token{
		js.TEMPLATEHEAD, js.IDENTIFIER, js.TEMPLATETAIL, js.DIV,
	}},
	// TODO if (a) /aaa/.compile()
}

func TestLexer(t *testing.T) {
	spacesRE := regexp.MustCompile(`^[\s\n]*((\/\* comment \*\/|\/\/ comment\n)[\s\n]*)?$`)
	l := new(js.Lexer)

	for _, test := range jsLexerTests {
		input := []byte(test.input)
		onError := func(line, offset, len int, msg string) {
			t.Errorf("%d, %d: %s", line, offset, msg)
		}

		l.Init(input, onError)

		next := l.Next()
		offset := 0
		index := 0
		for {
			s, e := l.Pos()
			if !spacesRE.Match(input[offset:s]) {
				t.Errorf("Spaces expected: `%s`", input[offset:s])
			}
			if next == js.EOI {
				break
			}
			offset = e
			token := string(input[s:e])
			if index >= len(test.expected) {
				t.Errorf("token %s `%s` is not expected in `%s`", next.String(), token, input)
				break
			}
			if test.expected[index] != next {
				t.Errorf("got %s `%s`, want `%s` in `%s`", next.String(), token, test.expected[index], input)
				break
			}
			index++
			next = l.Next()
		}

		if index < len(test.expected) {
			t.Errorf("got <none>, want `%s` in `%s`", test.expected[index], input)
		}
	}
}

const jsBenchmarkCode = `
  const a = 15;
  if (" abcd ".length = 20) /aaaa/.test('vvaaaaaaaa');
  var e = "some very long string" + [123,  9000000].length
  for (; b < a; b++) { }
  var c = (function() {})()

  ({ a:  function*() { yeild 1; }, b : "aaaaaaaa"}).a();

  class A extends B {
      constructor() { this.x = 1; }
      f() { return this.x; }
  }
`

func BenchmarkLexer(b *testing.B) {
	l := new(js.Lexer)
	for i := 0; i < b.N; i++ {
		l.Init([]byte(jsBenchmarkCode), func(line, offset, len int, msg string) {
			panic(fmt.Sprintf("%d, %d: %s", line, offset, msg))
		})
		next := l.Next()
		for next != js.EOI {
			next = l.Next()
		}
	}
	b.SetBytes(int64(len(jsBenchmarkCode)))
}
