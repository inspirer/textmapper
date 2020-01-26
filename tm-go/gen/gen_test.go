package gen_test

import (
	"github.com/inspirer/textmapper/tm-go/gen"
	"github.com/inspirer/textmapper/tm-go/util/diff"
	"io/ioutil"
	"path/filepath"
	"strings"
	"testing"
)

var grammars = []string{
	"../parsers/json/json.tm",
	"../parsers/simple/simple.tm",
	"../parsers/test/test.tm",
	"../../tm-parsers/tm/textmapper.tm",
	"../../tm-parsers/js/js.tm",
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
			err := gen.GenerateFile(filename, w, true /*compat*/)
			if err != nil {
				t.Errorf("failed with %v", err)
				return
			}

			for genfile, content := range w {
				if strings.HasSuffix(genfile, ".y") {
					// TODO compare final grammars
					continue
				}

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
