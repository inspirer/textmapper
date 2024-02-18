package lex

import (
	"fmt"
	"strings"
	"testing"
)

var compressTests = []struct {
	input   string
	wantOut string
	wantMap string
}{
	{``, `[]`, `[\x00=>1]`},
	{`[\x00\t]`, `[[1]]`, `[\x00=>1 \x01=>2 \t=>1 \n=>2]`},
	{`[a-z]`, `[[2]]`, `[\x00=>1 a=>2 \{=>1]`},
	{`[a-z][A-Z]`, `[[3] [2]]`, `[\x00=>1 A=>2 \[=>1 a=>3 \{=>1]`},
	{`[a-zA-Z][A-Z]`, `[[2 3] [2]]`, `[\x00=>1 A=>2 \[=>1 a=>3 \{=>1]`},
	{`[A-N][L-Z]`, `[[2 3] [3 4]]`, `[\x00=>1 A=>2 L=>3 O=>4 \[=>1]`},
	{`[A-L][L-Z]`, `[[2 3] [3 4]]`, `[\x00=>1 A=>2 L=>3 M=>4 \[=>1]`},
	{`[A-L][M-Z]`, `[[2] [3]]`, `[\x00=>1 A=>2 M=>3 \[=>1]`},
	{`[A-Z][CX]`, `[[2 3] [3]]`, `[\x00=>1 A=>2 C=>3 D=>2 X=>3 Y=>2 \[=>1]`},
	{`[A-Z][CX][CY-Z]`, `[[2 3 4 5] [3 4] [3 5]]`, `[\x00=>1 A=>2 C=>3 D=>2 X=>4 Y=>5 \[=>1]`},
	{`[CX][CY-Z][A-Z]`, `[[3 4] [3 5] [2 3 4 5]]`, `[\x00=>1 A=>2 C=>3 D=>2 X=>4 Y=>5 \[=>1]`},
	{`[Z][C][A]`, `[[4] [3] [2]]`, `[\x00=>1 A=>2 B=>1 C=>3 D=>1 Z=>4 \[=>1]`},
	{`[0-9][0-1][0-7]`, `[[2 3 4] [2] [2 3]]`, `[\x00=>1 0=>2 2=>3 8=>4 \:=>1]`},
	{`[0-9][0-1][0-7][0-9][A-Z]`, `[[2 3 4] [2] [2 3] [2 3 4] [5]]`, `[\x00=>1 0=>2 2=>3 8=>4 \:=>1 A=>5 \[=>1]`},
	{`[^b]`, `[[1]]`, `[\x00=>1 b=>2 c=>1]`},
	{`[^\p{Any}]`, `[[]]`, `[\x00=>1]`},
	{`[\x21-\U0010ffff][a-z]`, `[[2 3] [3]]`, `[\x00=>1 \!=>2 a=>3 \{=>2]`},
	{`[\x21-\U0010fff0]`, `[[2]]`, `[\x00=>1 \!=>2 \U0010fff1=>1]`},

	// Bytes modes.
	{`{#bytes}[\x00\t]`, `[[1]]`, `[\x00=>1 \x01=>2 \t=>1 \n=>2]`},
	{`{#bytes}[\x00-\xff]`, `[[1]]`, `[\x00=>1]`}, // no second class compared to the full Unicode mode
	{`[\x00-\xff]`, `[[1]]`, `[\x00=>1 \u0100=>2]`},
	{`{#bytes}[\x00-\xfe]`, `[[1]]`, `[\x00=>1 \u00ff=>2]`},
}

func TestCompressCharsets(t *testing.T) {
	for _, test := range compressTests {
		input := test.input
		var opts CharsetOptions
		input, opts.ScanBytes = strings.CutPrefix(input, "{#bytes}")
		sets, err := parseCharsets(input, opts)
		if err != nil {
			t.Errorf("parseCharsets(%q) failed with %v", test.input, err)
		}

		out, inputMap := compressCharsets(sets, opts)
		if outstr := fmt.Sprintf("%v", out); outstr != test.wantOut {
			t.Errorf("compressCharsets(%q).out = %v; want: %v", test.input, outstr, test.wantOut)
		}
		if mapstr := fmt.Sprintf("%v", inputMap); mapstr != test.wantMap {
			t.Errorf("compressCharsets(%q).inputMap = %v; want: %v", test.input, mapstr, test.wantMap)
		}
	}
}

func parseCharsets(input string, opts CharsetOptions) ([]charset, error) {
	var ret []charset
	var p parser
	p.source = input
	p.next()
	for p.ch == '[' {
		cs := p.parseClass(opts)
		ret = append(ret, cs)
	}
	if p.ch != -1 {
		p.error("unexpected end of input", p.offset, p.offset)
	}
	if p.err.Msg != "" {
		return nil, p.err
	}
	return ret, nil
}

func TestContains(t *testing.T) {
	input := symlist{0, 2, 7, 8, 10, 12, 15, 17, 20, 30, 40, 50, 52, 63}
	elements := make(map[Sym]bool)
	for _, sym := range input {
		elements[sym] = true
	}
	for sym := Sym(0); sym < 64; sym++ {
		want := elements[sym]
		if got := input.contains(sym); got != want {
			t.Errorf("symlist.contains(%v) = %v, want: %v", sym, got, want)
		}
	}
}
