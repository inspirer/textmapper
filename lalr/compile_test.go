package lalr

import (
	"bufio"
	"bytes"
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/diff"
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
		g, err := parseGrammar(tc.input)
		if err != nil {
			t.Errorf("parseGrammar(%v) failed with %v", tc.input, err)
			continue
		}
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
		`0 (LA): a -> 1; S -> 2; reduce S [EOI]; resolved: a->shift EOI->{S:};`,
		`1 (from 0, a): S: a _;`,
		`2 (from 0, S): EOI -> 3;`,
		`3 (from 2, EOI):`,
	}},
	{`S -> A; A -> aa`, []string{
		`0: a -> 1; S -> 4; A -> 2;`,
		`1 (from 0, a): A: a _ a; a -> 3;`,
		`2 (from 0, A): S: A _;`,
		`3 (from 1, a): A: a a _;`,
		`4 (from 0, S): EOI -> 5;`,
		`5 (from 4, EOI):`,
	}},
	{`S -> Sa; S -> a`, []string{
		`0: a -> 1; S -> 2;`,
		`1 (from 0, a): S: a _;`,
		`2 (from 0, S): S: S _ a; EOI -> 4; a -> 3;`, // shift-shift is still LR0
		`3 (from 2, a): S: S a _;`,
		`4 (from 2, EOI):`,
	}},
	{`S -> A; S -> B; A -> ab; B -> ac`, []string{
		`0: a -> 1; S -> 6; A -> 2; B -> 3;`,
		`1 (from 0, a): A: a _ b; B: a _ c; b -> 4; c -> 5;`,
		`2 (from 0, A): S: A _;`,
		`3 (from 0, B): S: B _;`,
		`4 (from 1, b): A: a b _;`,
		`5 (from 1, c): B: a c _;`,
		`6 (from 0, S): EOI -> 7;`,
		`7 (from 6, EOI):`,
	}},
	{`S -> SA; S -> A; A -> a; A -> b`, []string{
		`0: a -> 1; b -> 2; S -> 3; A -> 4;`,
		`1 (from 0, a): A: a _;`,
		`2 (from 0, b): A: b _;`,
		`3 (from 0, S): S: S _ A; EOI -> 6; a -> 1; b -> 2; A -> 5;`,
		`4 (from 0, A): S: A _;`,
		`5 (from 3, A): S: S A _;`,
		`6 (from 3, EOI):`,
	}},
	{`S -> SA; S -> ; A -> B; B -> Ca; B -> CAp; C -> c; C ->`, []string{
		`0: S -> 1; reduce S;`,
		`1 (from 0, S, LA): S: S _ A; EOI -> 9; c -> 2; A -> 3; B -> 4; C -> 5; reduce C [a,c]; resolved: EOI->shift c->shift a->{C:};`,
		`2 (from 1, c): C: c _;`,
		`3 (from 1, A): S: S A _;`,
		`4 (from 1, B): A: B _;`,
		`5 (from 1, C, LA): B: C _ a; B: C _ A p; a -> 6; c -> 2; A -> 7; B -> 4; C -> 5; reduce C [a,c]; resolved: a->shift c->shift;`,
		`6 (from 5, a): B: C a _;`,
		`7 (from 5, A): B: C A _ p; p -> 8;`,
		`8 (from 7, p): B: C A p _;`,
		`9 (from 1, EOI):`,

		// Both conflicts are resolved as shifts (see above).
		`input:6:0: input: S`,
		`shift/reduce conflict (next: c)`,
		`    C :`,
		``,
		`input:6:0: input: S C`,
		`shift/reduce conflict (next: a, c)`,
		`    C :`,
		``,
		`input:0:0: conflicts: 3 shift/reduce and 0 reduce/reduce`,
	}},
	{`S -> A; A -> Bb; B -> Aa; B ->`, []string{
		`0: S -> 5; A -> 1; B -> 2; reduce B;`,
		`1 (from 0, A, LA): S: A _; B: A _ a; a -> 3; reduce S [EOI]; resolved: a->shift EOI->{S: A};`,
		`2 (from 0, B): A: B _ b; b -> 4;`,
		`3 (from 1, a): B: A a _;`,
		`4 (from 2, b): A: B b _;`,
		`5 (from 0, S): EOI -> 6;`,
		`6 (from 5, EOI):`,
	}},

	// No-eoi (S - normal input; N - no-eoi)
	{`S -> N; N -> xNy; N -> a; N -> b`, []string{
		`0: x -> 2; a -> 3; b -> 4; S -> 8; N -> 5;`,
		`1: x -> 2; a -> 3; b -> 4; N -> 9;`,
		`2 (from 0, x): N: x _ N y; x -> 2; a -> 3; b -> 4; N -> 6;`,
		`3 (from 0, a): N: a _;`,
		`4 (from 0, b): N: b _;`,
		`5 (from 0, N): S: N _;`,
		`6 (from 2, N): N: x N _ y; y -> 7;`,
		`7 (from 6, y): N: x N y _;`,
		`8 (from 0, S): EOI -> 10;`,
		`9 (from 1, N):`,    // final state for N
		`10 (from 8, EOI):`, // final state for S
	}},

	// Simple lookahead.
	{`S -> Ca; S -> Bb; C -> a; B -> a`, []string{
		`0: a -> 1; S -> 6; C -> 2; B -> 3;`,
		`1 (from 0, a, LA): C: a _; B: a _; reduce C [a]; reduce B [b]; resolved: a->{C: a} b->{B: a};`,
		`2 (from 0, C): S: C _ a; a -> 4;`,
		`3 (from 0, B): S: B _ b; b -> 5;`,
		`4 (from 2, a): S: C a _;`,
		`5 (from 3, b): S: B b _;`,
		`6 (from 0, S): EOI -> 7;`,
		`7 (from 6, EOI):`,
	}},
	{`S -> A; S -> B; A -> a; A -> Aa; B -> ab`, []string{
		`0: a -> 1; S -> 6; A -> 2; B -> 3;`,
		`1 (from 0, a, LA): A: a _; B: a _ b; b -> 4; reduce A [EOI,a]; resolved: b->shift EOI->{A: a} a->{A: a};`,
		`2 (from 0, A, LA): S: A _; A: A _ a; a -> 5; reduce S [EOI]; resolved: a->shift EOI->{S: A};`,
		`3 (from 0, B): S: B _;`,
		`4 (from 1, b): B: a b _;`,
		`5 (from 2, a): A: A a _;`,
		`6 (from 0, S): EOI -> 7;`,
		`7 (from 6, EOI):`,
	}},

	// Several conflicting rules: we pick the first one.
	{`S -> A; S -> B; S -> C; A -> a; B -> a; C -> a %P b`, []string{
		`0: a -> 1; S -> 5; A -> 2; B -> 3; C -> 4;`,
		`1 (from 0, a, LA): A: a _; B: a _; C: a _; reduce A [EOI]; reduce B [EOI]; reduce C [EOI]; resolved: EOI->{A: a};`,
		`2 (from 0, A): S: A _;`,
		`3 (from 0, B): S: B _;`,
		`4 (from 0, C): S: C _;`,
		`5 (from 0, S): EOI -> 6;`,
		`6 (from 5, EOI):`,

		// Errors resolved as reduce first.
		// Note: all rules are reported as having problems.
		`input:4:0: input: a`,
		`reduce/reduce conflict (next: EOI)`,
		`    B : a`,
		`    A : a`,
		`    C : a %prec b`,
		``,
		`input:3:0: input: a`,
		`reduce/reduce conflict (next: EOI)`,
		`    B : a`,
		`    A : a`,
		`    C : a %prec b`,
		``,
		`input:5:0: input: a`,
		`reduce/reduce conflict (next: EOI)`,
		`    B : a`,
		`    A : a`,
		`    C : a %prec b`,
		``,
		`input:0:0: conflicts: 0 shift/reduce and 1 reduce/reduce`,
	}},

	// Associativity.
	{`S -> E; E -> c; E -> EaE; E -> EbE`, []string{
		`0: c -> 1; S -> 7; E -> 2;`,
		`1 (from 0, c): E: c _;`,
		`2 (from 0, E, LA): S: E _; E: E _ a E; E: E _ b E; a -> 3; b -> 4; reduce S [EOI]; resolved: a->shift b->shift EOI->{S: E};`,
		`3 (from 2, a): E: E a _ E; c -> 1; E -> 5;`,
		`4 (from 2, b): E: E b _ E; c -> 1; E -> 6;`,
		`5 (from 3, E, LA): E: E _ a E; E: E a E _; E: E _ b E; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->shift b->shift EOI->{E: E a E};`,
		`6 (from 4, E, LA): E: E _ a E; E: E _ b E; E: E b E _; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->shift b->shift EOI->{E: E b E};`,
		`7 (from 0, S): EOI -> 8;`,
		`8 (from 7, EOI):`,

		// Errors resolved as shifts.
		`input:2:0: input: E a E`,
		`shift/reduce conflict (next: a, b)`,
		`    E : E a E`,
		``,
		`input:3:0: input: E b E`,
		`shift/reduce conflict (next: a, b)`,
		`    E : E b E`,
		``,
		`input:0:0: conflicts: 4 shift/reduce and 0 reduce/reduce`,
	}},
	{`%L ab; S -> E; E -> c; E -> EaE; E -> EbE`, []string{
		`0: c -> 1; S -> 7; E -> 2;`,
		`1 (from 0, c): E: c _;`,
		`2 (from 0, E, LA): S: E _; E: E _ a E; E: E _ b E; a -> 3; b -> 4; reduce S [EOI]; resolved: a->shift b->shift EOI->{S: E};`,
		`3 (from 2, a): E: E a _ E; c -> 1; E -> 5;`,
		`4 (from 2, b): E: E b _ E; c -> 1; E -> 6;`,
		`5 (from 3, E, LA): E: E _ a E; E: E a E _; E: E _ b E; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->{E: E a E} b->{E: E a E} EOI->{E: E a E};`,
		`6 (from 4, E, LA): E: E _ a E; E: E _ b E; E: E b E _; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->{E: E b E} b->{E: E b E} EOI->{E: E b E};`,
		`7 (from 0, S): EOI -> 8;`,
		`8 (from 7, EOI):`,
	}},
	{`%NA ab; S -> E; E -> c; E -> EaE; E -> EbE`, []string{
		`0: c -> 1; S -> 7; E -> 2;`,
		`1 (from 0, c): E: c _;`,
		`2 (from 0, E, LA): S: E _; E: E _ a E; E: E _ b E; a -> 3; b -> 4; reduce S [EOI]; resolved: a->shift b->shift EOI->{S: E};`,
		`3 (from 2, a): E: E a _ E; c -> 1; E -> 5;`,
		`4 (from 2, b): E: E b _ E; c -> 1; E -> 6;`,
		`5 (from 3, E, LA): E: E _ a E; E: E a E _; E: E _ b E; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->err b->err EOI->{E: E a E};`,
		`6 (from 4, E, LA): E: E _ a E; E: E _ b E; E: E b E _; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->err b->err EOI->{E: E b E};`,
		`7 (from 0, S): EOI -> 8;`,
		`8 (from 7, EOI):`,
	}},
	{`%L a; %L b; S -> E; E -> c; E -> EaE; E -> EbE`, []string{
		`0: c -> 1; S -> 7; E -> 2;`,
		`1 (from 0, c): E: c _;`,
		`2 (from 0, E, LA): S: E _; E: E _ a E; E: E _ b E; a -> 3; b -> 4; reduce S [EOI]; resolved: a->shift b->shift EOI->{S: E};`,
		`3 (from 2, a): E: E a _ E; c -> 1; E -> 5;`,
		`4 (from 2, b): E: E b _ E; c -> 1; E -> 6;`,
		`5 (from 3, E, LA): E: E _ a E; E: E a E _; E: E _ b E; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->{E: E a E} b->shift EOI->{E: E a E};`,
		`6 (from 4, E, LA): E: E _ a E; E: E _ b E; E: E b E _; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->{E: E b E} b->{E: E b E} EOI->{E: E b E};`,
		`7 (from 0, S): EOI -> 8;`,
		`8 (from 7, EOI):`,
	}},
	{`%L a; %R b; S -> E; E -> c; E -> EaE; E -> EbE`, []string{
		`0: c -> 1; S -> 7; E -> 2;`,
		`1 (from 0, c): E: c _;`,
		`2 (from 0, E, LA): S: E _; E: E _ a E; E: E _ b E; a -> 3; b -> 4; reduce S [EOI]; resolved: a->shift b->shift EOI->{S: E};`,
		`3 (from 2, a): E: E a _ E; c -> 1; E -> 5;`,
		`4 (from 2, b): E: E b _ E; c -> 1; E -> 6;`,
		`5 (from 3, E, LA): E: E _ a E; E: E a E _; E: E _ b E; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->{E: E a E} b->shift EOI->{E: E a E};`,
		`6 (from 4, E, LA): E: E _ a E; E: E _ b E; E: E b E _; a -> 3; b -> 4; reduce E [EOI,a,b]; resolved: a->{E: E b E} b->shift EOI->{E: E b E};`,
		`7 (from 0, S): EOI -> 8;`,
		`8 (from 7, EOI):`,
	}},
	// Simulating unary minus via precedence.
	{`%L ab; %L c; %NA p; S -> E; E -> f; E -> EaE; E -> EbE; E -> EcE; E -> bE %P p`, []string{
		`0: b -> 1; f -> 2; S -> 11; E -> 3;`,
		`1 (from 0, b): E: b _ E; b -> 1; f -> 2; E -> 4;`,
		`2 (from 0, f): E: f _;`,
		`3 (from 0, E, LA): S: E _; E: E _ a E; E: E _ b E; E: E _ c E; a -> 5; b -> 6; c -> 7; reduce S [EOI]; resolved: a->shift b->shift c->shift EOI->{S: E};`,
		`4 (from 1, E, LA): E: E _ a E; E: E _ b E; E: E _ c E; E: b E _; a -> 5; b -> 6; c -> 7; reduce E [EOI,a,b,c]; resolved: a->{E: b E} b->{E: b E} c->{E: b E} EOI->{E: b E};`, // reduce E: bE on [a,b,c]
		`5 (from 3, a): E: E a _ E; b -> 1; f -> 2; E -> 8;`,
		`6 (from 3, b): E: E b _ E; b -> 1; f -> 2; E -> 9;`,
		`7 (from 3, c): E: E c _ E; b -> 1; f -> 2; E -> 10;`,
		`8 (from 5, E, LA): E: E _ a E; E: E a E _; E: E _ b E; E: E _ c E; a -> 5; b -> 6; c -> 7; reduce E [EOI,a,b,c]; resolved: a->{E: E a E} b->{E: E a E} c->shift EOI->{E: E a E};`,
		`9 (from 6, E, LA): E: E _ a E; E: E _ b E; E: E b E _; E: E _ c E; a -> 5; b -> 6; c -> 7; reduce E [EOI,a,b,c]; resolved: a->{E: E b E} b->{E: E b E} c->shift EOI->{E: E b E};`,
		`10 (from 7, E, LA): E: E _ a E; E: E _ b E; E: E _ c E; E: E c E _; a -> 5; b -> 6; c -> 7; reduce E [EOI,a,b,c]; resolved: a->{E: E c E} b->{E: E c E} c->{E: E c E} EOI->{E: E c E};`,
		`11 (from 0, S): EOI -> 12;`,
		`12 (from 11, EOI):`,
	}},
}

