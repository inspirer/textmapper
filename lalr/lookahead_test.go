package lalr

import (
	"fmt"
	"strings"
	"testing"
)

func TestLookahead(t *testing.T) {
	var tests = []struct {
		input [][]int // the last int in each row is the outcome
		want  string
	}{
		{[][]int{
			{1, 7},
			{2, 8},
		}, "ERR: ambiguous order"},
		{[][]int{
			{1, 2, 7},
			{2, 1, 8},
		}, "ERR: inconsistent order"},
		{[][]int{
			{1, 7},
			{-1, 8},
		}, "1 -> 7, default -> 8"},
		{[][]int{
			{-1, 8},
			{1, 7},
		}, "1 -> 7, default -> 8"},
		{[][]int{
			{1, 7},
			{1, -2, 8},
		}, "ERR: cannot decide on the next lookahead"},
		{[][]int{
			{1, -2, 7},
			{1, -2, 8},
		}, "ERR: cannot decide on the next lookahead"},
		{[][]int{
			{1, 2, 7},
			{1, -2, 8},
		}, "2 -> 7, default -> 8"},
		{[][]int{
			{1, 2, 7},
			{-2, 8},
		}, "2 -> 7, default -> 8"},
	}

	for _, tc := range tests {
		var lookaheads []Lookahead
		for _, la := range tc.input {
			var preds []Predicate
			for _, p := range la[:len(la)-1] {
				var negated bool
				if p < 0 {
					p = -p
					negated = true
				}
				preds = append(preds, Predicate{Input: int32(p), Negated: negated})
			}
			lookaheads = append(lookaheads, Lookahead{Predicates: preds, Nonterminal: Sym(la[len(la)-1])})
		}
		rule, err := newLookaheadRule(lookaheads)
		if err != nil {
			if got := "ERR: " + err.Error(); got != tc.want {
				t.Errorf("newLookaheadRule(%v) failed with %v, want %v", tc.input, got, tc.want)
			}
			continue
		}
		if got := ruleString(rule); got != tc.want {
			t.Errorf("newLookaheadRule(%v) = %s, want %s", tc.input, got, tc.want)
		}
	}
}

func ruleString(rule LookaheadRule) string {
	var b strings.Builder
	for _, c := range rule.Cases {
		if c.Negated {
			b.WriteString("!")
		}
		fmt.Fprintf(&b, "%d -> %d, ", c.Input, c.Target)
	}
	fmt.Fprintf(&b, "default -> %d", rule.DefaultTarget)
	return b.String()
}
