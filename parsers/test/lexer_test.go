package test_test

import (
	"testing"

	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/test"
	"github.com/inspirer/textmapper/parsers/test/token"
)

var lexerTests = []struct {
	tok    token.Token
	inputs []string
}{

	{token.IDENTIFIER, []string{
		`«abc» «brea» «abc-def»`,
		`«a-b-c-d»----  `,
		` «a»-`,
		` «a»--`,
		`«a»->«b»`,
		`«testfoo»----- testfoo----->`,
	}},
	{token.IDENTIFIER2, []string{
		`«^a» «^b»`,
		"«^\n» «^\x00»",
	}},

	{token.MINUS, []string{
		` «-» ->  a------b«-»  «-»«-»`,
	}},
	{token.MINUSGT, []string{
		`«->»`,
		`abcdef«->»`,
		`abcdef«->»   `,
		`testfoo1----«->»`,
	}},

	{token.BACKTRACKINGTOKEN, []string{
		`«test----->» «test->»  «testfoo->» testf->`,
	}},

	{token.TEST, []string{"«test»", "«test»-----"}},
	{token.DECL1, []string{"«decl1»"}},
	{token.DECL2, []string{"«decl2»"}},
	{token.IF, []string{"«if»"}},
	{token.ELSE, []string{"«else»"}},
	{token.EVAL, []string{"«eval»"}},
	{token.AS, []string{"«as»"}},
	{token.INTEGERCONSTANT, []string{"«123»  34\n «0» ", "«123» 0"}},
	{token.LASTINT, []string{"123 «0\n»45 «0»"}},

	{token.LBRACE, []string{"«{»"}},
	{token.RBRACE, []string{"«}»"}},
	{token.LPAREN, []string{"«(»"}},
	{token.RPAREN, []string{"«)»"}},
	{token.LBRACK, []string{"«[»"}},
	{token.RBRACK, []string{"«]»"}},
	{token.DOT, []string{
		"«.»",
		"«.»«.»",
	}},
	{token.MULTILINE, []string{
		"% \n «%q\n% q»\n%f",
		"«%q\n%q» !",
		"«%q\n%   q»",
	}},
	{token.DOTDOTDOT, []string{"«...»"}},
	{token.COMMA, []string{"«,»"}},
	{token.COLON, []string{"«:»"}},
	{token.PLUS, []string{"«+»"}},
	{token.ESC, []string{`«\»`}},
	{token.CHAR__, []string{`«_»`}},
	{token.FOO_, []string{`«foo_»`}},
	{token.F_A, []string{`«f_a»`}},

	{token.SINGLELINECOMMENT, []string{" «//abc»\r\n "}},
	{token.MULTILINECOMMENT, []string{
		" «/**/» «/***/» «/*\r\n*/» ",
		" «/* /* ****/  */»  nested",
	}},
	{token.SHARPATID, []string{
		" Zfoo «Zfoob» «Zfo\\u1111ob» ",
	}},
	{token.DQUOTE, []string{"«\"»"}},
	{token.APOS, []string{"«'»"}},
	{token.ZFOO, []string{
		" «Zfoo» Zfoob ",
	}},
	{token.INVALID_TOKEN, []string{
		" «#» ",
		" /**/ «/* /* ****/  *  nested»", // unfinished comment
		" «Zff\\» ",
		" \x00 «\U0001fffe»«#» ", // \x00 is valid whitespace
	}},
}

func TestLexer(t *testing.T) {
	l := new(test.Lexer)
	seen := map[token.Token]bool{}
	seen[token.WHITESPACE] = true
	seen[token.ERROR] = true
	for _, tc := range lexerTests {
		seen[tc.tok] = true
		for _, input := range tc.inputs {
			ptest := parsertest.New(t, tc.tok.String(), input)
			l.Init(ptest.Source())
			tok := l.Next()
			for tok != token.EOI {
				if tok == tc.tok {
					s, e := l.Pos()
					ptest.Consume(t, s, e)
				}
				tok = l.Next()
			}
			ptest.Done(t, nil)
		}
	}
	for tok := token.EOI + 1; tok < token.NumTokens; tok++ {
		if !seen[tok] {
			t.Errorf("%v is not tested", tok)
		}
	}
}
