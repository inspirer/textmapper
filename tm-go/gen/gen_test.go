package gen_test

import (
	"fmt"
	"github.com/inspirer/textmapper/tm-go/gen"
	"github.com/inspirer/textmapper/tm-go/grammar"
	"github.com/inspirer/textmapper/tm-go/util/diff"
	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"io/ioutil"
	"path/filepath"
	"testing"
)

var grammars = []string{
	"../parsers/json/json.tm",
}

type mapWriter map[string]string

func (w mapWriter) Write(filename, content string) error {
	w[filename] = content
	return nil
}

func TestGenerate(t *testing.T) {
	for _, filename := range grammars {
		filename := filename
		t.Run(filename, func(t *testing.T) {
			w := make(mapWriter)
			err := generate(filename, w)
			if err != nil {
				t.Errorf("failed with %v", err)
				return
			}

			for genfile, content := range w {
				p := filepath.Join(filepath.Dir(filename), genfile)
				ondisk, err := ioutil.ReadFile(p)
				if err != nil {
					t.Errorf("ReadFile(%v) failed with %v", genfile, err)
					continue
				}
				if diff := diff.LineDiff(string(ondisk), content); diff != "" {
					t.Errorf("The on-disk content differs from the generated one.\n--- %v\n+++ %v (generated)\n%v", p, genfile, diff)
				}
			}
		})
	}
}

func generate(path string, w gen.Writer) error {
	content, err := ioutil.ReadFile(path)
	if err != nil {
		return err
	}

	tree, err := ast.Parse(path, string(content), tm.StopOnFirstError)
	if err != nil {
		return err
	}

	g, err := grammar.Compile(ast.File{Node: tree.Root()})
	if err != nil {
		return err
	}

	if g.TargetLang == "" {
		// A source-only grammar.
		return fmt.Errorf("no target language")
	}

	return gen.Generate(g, w)
}
