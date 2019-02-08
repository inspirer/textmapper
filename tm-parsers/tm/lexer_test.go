package tm_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/parsertest"
	"github.com/inspirer/textmapper/tm-parsers/tm"
)

var lexerTests = []struct {
	tok    tm.Token
	inputs []string
}{

	{tm.ID, []string{
		`«abc» «brea» as // <- keyword`,
		`«abc123»`,
		`«_abc_»`,
	}},
	{tm.COMMENT, []string{
		` «// foo»
		  «# bar»

		  language a(go); «// end-of0line»

		  «// end-of-file»
		`,
		`«// abc»`,
	}},
	{tm.MULTILINECOMMENT, []string{
		`«/**
		   * comment ****
		   */»`,
		`«/***
		   * comment *
		   ***/»`,
	}},
	{tm.ICON, []string{
		`«0» «-1» «525»`,
	}},
	{tm.SCON, []string{
		`«"abc"» `,
		`«"ab ' and \"  c"» `,
	}},

	// Regular expressions vs division.
	{tm.REGEXP, []string{
		`aa = «/abc+/»
		 token1: «/{aa}/» (space)

		 a -> a/b : someTerm ;`,
	}},
	{tm.DIV, []string{
		`a -> a«/»b : someTerm ;`,
	}},

	// Code vs LBRACE.
	{tm.CODE, []string{
		`a «{abc}» : b ;`,
		`a : b «{abc}» ;`,
		`a «{abc}»: /abc/`,
		`«{ab{e}c{}}» }`,

		`«{ skip("{{{", '}', '\''); }» `,
		`«{ skip("\"}\\"); }» `,
		"language l(a); :: lexer\n<*> { error: /abc/ «{}» }",
	}},
	{tm.LBRACE, []string{
		`<a> «{» error: }`,
	}},
	{tm.TEMPLATES, []string{
		`  «%%  »`,
		`asd
    «%% 
     foo bar
        »`,
	}},

	// Punctuation.
	{tm.REM, []string{`«%»`}},
	{tm.COLONCOLON, []string{`«::»`}},
	{tm.OR, []string{`«|»`}},
	{tm.OROR, []string{`«||»`}},
	{tm.ASSIGN, []string{`«=»`}},
	{tm.ASSIGNASSIGN, []string{`«==»`}},
	{tm.EXCLASSIGN, []string{`«!=»`}},
	{tm.SEMICOLON, []string{`«;»`}},
	{tm.DOT, []string{`«.»`}},
	{tm.COMMA, []string{`«,»`}},
	{tm.COLON, []string{`«:»`}},
	{tm.LBRACK, []string{`«[»`}},
	{tm.RBRACK, []string{`«]»`}},
	{tm.LPAREN, []string{`«(»`}},
	{tm.LPARENQUESTASSIGN, []string{`«(?=»`}},
	{tm.MINUSGT, []string{`«->»`}},
	{tm.RPAREN, []string{`«)»`}},
	{tm.RBRACE, []string{`«}»`}},
	{tm.LT, []string{`«<»`}},
	{tm.GT, []string{`«>»`}},
	{tm.MULT, []string{`«*»`}},
	{tm.PLUS, []string{`«+»`}},
	{tm.PLUSASSIGN, []string{`«+=»`}},
	{tm.QUEST, []string{`«?»`}},
	{tm.EXCL, []string{`«!»`}},
	{tm.TILDE, []string{`«~»`}},
	{tm.AND, []string{`«&»`}},
	{tm.ANDAND, []string{`«&&»`}},
	{tm.DOLLAR, []string{`«$»`}},
	{tm.ATSIGN, []string{`«@»`}},

	// Keywords.
	{tm.AS, []string{`«as»`}},
	{tm.FALSE, []string{`«false»`}},
	{tm.IMPLEMENTS, []string{`«implements»`}},
	{tm.IMPORT, []string{`«import»`}},
	{tm.SEPARATOR, []string{`«separator»`}},
	{tm.SET, []string{`«set»`}},
	{tm.TRUE, []string{`«true»`}},
	{tm.ASSERT, []string{`«assert»`}},
	{tm.BRACKETS, []string{`«brackets»`}},
	{tm.CLASS, []string{`«class»`}},
	{tm.EMPTY, []string{`«empty»`}},
	{tm.EXPLICIT, []string{`«explicit»`}},
	{tm.FLAG, []string{`«flag»`}},
	{tm.GENERATE, []string{`«generate»`}},
	{tm.GLOBAL, []string{`«global»`}},
	{tm.INLINE, []string{`«inline»`}},
	{tm.INPUT, []string{`«input»`}},
	{tm.INTERFACE, []string{`«interface»`}},
	{tm.LALR, []string{`«lalr»`}},
	{tm.LANGUAGE, []string{`«language»`}},
	{tm.LAYOUT, []string{`«layout»`}},
	{tm.LEFT, []string{`«left»`}},
	{tm.LEXER, []string{`«lexer»`}},
	{tm.LOOKAHEAD, []string{`«lookahead»`}},
	{tm.NOMINUSEOI, []string{`«no-eoi»`}},
	{tm.NONASSOC, []string{`«nonassoc»`}},
	{tm.NONEMPTY, []string{`«nonempty»`}},
	{tm.PARAM, []string{`«param»`}},
	{tm.PARSER, []string{`«parser»`}},
	{tm.PREC, []string{`«prec»`}},
	{tm.RETURNS, []string{`«returns»`}},
	{tm.RIGHT, []string{`«right»`}},
	{tm.CHAR_S, []string{`«s»`}},
	{tm.SHIFT, []string{`«shift»`}},
	{tm.SOFT, []string{`«soft»`}},
	{tm.SPACE, []string{`«space»`}},
	{tm.VOID, []string{`«void»`}},
	{tm.CHAR_X, []string{`«x»`}},

	{tm.INVALID_TOKEN, []string{
		` «{abc»`,
		` «{abc("\\}"»`,
	}},
}

func TestLexer(t *testing.T) {
	l := new(tm.Lexer)
	seen := map[tm.Token]bool{}
	seen[tm.WHITESPACE] = true
	seen[tm.ERROR] = true
	for _, tc := range lexerTests {
		seen[tc.tok] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.tok.String(), input)
			l.Init(test.Source())
			tok := l.Next()
			for tok != tm.EOI {
				if tok == tc.tok {
					s, e := l.Pos()
					test.Consume(t, s, e)
				}
				tok = l.Next()
			}
			test.Done(t, nil)
		}
	}
	for tok := tm.EOI + 1; tok < tm.NumTokens; tok++ {
		if !seen[tok] {
			t.Errorf("%v is not tested", tok)
		}
	}
}
