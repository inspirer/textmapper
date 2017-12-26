package lex

import (
	"bytes"
	"fmt"
	"sort"
	"strings"
	"testing"
	"unicode"
)

var lexTests = []struct {
	rules []*Rule
	want  []string
}{
	{
		rules: []*Rule{
			{RE: MustParse(`a`), Action: 1},
		},
		want: []string{
			`0: EOI accept; [a] -> 1;`,
			`1: EOI exec 1; other exec 1; [a] exec 1;`,
		},
	},
	{
		rules: []*Rule{
			{RE: MustParse(`[a-z]+`), Action: 1},
		},
		want: []string{
			`0: EOI accept; [a-z] -> 1;`,
			`1: EOI exec 1; other exec 1; [a-z] -> 1;`,
		},
	},
	{
		rules: []*Rule{
			{RE: MustParse(`[ \t]+`), Action: 0},
			{RE: MustParse(`[a-zA-Z_][a-zA-Z_0-9]*`), Action: 2},
		},
		want: []string{
			`0: EOI accept; [\t ] -> 1; [A-Z_a-z] -> 2;`,
			`1: EOI exec 0; other exec 0; [0-9A-Z_a-z] exec 0; [\t ] -> 1;`,
			`2: EOI exec 2; other exec 2; [\t ] exec 2; [0-9A-Z_a-z] -> 2;`,
		},
	},
	{
		rules: []*Rule{
			{RE: MustParse(`(abcd?)`), Action: 1},
			{RE: MustParse(`ab`), Action: 2},
		},
		want: []string{
			`0: EOI accept; [a] -> 1;`,
			`1: [b] -> 2;`,
			`2: EOI exec 2; other exec 2; [a-bd] exec 2; [c] -> 3;`,
			`3: EOI exec 1; other exec 1; [a-c] exec 1; [d] -> 4;`,
			`4: EOI exec 1; other exec 1; [a-c] exec 1; [d] -> 5;`,
			`5: EOI exec 1; other exec 1; [a-d] exec 1;`,
		},
	},
}

func TestLex(t *testing.T) {
	for _, tc := range lexTests {
		tables, err := Compile(tc.rules, nil)
		if err != nil {
			t.Errorf("Compile(%v) failed with %v", tc.rules, err)
		}

		want := strings.Join(tc.want, "\n")
		if got := dumpTables(tables); got != want {
			t.Errorf("DFA =\n%v\nwant:\n%v", got, want)
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

	actionText := func(i int) string {
		switch {
		case i >= 0:
			return fmt.Sprintf("-> %v", i)
		case i == -1:
			return "error"
		case i == -2:
			return "accept"
		default:
			return fmt.Sprintf("exec %v", -i-3)
		}
	}

	var buf bytes.Buffer
	numStates := len(tables.Dfa) / tables.NumSymbols
	for state := 0; state < numStates; state++ {
		offset := state * tables.NumSymbols
		fmt.Fprintf(&buf, "%v:", state)
		if tables.Dfa[offset] != -1 {
			fmt.Fprintf(&buf, " EOI %v;", actionText(tables.Dfa[offset]))
		}
		if tables.Dfa[offset+1] != -1 {
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
			if t == -1 {
				continue
			}
			cs := newCharset(transitions[t]).String()
			fmt.Fprintf(&buf, " [%v] %v;", cs, actionText(t))
		}
		buf.WriteRune('\n')
	}
	return strings.TrimSuffix(buf.String(), "\n")
}
