package lex

import (
	"bytes"
	"fmt"
	"sort"
	"strings"
	"testing"
	"unicode"
)

type input struct {
	text       string
	wantAction int
}

var lexTests = []struct {
	rules     []*Rule
	scanBytes bool // if true, regexes must use {#bytes} as a prefix
	want      []string
	testOn    []input
}{
	{
		rules: []*Rule{
			rule("a", `a`, 1),
		},
		want: []string{
			`0: [a] -> 1;`,
			`1: EOI exec 1; other exec 1; [a] exec 1;`,
		},
		testOn: []input{
			{`«a»aaa`, 1},
			{`«a»`, 1},
			{`«»`, 0 /*EOI as invalid token*/},
			{`«»bb`, 0 /*Invalid token*/},
		},
	},
	{
		rules: []*Rule{
			rule("a", `[a-z]+`, 1),
		},
		want: []string{
			`0: [a-z] -> 1;`,
			`1: EOI exec 1; other exec 1; [a-z] -> 1;`,
		},
		testOn: []input{
			{`«axe»`, 1},
			{`«a»  foo`, 1},
		},
	},
	{
		rules: []*Rule{
			rule("a", `[ \t]+`, 0),
			rule("b", `[a-zA-Z_][a-zA-Z_0-9]*`, 2),
		},
		want: []string{
			`0: [A-Z_a-z] -> 1; [\t ] -> 2;`,
			`1: EOI exec 2; other exec 2; [\t ] exec 2; [0-9A-Z_a-z] -> 1;`,
			`2: [\t ] -> 2;`,
		},
		testOn: []input{
			{`«axe9_» `, 2},
			{"«  \t »foo", 0},
		},
	},
	{
		rules: []*Rule{
			rule("a", `(abcd?)`, 1),
			rule("b", `ab`, 2),
		},
		want: []string{
			`0: [a] -> 1;`,
			`1: [b] -> 2;`,
			`2: EOI exec 2; other exec 2; [a-bd] exec 2; [c] -> 3;`,
			`3: EOI exec 1; other exec 1; [a-c] exec 1; [d] -> 4;`,
			`4: EOI exec 1; other exec 1; [a-d] exec 1;`,
		},
		testOn: []input{
			{`«ab»d `, 2},
			{"«abc» d", 1},
			{"«abcd»  ", 1},
		},
	},
	{
		// Simple backtracking.
		rules: []*Rule{
			rule("a", `aaaa`, 1),
			rule("b", `a`, 2),
		},
		want: []string{
			`0: [a] -> 1;`,
			`1: EOI exec 2; other exec 2; [a] bt(exec 2) -> 2;`,
			`2: [a] -> 3;`,
			`3: [a] -> 4;`,
			`4: EOI exec 1; other exec 1; [a] exec 1;`,
		},
		testOn: []input{
			{`«a» `, 2},
			{`«a»a `, 2},
			{`«a»aa `, 2},
			{`«aaaa»a `, 1},
		},
	},
	{
		// Precedence resolution.
		rules: []*Rule{
			rule("a", `keyword`, 1),
			rule("b", `[a-z]+`, 2, -1),
			rule("c", `[a-zA-Z]+`, 3, -2),
		},
		testOn: []input{
			{`«abc» def`, 2},
			{`«keywor» def`, 2},
			{`«keyword» def`, 1},
			{`«keyworddef»!`, 2},
			{`«keywordDef»!`, 3},
		},
	},
	{
		// Numbers.
		rules: []*Rule{
			rule("a", `-?(0|[1-9][0-9]*)`, 1),
		},
		testOn: []input{
			{`«-100» `, 1},
			{`«-0»1 `, 1},
			{`«-0» `, 1},
			{`«-»-0 `, 0 /*Invalid token*/},
		},
	},
	{
		// Advanced backtracking.
		rules: []*Rule{
			rule("a", `[a-z](-*[a-z])*`, 1),
			rule("b", `test(foo)?-+>`, 2),
		},
		testOn: []input{
			{`«abc» `, 1},
			{`«abc»- `, 1},
			{`«abc-d» `, 1},
			{`«testfoo»--- `, 1},
			{`«testfoo--->» `, 2},
		},
	},
	{
		// + should reuse states.
		rules: []*Rule{
			rule("a", `ab|a(bc+)?`, 1),
		},
		want: []string{
			`0: [a] -> 1;`,
			`1: EOI exec 1; other exec 1; [ac] exec 1; [b] -> 2;`,
			`2: EOI exec 1; other exec 1; [a-b] exec 1; [c] -> 2;`,
		},
		testOn: []input{
			{`«a»- `, 1},
			{`«ab»b `, 1},
			{`«abc»- `, 1},
			{`«abccc»- `, 1},
		},
	},

	// Scanning Unicode as bytes (including backtracking).
	{
		scanBytes: true,
		rules: []*Rule{
			rule("alpha-beta", "{#bytes}αβγ?", 1),
		},
		want: []string{
			`0: [\u00ce] -> 1;`,
			`1: [\u00b1] -> 2;`,
			`2: [\u00ce] -> 3;`,
			`3: [\u00b2] -> 4;`,
			`4: EOI exec 1; other exec 1; [\u00b1-\u00b3] exec 1; [\u00ce] bt(exec 1) -> 5;`,
			`5: [\u00b3] -> 6;`,
			`6: EOI exec 1; other exec 1; [\u00b1-\u00b3\u00ce] exec 1;`,
		},
		testOn: []input{
			{`«αβ»- `, 1},
			{`«αβ»`, 1},
			{`«αβγ»`, 1},
			{"«\u03B1\u03B2»", 1},
			{"«\u03B1\u03B2\u03B3»!", 1},
		},
	},
	{
		scanBytes: true,
		rules: []*Rule{
			rule("alpha-beta", "{#bytes}\u03B1\\u03B2\u03B3?", 1), // αβγ
			rule("eoi", "{eoi}", 8),
		},
		want: []string{
			`0: EOI -> 7; [\u00ce] -> 1;`,
			`1: [\u00b1] -> 2;`,
			`2: [\u00ce] -> 3;`,
			`3: [\u00b2] -> 4;`,
			`4: EOI exec 1; other exec 1; [\u00b1-\u00b3] exec 1; [\u00ce] bt(exec 1) -> 5;`,
			`5: [\u00b3] -> 6;`,
			`6: EOI exec 1; other exec 1; [\u00b1-\u00b3\u00ce] exec 1;`,
			`7: EOI exec 8; other exec 8; [\u00b1-\u00b3\u00ce] exec 8;`,
		},
		testOn: []input{
			{`«αβ»`, 1},
			{`«αβγ»`, 1},
		},
	},
}

