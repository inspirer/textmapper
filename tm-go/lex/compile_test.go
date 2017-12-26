package lex

import (
	"bytes"
	"fmt"
	"strings"
	"testing"
)

type patternMap map[string]*Regexp

// Resolve implements lex.Resolver
func (m patternMap) Resolve(name string) *Regexp {
	return m[name]
}

var testPatterns = patternMap{
	"hex": MustParse(`[0-9a-fA-F]`),
}

var compileTests = []struct {
	rules []*Rule
	want  []string
}{
	{
		rules: []*Rule{
			{RE: MustParse(`abcd{2,}`), Action: 1},
		},
		want: []string{
			`[2],[3],[4],[5],[5],[5]+2,-1+1,=>1`,
		},
	},
	{
		rules: []*Rule{
			{RE: MustParse(`\s+{hex}+`), Action: 0},
			{RE: MustParse(`\w+`), Action: 1},
		},
		want: []string{
			`[2],[2]+2,-1+1,[3],[3]+2,-1+1,=>0`,
			`[3 4],[3 4]+2,-1+1,=>1`,
		},
	},
	{
		rules: []*Rule{
			{RE: MustParse(`([0-9]|[a-z])+`), Action: 0},
		},
		want: []string{
			`+1+3,[2],+4+6+9,[3],+2+4+7,+1+3+6,[2],-1+1+4,[3],-3-1+2,-4-2+1,=>0`,
		},
	},
	{
		rules: []*Rule{
			{RE: MustParse(`(a+)+`), Action: 42},
		},
		want: []string{
			`[2],[2]+2+6,-1+1+5,[2]+4,[2]-1+3,-1-2+2,-3+1,=>42`,
		},
	},
}

func TestCompile(t *testing.T) {
	for n, test := range compileTests {
		var index []int
		c := newCompiler(testPatterns)
		for i, r := range test.rules {
			offset, err := c.addRegexp(r.RE, r.Action, r)
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
	if inst.rule != nil {
		fmt.Fprintf(b, "=>%v", inst.rule.Action)
	}
}

func TestErrors(t *testing.T) {
	c := newCompiler(testPatterns)
	r := &Rule{
		RE:         MustParse(`((asdasd)?|[abc]?)`),
		Action:     42,
		OriginName: "rule1",
	}
	_, err := c.addRegexp(r.RE, r.Action, r)
	if err == nil || !strings.Contains(err.Error(), "`rule1` accepts empty text") {
		t.Errorf("addRegexp() = %v, want: accepts empty text", err)
	}
}
