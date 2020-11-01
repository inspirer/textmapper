package syntax_test

import (
	"fmt"
	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/dump"
	"strings"
	"testing"
)

var propagateTests = []struct {
	input string
	want  string
}{
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: C; C: [V] d;`,
		`%flag V; Z: A<V=true>; A<V>: B<V=V>; B<V>: C<V=V>; C<V>: [V] d;`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: (C | c); C: [V] d;`,
		`%flag V; Z: A<V=true>; A<V>: B<V=V>; B<V>: C<V=V> | c; C<V>: [V] d;`,
	},
	{
		`%flag P; %lookahead flag V; Z<P>: a A; A: B<V=true>; B: C; C: d Z<P=V>;`,
		`%flag P; %flag V; Z<P>: a A; A: B<V=true>; B<V>: C<V=V>; C<V>: d Z<P=V>;`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: C? c; C: [V] d;`,
		`ERR: input:1:40: cannot propagate lookahead flag V through nonterminal B; avoid nullable alternatives and optional clauses`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: (C | c)?; C: [V] d;`,
		`ERR: input:1:40: cannot propagate lookahead flag V through nonterminal B; avoid nullable alternatives and optional clauses`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: (C | c)*; C: [V] d;`,
		`ERR: input:1:40: cannot propagate lookahead flag V through nonterminal B; avoid nullable alternatives and optional clauses`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: (C | c)+; C: [V] d;`,
		`%flag V; Z: A<V=true>; A<V>: B<V=V>; B<V>: (C<V=V> | c)+; C<V>: [V] d;`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: (C | C<V=false> c)+; C: [V] d;`,
		`%flag V; Z: A<V=true>; A<V>: B<V=V>; B<V>: (C<V=V> | C<V=false> c)+; C<V>: [V] d;`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: (C | c?); C: [V] d;`,
		`ERR: input:1:40: cannot propagate lookahead flag V through nonterminal B; avoid nullable alternatives and optional clauses`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B; B: C; C: d;`,
		`ERR: input:1:25: V is not used in A`,
	},
	{
		`%lookahead flag V; Z: A<V=true>; A: B<V=true>; B: C; C: [V] d;`,
		`ERR: input:1:25: V is not used in A`,
	},
	{
		`%lookahead flag V;Z2:[V] A; A: B<V=true>; B: C; C: [V] d;`,
		`ERR: input:1:23: lookahead flag V is never provided`,
	},
	// Lookahead flag arguments in token sets.
	{
		`%lookahead flag V; Z: A set(B<V=true>); A: c; B: C; C: [V] d;`,
		`%flag V; Z: A set(B<V=true>); A: c; B<V>: C<V=V>; C<V>: [V] d; `,
	},
}

func TestPropagateLookaheads(t *testing.T) {
	for _, tc := range propagateTests {
		model, err := parse(tc.input)
		if err != nil {
			t.Errorf("cannot parse %q: %v", tc.input, err)
			continue
		}

		if err := syntax.PropagateLookaheads(model); err != nil {
			const prefix = "ERR: "
			if !strings.HasPrefix(tc.want, prefix) {
				t.Errorf("PropagateLookaheads(%v) failed with %v", tc.input, err)
				continue
			}
			if got := fmt.Sprintf("ERR: %v", err); got != tc.want {
				t.Errorf("PropagateLookaheads(%v) failed with %v, want: %v", tc.input, got, tc.want)
			}
			continue
		}

		want, err := parse(tc.want)
		if err != nil {
			t.Errorf("cannot parse %q: %v", tc.want, err)
			continue
		}

		stripOrigin(model)
		stripOrigin(want)
		if diff := dump.Diff(want, model); diff != "" {
			t.Errorf("PropagateLookaheads(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}
