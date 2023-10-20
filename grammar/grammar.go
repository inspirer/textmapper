// Package grammar contains the model of a compiled Textmapper grammar ready for code generation.
package grammar

import (
	"fmt"
	"sort"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/lex"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
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

	Options *Options
	Lexer   *Lexer
	Parser  *Parser

	CustomTemplates string
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

// Lexer is a model of a generated lexer.
type Lexer struct {
	StartConditions []string
	Tables          *lex.Tables
	ClassActions    []ClassAction
	Actions         []SemanticAction
	InvalidToken    int
	RuleToken       []int               // maps actions into tokens; empty if the mapping is 1:1
	MappedTokens    []syntax.RangeToken // TODO move into Parser
	UsedFlags       []string            // list of used flags inside mapped tokens
}

// Rule is a parser rule with a semantic action.
type Rule struct {
	lalr.Rule
	Value *syntax.Expr // non-nil
}

// Parser is a model of a generated parser.
type Parser struct {
	Inputs       []syntax.Input
	Nonterms     []*syntax.Nonterm
	Prec         []lalr.Precedence // TODO remove since this is a lalr input
	Rules        []*Rule
	Tables       *lalr.Tables
	Actions      []SemanticAction
	UsedFlags    []string
	Types        *syntax.Types
	IsRecovering bool
	ErrorSymbol  int
	NumTerminals int
}

func (p *Parser) TableStats() string {
	var b strings.Builder

	t := p.Tables
	if t == nil {
		return "No tables\n"
	}

	fmt.Fprintf(&b, "LALR:\n\t%v terminals, %v nonterminals, %v rules, %v states, %v markers, %v lookaheads\n", p.NumTerminals, len(p.Nonterms), len(t.RuleLen), t.NumStates, len(t.Markers), len(t.Lookaheads))
	fmt.Fprintf(&b, "Action Table:\n\t%d x %d, expanded size = %.1f KB\n", t.NumStates, p.NumTerminals, float64(t.NumStates*p.NumTerminals*4)/1024.)
	var lr0, nonZero, total int
	for _, val := range t.Action {
		if val >= -2 {
			lr0++
			continue
		}
		total += p.NumTerminals
		for a := -3 - val; t.Lalr[a] >= 0; a += 2 {
			if t.Lalr[a+1] >= 0 {
				nonZero++
			}
		}
	}
	fmt.Fprintf(&b, "\tLR0 states: %v (%.2v%%)\n", lr0, float64(lr0*100)/float64(t.NumStates))
	fmt.Fprintf(&b, "\t%.2v%% of the LALR table is reductions (%.1f KB)\n", float64(nonZero*100)/float64(total), float64(nonZero*4)/1024.)

	syms := p.NumTerminals + len(p.Nonterms)
	fmt.Fprintf(&b, "Goto Table:\n\t%d x %d, expanded size = %.1f KB\n", t.NumStates, syms, float64(t.NumStates*syms*4)/float64(1024))

	nonZero = len(t.FromTo) / 2
	total = t.NumStates * syms
	fmt.Fprintf(&b, "\t%.2v%% of the GOTO table is populated (%.1f KB)\n", float64(nonZero*100)/float64(total), float64(nonZero*4)/1024.)

	return b.String()
}

// Options carries grammar generation parameters.
type Options struct {
	Copyright  bool
	CustomImpl []string

	// Lexer features.
	TokenLine       bool // true by default
	TokenLineOffset bool
	TokenColumn     bool
	NonBacktracking bool

	// Parser features.
	GenParser           bool // true by default
	Cancellable         bool // Go-specific.
	RecursiveLookaheads bool
	DebugParser         bool
	WriteBison          bool // Output the expanded grammar in a Bison-like format.
	OptimizeTables      bool
	DefaultReduce       bool // Prefer some common reduction to errors in non-LR0 states to compress tables even further.

	// AST generation. Go-specific for now.
	EventBased    bool
	GenSelector   bool
	EventFields   bool
	EventAST      bool
	FixWhitespace bool
	ReportTokens  []int // Tokens that should appear in the AST.
	ExtraTypes    []syntax.ExtraType
	FileNode      string // The top-level node gets the byte range of the whole input.
	NodePrefix    string // Prefix for node types.

	// Go.
	Package          string
	CancellableFetch bool // only in Cancellable parsers

	// C++
	Namespace          string
	IncludeGuardPrefix string
	FilenamePrefix     string
	AbslIncludePrefix  string   // "absl" by default
	DirIncludePrefix   string   // for generated headers
	ParseParams        []string // parser fields initialized in the constructor
}
