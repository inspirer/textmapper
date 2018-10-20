package grammar

import (
	"github.com/inspirer/textmapper/tm-go/lex"
	"github.com/inspirer/textmapper/tm-go/status"
)

// Names of common terminals with predefined meaning.
const (
	Eoi = "eoi"
	Error = "error"
	InvalidToken = "invalid_token"
)

// Symbol is a grammar symbol.
type Symbol struct {
	Index   int
	ID      string // unique identifier to be used in generated code
	Name    string
	Comment string
	Type    string
	Origin  status.SourceNode
}

// PrettyType returns a user-friendly representation of the symbol type.
func (sym *Symbol) PrettyType() string {
	if sym.Type != "" {
		return sym.Type
	}
	return "<no type>"
}

// Grammar is a fully resolved and compiled Textmapper grammar.
type Grammar struct {
	Syms      []Symbol
	NumTokens int
	*Lexer
}

// Tokens returns all lexical tokens defined in the grammar.
func (g *Grammar) Tokens() []Symbol {
	return g.Syms[:g.NumTokens]
}

// SemanticAction is a piece of code that will be executed upon some event.
type SemanticAction struct {
	Index  int
	Code   string
	Origin status.SourceNode
}

// ClassAction resolves class terminals into more specific tokens (such as keywords).
type ClassAction struct {
	Index  int
	Custom map[string]int // maps constant terminals back into actions
}

// Lexer is a model of a generated lexer.
type Lexer struct {
	StartConditions []string
	Tables          *lex.Tables
	ClassActions    []ClassAction
	Actions         []SemanticAction
	InvalidToken    int
	RuleToken       []int // maps actions into tokens; empty if the mapping is 1:1
}
