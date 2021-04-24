package syntax_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/dump"
)

var setTests = []struct {
	input string
	want  []string
}{
	{`%input A; Z: set(first A); A: ;`, []string{
		"%empty",
	}},
	{`%input A; Z: set(A); A: a b | c;`, []string{
		"a | b | c",
	}},
	{`%input A; Z: set(first A); A: b;`, []string{
		"b",
	}},
	{`%input A; Z: set(first A); A: a b;`, []string{
		"a",
	}},
	{`%input A; Z: set(first A); A: a? b;`, []string{
		"a | b",
	}},
	{`%input A; Z: set(first A); A: B; B: a | B c;`, []string{
		"a",
	}},
	{`%input A; Z: set(first A); A: B; B: a | B c | ;`, []string{
		"a | c",
	}},
	{`%input A; Z: set(last A); A: B; B: a | B c ;`, []string{
		"a | c",
	}},
	{`%input A; Z: set(last A); A: B; B: a | c B ;`, []string{
		"a",
	}},

	// precede and follow
	{`%input A; Z: set(precede a); A: B; B: c | B a ;`, []string{
		"a | c",
	}},
	{`%input A; Z: set(precede a); A: B; B: c d | B a ;`, []string{
		"a | d",
	}},
	{`%input A; Z: set(precede a); A: B; B: c d | a ;`, []string{
		"%empty",
	}},
	{`%input A; Z: set(precede a); A: B b; B: c d | A a ;`, []string{
		"b",
	}},
	{`%input A; Z: set(precede a); A: B b?; B: c d | A a ;`, []string{
		"a | b | d",
	}},

	// unions
	{`%input A; Z: set(B | C); A: B b C; B: c d | a ; C : x y;`, []string{
		"c | d | a | x | y",
	}},
	{`%input A; Z: set(last B | first C); A: B b C; B: c d | a ; C : x y;`, []string{
		"d | a | x",
	}},
	{`%input A; Z: set(last B | first C); A: B b C; B: c d | a ; C : x? y;`, []string{
		"d | a | x | y",
	}},

	// intersection
	{`%input A; Z: set(B & C); A: B a C; B: c d | A; C: x y z;`, []string{
		"x | y | z",
	}},
	{`%input A; Z: set(B & C); A: B a C; B: c d | y z; C: x y z;`, []string{
		"y | z",
	}},

	// complement
	{`%input A; Z: set(~(last B | first C)); A: B b C; B: c d | a ; C : x? y;`, []string{
		"EOI | b | c",
	}},
	{`%input A; Z: set(~(b | c)); A: a b c d e;`, []string{
		"EOI | a | d | e",
	}},
	{`%input A; Z: set(A); A: a set(~A) b;`, []string{
		"ERR: input:1:31: set complement cannot transitively depend on itself",
	}},
}

func TestSets(t *testing.T) {
	for _, tc := range setTests {
		model, err := parse(tc.input)
		if err != nil {
			t.Errorf("cannot parse %q: %v", tc.input, err)
			continue
		}
		err = syntax.Expand(model)
		if err != nil {
			t.Errorf("cannot expand %q: %v", tc.input, err)
			continue
		}

		var indices []int
		for i, nt := range model.Nonterms {
			if nt.Value.Kind == syntax.Set {
				indices = append(indices, i)
			}
		}
		if len(indices) == 0 {
			t.Errorf("no sets found in %q: %v", tc.input, model.Nonterms)
			continue
		}

		var got []string
		if err := syntax.ResolveSets(model); err != nil {
			got = append(got, "ERR: "+err.Error())
		} else {
			for _, i := range indices {
				got = append(got, model.Nonterms[i].Value.String())
			}
		}

		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("ResolveSets(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}
