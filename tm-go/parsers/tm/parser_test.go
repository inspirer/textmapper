package tm_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
)

var parseTests = []struct {
	nt     tm.NodeType
	inputs []string
}{

	{tm.Comment, []string{
		rule(` «# abc»
		  «# abc2»
		  a : abc ;    «# 8»
		  «# abc2»`),
	}},
	{tm.MultilineComment, []string{
		rule(`a void : «/* te ** / st */» ;`),
		rule(`«/* abc */» a:b;`),
	}},
	{tm.InvalidToken, []string{
		rule("a : «'»\n   ;"),
	}},

	// TODO add tests
}

func TestParser(t *testing.T) {
	l := new(tm.Lexer)
	p := new(tm.Parser)

	seen := map[tm.NodeType]bool{}
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
			test.Done(t, p.ParseInput(l))
		}
	}
	for n := tm.NodeType(1); n < tm.NodeTypeMax; n++ {
		if !seen[n] {
			// TODO t.Errorf("%v is not tested", n)
		}
	}
}

func rule(s string) string {
	return `language l(a); :: lexer a = /abc/ :: parser ` + s
}
