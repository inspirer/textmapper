// Package grammar contains the model of a compiled Textmapper grammar ready for code generation.
package grammar

import (
	"fmt"
	"sort"
	"strconv"

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
	FlexID    int  // Flex token ID (in flex-mode only)
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
	UnionDefinition string
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
 	NtName   string // empty for mid-rule actions
}

// ActionVars captures enough information about a production rule to interpret its semantic action.
type ActionVars struct {
	syntax.CmdArgs

	// position -> type of the references of the original rule.
	//
	// Note: types are indexed by position rather than index to support getting types of references
	// that are not present in the current expansion of the rule.
	Types   map[int]string
	LHSType string

	// Not every symbol reference is present in the desugared rule.
	//
	// position -> index (position on the stack relative to the first RHS symbol)
	Remap map[int]int

	// Number of RHS symbols in the expanded rule (i.e. rule length up to the current command).
	SymRefCount int
}

// Reference is a symbol reference in a semantic action.
type Reference struct {
	// Position of the reference in the original rule. 1-based. Used to identify the symbol in
	// semantic actions code blocks.
	Pos int

	// Index of the symbol in the expanded rule. 0-based. Used to locate the symbol in the
	// parsing stack.
	//
	// -1 means that the reference is present in the original rule but not in this expansion.
	Index int
}

// Resolve resolves a symbol reference. `val` can either be a 0-based index (e.g. "0" in "$0")
// or a named symbol (e.g. "a" in "$a").
//
// Returns an error if `val` is not a valid reference in the original rule, e.g. using "$a" in
// "start: b".
//
// Returns a Reference with Index -1 if `val` is a valid symbol in the original rule but does not
// show up in the expanded rule. For example, a: b? expands into two rules:
//
//		a: b
//	  | %empty
//
// For the %empty rule, Resolve("b") returns -1.
func (a *ActionVars) Resolve(val string, origin status.SourceNode) (Reference, error) {
	return a.resolve(val, true /*zeroBased*/, -1, val, origin)
}

// ResolveOneBased is similar to Resolve, except that `val` is 1-based if it is a number.
func (a *ActionVars) ResolveOneBased(val string, maxRuleSizeForOrdinalRef int, name string, origin status.SourceNode) (Reference, error) {
	return a.resolve(val, false /*zeroBased*/, maxRuleSizeForOrdinalRef, name, origin)
}

func (a *ActionVars) resolve(val string, zeroBased bool, maxRuleSizeForOrdinalRef int, name string, origin status.SourceNode) (Reference, error) {
	// `pos` is always 1-based.
	pos, err := strconv.Atoi(val)
	if err == nil {
		if maxRuleSizeForOrdinalRef >= 0 && a.SymRefCount > maxRuleSizeForOrdinalRef {
			return Reference{}, status.Errorf(origin, "invalid reference %q. Ordinal references disabled for rules with more than %v symbols, use the symbol alias instead", name, maxRuleSizeForOrdinalRef)
		}
		// The input "val" is a number reference, e.g. $1.
		if zeroBased {
			// The input reference starts from 0, e.g. $0 references the first symbol. Change it to
			// 1-based.
			pos++
		}
		if pos < 1 || pos >= a.CmdArgs.MaxPos {
			// Index out of range.
			if zeroBased {
				return Reference{}, status.Errorf(origin, "index %v is out of range [0, %v]", pos, a.CmdArgs.MaxPos-1)
			}
			return Reference{}, status.Errorf(origin, "index %v is out of range [1, %v]", pos, a.CmdArgs.MaxPos)
		}
	} else {
		// The input "val" is a named symbol reference, e.g. $a.
		var exists bool
		pos, exists = a.CmdArgs.Names[val]
		if !exists {
			// No such a symbol exists in the original rule.
			return Reference{}, status.Errorf(origin, "invalid reference %q. Cannot find symbol %q in rule", name, val)
		}
	}
	idx, exists := a.Remap[pos]
	if !exists {
		idx = -1
	}
	return Reference{Index: idx, Pos: pos}, nil
}

// String is used as a digest of a semantic action environment (and also as a debug string).
func (a *ActionVars) String() string {
	if a == nil {
		return "nil"
	}
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
	return fmt.Sprintf("{#%v %v %#v->%v d=%v}", a.MaxPos-1, ret, a.Types, a.LHSType, a.Delta)
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
	RuleToken       []int    // maps actions into tokens; empty if the mapping is 1:1
	UsedFlags       []string // list of used flags inside mapped tokens
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
	MappedTokens []syntax.RangeToken
	Rules        []*Rule
	Tables       *lalr.Tables
	Actions      []SemanticAction
	UsedFlags    []string
	Types        *syntax.Types
	IsRecovering bool
	ErrorSymbol  int
	NumTerminals int
}

// Options carries grammar generation parameters.
type Options struct {
	Copyright  bool
	CustomImpl []string

	// Lexer features.
	ScanBytes         bool // generate a 8-bit scanner that consumes bytes instead of runes
	CaseInsensitive   bool // generate a case-insensitive scanner
	TokenLine         bool // true by default
	TokenLineOffset   bool
	TokenColumn       bool
	NonBacktracking   bool
	FlexMode          bool // assume that the lexer is implemented using Flex (C/C++ only)
	SkipByteOrderMark bool // true by default

	// Parser features.
	GenParser              bool // true by default
	Cancellable            bool // Go-specific.
	RecursiveLookaheads    bool
	DebugParser            bool
	WriteBison             bool // Output the expanded grammar in a Bison-like format.
	OptimizeTables         bool
	DefaultReduce          bool   // Prefer some common reduction to errors in non-LR0 states to compress tables even further.
	NoEmptyRules           bool   // Report empty rules without an %empty marker. True by default for C++.
	MaxLookahead           int    // If set, all lookaheads expressions will be validated to fit this limit.
	OptInstantiationSuffix string // Suffix that triggers auto-instantiation optional nonterminals (e.g. "opt" or "_opt").

	DisableSyntax  []string // Lists grammar syntaxes that should be disabled.
	ExpansionLimit int      // Error if a rule expansion produces more than this many rules.
	ExpansionWarn  int      // Print warning if a rule expansion produces more than this many rules.

	// AST generation. Go-specific for now.
	TokenStream   bool
	EventBased    bool
	GenSelector   bool
	EventFields   bool
	EventAST      bool
	FixWhitespace bool
	ExtraTypes    []syntax.ExtraType
	FileNode      string // The top-level node gets the byte range of the whole input.
	NodePrefix    string // Prefix for node types.

	// Go.
	Package          string
	CancellableFetch bool // only in Cancellable parsers

	// C++
	Namespace                string
	IncludeGuardPrefix       string
	FilenamePrefix           string
	AbslIncludePrefix        string   // "absl" by default
	DirIncludePrefix         string   // for generated headers
	ParseParams              []string // parser fields initialized in the constructor
	VariantStackEntry        bool     // whether to generate a std::variant stackEntry rather than a union. Default false.
	TrackReduces             bool     // whether to track reduced states since most recent shift for error message generation.
	MaxRuleSizeForOrdinalRef int      // The number of rhs symbols after which ordinal references are disabled in semantic actions.
}
