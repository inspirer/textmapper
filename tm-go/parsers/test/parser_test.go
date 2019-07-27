package test_test

import (
	"testing"

	"context"
	"github.com/inspirer/textmapper/tm-go/parsers/test"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
)

var parseTests = []struct {
	nt     test.NodeType
	inputs []string
}{

	{test.Test, []string{
		` «decl2 decl1(a)»`,
	}},
	{test.Block, []string{
		`«{decl2}»`,
		`«{-decl2}»`,
		`«{--decl2}»`,
		`«{--}»`,
		`«{-}»`,
		`«{}»`,
	}},
	{test.Negation, []string{
		`{«-»decl2}`,
		`{«--»decl2}`,
		`{«--»}`,
		`{«-»}`,
		`{«-»{«-»decl2}}`,
	}},
	{test.Decl1, []string{
		`{«decl1(a.b.c.d123)»}`,
	}},
	{test.Decl2, []string{
		`«decl2»`,
	}},
	{test.Int, []string{
		`«42» «7» «9»`,
		`{«42»}`,
		`{«42[]»}`,
	}},
	{test.Int7, []string{
		`«7» 42`,
		`{«7»}`,
		`{«7[]»}`,
	}},
	{test.Int9, []string{
		`«9»`,
		`{3 «9» 11 «9»}`,
		`{-- 5 «9[]» 3}`,
	}},
	{test.TestClause, []string{
		`«test {}»`,
		`«test { decl1 }»`,
		`«test { decl2 decl1 }»`,
	}},

	{test.MultiLineComment, []string{
		` decl2 «/* ****/» decl1(a)`,
	}},
	{test.SingleLineComment, []string{
		` decl2 «// abc»
		 decl1(a)`,
	}},
	{test.InvalidToken, []string{
		` decl2 «%» `,
	}},
	{test.Identifier, []string{
		` decl1(«abc».«def1») `,
	}},
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
			p.Init(func(tn test.NodeType, offset, endoffset int) {
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
