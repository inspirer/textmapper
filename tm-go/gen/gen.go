// Package gen generates code for compiled grammars.
package gen

import (
	"github.com/inspirer/textmapper/tm-go/grammar"
)

// Writer provides a way to save generated files to disk.
type Writer interface {
	Write(filename, content string) error
}

func Generate(g *grammar.Grammar, w Writer) error {
	// TODO implement
	return nil
}
