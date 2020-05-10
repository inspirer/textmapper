package test_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/test"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
)

var lexerTests = []struct {
	tok    test.Token
	inputs []string
}{

	{test.IDENTIFIER, []string{
		`«abc» «brea» «abc-def»`,
		`«a-b-c-d»----  `,
		` «a»-`,
		` «a»--`,
		`«a»->«b»`,
		`«testfoo»----- testfoo----->`,
	}},

	{test.MINUS, []string{
		` «-» ->  a------b«-»  «-»«-»`,
	}},
	{test.MINUSGT, []string{
		`«->»`,
		`abcdef«->»`,
		`abcdef«->»   `,
		`testfoo1----«->»`,
	}},

	{test.BACKTRACKINGTOKEN, []string{
		`«test----->» «test->»  «testfoo->» testf->`,
	}},

	{test.TEST, []string{"«test»", "«test»-----"}},
	{test.DECL1, []string{"«decl1»"}},
	{test.DECL2, []string{"«decl2»"}},
	{test.INTEGERCONSTANT, []string{"«123»  34\n «0» ", "«123» 0"}},
	{test.LASTINT, []string{"123 «0\n»45 «0»"}},

	{test.LBRACE, []string{"«{»"}},
	{test.RBRACE, []string{"«}»"}},
	{test.LPAREN, []string{"«(»"}},
	{test.RPAREN, []string{"«)»"}},
	{test.LBRACK, []string{"«[»"}},
	{test.RBRACK, []string{"«]»"}},
	{test.DOT, []string{"«.»"}},
	{test.COMMA, []string{"«,»"}},
	{test.COLON, []string{"«:»"}},

	{test.SINGLELINECOMMENT, []string{" «//abc»\r\n "}},
	{test.MULTILINECOMMENT, []string{
		" «/**/» «/***/» «/*\r\n*/» ",
		" «/* /* ****/  */»  nested",
	}},
	{test.INVALID_TOKEN, []string{
		" «#» ",
		" /**/ «/* /* ****/  *  nested»", // unfinished comment
	}},
}

func TestLexer(t *testing.T) {
	l := new(test.Lexer)
	seen := map[test.Token]bool{}
	seen[test.WHITESPACE] = true
	seen[test.ERROR] = true
	for _, tc := range lexerTests {
		seen[tc.tok] = true
		for _, input := range tc.inputs {
			ptest := parsertest.New(t, tc.tok.String(), input)
			l.Init(ptest.Source())
			tok := l.Next()
			for tok != test.EOI {
				if tok == tc.tok {
					s, e := l.Pos()
					ptest.Consume(t, s, e)
				}
				tok = l.Next()
			}
			ptest.Done(t, nil)
		}
	}
	for tok := test.EOI + 1; tok < test.NumTokens; tok++ {
		if !seen[tok] {
			t.Errorf("%v is not tested", tok)
		}
	}
}
