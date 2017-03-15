package json_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/json"
	pt "github.com/inspirer/textmapper/tm-parsers/testing"
)

var jsParseTests = []struct {
	nt     json.NodeType
	inputs []string
}{

	{json.EmptyObject, []string{
		`«{}»`,
		`«{ /* comment */ }»`,
		`{"aa": «{}» }`,
	}},
	{json.JSONObject, []string{
		`«{ "a" : "b" }»`,
		`«{ "a" : ["b"] }»`,
		`«{ "a" : {} }»`,
		`«{ "a" : «{"q":B}» }»`,
	}},
	{json.JSONArray, []string{
		`{ "a" : «["b"]» }`,
		` «[]» `,
	}},
	{json.JSONText, []string{
		`«{ "a" : ["b", A] }»`,
		` «"aa"» `,
		` «A» `,
	}},
	{json.JSONMember, []string{
		`[{ «"a" : ["b"]», «"q":[]» }]`,
	}},
	{json.JSONValue, []string{
		`«{ "a" : «[«"b"»]» }»`,
		` «"aa"» `,
	}},
	{json.InvalidToken, []string{
		`  «%» null `,
	}},
	{json.NonExistingType, []string{}},
	{json.MultiLineComment, []string{
		`{ "a"«/* abc */» : [] }`,
	}},
	{json.JsonString, []string{
		`{ «"a"» : [«"b"»] }`,
	}},
}

func TestParser(t *testing.T) {
	l := new(json.Lexer)
	p := new(json.Parser)

	seen := map[json.NodeType]bool{}
	for _, tc := range jsParseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			test := pt.NewParserTest(tc.nt.String(), input, t)
			l.Init(test.Source())
			p.Init(test.ErrorWithLine, func(t json.NodeType, offset, endoffset int) {
				if t == tc.nt {
					test.Consume(offset, endoffset)
				}
			})
			test.Done(p.Parse(l))
		}
	}
	for n := json.NodeType(1); n < json.NodeTypeMax; n++ {
		if !seen[n] {
			t.Errorf("%v is not tested", n)
		}
	}
}

func BenchmarkParser(b *testing.B) {
	l := new(json.Lexer)
	p := new(json.Parser)
	onError := func(line, offset, len int, msg string) {
		b.Errorf("%d, %d: %s", line, offset, msg)
	}

	p.Init(onError, func(t json.NodeType, offset, endoffset int) {})
	for i := 0; i < b.N; i++ {
		l.Init(jsonExample)
		p.Parse(l)
	}
	b.SetBytes(int64(len(jsonExample)))
}
