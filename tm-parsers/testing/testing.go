// Package testing provides a class for writing unit tests on generated parsers.
// Each test evaluates a single nonterminal against an input with expectation markers.
// These markers can be nested and define all ranges that must be returned by the parser
// for the given nonterminal.

package testing

import (
	"strings"
	"testing"
)

const separator rune = '“'
const nestedLeft rune = '«'
const nestedRight rune = '»'

type node struct {
	offset, endoffset int
}

type ParserTest struct {
	name          string
	source        []byte
	exp           []node
	expectFailure bool
	t             *testing.T
}

func NewParserTest(name, input string, t *testing.T) *ParserTest {
	out, exp := splitInput(name, input, t)
	expectFailure := strings.HasSuffix(input, "/*fails*/")
	return &ParserTest{
		name:          name,
		source:        out,
		exp:           exp,
		expectFailure: expectFailure,
		t:             t,
	}
}

func (pt *ParserTest) Source() []byte {
	return pt.source
}

func (pt *ParserTest) Error(line, offset, len int, msg string) {
	if !pt.expectFailure {
		pt.t.Errorf("Test %s: unexpected error at %d in `%s`: %s", pt.name, offset, pt.source, msg)
	}
}

func (pt *ParserTest) Consume(offset, endoffset int) {
	if len(pt.exp) == 0 {
		pt.t.Errorf("Test %s: unexpected occurrence: `%s` in `%s`", pt.name, pt.source[offset:endoffset], pt.source)
	} else if pt.exp[0].offset != offset || pt.exp[0].endoffset != endoffset {
		first := pt.exp[0]
		pt.t.Errorf("Test %s: got `%s`, want `%s`", pt.name, pt.source[offset:endoffset], pt.source[first.offset:first.endoffset])
	} else {
		pt.exp = pt.exp[1:]
	}
}

func (pt *ParserTest) Done(parsed bool) {
	if parsed != !pt.expectFailure {
		pt.t.Errorf("Test %s: Parse() returned %v for `%s`", pt.name, parsed, pt.source)
		return
	}
	if len(pt.exp) > 0 {
		first := pt.exp[0]
		pt.t.Errorf("Test %s: `%s` was not reported in `%s`", pt.name, pt.source[first.offset:first.endoffset], pt.source)
	}
}

func splitInput(name, input string, t *testing.T) (out []byte, exp []node) {
	var stack []int
	for index, ch := range input {
		switch ch {
		case separator, nestedRight, nestedLeft:
			if ch == nestedLeft || ch == separator && len(stack) == 0 {
				stack = append(stack, len(out))
			} else if len(stack) == 0 {
				t.Fatalf("Test %s: unexpected closing parenthesis at %d in `%s`", name, index, input)
			} else {
				exp = append(exp, node{stack[len(stack)-1], len(out)})
				stack = stack[:len(stack)-1]
			}
			continue
		}
		out = append(out, string(ch)...)
	}
	if len(stack) > 0 {
		t.Fatalf("Test %s: missing closing separator at %d in `%s`", name, stack[len(stack)-1], input)
	}
	if len(exp) == 0 && !strings.HasPrefix(input, "/*no expectations*/") {
		t.Errorf("Test %s: no expectations in `%s`", name, input)
	}
	return
}
