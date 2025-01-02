package grammar

import (
	"fmt"
	"math"
	"strings"

	"github.com/inspirer/textmapper/util/debug"
)

// TableStats returns a string with statistics about the generated lexer tables.
func (l *Lexer) TableStats() string {
	var b strings.Builder

	t := l.Tables
	if t == nil {
		return "No tables\n"
	}

	if t.ScanBytes {
		fmt.Fprintf(&b, "Lexer (bytes):\n")
	} else {
		fmt.Fprintf(&b, "Lexer (unicode):\n")
	}

	fmt.Fprintf(&b, "\t%v states, %v symbols, %v start conditions, %v backtracking checkpoints\n", len(t.Dfa)/t.NumSymbols, t.NumSymbols, len(t.StateMap), len(t.Backtrack))
	fmt.Fprintf(&b, "\tDFA = %s, Backtracking = %s, ", debug.Size(sizeBytes(t.Dfa)), debug.Size(len(t.Backtrack)*8))
	fmt.Fprintf(&b, "StateMap = %s, SymbolMap = %s\n", debug.Size(len(t.StateMap)*4), debug.Size(len(t.SymbolMap)*8))
	return b.String()
}

func (p *Parser) TableStats() string {
	var b strings.Builder

	t := p.Tables
	if t == nil {
		return "No tables\n"
	}

	fmt.Fprintf(&b, "LALR:\n\t%v terminals, %v nonterminals, %v rules, %v states, %v markers, %v lookaheads\n", p.NumTerminals, len(p.Nonterms), len(t.RuleLen), t.NumStates, len(t.Markers), len(t.Lookaheads))
	fmt.Fprintf(&b, "Action Table:\n\t%d x %d, expanded size = %s (%s in default encoding)\n", t.NumStates, p.NumTerminals, debug.Size(t.NumStates*p.NumTerminals*4), debug.Size((len(t.Action)+len(t.Lalr))*4))
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
	fmt.Fprintf(&b, "\t%.2v%% of the LALR table is reductions (%s)\n", float64(nonZero*100)/float64(total), debug.Size(nonZero*4))

	syms := p.NumTerminals + len(p.Nonterms)
	fmt.Fprintf(&b, "Goto Table:\n\t%d x %d, expanded size = %s (%s in default encoding)\n", t.NumStates, syms, debug.Size(t.NumStates*syms*4), debug.Size(len(t.Goto)*4+sizeBytes(t.FromTo)))

	nonZero = len(t.FromTo) / 2
	total = t.NumStates * syms
	fmt.Fprintf(&b, "\t%.2v%% of the GOTO table is populated (%s), %v transitions\n", float64(nonZero*100)/float64(total), debug.Size(nonZero*4), nonZero)

	return b.String()
}

func sizeBytes(arr []int) int {
	v := 1
	for _, i := range arr {
		if i < math.MinInt8 || i > math.MaxInt8 {
			if i < math.MinInt16 || i > math.MaxInt16 {
				v = 4
				break
			}
			v = 2
		}
	}
	return len(arr) * v
}
