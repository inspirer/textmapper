// Package lex implements a lexer generator.
package lex

// Rule is a single lexer generator rule.
type Rule struct {
	RE              *Regexp
	StartConditions []int // An empty list is equivalent to the initial start condition (== 0).
	Precedence      int   // Precedence disambiguates when two rules match the same input prefix.
	Action          int
	Location        interface{}
}

// Tables holds generated lexer tables.
type Tables struct {
	// We map all Unicode runes into a smaller set of character classes, combining runes that are
	// indistinguishable by the DFA into the same class. The following two character classes have
	// special meaning:
	//   0 - EOI (no rune is mapped to this class)
	//   1 - all runes not explicitly mentioned in the grammar
	//
	// RuneMap covers only those runes that do not map to 1.
	RuneMap []int
	// The maximum value in the RuneMap slice plus one.
	NumClasses int
	// Maps start conditions into DFA states.
	StateMap []int
	// A matrix of NumClasses x NumStates. Positive values denote transitions to other states. Low
	// negative values [-1 .. -len(Backtrack)/2] are reserved for backtracking resolution. Other
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