func TestStates(t *testing.T) {
	for _, tc := range stateTests {
		g, err := parseGrammar(tc.input)
		if err != nil {
			t.Errorf("parseGrammar(%v) failed with %v", tc.input, err)
			continue
		}
		c := &compiler{
			grammar: g,
			out: &Tables{
				DefaultEnc: &DefaultEnc{},
			},
			empty: container.NewBitSet(len(g.Symbols)),
		}
		c.init()
		c.computeEmpty()
		c.computeSets()
		c.computeStates()

		c.initLalr()
		c.buildFollow()
		c.buildLA()
		c.populateTables()
		c.reportConflicts()

		var buf strings.Builder
		for i, state := range c.states {
			if state.sourceState >= 0 {
				var suffix string
				if !state.lr0 {
					suffix = ", LA"
				}
				fmt.Fprintf(&buf, "%v (from %v, %v%v):", state.index, state.sourceState, g.Symbols[state.symbol], suffix)
			} else if !state.lr0 {
				fmt.Fprintf(&buf, "%v (LA):", state.index)
			} else {
				fmt.Fprintf(&buf, "%v:", state.index)
			}
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
			if !state.lr0 {
				action := c.out.Action[state.index]
				if action > -3 {
					t.Errorf("non-LR0 state %v requires disambiguation (in %q)", state.index, tc.input)
				}
				buf.WriteString(" resolved:")
				for i := -3 - action; c.out.Lalr[i] >= 0; i += 2 {
					buf.WriteString(" ")
					buf.WriteString(g.Symbols[c.out.Lalr[i]])
					buf.WriteString("->")
					switch action := c.out.Lalr[i+1]; {
					case action == -1:
						buf.WriteString("shift")
					case action == -2:
						buf.WriteString("err")
					case action >= 0:
						buf.WriteString("{")
						c.writeRule(action, &buf)
						buf.WriteString("}")
					default:
						// Internal error.
						buf.WriteString("FAILURE")
					}
				}
				buf.WriteString(";")
			}
			buf.WriteByte('\n')
		}
		for _, e := range c.s {
			buf.WriteString(e.Error())
			buf.WriteString("\n")
		}
		got := strings.TrimRight(buf.String(), "\n")
		if diff := diff.LineDiff(strings.Join(tc.want, "\n"), got); diff != "" {
			t.Errorf("states(%v) output differs from the expected one.\n--- want\n+++ got\n%v\n", tc.input, diff)
		}
	}
}

