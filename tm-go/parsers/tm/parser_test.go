package tm_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/tm"
	pt "github.com/inspirer/textmapper/tm-parsers/testing"
)

var parseTests = []struct {
	nt     tm.NodeType
	inputs []string
}{

	{tm.Comment, []string{
		rule(` «# abc»
		  «# abc2»
		  a ::= abc ;    «# 8»
		  «# abc2»`),
	}},
	{tm.MultilineComment, []string{
		rule(`a void ::= «/* te ** / st */» ;`),
		rule(`«/* abc */» a::=b;`),
	}},
	{tm.InvalidToken, []string{
		rule("a ::= «'»\n   ;"),
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
			test := pt.NewParserTest(tc.nt.String(), input, t)
			l.Init(test.Source(), test.Error)
			p.Init(test.Error, func(t tm.NodeType, offset, endoffset int) {
				if t == tc.nt {
					test.Consume(offset, endoffset)
				}
			})
			test.Done(p.ParseInput(l))
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
