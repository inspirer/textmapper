package syntax_test

import (
	"github.com/inspirer/textmapper/tm-go/syntax"
	"testing"
)

var nameTests = []struct {
	input string
	want  string
}{
	{`%input Z; Z: a+;`, "A_list"},
	{`%input Z; Z: a*;`, "A_optlist"},
	{`%input Z; Z: a* -> Foo;`, "A_optlist"},
	{`%input Z; Z: QQ=a+;`, "A_list"},
	{`%input Z; Z: (a separator b)+;`, "A_list_B_separated"},
	{`%input Z; Z: .foo (a separator b)* .bar;`, "A_optlist_B_separated"},
	{`%input Z; Z: .foo (a separator b c)* .bar;`, "A_optlist_withsep"},
	{`%input Z; Z: a?;`, "Aopt"},
	{`%input Z; Z: B?; B:;`, "Bopt"},
	{`%input Z; Z: (a separator b c)+?;`, "A_list_withsepopt"},
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
