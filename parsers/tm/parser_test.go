package tm_test

import (
	"context"
	"testing"

	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/tm"
)

var parseTests = []struct {
	nt     tm.NodeType
	inputs []string
}{

	{tm.Identifier, []string{
		`  language «a»(«b»); :: lexer «error»: `,
	}},
	{tm.Option, []string{
		header + ` «a = 5»  «list = [5]»  «feature = true» `,
	}},
	{tm.IntegerLiteral, []string{
		header + ` a = «5»  list = [«5»]  feature = true `,
	}},
	{tm.BooleanLiteral, []string{
		header + ` a = «true»`,
	}},
	{tm.Lexeme, []string{
		lexerPre + ` «error:»`,
		lexerPre + ` «<foo, bar> error:»`,
		lexerPre + ` <foo, bar> { «error:» }`,
		lexerPre + ` «error: /abc/ -1»  «def:»`,
		lexerPre + ` «error: /abc/ {}»`,
		lexerPre + ` <*> { «error: /abc/ {}» }`,
		lexerPre + ` «int {Type}: /[0-9]+/ { $$ = parseInt(); }»`,
	}},
	{tm.Command, []string{
		lexerPre + ` abc: /abc/ «{}»`,
		lexerPre + ` abc: /abc/ «{ printf("}") }»`,
	}},
	{tm.Comment, []string{
		parserPre + ` «# abc»
		  «# abc2»
		  a : abc ;    «# 8»
		  «# abc2»`,
	}},
	{tm.MultilineComment, []string{
		parserPre + `a : «/* te ** / st */» ;`,
		parserPre + `«/* abc */» a:b;`,

		// While recovering.
		parserPre + " a : (§:: a «/*aaa*/» b ) ; ",
	}},
	{tm.InvalidToken, []string{
		parserPre + "a : «'»\n   ;",
	}},

	{tm.Rule, []string{
		parserPre + " a : /* empty */ «»| «abc» | «abc -> def» ; ",
	}},
	{tm.DirectiveExpect, []string{
		parserPre + ` «%expect 0;» `,
	}},
	{tm.DirectiveExpectRR, []string{
		parserPre + ` «%expect-rr 8;» `,
	}},
	{tm.DirectiveInject, []string{
		parserPre + ` «%inject comment -> Comment/a,b;» `,
	}},
	{tm.SyntaxProblem, []string{
		parserPre + " a : (§«:: a /*aaa*/ b» ) ; ",
		parserPre + " a : §«+ a» ; ",
		header + ` a = 5  «b §a b c = 5» :: lexer a: /a/`,
	}},

	// TODO add tests
}

func TestParser(t *testing.T) {
	var l tm.Lexer
	var p tm.Parser

	ctx := context.Background()
	seen := make(map[tm.NodeType]bool)
	seen[tm.File] = true
	for _, tc := range parseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.nt.String(), input)
			l.Init(test.Source())
			errHandler := func(se tm.SyntaxError) bool {
				test.ConsumeError(t, se.Offset, se.Endoffset)
				return true
			}
			p.Init(errHandler, func(nt tm.NodeType, offset, endoffset int) {
				if nt == tc.nt {
					test.Consume(t, offset, endoffset)
				}
			})
			test.Done(t, p.ParseFile(ctx, &l))
		}
	}
	for n := tm.NodeType(1); n < tm.NodeTypeMax; n++ {
		if !seen[n] {
			// TODO t.Errorf("%v is not tested", n)
		}
	}
}

const header = "language l(a); "
const lexerPre = "language l(a); :: lexer\n"
const parserPre = "language l(a); :: lexer a = /abc/ :: parser "
