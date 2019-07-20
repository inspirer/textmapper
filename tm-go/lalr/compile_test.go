package lalr

import (
	"bufio"
	"bytes"
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/util/container"
	"github.com/inspirer/textmapper/tm-go/util/diff"
)

var emptyTests = []struct {
	input string
	want  string
}{
	{`S -> AA; A -> aA; A -> b; C ->`, `C`},
	{`S -> AA; A -> aA; A -> b; A ->`, `SA`},
	{`S -> AA; A ->`, `SA`},
}

func TestEmpty(t *testing.T) {
	for _, tc := range emptyTests {
		g := parseGrammar(t, tc.input)
		c := &compiler{
			grammar: g,
			empty:   container.NewBitSet(len(g.Symbols)),
		}
		c.computeEmpty()
		var buf strings.Builder
		for i, sym := range g.Symbols {
			if c.empty.Get(i) {
				buf.WriteString(sym)
			}
		}
		if got := buf.String(); got != tc.want {
			t.Errorf("empty(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}

var stateTests = []struct {
	input string
	want  []string
}{
	{`S ->; S -> a`, []string{
		`0 (from 0, EOI, LA): a -> 1; S -> 2; reduce S [EOI];`, // resolved S/R
		`1 (from 0, a): S : a _;`,
		`2 (from 0, S): EOI -> 3;`,
		`3 (from 2, EOI):`,
	}},
	{`S -> A; A -> aa`, []string{
		`0 (from 0, EOI): a -> 1; S -> 4; A -> 2;`,
		`1 (from 0, a): A : a _ a; a -> 3;`,
		`2 (from 0, A): S : A _;`,
		`3 (from 1, a): A : a a _;`,
		`4 (from 0, S): EOI -> 5;`,
		`5 (from 4, EOI):`,
	}},
	{`S -> Sa; S -> a`, []string{
		`0 (from 0, EOI): a -> 1; S -> 2;`,
		`1 (from 0, a): S : a _;`,
		`2 (from 0, S): S : S _ a; EOI -> 4; a -> 3;`, // shift-shift is still LR0
		`3 (from 2, a): S : S a _;`,
		`4 (from 2, EOI):`,
	}},
	{`S -> A; S -> B; A -> ab; B -> ac`, []string{
		`0 (from 0, EOI): a -> 1; S -> 6; A -> 2; B -> 3;`,
		`1 (from 0, a): A : a _ b; B : a _ c; b -> 4; c -> 5;`,
		`2 (from 0, A): S : A _;`,
		`3 (from 0, B): S : B _;`,
		`4 (from 1, b): A : a b _;`,
		`5 (from 1, c): B : a c _;`,
		`6 (from 0, S): EOI -> 7;`,
		`7 (from 6, EOI):`,
	}},
	{`S -> SA; S -> A; A -> a; A -> b`, []string{
		`0 (from 0, EOI): a -> 1; b -> 2; S -> 3; A -> 4;`,
		`1 (from 0, a): A : a _;`,
		`2 (from 0, b): A : b _;`,
		`3 (from 0, S): S : S _ A; EOI -> 6; a -> 1; b -> 2; A -> 5;`,
		`4 (from 0, A): S : A _;`,
		`5 (from 3, A): S : S A _;`,
		`6 (from 3, EOI):`,
	}},
	{`S -> SA; S -> ; A -> B; B -> Ca; B -> CAp; C -> c; C ->`, []string{
		`0 (from 0, EOI, LA): S -> 1; reduce S [EOI,a,c];`, // TODO no 'a'?
		`1 (from 0, S, LA): S : S _ A; EOI -> 9; c -> 2; A -> 3; B -> 4; C -> 5; reduce C [a,c];`,
		`2 (from 1, c): C : c _;`,
		`3 (from 1, A): S : S A _;`,
		`4 (from 1, B): A : B _;`,
		`5 (from 1, C, LA): B : C _ a; B : C _ A p; a -> 6; c -> 2; A -> 7; B -> 4; C -> 5; reduce C [a,c];`, // shift-reduce conflict
		`6 (from 5, a): B : C a _;`,
		`7 (from 5, A): B : C A _ p; p -> 8;`,
		`8 (from 7, p): B : C A p _;`,
		`9 (from 1, EOI):`,
	}},
	{`S -> A; A -> Bb; B -> Aa; B ->`, []string{
		`0 (from 0, EOI, LA): S -> 5; A -> 1; B -> 2; reduce B [b];`,
		`1 (from 0, A, LA): S : A _; B : A _ a; a -> 3; reduce S [EOI];`,
		`2 (from 0, B): A : B _ b; b -> 4;`,
		`3 (from 1, a): B : A a _;`,
		`4 (from 2, b): A : B b _;`,
		`5 (from 0, S): EOI -> 6;`,
		`6 (from 5, EOI):`,
	}},

	// No-eoi
	{`S -> N; N -> xNy; N -> a; N -> b`, []string{
		`0 (from 0, EOI): x -> 2; a -> 3; b -> 4; S -> 8; N -> 5;`,
		`1 (from 0, EOI): x -> 2; a -> 3; b -> 4; N -> 10;`,
		`2 (from 0, x): N : x _ N y; x -> 2; a -> 3; b -> 4; N -> 6;`,
		`3 (from 0, a): N : a _;`,
		`4 (from 0, b): N : b _;`,
		`5 (from 0, N): S : N _;`,
		`6 (from 2, N): N : x N _ y; y -> 7;`,
		`7 (from 6, y): N : x N y _;`,
		`8 (from 0, S): EOI -> 9;`,
		`9 (from 8, EOI):`,
		`10 (from 1, N):`,
	}},

	// Simple lookahead.
	{`S -> Ca; S -> Bb; C -> a; B -> a`, []string{
		`0 (from 0, EOI): a -> 1; S -> 6; C -> 2; B -> 3;`,
		`1 (from 0, a, LA): C : a _; B : a _; reduce C [a]; reduce B [b];`,
		`2 (from 0, C): S : C _ a; a -> 4;`,
		`3 (from 0, B): S : B _ b; b -> 5;`,
		`4 (from 2, a): S : C a _;`,
		`5 (from 3, b): S : B b _;`,
		`6 (from 0, S): EOI -> 7;`,
		`7 (from 6, EOI):`,
	}},
}

func TestStates(t *testing.T) {
	for _, tc := range stateTests {
		g := parseGrammar(t, tc.input)
		c := &compiler{
			grammar: g,
			out:     &Tables{},
			empty:   container.NewBitSet(len(g.Symbols)),
		}
		c.init()
		c.computeEmpty()
		c.computeSets()
		c.computeStates()

		c.initLalr()
		c.buildFollow()
		c.buildLA()

		var buf strings.Builder
		for i, state := range c.states {
			var suffix string
			if !state.lr0 {
				suffix = ", LA"
			}
			fmt.Fprintf(&buf, "%v (from %v, %v%v):", state.index, state.sourceState, g.Symbols[state.symbol], suffix)
			for _, item := range state.core {
				buf.WriteByte(' ')
				c.writeItem(item, &buf)
				buf.WriteByte(';')
			}
			for _, target := range state.shifts {
				state := c.states[target]
				fmt.Fprintf(&buf, " %v -> %v;", c.grammar.Symbols[state.symbol], state.index)
			}
			if i < len(g.Inputs) || !state.lr0 {
				for i, rule := range state.reduce {
					fmt.Fprintf(&buf, " reduce %v", c.grammar.Symbols[g.Rules[rule].LHS])
					if !state.lr0 {
						la := state.la[i].Slice(nil)
						if len(la) > 0 {
							buf.WriteString(" [")
							for i, term := range la {
								if i > 0 {
									buf.WriteByte(',')
								}
								buf.WriteString(c.grammar.Symbols[term])
							}
							buf.WriteString("]")
						}
					}
					buf.WriteByte(';')
				}
			}
			buf.WriteByte('\n')
		}
		got := strings.TrimRight(buf.String(), "\n")
		if diff := diff.LineDiff(strings.Join(tc.want, "\n"), got); diff != "" {
			t.Errorf("states(%v) output differs from the expected one.\n--- want\n+++ got\n%v\n", tc.input, diff)
		}
	}
}

var ruleRE = regexp.MustCompile(`^([A-Z])\s+->(?:\s+([a-zA-Z]*))?$`)

func parseGrammar(t *testing.T, input string) *Grammar {
	index := make(map[rune]Sym)
	var symbols []string
	index[0] = EOI
	symbols = append(symbols, "EOI")
	sym := func(r rune) Sym {
		if _, ok := index[r]; !ok {
			index[r] = Sym(len(index))
			symbols = append(symbols, fmt.Sprintf("%c", r))
		}
		return index[r]
	}
	for _, c := range input {
		if c >= 'a' && c <= 'z' {
			sym(c)
		}
	}

	scanner := bufio.NewScanner(bytes.NewReader([]byte(strings.Replace(input, ";", "\n", -1))))
	ret := &Grammar{
		Terminals: len(index),
		Inputs:    []Input{{Nonterminal: sym('S'), Eoi: true}},
	}
	var action int
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if match := ruleRE.FindStringSubmatch(line); match != nil {
			rule := Rule{
				LHS:        sym(rune(match[1][0])),
				Action:     action,
				OriginName: match[1],
			}
			action++
			for _, r := range match[2] {
				rule.RHS = append(rule.RHS, sym(r))
			}
			ret.Rules = append(ret.Rules, rule)
			continue
		}
		t.Fatalf("cannot parse `%v`", line)
	}
	if sym, ok := index['N']; ok {
		ret.Inputs = append(ret.Inputs, Input{Nonterminal: sym, Eoi: false})
	}
	ret.Symbols = symbols
	return ret
}
