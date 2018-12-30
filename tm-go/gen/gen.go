// Package gen generates code for compiled grammars.
package gen

import (
	"bytes"
	"github.com/inspirer/textmapper/tm-go/grammar"
	"text/template"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
}

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
