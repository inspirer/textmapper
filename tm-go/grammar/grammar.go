package grammar

import (
	"fmt"
	"log"
	"sort"
	"strconv"
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
	Index     int
	ID        string // unique identifier to be used in generated code
	Name      string
	Comment   string
	Type      string
	Space     bool // tokens that should be ignored by the parser.
	CanBeNull bool // the 'error' token and nullable nonterminals can match an empty string
	Origin    status.SourceNode
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

// TokensWithoutPrec returns all lexical tokens defined in the grammar that don't participate in
// precedence resolution. This method facilitates grammar conversion into Bison-like syntax.
func (g *Grammar) TokensWithoutPrec() []Symbol {
	var ret []Symbol
	seen := make(map[int]bool)
	for _, prec := range g.Prec {
		for _, term := range prec.Terminals {
			seen[int(term)] = true
		}
	}
	for _, sym := range g.Syms[:g.NumTokens] {
		if !seen[sym.Index] {
			ret = append(ret, sym)
		}
	}
	return ret
}

// ReportTokens returns a list of tokens that need to be injected into the AST.
func (g *Grammar) ReportTokens(space bool) []Symbol {
	var ret []Symbol
	for _, t := range g.Options.ReportTokens {
		isSpace := g.Syms[t].Space || g.Syms[t].Name == "invalid_token"
		if isSpace == space {
			ret = append(ret, g.Syms[t])
		}
	}
	return ret
}

func (g *Grammar) ReportsInvalidToken() bool {
	for _, t := range g.Options.ReportTokens {
		if g.Syms[t].Name == "invalid_token" {
			return true
		}
	}
	return false
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

// ExprString returns a user-friendly rendering of a given rule.
func (g *Grammar) ExprString(e *syntax.Expr) string {
	switch e.Kind {
	case syntax.Empty:
		return "%empty"
	case syntax.Prec:
		return g.ExprString(e.Sub[0]) + " %prec " + g.Syms[e.Symbol].ID
	case syntax.Assign, syntax.Append, syntax.Arrow:
		return g.ExprString(e.Sub[0])
	case syntax.Sequence:
		var buf strings.Builder
		for _, sub := range e.Sub {
			inner := g.ExprString(sub)
			if inner == "" || inner == "%empty" {
				continue
			}
			if buf.Len() > 0 {
				buf.WriteByte(' ')
			}
			buf.WriteString(inner)
		}
		if buf.Len() == 0 {
			return "%empty"
		}
		return buf.String()
	case syntax.Reference:
		return e.String()
	case syntax.StateMarker:
		return "/*." + e.Name + "*/"
	case syntax.Command:
		return ""
	case syntax.Lookahead:
		// TODO process lookaheads before this method gets called
		return "(?= ...)"
	default:
		log.Fatalf("cannot stringify kind=%v", e.Kind)
		return ""
	}
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
	if r.Precedence > 0 {
		sb.WriteString(" %prec ")
		sb.WriteString(g.Syms[r.Precedence].Name)
	}
	return sb.String()
}

func (g *Grammar) NontermID(nonterm int) string {
	return g.Syms[g.NumTokens+nonterm].ID
}

func (g *Grammar) NeedsSession() bool {
	return len(g.Parser.Tables.Lookaheads) > 0 && (g.Options.RecursiveLookaheads || g.Options.Cancellable)
}

func (g *Grammar) HasTrailingNulls(r lalr.Rule) bool {
	for i := len(r.RHS) - 1; i >= 0; i-- {
		sym := r.RHS[i]
		if sym.IsStateMarker() {
			continue
		}
		return g.Syms[sym].CanBeNull
	}
	return false
}

// Range marks the portion of a rule that needs to be reported.
type Range struct {
	Start int
	End   int // exclusive
	Type  int // index in Parser.RangeTypes
	Flags []string
}

// SemanticAction is a piece of code that will be executed upon some event.
type SemanticAction struct {
	Action   int
	Code     string
	Space    bool // this is a space token
	Comments []string
	Report   []Range // left to right, inner first
	Vars     *ActionVars
	Origin   status.SourceNode
}

// ActionVars captures enough information about a production rule to interpret its semantic action.
type ActionVars struct {
	syntax.CmdArgs

	// Types of the references of the rule.
	Types   []string
	LHSType string

	// Not every symbol reference is present in the desugared rule.
	Remap map[int]int
}

// Resolve resolves "val" to an RHS index for the current rule.
func (a ActionVars) Resolve(val string) (int, bool) {
	pos, ok := a.CmdArgs.Names[val]
	if !ok {
		var err error
		pos, err = strconv.Atoi(val)
		if err != nil {
			return 0, false
		}
		pos++ // "val" is 0-based, while positions are 1-based.
		if pos < 1 || pos >= a.CmdArgs.MaxPos {
			// Index out of range.
			return 0, false
		}
	}
	ret, ok := a.Remap[pos]
	if !ok {
		ret = -1
	}
	return ret, true
}

// String is used in test failure messages.
func (a ActionVars) String() string {
	var ret []string
	for k, pos := range a.CmdArgs.Names {
		v, ok := a.Remap[pos]
		if !ok {
			v = -1
		}
		ret = append(ret, fmt.Sprintf("%v:%v", k, v))
	}
	for k, v := range a.Remap {
		ret = append(ret, fmt.Sprintf("%v:%v", k, v))
	}
	sort.Strings(ret)
	return fmt.Sprintf("{#%v %v %#v->%v}", a.MaxPos-1, ret, a.Types, a.LHSType)
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
	Inputs       []syntax.Input
	Nonterms     []*syntax.Nonterm
	Prec         []lalr.Precedence // TODO remove since this is a lalr input
	Rules        []lalr.Rule
	Tables       *lalr.Tables
	Actions      []SemanticAction
	UsesFlags    bool
	Types        *syntax.Types
	MappedTokens []syntax.RangeToken
	IsRecovering bool
	ErrorSymbol  int
}

func (p *Parser) HasAssocValues() bool {
	for _, nt := range p.Nonterms {
		if nt.Type != "" {
			return true
		}
	}
	return false
}

func (p *Parser) HasInputAssocValues() bool {
	for _, inp := range p.Inputs {
		if p.Nonterms[inp.Nonterm].Type != "" {
			return true
		}
	}
	return false
}

func (p *Parser) HasMultipleUserInputs() bool {
	var count int
	for _, inp := range p.Inputs {
		if inp.Synthetic {
			continue
		}
		count++
	}
	return count > 1
}

func (p *Parser) HasActions() bool {
	for _, r := range p.Rules {
		if r.Action > 0 {
			act := p.Actions[r.Action]
			if len(act.Report) > 0 || act.Code != "" {
				return true
			}
		}
	}
	return len(p.Tables.Lookaheads) > 0
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
	EventBased    bool
	GenSelector   bool
	EventFields   bool
	EventAST      bool
	FixWhitespace bool
	WriteBison    bool  // Output the expanded grammar in a Bison-like format.
	ReportTokens  []int // Tokens that should appear in the AST.
	ExtraTypes    []string
	FileNode      string // The top-level node gets the byte range of the whole input.
}
