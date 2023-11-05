package gen_test

import (
	"context"
	"os"
	"path/filepath"
	"testing"

	"github.com/inspirer/textmapper/gen"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/diff"
)

var grammars = []string{
	"../parsers/json/json.tm",
	"../parsers/simple/simple.tm",
	"../parsers/test/test.tm",
	"../parsers/tm/textmapper.tm",
	"../parsers/js/js.tm",
}

type mapWriter struct {
	files   []string
	content map[string]string
}

func (w *mapWriter) Write(filename, content string) error {
	w.files = append(w.files, filename)
	w.content[filename] = content
	return nil
}

func TestGenerate(t *testing.T) {
	ctx := context.Background()
	for _, filename := range grammars {
		filename := filename
		t.Run(filename, func(t *testing.T) {
			w := &mapWriter{content: make(map[string]string)}
			_, err := gen.GenerateFile(ctx, filename, w, gen.Options{})
			if err != nil {
				s := status.FromError(err)
				s.Sort()
				for _, err := range s {
					t.Errorf("GenerateFile() failure: %v", err)
				}
				return
			}

			for _, genfile := range w.files {
				content := w.content[genfile]
				p := filepath.Join(filepath.Dir(filename), genfile)
				ondisk, err := os.ReadFile(p)
				if err != nil {
					t.Errorf("ReadFile(%v) failure: %v", genfile, err)
					continue
				}
				t.Logf("comparing %v", p)
				if diff := diff.LineDiff(string(ondisk), content); diff != "" {
					t.Errorf("The on-disk content differs from the generated one.\n--- %v\n+++ %v (generated)\n%v", p, genfile, diff)
				}
			}
		})
	}
}
