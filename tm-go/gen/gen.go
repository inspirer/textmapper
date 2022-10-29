// Package gen generates code for compiled grammars.
package gen

import (
	"fmt"
	"os"
	"strings"
	"text/template"

	"github.com/inspirer/textmapper/tm-go/grammar"
	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
}

// Generate generates code for a grammar.
func Generate(g *grammar.Grammar, w Writer) error {
	var templates []file
	templates = append(templates, lexerFiles...)
	if g.Parser.Tables != nil {
		templates = append(templates, parserFiles...)
	}
	if g.Parser.Types != nil {
		templates = append(templates, listenerFile)
	}
	if g.Options.WriteBison {
		templates = append(templates, file{name: g.Name + ".y", template: bisonTpl})
	}

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
		src := Format(f.name, ExtractImports(buf.String()))
		if err := w.Write(f.name, src); err != nil {
			return err
		}
	}
	return nil
}

// GenerateFile reads, compiles, and generates code for a grammar stored in a file.
func GenerateFile(path string, w Writer, compat bool) error {
	content, err := os.ReadFile(path)
	if err != nil {
		return err
	}

	tree, err := ast.Parse(path, string(content), tm.StopOnFirstError)
	if err != nil {
		return err
	}

	g, err := grammar.Compile(ast.File{Node: tree.Root()}, compat)
	if err != nil {
		return err
	}

	if g.TargetLang == "" {
		// A source-only grammar.
		return fmt.Errorf("no target language")
	}

	return Generate(g, w)
}
