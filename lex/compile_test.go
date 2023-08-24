package lex

import (
	"bytes"
	"fmt"
	"strings"
	"testing"
)

type patternMap map[string]*Pattern

// Resolve implements lex.Resolver
func (m patternMap) Resolve(name string) *Pattern {
	return m[name]
}

var testPatterns = make(patternMap)

func init() {
	p := pattern("hex", "[0-9a-fA-F]")
	testPatterns[p.Name] = p
}

var compileTests = []struct {
	rules []*Rule
	want  []string
}{
	{
		rules: []*Rule{
			rule("a", `abcd{2,}`, 1),
		},
		want: []string{
			`[2],[3],[4],+1,[5],[5],-1+1,=>1`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `AB|A(BC+)?`, 1),
		},
		want: []string{
			`+1+4,[2],[3],+9,[2],+1+7,[3]+6,+1,[4],-1+3,+2,+1,=>1`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `\s+{hex}+`, 0),
			rule("b", `\w+`, 1),
		},
		want: []string{
			`+1,[2],-1+2,+1,[3],-1+1,=>0`,
			`+1,[3 4],-1+1,=>1`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `([0-9]|[a-z])+`, 0),
		},
		want: []string{
			`+2+4,+1+3,[2],-1+1+4,[3],-3-1+2,+1-4-2,=>0`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `(a+)+`, 42),
		},
		want: []string{
			`+2,+1,[2],-1+2,+1-2,=>42`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `-?0`, 42),
		},
		want: []string{
			`+1+3,[2]+2,+1,[3],=>42`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `[a-z](-*[a-z])*`, 11),
		},
		want: []string{
			`[3],+2+4+6,+1+5+3,[2]+2,-1+1,[3],+1-3-1,=>11`,
		},
	},
	{
		rules: []*Rule{
			rule("a", `{eoi}`, 5),
		},
		want: []string{
			`[0],=>5`,
		},
	},
}

func TestCompile(t *testing.T) {
	for n, test := range compileTests {
		var index []int
		c := newCompiler()
		for i, r := range test.rules {
			offset, err := c.addPattern(r.Pattern, r)
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
				t.Errorf("#%v compile(%v) = %v, want: %v", n+1, test.rules[i].Pattern.Text, got[i], test.want[i])
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
	c := newCompiler()
	r := rule("rule1", `((asdasd)?|[abc]?)`, 42)
	_, err := c.addPattern(r.Pattern, r)
	if err == nil || !strings.Contains(err.Error(), "`rule1` accepts empty text") {
		t.Errorf("addRegexp() = %v, want: accepts empty text", err)
	}
}

func pattern(name, text string) *Pattern {
	return &Pattern{Name: name, Text: text, RE: MustParse(text)}
}

func rule(name, re string, action int) *Rule {
	return &Rule{
		Pattern:  pattern(name, re),
		Action:   action,
		Resolver: testPatterns,
	}
}
