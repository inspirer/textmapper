package syntax_test

import (
	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/dump"
	"testing"
)

var nullableTests = []struct {
	input string
	want  []string
}{
	{`A: a?; B: .foo (a b)?;`, []string{"A", "B"}},
	{`A:; B: b | A; C: c;`, []string{"A", "B"}},
	{`A:; B: b | b A; C: c;`, []string{"A"}},
	{`A: B; B: C D; C:; D: a?; `, []string{"A", "B", "C", "D"}},
	{`A: B; B: C D; C:; D: a A; `, []string{"C"}},
	{`A: set(a); B: A+; C: A*;`, []string{"C"}},
	{`A: a %prec a; B: a? %prec a;`, []string{"B"}},
	{`A: set(a); B: (A separator a)+; C: (A separator b)*;`, []string{"C"}},
	// Approximation: conditionals are never nullable.
	{`%flag Foo; A: a?; B<Foo>: [Foo] a?;`, []string{"A"}},
}

func TestNullable(t *testing.T) {
	for _, tc := range nullableTests {
		model, err := parse(tc.input)
		if err != nil {
			t.Errorf("cannot parse %q: %v", tc.input, err)
			continue
		}

		var got []string
		for _, nt := range syntax.Nullable(model).Slice(nil) {
			got = append(got, model.Nonterms[nt-len(model.Terminals)].Name)
		}

		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("Nullable(%v) diff (-want +got):\n%s", tc.input, diff)
		}
	}
}
