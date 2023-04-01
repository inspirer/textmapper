// Package gen generates code for compiled grammars.
package gen

import (
	"fmt"
	"os"
	"strings"
	"text/template"
	"time"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
}

var languages = map[string]*language{
	"go": golang,
}

// Generate generates code for a grammar.
func Generate(g *grammar.Grammar, w Writer, compat bool) error {
	lang, ok := languages[g.TargetLang]
	if !ok {
		return fmt.Errorf("unsupported language: %s", g.TargetLang)
	}

	templates := lang.templates(g)
	base, err := template.New("main").Funcs(funcMap).Parse(sharedDefs)
	if err != nil {
		return err
	}
	for _, f := range templates {
		tmpl, err := template.Must(base.Clone()).Parse(f.template)
		if err != nil {
			return err
		}

		// TODO come up with a way to parse this once
		_, err = tmpl.Parse(g.CustomTemplates)
		if err != nil {
			return err
		}

		var buf strings.Builder
		err = tmpl.Execute(&buf, g)
		if err != nil {
			return err
		}
		src := Format(f.name, ExtractImports(buf.String()), compat)
		if err := w.Write(f.name, src); err != nil {
			return err
		}
	}
	return nil
}

type Stats struct {
	Compiling time.Duration
	Gen       time.Duration
	States    int
}

func (s Stats) String() string {
	var ret []string
	if s.Compiling > 0 {
		ret = append(ret, fmt.Sprintf("lalr: %v", s.Compiling.Round(time.Millisecond)))
	}
	if s.Gen > 0 {
		ret = append(ret, fmt.Sprintf("text: %v", s.Gen.Round(time.Millisecond)))
	}
	if s.States > 0 {
		ret = append(ret, fmt.Sprintf("parser: %v states", s.States))
	}
	return strings.Join(ret, ", ")
}

// GenerateFile reads, compiles, and generates code for a grammar stored in a file.
func GenerateFile(path string, w Writer, compat bool) (Stats, error) {
	var ret Stats
	content, err := os.ReadFile(path)
	if err != nil {
		return ret, err
	}

	tree, err := ast.Parse(path, string(content), tm.StopOnFirstError)
	if err != nil {
		return ret, err
	}

	start := time.Now()
	g, err := grammar.Compile(ast.File{Node: tree.Root()}, compat)
	ret.Compiling = time.Since(start)
	if err != nil {
		return ret, err
	}

	if g.Parser != nil && g.Parser.Tables != nil {
		ret.States = g.Parser.Tables.NumStates
	}
	if g.TargetLang == "" {
		// A source-only grammar.
		return ret, fmt.Errorf("no target language")
	}

	start = time.Now()
	err = Generate(g, w, compat)
	ret.Gen = time.Since(start)
	return ret, err
}
