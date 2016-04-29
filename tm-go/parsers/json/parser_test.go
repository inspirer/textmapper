package json_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/json"
)

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
