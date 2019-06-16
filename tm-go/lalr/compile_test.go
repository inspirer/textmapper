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
		`0 (from 0, EOI): a -> 1;`,
		`1 (from 0, a): S : a _;`,
	}},
	{`S -> A; A -> aa`, []string{
		`0 (from 0, EOI): a -> 1; A -> 2;`,
		`1 (from 0, a): A : a _ a; a -> 3;`,
		`2 (from 0, A): S : A _;`,
		`3 (from 1, a): A : a a _;`,
	}},
	{`S -> Sa; S -> a`, []string{
		`0 (from 0, EOI): a -> 1; S -> 2;`,
		`1 (from 0, a): S : a _;`,
		`2 (from 0, S): S : S _ a; a -> 3;`,
		`3 (from 2, a): S : S a _;`,
	}},
	{`S -> A; S -> B; A -> ab; B -> ac`, []string{
		`0 (from 0, EOI): a -> 1; A -> 2; B -> 3;`,
		`1 (from 0, a): A : a _ b; B : a _ c; b -> 4; c -> 5;`,
		`2 (from 0, A): S : A _;`,
		`3 (from 0, B): S : B _;`,
		`4 (from 1, b): A : a b _;`,
		`5 (from 1, c): B : a c _;`,
	}},
	{`S -> SA; S -> A; A -> a; A -> b`, []string{
		`0 (from 0, EOI): a -> 1; b -> 2; S -> 3; A -> 4;`,
		`1 (from 0, a): A : a _;`,
		`2 (from 0, b): A : b _;`,
		`3 (from 0, S): S : S _ A; a -> 1; b -> 2; A -> 5;`,
		`4 (from 0, A): S : A _;`,
		`5 (from 3, A): S : S A _;`,
	}},
	{`S -> SA; S -> ; A -> B; B -> Ca; B -> CAp; C -> c; C ->`, []string{
		`0 (from 0, EOI): S -> 1;`,
		`1 (from 0, S): S : S _ A; c -> 2; A -> 3; B -> 4; C -> 5;`,
		`2 (from 1, c): C : c _;`,
		`3 (from 1, A): S : S A _;`,
		`4 (from 1, B): A : B _;`,
		`5 (from 1, C): B : C _ a; B : C _ A p; a -> 6; c -> 2; A -> 7; B -> 4; C -> 5;`,
		`6 (from 5, a): B : C a _;`,
		`7 (from 5, A): B : C A _ p; p -> 8;`,
		`8 (from 7, p): B : C A p _;`,
	}},
}

func TestStates(t *testing.T) {
	for _, tc := range stateTests {
		g := parseGrammar(t, tc.input)
		c := &compiler{
			grammar: g,
			empty:   container.NewBitSet(len(g.Symbols)),
		}
		c.init()
		c.computeEmpty()
		c.computeSets()
		c.computeStates()

		var buf strings.Builder
		for _, state := range c.states {
			fmt.Fprintf(&buf, "%v (from %v, %v):", state.index, state.sourceState, g.Symbols[state.symbol])
			for _, item := range state.core {
				buf.WriteByte(' ')
				c.writeItem(item, &buf)
				buf.WriteByte(';')
			}
			for _, target := range state.shifts {
				state := c.states[target]
				fmt.Fprintf(&buf, " %v -> %v;", c.grammar.Symbols[state.symbol], state.index)
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
	ret.Symbols = symbols
	return ret
}
