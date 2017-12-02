package lex

import (
	"bytes"
	"fmt"
	"log"
	"testing"
)

type patternMap map[string]*Regexp

// Resolve implements lex.Resolver
func (m patternMap) Resolve(name string) *Regexp {
	return m[name]
}

var testPatterns = patternMap{
	"hex": mustParse(`[0-9a-fA-F]`),
}

var compileTests = []struct {
	rules []Rule
	want  []string
}{
	{
		rules: []Rule{
			{RE: mustParse(`abcd{2,}`), Action: 1}, // whitespace
		},
		want: []string{
			`[2],[3],[4],[5],[5],[5]+2,-1,=>1`,
		},
	},
	{
		rules: []Rule{
			{RE: mustParse(`\s+{hex}+`), Action: 0}, // whitespace
			{RE: mustParse(`\w+`), Action: 1},
		},
		want: []string{
			`[2],[2]+2,-1,[3],[3]+2,-1,=>0`,
			`[3 4],[3 4]+2,-1,=>1`,
		},
	},
	{
		rules: []Rule{
			{RE: mustParse(`([0-9]|[a-z])+`), Action: 0}, // whitespace
		},
		want: []string{
			`+1+3,[2],+3,[3],+1,+1+3+6,[2],+3,[3],+1,-5,=>0`,
		},
	},
}

func TestCompile(t *testing.T) {
	for n, test := range compileTests {
		var index []int
		c := newCompiler(testPatterns)
		for i, r := range test.rules {
			offset, err := c.addRegexp(r.RE, r.Action)
			if err != nil {
				t.Fatalf("cannot compile regexp in test #%v", i)
			}
			index = append(index, offset)
		}
		ins, _ := c.compile()

		var got []string
		var b bytes.Buffer
		for off, inst := range ins {
			if len(index) > 0 && index[0] == off {
				if b.Len() > 0 {
					got = append(got, b.String())
					b.Reset()
				}
				index = index[1:]
			} else {
				b.WriteString(",")
			}
			dumpInst(off, inst, &b)
		}
		got = append(got, b.String())

		if len(got) != len(test.rules) {
			t.Fatalf("#%v got %v results; want: %v", n+1, len(got), len(test.rules))
		}

		for i := range got {
			if got[i] != test.want[i] {
				t.Errorf("#%v compile(%v) = %v, want: %v", n+1, test.rules[i].RE, got[i], test.want[i])
			}
		}
	}
}

func mustParse(pattern string) *Regexp {
	re, err := ParseRegexp(pattern)
	if err != nil {
		log.Fatalf("%q: %v", pattern, err)
	}
	return re
}

func dumpInst(i int, inst inst, b *bytes.Buffer) {
	if len(inst.consume) > 0 {
		b.WriteString("[")
	}
	for i, sym := range inst.consume {
		if i > 0 {
			b.WriteString(" ")
		}
		fmt.Fprintf(b, "%v", sym)
	}
	if len(inst.consume) > 0 {
		b.WriteString("]")
	}
	for _, link := range inst.links {
		fmt.Fprintf(b, "%+d", link)
	}
	if inst.action >= 0 {
		fmt.Fprintf(b, "=>%v", inst.action)
	}
}
