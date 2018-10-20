// Package lex implements a lexer generator.
package lex

import (
	"fmt"

	"github.com/inspirer/textmapper/tm-go/status"
)

// Rule is a lexer generator rule.
type Rule struct {
	RE              *Regexp
	StartConditions []int // An empty list is equivalent to the initial start condition (== 0).
	Precedence      int   // Precedence disambiguates between rules that match the same prefix.
	Action          int
	Origin          status.SourceNode
	OriginName      string
}

// Sym is a DFA input symbol, which represents zero or more runes indistinguishable by the
// generated lexer.
type Sym int

// EOI is an end of stream indicator.
const EOI Sym = 0

// Initial is the name of the default lexer start condition.
const Initial = "initial"

// RangeEntry translates a segment of runes into a DFA input symbol.
type RangeEntry struct {
	Start  rune // inclusive
	Target Sym
}

func (re RangeEntry) String() string {
	return fmt.Sprintf("%v=>%v", charset{re.Start, re.Start}.String(), re.Target)
}

// Tables holds generated lexer tables.
type Tables struct {
	// SymbolMap translates runes into DFA symbols. This slice is sorted by "Start" and covers all
	// unicode runes.
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

// Resolver retrieves named regular expressions.
type Resolver interface {
	Resolve(name string) *Regexp
}

// Compile combines a set of rules into a lexer.
func Compile(rules []*Rule, resolver Resolver) (*Tables, error) {
	var s status.Status
	var index []int
	var maxSC int
	c := newCompiler(resolver)
	for _, r := range rules {
		i, err := c.addRegexp(r.RE, r.Action, r)
		if err != nil {
			s.Add(r.Origin.SourceRange(), err.Error())
		}
		for _, sc := range r.StartConditions {
			if sc > maxSC {
				maxSC = sc
			}
		}
		index = append(index, i)
	}
	ins, inputMap := c.compile()

	var maxSym Sym
	for _, re := range inputMap {
		if re.Target > maxSym {
			maxSym = re.Target
		}
	}
	numSymbols := int(maxSym) + 1

	startStates := make([][]int, maxSC+1)
	for i, r := range rules {
		if len(r.StartConditions) == 0 {
			startStates[0] = append(startStates[0], index[i])
		}
		for _, sc := range r.StartConditions {
			startStates[sc] = append(startStates[sc], index[i])
		}
	}

	g := newGenerator(ins, numSymbols)
	stateMap := make([]int, maxSC+1)
	for i, states := range startStates {
		stateMap[i] = g.addState(states)
	}
	dfa, backtrack, err := g.generate()
	s.AddError(err)

	ret := &Tables{
		SymbolMap:  inputMap,
		NumSymbols: numSymbols,
		StateMap:   stateMap,
		Dfa:        dfa,
		Backtrack:  backtrack,
	}
	return ret, s.Err()
}
