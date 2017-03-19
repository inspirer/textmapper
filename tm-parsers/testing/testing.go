// Package testing provides a class for writing unit tests on generated parsers.
// Each test evaluates a single range type against an input with expectation markers.
// These markers can be nested and define all ranges that must be returned by the parser
// for the given type.

package testing

import (
	"bytes"
	"fmt"
	"strings"
	"testing"
)

const (
	nestedLeft  = '«'
	nestedRight = '»'
	errorMarker = '§'
)

type node struct {
	offset, endoffset int
}

type ParserTest struct {
	name          string
	source        string
	exp           map[node]int
	expErrors     []int
	expectFailure bool
	t             *testing.T
}

func NewParserTest(name, input string, t *testing.T) *ParserTest {
	out, exp, errors := splitInput(name, input, t)
	expectFailure := strings.Contains(input, "/*fails*/")
	return &ParserTest{
		name:          name,
		source:        out,
		exp:           exp,
		expErrors:     errors,
		expectFailure: expectFailure,
		t:             t,
	}
}

func (pt *ParserTest) Source() string {
	return pt.source
}

func (pt *ParserTest) Error(offset, length int) {
	if len(pt.expErrors) > 0 && pt.expErrors[0] == offset {
		pt.expErrors = pt.expErrors[1:]
		return
	}
	pt.t.Errorf("Test %s: unexpected error at %d in `%s_%s`", pt.name, offset, pt.source[:offset], pt.source[offset:])
}

func (n node) highlight(s string) string {
	return fmt.Sprintf("%v«%v»%v", s[:n.offset], s[n.offset:n.endoffset], s[n.endoffset:])
}

func (pt *ParserTest) Consume(offset, endoffset int) {
	if offset > endoffset || endoffset > len(pt.source) {
		pt.t.Errorf("Test %s: [%v, %v] must be a subrange of [0, %v]", offset, endoffset, len(pt.source))
		return
	}

	n := node{offset, endoffset}
	if pt.exp[n] > 0 {
		pt.exp[n]--
		if pt.exp[n] == 0 {
			delete(pt.exp, n)
		}
	} else {
		pt.t.Errorf("Test %s: unexpected occurrence: `%s`", pt.name, n.highlight(pt.source))
	}
}

func (pt *ParserTest) Done(err error) {
	if (err != nil) != pt.expectFailure {
		pt.t.Errorf("Test %s: Parse() = %v for `%s`", pt.name, err, pt.source)
		return
	}
	if len(pt.exp) > 0 {
		for n := range pt.exp {
			pt.t.Errorf("Test %s: not reported: `%s`", pt.name, n.highlight(pt.source))
		}
	}
}

func splitInput(name, input string, t *testing.T) (out string, exp map[node]int, errors []int) {
	var stack []int
	var buffer bytes.Buffer

	exp = make(map[node]int)
	for index, ch := range input {
		switch ch {
		case nestedRight, nestedLeft:
			if ch == nestedLeft {
				stack = append(stack, buffer.Len())
			} else if len(stack) == 0 {
				t.Fatalf("Test %s: unexpected closing parenthesis at %d in `%s`", name, index, input)
			} else {
				n := node{stack[len(stack)-1], buffer.Len()}
				exp[n]++
				stack = stack[:len(stack)-1]
			}
			continue
		case errorMarker:
			errors = append(errors, buffer.Len())
			continue
		}
		buffer.WriteRune(ch)
	}
	out = buffer.String()
	if len(stack) > 0 {
		t.Fatalf("Test %s: missing closing separator at %d in `%s`", name, stack[len(stack)-1], input)
	}
	if len(exp) == 0 && !strings.HasPrefix(input, "/*no expectations*/") {
		t.Errorf("Test %s: no expectations in `%s`", name, input)
	}
	return
}
