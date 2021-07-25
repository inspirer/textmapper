package grammar

import (
	"fmt"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/tm-go/lalr"
	"github.com/inspirer/textmapper/tm-go/lex"
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/syntax"
)

// Names of common terminals with predefined meaning.
const (
	Eoi          = "eoi"
	Error        = "error"
	InvalidToken = "invalid_token"
)

// Symbol is a grammar symbol.
type Symbol struct {
	Index   int
	ID      string // unique identifier to be used in generated code
	Name    string
	Comment string
	Type    string
	Space   bool // tokens that should be ignored by the parser.
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
	Name       string // lowercase
	TargetLang string
	Syms       []Symbol
	Sets       []*NamedSet
	NumTokens  int
	*Lexer
	*Parser
	*Options
	CustomTemplates string
}

// Tokens returns all lexical tokens defined in the grammar.
func (g *Grammar) Tokens() []Symbol {
	return g.Syms[:g.NumTokens]
}

// SpaceActions returns a sorted list of space-only actions.
func (g *Grammar) SpaceActions() []int {
	var ret []int
	for _, a := range g.Lexer.Actions {
		if a.Space {
			ret = append(ret, a.Action)
		}
	}
	sort.Ints(ret)
	return ret
}

// RuleString returns a user-friendly rendering of a given rule.
func (g *Grammar) RuleString(r *lalr.Rule) string {
	var sb strings.Builder
	fmt.Fprintf(&sb, "%v :", g.Syms[r.LHS].Name)
	for _, sym := range r.RHS {
		sb.WriteByte(' ')
		if sym.IsStateMarker() {
			sb.WriteByte('.')
			sb.WriteString(g.Parser.Tables.Markers[sym.AsMarker()].Name)
			continue
		}
		sb.WriteString(g.Syms[sym].Name)
	}
	return sb.String()
}

// SemanticAction is a piece of code that will be executed upon some event.
type SemanticAction struct {
	Action   int
	Code     string
	Space    bool // this is a space token
	Comments []string
	Origin   status.SourceNode
}

// ClassAction resolves class terminals into more specific tokens (such as keywords).
type ClassAction struct {
	Action int
	Custom map[string]int // maps constant terminals back into actions
}

// NamedSet is a named terminal set fully resolved for the grammar.
type NamedSet struct {
	Name      string
	Terminals []int
	Expr      string // original expression
}

// ValueString returns a comma-separated list of terminal IDs.
func (s *NamedSet) ValueString(g *Grammar) string {
	var ret []string
	for _, t := range s.Terminals {
		ret = append(ret, g.Syms[t].ID)
	}
	return strings.Join(ret, ", ")
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

// Parser is a model of a generated parser.
type Parser struct {
	Inputs   []syntax.Input
	Nonterms []*syntax.Nonterm
	Prec     []lalr.Precedence // TODO remove
	Rules    []lalr.Rule
	Tables   *lalr.Tables
	Actions  []SemanticAction
}

// Options carries grammar generation parameters.
type Options struct {
	Package   string
	Copyright bool

	// Parser features.
	TokenLine           bool // true by default
	TokenLineOffset     bool
	TokenColumn         bool
	NonBacktracking     bool
	Cancellable         bool
	RecursiveLookaheads bool
	DebugParser         bool

	// AST generation.
	EventBased   bool
	GenSelector  bool
	EventFields  bool
	EventAST     bool
	WriteBison   bool  // Output the expanded grammar in a Bison-like format.
	ReportTokens []int // Tokens that should appear in the AST.
	ExtraTypes   []string
	FileNode     string // The top-level node gets the byte range of the whole input.
}