var ruleRE = regexp.MustCompile(`^([A-Z])\s+->(?:\s+([a-zA-Z]*))?(?:\s+%P\s+([a-z]))?$`)
var precRE = regexp.MustCompile(`^%(L|R|NA)\s+([a-z]+)$`)

type node int

func (n node) SourceRange() status.SourceRange {
	return status.SourceRange{Filename: "input", Line: int(n)}
}

func parseGrammar(input string) (*Grammar, error) {
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
		Origin:    node(0),
	}
	var action int
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if match := ruleRE.FindStringSubmatch(line); match != nil {
			rule := Rule{
				LHS:    sym(rune(match[1][0])),
				Action: action,
				Origin: node(action),
			}
			action++
			for _, r := range match[2] {
				rule.RHS = append(rule.RHS, sym(r))
			}
			for _, r := range match[3] {
				rule.Precedence = sym(r)
			}
			ret.Rules = append(ret.Rules, rule)
			continue
		}
		if match := precRE.FindStringSubmatch(line); match != nil {
			var term []Sym
			for _, r := range match[2] {
				term = append(term, sym(r))
			}
			switch match[1] {
			case "NA":
				ret.Precedence = append(ret.Precedence, Precedence{Associativity: NonAssoc, Terminals: term})
				continue
			case "L":
				ret.Precedence = append(ret.Precedence, Precedence{Associativity: Left, Terminals: term})
				continue
			case "R":
				ret.Precedence = append(ret.Precedence, Precedence{Associativity: Right, Terminals: term})
				continue
			}
		}
		return nil, fmt.Errorf("cannot parse `%v`", line)
	}
	if sym, ok := index['N']; ok {
		ret.Inputs = append(ret.Inputs, Input{Nonterminal: sym, Eoi: false})
	}
	ret.Symbols = symbols
	return ret, nil
}
