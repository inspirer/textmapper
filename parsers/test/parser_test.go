package test_test

import (
	"testing"

	"context"

	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/test"
)

var parseTests = []struct {
	nt     test.NodeType
	flags  test.NodeFlags
	inputs []string
}{

	{test.Test, 0, []string{
		` «decl2 decl1(a)»`,
	}},
	{test.Block, 0, []string{
		`«{decl2}»`,
		`«{-decl2}»`,
		`«{--decl2}»`,
		`«{--}»`,
		`«{-}»`,
		`«{}»`,
	}},
	{test.Negation, 0, []string{
		`{«-»decl2}`,
		`{«--»decl2}`,
		`{«--»}`,
		`{«-»}`,
		`{«-»{«-»decl2}}`,
	}},
	{test.Decl1, 0, []string{
		`{«decl1(a.b.c.d123)»}`,
	}},
	{test.Decl2, 0, []string{
		`«decl2»`,
	}},
	{test.If, 0, []string{
		`«if(as) decl2»`,
		`«if(as) decl2 else decl2»`,
		`«if(as) decl2 else «if(as) decl2 else decl2»»`,
		`«if(as) «if(as) decl2 else decl2»»`,
		`«if(as) «if(as) decl2 else decl2» else decl2»`,
	}},
	{test.Elem, 0, []string{
		`if(«as») decl2`,
		`if(«as» «f_a») decl2`,
		`if(«f_a» «as» «f_a») decl2`,
	}},
	{test.Int, 0, []string{
		`«42» «7» «9» `,
		`{«42»}`,
		`{«42[]»}`,
	}},
	{test.Int7, 0, []string{
		`«7» 42 `,
		`{«7»}`,
		`{«7[]»}`,
	}},
	{test.Int9, 0, []string{
		`9
          «9»    `,
		`{3 «9» 11 «9»}`,
		`{-- 5 «9[]» 3}`,
	}},
	{test.LastInt, 0, []string{
		"«9»",
		"«9\n»\n9 ",
		"«9\n»\n«9»",
	}},
	{test.Empty1, 0, []string{
		` test (   «»)  `,
	}},
	{test.TestClause, 0, []string{
		`«test {}»`,
		`«test { decl1 }»`,
		`«test { decl2 decl1 }»`,
		` «test decl1 test»`,
		` «test decl2 test»`,
	}},
	{test.TestIntClause, test.InTest | test.InFoo, []string{
		`{ «test 1» }`,
	}},
	{test.Icon, test.InTest, []string{
		`{ test «1» }`,
	}},

	{test.MultiLineComment, 0, []string{
		` decl2 «/* ****/» decl1(a)`,
	}},
	{test.SingleLineComment, 0, []string{
		` decl2 «// abc»
		 decl1(a)`,
	}},
	{test.InvalidToken, 0, []string{
		` decl2 «%»`,
	}},
	{test.Identifier, 0, []string{
		` decl1(«abc».«def1») `,
		` test «decl1» test`,
		` test «decl2» test`,
	}},
	{test.PlusExpr, 0, []string{
		` eval(««1 + 2» + 3») `,
		` eval(1 as «2 + 3») `,
		` eval(1 as «\ 2 + 3») `, // via semantic action
	}},
	{test.AsExpr, 0, []string{
		` eval(«1 as 2 + 3») `,
		` eval(«1 as 2 + «3 as 5+6+7»») `,
	}},
	{test.IntExpr, 0, []string{
		` eval(«1» as «2» + «3») `,
	}},
	{test.EvalEmpty1, 0, []string{
		` «eval(1+1)» decl2`,
	}},
	{test.EvalFoo, 0, []string{
		` «eval(4.1 as 2 + 3)» `,
		` «eval(4.1 as 2)» `,
	}},
	{test.EvalFoo2, 0, []string{
		` «eval(4.1+2)» `,
	}},
	{test.DeclOptQual, 0, []string{
		` «decl2:  a.b.c»  decl2`,
		` «decl2 :»  decl2`,
	}},
	{test.Bar, 0, []string{
		` eval(4.«»1 as «»2 + «»3) `,
	}},

	// Testing LALR(2).
	{test.X, 0, []string{`z «z» z x `}},
	{test.Y, 0, []string{`z «z» z y `}},
	{test.AX, 0, []string{` «z z z x» `}},
	{test.AY, 0, []string{` «z z z y» `}},
}

func TestParser(t *testing.T) {
	l := new(test.Lexer)
	p := new(test.Parser)

	seen := map[test.NodeType]bool{}
	ctx := context.Background()
	for _, tc := range parseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			pt := parsertest.New(t, tc.nt.String(), input)
			l.Init(pt.Source())
			p.Init(func(tn test.NodeType, flags test.NodeFlags, offset, endoffset int) {
				if tn == tc.nt {
					pt.Consume(t, offset, endoffset)
				}
			})
			pt.Done(t, p.ParseTest(ctx, l))
		}
	}
	for n := test.NodeType(1); n < test.NodeTypeMax; n++ {
		if !seen[n] {
			t.Errorf("%v is not tested", n)
		}
	}
}