func TestLex(t *testing.T) {
	repl := strings.NewReplacer("«", "", "»", "")
	for _, tc := range lexTests {
		tables, err := Compile(tc.rules, tc.scanBytes, true /*allowBacktracking*/)
		if err != nil {
			t.Errorf("Compile(%v) failed with %v", tc.rules, err)
		}

		if len(tc.want) > 0 {
			want := strings.Join(tc.want, "\n")
			if got := dumpTables(tables); got != want {
				t.Errorf("DFA =\n%v\nwant:\n%v", got, want)
			}
		}

		for _, inp := range tc.testOn {
			text := repl.Replace(inp.text)
			size, act := tables.Scan(0, text)
			got := fmt.Sprintf("«%v»%v", text[:size], text[size:])
			if got != inp.text {
				t.Errorf("Scan(%v).Token = %v, want: %v", text, got, inp.text)
			}
			if act != inp.wantAction {
				t.Errorf("Scan(%v).Action = %v, want: %v", inp.text, act, inp.wantAction)
			}
		}
	}
}

func dumpTables(tables *Tables) string {
	cs := make([]charset, tables.NumSymbols)
	for i, re := range tables.SymbolMap {
		end := unicode.MaxRune
		if i+1 < len(tables.SymbolMap) {
			end = tables.SymbolMap[i+1].Start - 1
		}
		cs[re.Target] = append(cs[re.Target], re.Start, end)
	}

	actionStart := -1 - len(tables.Backtrack)
	actionText := func(i int) string {
		switch {
		case i >= 0:
			return fmt.Sprintf("-> %v", i)
		case i > actionStart:
			bt := tables.Backtrack[-1-i]
			return fmt.Sprintf("bt(exec %v) -> %v", bt.Action, bt.NextState)
		case i == actionStart:
			return "error"
		default:
			return fmt.Sprintf("exec %v", actionStart-i)
		}
	}

	var buf bytes.Buffer
	numStates := len(tables.Dfa) / tables.NumSymbols
	for state := 0; state < numStates; state++ {
		offset := state * tables.NumSymbols
		fmt.Fprintf(&buf, "%v:", state)
		if tables.Dfa[offset] != actionStart {
			fmt.Fprintf(&buf, " EOI %v;", actionText(tables.Dfa[offset]))
		}
		if tables.Dfa[offset+1] != actionStart {
			fmt.Fprintf(&buf, " other %v;", actionText(tables.Dfa[offset+1]))
		}
		transitions := make(map[int][]rune)
		for i := 2; i < tables.NumSymbols; i++ {
			act := tables.Dfa[offset+i]
			transitions[act] = append(transitions[act], cs[i]...)
		}
		var targets []int
		for t := range transitions {
			targets = append(targets, t)
		}
		sort.Ints(targets)
		for _, t := range targets {
			if t == actionStart {
				continue
			}
			cs := newCharset(transitions[t]).String()
			fmt.Fprintf(&buf, " [%v] %v;", cs, actionText(t))
		}
		buf.WriteRune('\n')
	}
	return strings.TrimSuffix(buf.String(), "\n")
}

