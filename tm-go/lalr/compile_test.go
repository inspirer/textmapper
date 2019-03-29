package lalr

import (
	"bufio"
	"bytes"
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-go/util/container"
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
		g, symbols := parseGrammar(t, tc.input)
		c := &compiler{
			grammar: g,
			empty:   container.NewBitSet(g.Symbols),
		}
		c.computeEmpty()
		var buf strings.Builder
		for i, sym := range symbols {
			if c.empty.Get(i) {
				buf.WriteString(sym)
			}
		}
		if got := buf.String(); got != tc.want {
			t.Errorf("empty(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}

var ruleRE = regexp.MustCompile(`^([A-Z])\s+->(?:\s+([a-zA-Z]*))?$`)

func parseGrammar(t *testing.T, input string) (*Grammar, []string) {
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
	ret.Symbols = len(index)
	return ret, symbols
}
