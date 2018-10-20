package grammar_test

import (
	"io/ioutil"
	"path/filepath"
	"testing"

	"github.com/inspirer/textmapper/tm-go/grammar"
	"github.com/inspirer/textmapper/tm-go/parser"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
	"strings"
)

var testFiles = []string{
	`lexer.tmerr`,
}

func TestErrors(t *testing.T) {
	for _, file := range testFiles {
		content, err := ioutil.ReadFile(filepath.Join("testdata", file))
		if err != nil {
			t.Errorf("cannot read %v: %v", file, err)
			continue
		}

		inp := string(content)
		pt := parsertest.New(t, "error", inp)
		file, err := parser.Parse(file, pt.Source())
		if err != nil {
			t.Errorf("parsing failed with %v\n%v", err, inp)
			continue
		}

		var want []string
		for _, line := range strings.Split(inp, "\n") {
			const prefix = "# err: "
			if strings.HasPrefix(line, prefix) {
				want = append(want, line[len(prefix):])
			}
		}

		_, err = grammar.Compile(file)
		if err != nil {
			for _, e := range status.FromError(err) {
				pt.Consume(t, e.Origin.Offset, e.Origin.EndOffset)
				if len(want) == 0 {
					t.Errorf("unexpected error at line %v: %v", e.Origin.Line, e.Msg)
					continue
				}
				if want[0] != e.Msg {
					t.Errorf("unexpected error at line %v: %v, want: %v", e.Origin.Line, e.Msg, want[0])
				}
				want = want[1:]
			}
		}
		if len(want) != 0 {
			t.Errorf("not reported errors:\n%v", want)
		}
		pt.Done(t, nil)
	}
}
