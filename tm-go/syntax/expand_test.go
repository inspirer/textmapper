package syntax_test

import (
	"github.com/inspirer/textmapper/tm-go/syntax"
	"testing"
)

var nameTests = []struct {
	input string
	want  string
}{
	{`%input Z; Z: a+;`, "a_list"},
	{`%input Z; Z: a*;`, "a_optlist"},
	{`%input Z; Z: a* -> Foo;`, "a_optlist"},
	{`%input Z; Z: QQ=a+;`, "a_list"},
	{`%input Z; Z: (a separator b)+;`, "a_list_b_separated"},
	{`%input Z; Z: .foo (a separator b)* .bar;`, "a_optlist_b_separated"},
	{`%input Z; Z: .foo (a separator b c)* .bar;`, "a_optlist_withsep"},
	{`%input Z; Z: a?;`, "aopt"},
	{`%input Z; Z: B?; B:;`, "Bopt"},
	{`%input Z; Z: (a separator b c)+?;`, "a_list_withsepopt"},
}

func TestProvisionalName(t *testing.T) {
	for _, tc := range nameTests {
		model, err := parse(tc.input)
		if err != nil {
			t.Errorf("cannot parse %q: %v", tc.input, err)
			continue
		}

		inp := model.Inputs[0].Nonterm
		name := syntax.ProvisionalName(model.Nonterms[inp].Value, model)
		if name != tc.want {
			t.Errorf("ProvisionalName(%v) = %v, want %v", tc.input, name, tc.want)
		}
	}
}
