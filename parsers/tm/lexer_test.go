package tm_test

import (
	"testing"

	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/token"
)

var lexerTests = []struct {
	tok    token.Type
	inputs []string
}{

	{token.ID, []string{
		`«abc» «brea» as // <- keyword`,
		`«abc123»`,
		`«_abc_»`,
	}},
	{token.QUOTED_ID, []string{
		`«'a'» «'\n'»`,
	}},
	{token.COMMENT, []string{
		` «// foo»
		  «# bar»

		  language a(go); «// end-of0line»

		  «// end-of-file»
		`,
		`«// abc»`,
	}},
	{token.MULTILINECOMMENT, []string{
		`«/**
		   * comment ****
		   */»`,
		`«/***
		   * comment *
		   ***/»`,
	}},
	{token.ICON, []string{
		`«0» «-1» «525»`,
	}},
	{token.SCON, []string{
		`«"abc"» `,
		`«"ab ' and \"  c"» `,
	}},

	// Regular expressions vs division.
	{token.REGEXP, []string{
		`aa = «/abc+/»
		 token1: «/{aa}/» (space)

		 a -> a/b : someTerm ;`,
	}},
	{token.DIV, []string{
		`a -> a«/»b : someTerm ;`,
	}},

	// Code vs LBRACE.
	{token.CODE, []string{
		`a «{abc}» : b ;`,
		`a : b «{abc}» ;`,
		`a «{abc}»: /abc/`,
		`«{ab{e}c{}}» }`,

		`«{ skip("{{{", '}', '\''); }» `,
		`«{ skip("\"}\\"); }» `,
		`«{ skip(  // }
		       }» `,
		`«{ skip(  /* }
               " and ' are ok
		     */  }» `,
		`«{ skip(  /* }
               " and ' are ok
		     */}» `,
		`«{ skip(  /* <-not closed }» `,
		`«{ skip(  // <-not closed }» `,
		`«{ skip(  a/b }» `,
		"language l(a); :: lexer\n<*> { error: /abc/ «{}» }",
	}},
	{token.LBRACE, []string{
		`<a> «{» error: }`,
	}},
	{token.TEMPLATES, []string{
		`  «%%  »`,
		`asd
    «%%
     foo bar
        »`,
	}},

	// Punctuation.
	{token.REM, []string{`«%»`}},
	{token.COLONCOLON, []string{`«::»`}},
	{token.OR, []string{`«|»`}},
	{token.OROR, []string{`«||»`}},
	{token.ASSIGN, []string{`«=»`}},
	{token.ASSIGNASSIGN, []string{`«==»`}},
	{token.EXCLASSIGN, []string{`«!=»`}},
	{token.SEMICOLON, []string{`«;»`}},
	{token.DOT, []string{`«.»`}},
	{token.COMMA, []string{`«,»`}},
	{token.COLON, []string{`«:»`}},
	{token.LBRACK, []string{`«[»`}},
	{token.RBRACK, []string{`«]»`}},
	{token.LPAREN, []string{`«(»`}},
	{token.LPARENQUESTASSIGN, []string{`«(?=»`}},
	{token.MINUSGT, []string{`«->»`}},
	{token.RPAREN, []string{`«)»`}},
	{token.RBRACE, []string{`«}»`}},
	{token.LT, []string{`«<»`}},
	{token.GT, []string{`«>»`}},
	{token.MULT, []string{`«*»`}},
	{token.PLUS, []string{`«+»`}},
	{token.PLUSASSIGN, []string{`«+=»`}},
	{token.QUEST, []string{`«?»`}},
	{token.EXCL, []string{`«!»`}},
	{token.TILDE, []string{`«~»`}},
	{token.AND, []string{`«&»`}},
	{token.ANDAND, []string{`«&&»`}},
	{token.DOLLAR, []string{`«$»`}},
	{token.AT, []string{`«@»`}},

	// Keywords.
	{token.AS, []string{`«as»`}},
	{token.FALSE, []string{`«false»`}},
	{token.IMPORT, []string{`«import»`}},
	{token.SEPARATOR, []string{`«separator»`}},
	{token.SET, []string{`«set»`}},
	{token.TRUE, []string{`«true»`}},
	{token.ASSERT, []string{`«assert»`}},
	{token.BRACKETS, []string{`«brackets»`}},
	{token.CLASS, []string{`«class»`}},
	{token.EMPTY, []string{`«empty»`}},
	{token.EXPECT, []string{`«expect»`}},
	{token.EXPECTMINUSRR, []string{`«expect-rr»`}},
	{token.EXPLICIT, []string{`«explicit»`}},
	{token.EXTEND, []string{`«extend»`}},
	{token.FLAG, []string{`«flag»`}},
	{token.GENERATE, []string{`«generate»`}},
	{token.GLOBAL, []string{`«global»`}},
	{token.INJECT, []string{`«inject»`}},
	{token.INLINE, []string{`«inline»`}},
	{token.INPUT, []string{`«input»`}},
	{token.INTERFACE, []string{`«interface»`}},
	{token.LALR, []string{`«lalr»`}},
	{token.LANGUAGE, []string{`«language»`}},
	{token.LAYOUT, []string{`«layout»`}},
	{token.LEFT, []string{`«left»`}},
	{token.LEXER, []string{`«lexer»`}},
	{token.LOOKAHEAD, []string{`«lookahead»`}},
	{token.NOMINUSEOI, []string{`«no-eoi»`}},
	{token.NONASSOC, []string{`«nonassoc»`}},
	{token.NONEMPTY, []string{`«nonempty»`}},
	{token.PARAM, []string{`«param»`}},
	{token.PARSER, []string{`«parser»`}},
	{token.PREC, []string{`«prec»`}},
	{token.RIGHT, []string{`«right»`}},
	{token.CHAR_S, []string{`«s»`}},
	{token.SHIFT, []string{`«shift»`}},
	{token.SPACE, []string{`«space»`}},
	{token.CHAR_X, []string{`«x»`}},

	{token.INVALID_TOKEN, []string{
		` «{abc»`,
		` «{abc("\\}"»`,
	}},
}

func TestLexer(t *testing.T) {
	l := new(tm.Lexer)
	seen := map[token.Type]bool{}
	seen[token.WHITESPACE] = true
	seen[token.ERROR] = true
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
	for tok := token.EOI + 1; tok < token.NumTokens; tok++ {
		if !seen[tok] {
			t.Errorf("%v is not tested", tok)
		}
	}
}
