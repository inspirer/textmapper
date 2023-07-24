package lalr

import (
	"math"

	"github.com/inspirer/textmapper/status"
)

// Sym represents all terminal and non-terminal symbols in a grammar.
// Negative values are reserved for state markers.
type Sym int

// EOI is an "end of stream" terminal.
const EOI Sym = 0

// IsStateMarker returns true for state marker symbols.
func (s Sym) IsStateMarker() bool {
	return s < 0
}

// AsMarker returns the index of a state marker
func (s Sym) AsMarker() int {
	return -1 - int(s)
}

// Marker returns a new state marker symbol.
func Marker(index int) Sym {
	return Sym(-1 - index)
}

// Input describes a start nonterminal in a grammar.
type Input struct {
	Nonterminal Sym

	// This field indicates whether the generated parser can rely on the EOI symbol presence.
	Eoi bool
}

// Rule is a grammar production rule.
type Rule struct {
	LHS        Sym
	RHS        []Sym
	Precedence Sym
	Action     int
	Type       int // index in Parser.RangeTypes; default node for the rule; -1 when unset
	Flags      []string
	Origin     status.SourceNode
	OriginName string
}

// Associativity decides on grouping of repeated operators.
type Associativity uint8

func (a Associativity) String() string {
	return assocStr[a]
}

// Available associativities.
const (
	Left     Associativity = iota // (x . y) . z
	Right                         // x . (y . z)
	NonAssoc                      // treat `x . y . z` as a syntax error
)

var assocStr = [...]string{"left", "right", "nonassoc"}

// Precedence declares one or more terminals as operators with the same associativity
// and precedence.
type Precedence struct {
	Associativity
	Terminals []Sym
}

// Lookahead is a special kind of nonterminal that accepts an empty string, but is able to survive
// reduce/reduce conflicts. When two or more lookahead nonterminals can be reduced in the same
// state, their predicates are used to determine which one should be reduced.
type Lookahead struct {
	Nonterminal Sym
	Predicates  []Predicate
	Origin      status.SourceNode
}

// Accepts checks if the lookahead accepts a given input.
func (l Lookahead) Accepts(input int32) (negated, ok bool) {
	for _, p := range l.Predicates {
		if p.Input == input {
			return p.Negated, true
		}
	}
	return false, false
}

// Grammar is an input to the parser generator.
type Grammar struct {
	Inputs     []Input
	Rules      []Rule
	Terminals  int
	Symbols    []string
	Precedence []Precedence // later declarations have higher precedence
	Lookaheads []Lookahead  // Note: each lookahead nonterminal should have an empty rule in Rules
	Markers    []string
	ExpectSR   int
	ExpectRR   int
	Origin     status.SourceNode
}

// Tables holds generated parser tables.
type Tables struct {
	*DefaultEnc
	Optimized *DisplacementEnc

	RuleLen     []int
	FinalStates []int
	RuleSymbol  []int
	Markers     []StateMarker
	Lookaheads  []LookaheadRule
	NumStates   int
}

// DefaultEnc is a compact representation of the parser tables.
type DefaultEnc struct {
	Action []int // -2: error, -1: shift, >= 0: rule to reduce, < -2: lookahead (-3-index in Lalr)
	Lalr   []int // array of pairs: (LA terminal -> action), each sequence ends with LA=-1
	Goto   []int // sym -> index in FromTo
	FromTo []int // array of state pairs (from, to)
}

func (enc *DefaultEnc) gotoState(state, symbol int) int {
	min := enc.Goto[symbol]
	max := enc.Goto[symbol+1]

	if max-min < 32 {
		for i := min; i < max; i += 2 {
			if enc.FromTo[i] == state {
				return enc.FromTo[i+1]
			}
		}
	} else {
		for min < max {
			e := (min + max) >> 1 &^ int(1)
			i := enc.FromTo[e]
			if i == state {
				return enc.FromTo[e+1]
			} else if i < state {
				min = e + 2
			} else {
				max = e
			}
		}
	}
	return -1
}

func bytesPerElement(arr []int) int {
	ret := 1
	for _, i := range arr {
		if i < math.MinInt8 || i > math.MaxInt8 {
			if i < math.MinInt16 || i > math.MaxInt16 {
				return 4
			}
			ret = 2
		}
	}
	return ret
}

func (t *Tables) SizeBytes() int {
	var ret int
	if opt := t.Optimized; opt != nil {
		ret += len(opt.Action) * 4
		ret += len(opt.DefAct) * 4
		ret += len(opt.Goto) * 4
		ret += len(opt.DefGoto) * 4
		ret += len(opt.Check) * bytesPerElement(opt.Check)
		ret += len(opt.Table) * bytesPerElement(opt.Table)
	} else {
		ret += len(t.Action) * 4
		ret += len(t.Lalr) * 4
		ret += len(t.Goto) * 4
		ret += len(t.FromTo) * bytesPerElement(t.FromTo)
	}

	ret += len(t.RuleLen) * bytesPerElement(t.RuleLen)
	ret += len(t.RuleSymbol) * 4
	return ret
}

func (t *Tables) mark(state, marker int) {
	states := t.Markers[marker].States
	if len(states) > 0 && states[len(states)-1] == state {
		return // ignore duplicates
	}
	t.Markers[marker].States = append(states, state)
}

// StateMarker contains the list of actual states behind a given marker.
type StateMarker struct {
	Name   string
	States []int
}

// LookaheadRule solves a grammar conflict by performing a series of lookaheads. Such rules get
// triggered when two or more lookahead nonterminals can be reduced and the parser needs to decide
// which of them to reduce in order to proceed.
type LookaheadRule struct {
	Cases         []LookaheadCase
	DefaultTarget Sym
}

// LookaheadCase describes a single lookahead attempt.
type LookaheadCase struct {
	Predicate
	Target Sym // an empty lookahead nonterminal helping guide the parser into an unambiguous state.
}

// Predicate is a lookahead expression that evaluates to true or false on the remaining input.
type Predicate struct {
	Input   int32
	Negated bool
}
