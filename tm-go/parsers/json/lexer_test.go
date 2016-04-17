package json_test

import (
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/json"
)

const jsonExample = `
{
    "some key": [{
        "title": "example glossary",
        "float value": 1e9,
        "float value 2": -0.9e-5,
		"Gloss \u1234 \nDiv": {
             "title": "S",   			"items": {
                "read": {
                    "ID": "xml",
					"SortAs": "price",
					"type": "Markup Language",
					"Acronym": "XML",
					"UniqueID": "850257207432",
					"def": {
                "json": "Lorem ipsum dolor sit amet, ad prima imperdiet sea. Homero reprimique no duo, mundi iriure expetenda ei est. No nec denique efficiantur, pri ad oratio adipisci expetendis.",
						"links": ["ABC", "Echo", "a", "b", "c"]
                    },
					"render as": "markup", "null": null, "true": true, "false": false
                }
            }
        }
    }]
}
`

func PanicOnError(line, offset, len int, msg string) {
	panic(fmt.Sprintf("%d, %d: %s", line, offset, msg))
}

func testLexer(input []byte, t *testing.T) {
	l := new(json.Lexer)
	l.Init(input, PanicOnError)
	spacesRE := regexp.MustCompile(`^\s+$`)

	next := l.Next()
	offset := 0
	for next != json.EOI {
		s, e := l.Pos()
		if s > offset && !spacesRE.Match(input[offset:s]) {
			t.Errorf("Spaces expected: %s", input[offset:s])
		}
		offset = e
		token := string(input[s:e])
		switch next {
		case json.LCURLY, json.RCURLY, json.LSQUARE, json.RSQUARE,
			json.COLON, json.COMMA, json.NULL, json.TRUE, json.FALSE:
			if token != next.String() {
				t.Errorf("Bad token %v: %s", next, token)
			}
		case json.JSONSTR:
			if !strings.HasPrefix(token, `"`) || !strings.HasSuffix(token, `"`) {
				t.Errorf("Bad string literal: %s", token)
			}
		}
		next = l.Next()
	}
}

func TestLexerExample(t *testing.T) {
	testLexer([]byte(jsonExample), t)
}

func BenchmarkLexer(b *testing.B) {
	l := new(json.Lexer)
	for i := 0; i < b.N; i++ {
		l.Init([]byte(jsonExample), PanicOnError)
		next := l.Next()
		for next != json.EOI {
			next = l.Next()
		}
	}
	b.SetBytes(int64(len(jsonExample)))
}
