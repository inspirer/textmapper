package simple_test

import (
	"testing"

	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/simple"
	"github.com/inspirer/textmapper/parsers/simple/token"
)

var lexerTests = []struct {
	tok    token.Type
	inputs []string
}{

	{token.ID, []string{
		`«\abc» «\brea» break`,
		`«\abc123»`,
		`«\_abc_»`,
	}},
	{token.CHAR_A, []string{`«a»`}},
	{token.CHAR_B, []string{`«b»`}},
	{token.CHAR_C, []string{`«c»`}},
	{token.SIMPLE, []string{`«simple»`}},
	{token.INVALID_TOKEN, []string{`«si»`}},
}

func TestLexer(t *testing.T) {
	l := new(simple.Lexer)
	seen := make(map[token.Type]bool)
	seen[token.WHITESPACE] = true
	for _, tc := range lexerTests {
		seen[tc.tok] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.tok.String(), input)
			l.Init(test.Source())
			tok := l.Next()
			for tok != token.EOI {
				if tok == tc.tok {
					s, e := l.Pos()
					test.Consume(t, s, e)
				}
				tok = l.Next()
			}
			test.Done(t, nil)
		}
	}
	for tok := token.Type(1); tok < token.NumTokens; tok++ {
		if !seen[tok] {
			t.Errorf("%v is not tested", tok)
		}
	}
}
