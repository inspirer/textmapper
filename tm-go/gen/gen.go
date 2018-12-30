// Package gen generates code for compiled grammars.
package gen

import (
	"bytes"
	"fmt"
	"github.com/inspirer/textmapper/tm-go/grammar"
	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-parsers/tm/ast"
	"io/ioutil"
	"text/template"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
}

// Generate generates code for a grammar.
func Generate(g *grammar.Grammar, w Writer) error {
	tmpl, err := template.New("tokenTpl").Funcs(funcMap).Parse(tokenTpl)
	if err != nil {
		return err
	}
	if _, err = tmpl.Parse(sharedDefs); err != nil {
		return err
	}
	var buf bytes.Buffer
	err = tmpl.Execute(&buf, g)
	if err != nil {
		return err
	}
	src := ExtractImports(buf.String())

	const filename = "token.go"
	ret, err := Format(filename, src)
	if err != nil {
		return err
	}

	return w.Write(filename, ret)
}

// GenerateFile reads, compiles, and generates code for a grammar stored in a file.
func GenerateFile(path string, w Writer) error {
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

	return Generate(g, w)
}
