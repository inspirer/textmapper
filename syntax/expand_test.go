package syntax_test

import (
	"testing"

	"github.com/inspirer/textmapper/syntax"
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

	// sets
	{`%input Z; Z: set(a);`, "setof_a"},
	{`%input Z; Z: set(a | b);`, "setof_a_or_b"},
	{`%input Z; Z: set(a | b)+;`, "setof_a_or_b_list"},
	{`%input Z; Z: set(a | b)+?;`, "setof_a_or_b_listopt"},
	{`%input Z; Z: set(a | b)*;`, "setof_a_or_b_optlist"},
	{`%input Z; Z: set(Q); Q: c;`, "setof_Q"},
	{`%input Z; Z: set(precede Q); Q: c;`, "setof_precede_Q"},
	{`%input Z; Z: set(~Q | follow b); Q: c;`, "setof_not_Q_or_follow_b"},
	{`%input Z; Z: set(first A | last B)?; A: a; B: b;`, "setof_first_A_or_last_Bopt"},

	// lookaheads
	{`%input Z; Z: (?= A & !B); A:; B:;`, "lookahead_A_notB"},
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