func TestSymbolArr(t *testing.T) {
	var tests = []struct {
		input   []RangeEntry
		maxRune rune
		want    string
	}{
		{[]RangeEntry{{0, 1}}, 0, "[]"},
		{[]RangeEntry{{0, 1}}, 1, "[]"},
		{[]RangeEntry{{0, 1}, {1, 2}, {3, 4}}, 0, "[1 2 2]"},
		{[]RangeEntry{{0, 1}, {1, 2}, {3, 4}}, 2, "[1 2]"},
		{[]RangeEntry{{0, 7}, {5, 3}}, 0, "[7 7 7 7 7]"},
		{[]RangeEntry{{0, 7}, {5, 3}}, 4, "[7 7 7 7]"},
		{[]RangeEntry{{0, 7}, {5, 3}}, 1, "[7]"},
	}
	for _, tc := range tests {
		tables := Tables{SymbolMap: tc.input}
		arr := tables.SymbolArr(tc.maxRune)
		if got := fmt.Sprintf("%v", arr); got != tc.want {
			t.Errorf("SymbolArr(%v, %v) = %v, want: %v", tc.input, tc.maxRune, got, tc.want)
		}
	}
}

func TestCompressedMap(t *testing.T) {
	var tests = []struct {
		input []RangeEntry
		start rune
		want  string
	}{
		{[]RangeEntry{{0, 7}, {5, 100}, {200, 1}}, 0, "[[0,200]=[7 7 7 7 7],default=100]"},
		{[]RangeEntry{{0, 7}, {5, 100}, {200, 1}}, 4, "[[4,200]=[7],default=100]"},
		{[]RangeEntry{{0, 7}, {5, 3}, {200, 1}, {220, 2}, {240, 1}},
			0, "[[0,200]=[7 7 7 7 7],default=3 [220,240]=[],default=2]"},
		{[]RangeEntry{{0, 7}, {5, 3}, {10, 1}, {220, 2}, {240, 1}},
			0, "[[0,10]=[7 7 7 7 7],default=3 [220,240]=[],default=2]"},
		{[]RangeEntry{{0, 7}, {5, 3}, {200, 1}, {220, 2}, {240, 1}},
			200, "[[220,240]=[],default=2]"},
		{[]RangeEntry{{0, 1}, {1, 2}, {1000, 3}, {1001, 2}},
			200, "[[1000,1001]=[],default=3]"},
	}
	for _, tc := range tests {
		tables := Tables{SymbolMap: tc.input}
		m := tables.CompressedMap(tc.start)
		if got := fmt.Sprintf("%v", m); got != tc.want {
			t.Errorf("CompressedMap(%v, %v) = %v, want: %v", tc.input, tc.start, got, tc.want)
		}
	}
}
