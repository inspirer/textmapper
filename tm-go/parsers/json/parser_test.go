package json_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/json"
	"reflect"
	"strings"
)

const separator rune = '“'
const nestedLeft rune = '«'
const nestedRight rune = '»'

type jsonTestCase struct {
	nt     json.NodeType
	inputs []string
}

type jsonTestExpectation struct {
	offset, endoffset int
}

var jsParseTests = []jsonTestCase{
	{json.EmptyObject, []string{
		`“{}“`,
		`“{ /* comment */ }“`,
		`{"aa": “{}“ }`,
	}},
	{json.JSONObject, []string{
		`“{ "a" : "b" }“`,
		`“{ "a" : ["b"] }“`,
		`“{ "a" : {} }“`,
		`“{ "a" : «{"q":B}» }“`,
	}},
	{json.JSONArray, []string{
		`{ "a" : “["b"]“ }`,
		` “[]“ `,
	}},
	{json.JSONText, []string{
		`“{ "a" : ["b", A] }“`,
		` “"aa"“ `,
		` “A“ `,
	}},
	{json.JSONMember, []string{
		`[{ “"a" : ["b"]“, “"q":[]“ }]`,
	}},
	{json.JSONValue, []string{
		`“{ "a" : «[«"b"»]» }“`,
		` “"aa"“ `,
	}},
}

func splitInput(input string, t *testing.T) (out []byte, exp []jsonTestExpectation) {
	var stack []int
	for index, ch := range input {
		switch ch {
		case separator, nestedRight, nestedLeft:
			if ch == nestedLeft || ch == separator && len(stack) == 0 {
				stack = append(stack, len(out))
			} else if len(stack) == 0 {
				t.Fatalf("Unexpected closing parenthesis at %d in `%s`", index, input)
			} else {
				exp = append(exp, jsonTestExpectation{stack[len(stack)-1], len(out)})
				stack = stack[:len(stack)-1]
			}
			continue
		}
		out = append(out, string(ch)...)
	}
	if len(stack) > 0 {
		t.Fatalf("Missing closing separator at %d in `%s`", stack[len(stack)-1], input)
	}
	return
}

func TestSplitInput(t *testing.T) {
	res, exp := splitInput(`abc“def“cdf“q1“q2`, t)
	if string(res) != `abcdefcdfq1q2` {
		t.Errorf("Unexpected result: %s", res)
	}
	if !reflect.DeepEqual(exp, []jsonTestExpectation{{3, 6}, {9, 11}}) {
		t.Errorf("Unexpected expectations: %v", exp)
	}

	res, exp = splitInput(``, t)
	if string(res) != `` || len(exp) != 0 {
		t.Errorf("splitInput(``) is broken: %v", res)
	}

	res, exp = splitInput(`“abc“ «a«b«c»»»`, t)
	if string(res) != `abc abc` {
		t.Errorf("Unexpected result: %s", res)
	}
	if !reflect.DeepEqual(exp, []jsonTestExpectation{{0, 3}, {6, 7}, {5, 7}, {4, 7}}) {
		t.Errorf("Unexpected expectations: %v", exp)
	}
}

type node struct {
	offset, endoffset int
}

type expTest struct {
	source       []byte
	expectedType json.NodeType
	exp          []jsonTestExpectation
	t            *testing.T
	parsed       []node
}

func (e *expTest) Node(nt json.NodeType, offset, endoffset int) {
	e.parsed = append(e.parsed, node{offset, endoffset})
	if e.expectedType != nt {
		return //len(e.parsed)
	}
	if len(e.exp) == 0 {
		e.t.Errorf("Unexpected %v: `%s` in `%s`", nt, e.source[offset:endoffset], e.source)
	} else if e.exp[0].offset != offset || e.exp[0].endoffset != endoffset {
		first := e.exp[0]
		e.t.Errorf("got `%s`, want `%s`", e.source[offset:endoffset], e.source[first.offset:first.endoffset])
	} else {
		e.exp = e.exp[1:]
	}
	return //len(e.parsed)
}

func (e *expTest) done() {
	if len(e.exp) > 0 {
		first := e.exp[0]
		e.t.Errorf("`%s` was not reported in `%s`", e.source[first.offset:first.endoffset], e.source)
	}
}

func TestParser(t *testing.T) {
	l := new(json.Lexer)
	p := new(json.Parser)

	seen := map[json.NodeType]bool{}
	for _, tc := range jsParseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			source, exp := splitInput(input, t)
			if len(exp) == 0 && !strings.HasPrefix(input, "/*no expectations*/") {
				t.Errorf("No expectations in `%s`", input)
			}
			expected := !strings.HasSuffix(input, "/*fails*/")

			onError := func(line, offset, len int, msg string) {
				if expected {
					t.Errorf("%d, %d: %s", line, offset, msg)
				}
			}
			expTest := &expTest{source, tc.nt, exp, t, nil}

			l.Init([]byte(source), onError)
			p.Init(onError, expTest)
			res, _ := p.Parse(l)
			if res != expected {
				t.Errorf("Parse() returned %v for `%s`", res, source)
			} else {
				expTest.done()
			}
		}
	}
	for n := json.NodeType(1); n < json.NodeTypeMax; n++ {
		if !seen[n] {
			t.Errorf("%v is not tested", n)
		}
	}
}

type consumer struct {
}

func (c consumer) Node(t json.NodeType, offset, endoffset int) {
}

func testParser(input []byte, t *testing.T) {
	l := new(json.Lexer)
	l.Init(input, PanicOnError)

	p := new(json.Parser)
	p.Init(PanicOnError, consumer{})
	p.Parse(l)
}

func TestParserExample(t *testing.T) {
	testParser([]byte(jsonExample), t)
}

func BenchmarkParser(b *testing.B) {
	l := new(json.Lexer)
	p := new(json.Parser)
	p.Init(PanicOnError, consumer{})
	for i := 0; i < b.N; i++ {
		l.Init([]byte(jsonExample), PanicOnError)
		p.Parse(l)
	}
	b.SetBytes(int64(len(jsonExample)))
}
