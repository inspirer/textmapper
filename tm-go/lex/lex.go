// Package lex implements a lexer generator.
package lex

import "fmt"

// Rule is a lexer generator rule.
type Rule struct {
	RE              *Regexp
	StartConditions []int // An empty list is equivalent to the initial start condition (== 0).
	Precedence      int   // Precedence disambiguates between rules that match the same prefix.
	Action          int
	Location        interface{}
}

// Sym is a DFA input symbol, which represents zero or more runes indistinguishable by the
// generated lexer.
type Sym int

// Predefined symbols.
const (
	EOI    Sym = 0 // end of stream indicator.
	Others Sym = 1 // all runes not explicitly mentioned in the grammar (can be empty).
)

// RangeEntry translates a segment of runes into a DFA input symbol.
type RangeEntry struct {
	Start, End rune // inclusive
	Target     Sym
}

func (re RangeEntry) String() string {
	return fmt.Sprintf("%v=>%v", charset{re.Start, re.End}.String(), re.Target)
}

// Tables holds generated lexer tables.
type Tables struct {
	// SymbolMap translates runes into DFA symbols. This slice is sorted by "Start" and covers all
	// unicode runes except those that map to "Others".
	SymbolMap []RangeEntry
	// The maximum value in SymbolMap.Target plus one.
	NumSymbols int
	// Maps start conditions into DFA states.
	StateMap []int
	// A matrix of NumSymbols x NumStates. Positive values denote transitions to other states. Low
	// negative values [-1 .. -len(Backtrack)/2] are reserved for backtracking rules. Other
	// negative values accept the current prefix, leaving the current character for the next token.
	Dfa []int
	// A flattened array of pairs [action to backtrack to, next state to try].
	Backtrack []int
}

// Compile combines a set of rules into a lexer.
func Compile(rules []Rule) (*Tables, error) {
	// TODO implement
	return nil, nil
}
