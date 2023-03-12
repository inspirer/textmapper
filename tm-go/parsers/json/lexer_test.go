package json_test

import (
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/json"
	"github.com/inspirer/textmapper/tm-go/parsers/json/token"
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
					"Acronym": {},
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

func testLexer(input string, t *testing.T) {
	l := new(json.Lexer)
	l.Init(input)
	spacesRE := regexp.MustCompile(`^\s+$`)

	next := l.Next()
	var offset int
	for next != token.EOI {
		s, e := l.Pos()
		if s > offset && !spacesRE.MatchString(input[offset:s]) {
			t.Errorf("Spaces expected: %s", input[offset:s])
		}
		offset = e
		tok := input[s:e]
		switch next {
		case token.LBRACE, token.RBRACE, token.LBRACK, token.RBRACK, token.COLON, token.COMMA, token.NULL, token.TRUE, token.FALSE:
			if tok != next.String() {
				t.Errorf("Bad token %v: %s", next, tok)
			}
		case token.JSONSTRING:
			if !strings.HasPrefix(tok, `"`) || !strings.HasSuffix(tok, `"`) {
				t.Errorf("Bad string literal: %s", tok)
			}
		}
		next = l.Next()
	}
}

func TestLexerExample(t *testing.T) {
	testLexer(jsonExample, t)
}

func BenchmarkLexer(b *testing.B) {
	l := new(json.Lexer)
	for i := 0; i < b.N; i++ {
		l.Init(jsonExample)
		next := l.Next()
		for next != token.EOI {
			next = l.Next()
		}
	}
	b.SetBytes(int64(len(jsonExample)))
}
