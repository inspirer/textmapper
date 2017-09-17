package tm_test

import (
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/parsertest"
	"github.com/inspirer/textmapper/tm-parsers/tm"
)

var parseTests = []struct {
	nt     tm.NodeType
	inputs []string
}{

	{tm.Comment, []string{
		rule(` «# abc»
		  «# abc2»
		  a : abc ;    «# 8»
		  «# abc2»`),
	}},
	{tm.MultilineComment, []string{
		rule(`a void : «/* te ** / st */» ;`),
		rule(`«/* abc */» a:b;`),
	}},
	{tm.InvalidToken, []string{
		rule("a : «'»\n   ;"),
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
			test := parsertest.New(t, tc.nt.String(), input)
			l.Init(test.Source())
			errHandler := func(se tm.SyntaxError) bool {
				test.ConsumeError(t, se.Offset, se.Endoffset)
				return true
			}
			p.Init(errHandler, func(nt tm.NodeType, offset, endoffset int) {
				if nt == tc.nt {
					test.Consume(t, offset, endoffset)
				}
			})
			test.Done(t, p.ParseInput(l))
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

const wantTextmapperFiles = 29

func TestExistingFiles(t *testing.T) {
	var sources []string
	err := filepath.Walk("../..", func(path string, info os.FileInfo, err error) error {
		if info.IsDir() && info.Name() == "build" {
			return filepath.SkipDir
		}
		if !info.IsDir() && strings.HasSuffix(info.Name(), ".tm") {
			sources = append(sources, path)
		}
		return err
	})
	if err != nil {
		t.Errorf("cannot collect all Textmapper files: %v", err)
	}
	if len(sources) != wantTextmapperFiles {
		t.Errorf("found %v Textmapper files, want: %v", len(sources), wantTextmapperFiles)
	}

	for _, path := range sources {
		content, err := ioutil.ReadFile(path)
		if err != nil {
			t.Errorf("cannot read %v: %v", path, err)
			continue
		}

		l := new(tm.Lexer)
		l.Init(string(content))
		p := new(tm.Parser)
		errHandler := func(se tm.SyntaxError) bool { return false }
		p.Init(errHandler, func(nt tm.NodeType, offset, endoffset int) { /* noop */ })
		err = p.ParseInput(l)
		if err != nil {
			t.Errorf("%v: parser failed with %v", path, err)
		}
	}
}
