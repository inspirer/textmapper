// Package parsertest provides a standard way of writing unit tests on generated parsers.
// Each test evaluates a single range type against an input with expectation markers.
// These markers can be nested and define all ranges that must be returned by the parser
// for the given type.
package parsertest

import (
	"bytes"
	"fmt"
	"strings"
	"testing"
)

const (
	markerLeft  = '«'
	markerRight = '»'
	errorMarker = '§'
)

type node struct {
	offset, endoffset int
}

// ParserTest encapsulates a parser input and a set of expectations.
type ParserTest struct {
	name          string
	source        string
	exp           map[node]int
	expErrors     []int
	expectFailure bool
}

// New creates a new test by parsing out expectations (marked by guillemets) from the given
// input string.
func New(t *testing.T, name, input string) *ParserTest {
	t.Helper()
	out, exp, errors := splitInput(t, name, input)
	expectFailure := strings.Contains(input, "/*fails*/")
	return &ParserTest{
		name:          name,
		source:        out,
		exp:           exp,
		expErrors:     errors,
		expectFailure: expectFailure,
	}
}

// Source returns the pre-processed input string stripped off of all expectation metacharacters.
func (pt *ParserTest) Source() string {
	return pt.source
}

// ConsumeError records the reported syntax error and fails the test if the error is not expected.
func (pt *ParserTest) ConsumeError(t *testing.T, offset, endoffset int) {
	t.Helper()
	if len(pt.expErrors) > 0 && pt.expErrors[0] == offset {
		pt.expErrors = pt.expErrors[1:]
		return
	}
	t.Errorf("Test %s: unexpected error at %d in `%s_%s`", pt.name, offset, pt.source[:offset], pt.source[offset:])
}

func (n node) highlight(s string) string {
	return fmt.Sprintf("%v«%v»%v", s[:n.offset], s[n.offset:n.endoffset], s[n.endoffset:])
}

// Consume records the reported range and checks if it is expected.
func (pt *ParserTest) Consume(t *testing.T, offset, endoffset int) {
	t.Helper()
	if offset > endoffset || endoffset > len(pt.source) {
		t.Errorf("Test %s: [%v, %v] must be a subrange of [0, %v]", pt.name, offset, endoffset, len(pt.source))
		return
	}

	n := node{offset, endoffset}
	if pt.exp[n] > 0 {
		pt.exp[n]--
		if pt.exp[n] == 0 {
			delete(pt.exp, n)
		}
	} else {
		t.Errorf("Test %s: unexpected occurrence: `%s`", pt.name, n.highlight(pt.source))
	}
}

// Done checks that all expectations have been satisfied.
func (pt *ParserTest) Done(t *testing.T, err error) {
	t.Helper()
	if (err != nil) != pt.expectFailure {
		t.Errorf("Test %s: Parse() = %v for `%s`", pt.name, err, pt.source)
		return
	}
	if len(pt.exp) > 0 {
		for n := range pt.exp {
			t.Errorf("Test %s: not reported: `%s`", pt.name, n.highlight(pt.source))
		}
	}
}

func splitInput(t *testing.T, name, input string) (out string, exp map[node]int, errors []int) {
	t.Helper()
	var stack []int
	var buffer bytes.Buffer

	exp = make(map[node]int)
	for index, ch := range input {
		switch ch {
		case markerRight, markerLeft:
			if ch == markerLeft {
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
