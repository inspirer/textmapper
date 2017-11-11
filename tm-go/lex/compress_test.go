package lex

import (
	"fmt"
	"testing"
)

var compressTests = []struct {
	input   string
	wantOut string
	wantMap string
}{
	{``, `[]`, `[]`},
	{`[a-z]`, `[[2]]`, `[a-z=>2]`},
	{`[a-z][A-Z]`, `[[3] [2]]`, `[A-Z=>2 a-z=>3]`},
	{`[a-zA-Z][A-Z]`, `[[2 3] [2]]`, `[A-Z=>2 a-z=>3]`},
	{`[A-N][L-Z]`, `[[2 3] [3 4]]`, `[A-K=>2 L-N=>3 O-Z=>4]`},
	{`[A-L][L-Z]`, `[[2 3] [3 4]]`, `[A-K=>2 L=>3 M-Z=>4]`},
	{`[A-L][M-Z]`, `[[2] [3]]`, `[A-L=>2 M-Z=>3]`},
	{`[A-Z][CX]`, `[[2 3] [3]]`, `[A-B=>2 C=>3 D-W=>2 X=>3 Y-Z=>2]`},
	{`[A-Z][CX][CY-Z]`, `[[2 3 4 5] [3 4] [3 5]]`, `[A-B=>2 C=>3 D-W=>2 X=>4 Y-Z=>5]`},
	{`[CX][CY-Z][A-Z]`, `[[3 4] [3 5] [2 3 4 5]]`, `[A-B=>2 C=>3 D-W=>2 X=>4 Y-Z=>5]`},
	{`[Z][C][A]`, `[[4] [3] [2]]`, `[A=>2 C=>3 Z=>4]`},
	{`[0-9][0-1][0-7]`, `[[2 3 4] [2] [2 3]]`, `[0-1=>2 2-7=>3 8-9=>4]`},
	{`[0-9][0-1][0-7][0-9][A-Z]`, `[[2 3 4] [2] [2 3] [2 3 4] [5]]`, `[0-1=>2 2-7=>3 8-9=>4 A-Z=>5]`},
}

func TestCompressCharsets(t *testing.T) {
	for _, test := range compressTests {
		sets, err := parseCharsets(test.input)
		if err != nil {
			t.Errorf("parseCharsets(%q) failed with %v", test.input, err)
		}

		out, inputMap := compressCharsets(sets)
		if outstr := fmt.Sprintf("%v", out); outstr != test.wantOut {
			t.Errorf("compressCharsets(%q).out = %v; want: %v", test.input, outstr, test.wantOut)
		}
		if mapstr := fmt.Sprintf("%v", inputMap); mapstr != test.wantMap {
			t.Errorf("compressCharsets(%q).inputMap = %v; want: %v", test.input, mapstr, test.wantMap)
		}
	}
}

func parseCharsets(input string) ([]charset, error) {
	var ret []charset
	var p parser
	p.source = input
	p.next()
	for p.ch == '[' {
		cs := p.parseClass(false)
		ret = append(ret, cs)
	}
	if p.ch != -1 {
		p.error("unexpected end of input")
	}
	if p.err.msg != "" {
		return nil, p.err
	}
	return ret, nil
}
