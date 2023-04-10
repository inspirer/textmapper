package syntax_test

import (
	"testing"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/dump"
)

var typesTests = []struct {
	input string
	want  []string
}{
	{`%input A; A: a -> foo;`, []string{
		"foo: ",
	}},
	{`%input A; A: a -> foo; B: -> bar;`, []string{
		"foo: ", // bar is ignored, unless used
	}},
	{`%input A; B: -> bar; A: B .a a -> foo;`, []string{
		"bar: ",
		"foo: bar",
	}},
	{`%input A; A: (a->b) -> A;`, []string{
		"A: b",
		"b: ",
	}},
	{`%input A; A: foo=(a->b) -> A;`, []string{
		"A: foo=b",
		"b: ",
	}},
	{`%input A; A: foo=(a->b) bar=(a->b) -> A;`, []string{
		"A: foo=b bar=b",
		"b: ",
	}},
	{`%input A; A: foo+=(a->b) -> A;`, []string{
		"A: foo=(b)+",
		"b: ",
	}},
	{`%input A; A: foo+=(a->a) foo+=(b->b) -> A;`, []string{
		"A: foo=(a | b)+",
		"a: ",
		"b: ",
	}},
	{`%input A; A: (a->expr) (b->expr) -> A;`, []string{
		"A: (expr)+",
		"expr: ",
	}},
	{`%input A; A: (a->expr| ) -> A | (b->expr) -> A;`, []string{
		"A: expr?",
		"expr: ",
	}},
	{`%input A; A: B -> A; B: C -> B; C: c -> C;`, []string{
		"A: B",
		"B: C",
		"C: ",
	}},
	{`%input A; A: B -> A; B: C -> B; C: B c -> C;`, []string{
		"A: B",
		"B: C",
		"C: B",
	}},
	{`%input A; A: B -> A; B: (b -> B) C (b -> B); C: c %prec c -> C;`, []string{
		"A: (B)+ C",
		"B: ",
		"C: ",
	}},

	// Cycles.
	{`%input A; A: B -> A; B: C; C: B (c -> C);`, []string{
		"A: (C)+",
		"C: ",
	}},
	{`%input A; A: B -> A; B: C; C: B | (c -> C);`, []string{
		"A: (C)*",
		"C: ",
	}},
	{`%input A; A: B -> A; B: C | D; C: B | (c -> C); D: B | d=(d -> D);`, []string{
		"A: (C)* d=(D)*",
		"C: ",
		"D: ",
	}},
	{`%input A; A: C -> A; C: B | (c -> C); D: B | d=(d -> D); B: C | D;`, []string{
		"A: (C)* d=(D)*",
		"C: ",
		"D: ",
	}},
	{`%input A; A: B -> A; B: C; C: D; D: E; E: C a=A;`, []string{
		"A: a=(A)+",
	}},

	// Plain list.
	{`%input A; A: bb=B -> A; B: (b -> B) | B (b -> B);`, []string{
		"A: bb=(B)+",
		"B: ",
	}},

	// Errors.
	{`%input A; A: bb=B -> A; B: (b -> B) | B (b -> B) (c -> C);`, []string{
		"err: multiple fields found behind an assignment: [(B)+ (C)*]",
		"A: (B)+ (C)*",
		"B: ",
		"C: ",
	}},

	// Categories
	{`%input A; %interface E; A: B -> A; B: a -> B -> E | left=B c right=B -> C -> E;`, []string{
		"A: E",
		"B: ",
		"C: left=E right=E",
		"E: B C",
	}},
	{`%input A; %interface E, D; A: B -> D -> A; B: a -> B -> E | left=B c right=B -> C -> E;`, []string{
		"A: D",
		"B: ",
		"C: left=E right=E",
		"D: B C",
		"E: B C",
	}},
	{`%input A; %interface E; A: B -> A; B: (a -> B) c -> E | C -> E; C: left=B c right=B -> C;`, []string{
		"A: E",
		"B: ",
		"C: left=E right=E",
		"E: B C",
	}},

	// Errors.
	{`%input A; %interface X; A: -> X;`, []string{
		"err: '%empty' must produce exactly one node",
		"X:",
	}},
	{`%input A; %interface X; A: B -> X; B: b;`, []string{
		"err: 'B' must produce exactly one node",
		"X:",
	}},
	{`%input A; %interface X; A: B -> X | -> X; B: b -> B;`, []string{
		"err: '%empty' must produce exactly one node",
		"B: ",
		"X: B",
	}},
	{`%input A; %interface X; A: B -> X; B: a -> A | b -> B | ;`, []string{
		"err: 'a -> A | b -> B | %empty' must produce exactly one node",
		"A: ",
		"B: ",
		"X: A B",
	}},
	{`%input A; %interface X; A: B (c -> C) (d -> D) -> X; B: b;`, []string{
		"err: 'B (c -> C) (d -> D)' must produce exactly one node",
		"C: ",
		"D: ",
		"X: C D",
	}},
	{`%input A; %interface X; A: c B -> X; B: b -> B | %prec c;`, []string{
		"err: 'b -> B | %empty %prec c' must produce exactly one node",
		"B: ",
		"X: B",
	}},
	{`%input A; %interface X, Y; A: B -> X | D -> Y; B: C; C: B; D: B;`, []string{
		"err: 'B' is recursive and cannot be used inside a category expression",
		"X:",
		"Y:",
	}},

	// Conflicting fields.
	{`%input A; %interface X,Y; A: x=B y=D -> A; B: b->B-> X | c->C->X; D: d -> B | ;`, []string{
		// OK
		"A: x=X y=B?",
		"B: ",
		"C: ",
		"X: B C",
	}},
	{`%input A; %interface X,Y; A: x=B y=D -> A; B: b->B-> X | c->C->X | B B; D: d -> B;`, []string{
		// Lists are not allowed.
		"err: fields x and y of A contain overlapping sets of node types",
		"A: x=(X)* y=B",
		"B: ",
		"C: ",
		"X: B C",
	}},
	{`%input A; %interface X,Y; A: x=B y=D -> A | y=D -> A; B: b->B-> X | c->C->X ; D: d -> B;`, []string{
		// Nullable clauses are not allowed.
		"err: fields x and y of A contain overlapping sets of node types",
		"A: x=X? y=B",
		"B: ",
		"C: ",
		"X: B C",
	}},
	{`%input A; %interface X,Y; A: x=B y=D -> A; B: b->B-> X | c->C->X | B B; D: d -> B;`, []string{
		// Lists are not allowed.
		"err: fields x and y of A contain overlapping sets of node types",
		"A: x=(X)* y=B",
		"B: ",
		"C: ",
		"X: B C",
	}},
	{`%input A; %interface X,Y; A: x=B y=D -> A | y=D x=B -> A; B: b->B-> X | c->C->X ; D: d -> B;`, []string{
		// Unordered clauses are not allowed.
		"err: fields y and x of A contain overlapping sets of node types",
		"A: y=B x=X",
		"B: ",
		"C: ",
		"X: B C",
	}},
}

func TestTypes(t *testing.T) {
	for _, tc := range typesTests {
		model, err := parse(tc.input)
		if err != nil {
			t.Errorf("cannot parse %q: %v", tc.input, err)
			continue
		}

		var got []string
		types, err := syntax.ExtractTypes(model, nil /*tokens*/, syntax.TypeOptions{EventFields: true})
		if err != nil {
			s := status.FromError(err)
			s.Sort()
			for _, e := range s {
				got = append(got, "err: "+e.Msg)
			}
			if len(s) == 0 {
				t.Errorf("ExtractTypes(%q) failed with %v", tc.input, err)
				continue
			}
		}
		for _, t := range types.RangeTypes {
			got = append(got, t.Name+": "+t.Descriptor())
		}
		for _, cat := range types.Categories {
			got = append(got, cat.String())
		}

		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("ExtractTypes(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}
