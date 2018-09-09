package container_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-go/util/container"
)

var intersectTests = []struct {
	a, b container.IntSet
	want string
}{
	{set(), complementOf(), "[]"},
	{complementOf(), complementOf(), "~[]"},
	{set(1, 2, 3), complementOf(), "[1 2 3]"},
	{set(1), set(), "[]"},
	{set(1), set(1), "[1]"},
	{set(1), set(5), "[]"},
	{set(7, 9), set(7, 8, 9), "[7 9]"},
	{set(0, 1, 3), set(1, 2), "[1]"},
	{set(0, 1, 3), complementOf(1), "[0 3]"},
	{complementOf(3), complementOf(1), "~[1 3]"},
	{complementOf(2, 3), complementOf(1, 2), "~[1 2 3]"},
}

func TestIntersect(t *testing.T) {
	for _, test := range intersectTests {
		c := container.Intersect(test.a, test.b, nil)
		if got := c.String(); got != test.want {
			t.Errorf("Intersect(%v, %v) = %v, want: %v", test.a, test.b, got, test.want)
		}

		c = container.Intersect(test.b, test.a, nil)
		if got := c.String(); got != test.want {
			t.Errorf("Intersect(%v, %v) = %v, want: %v", test.b, test.a, got, test.want)
		}
	}
}

var mergeTests = []struct {
	a, b container.IntSet
	want string
}{
	{set(), set(), "[]"},
	{set(), complementOf(), "~[]"},
	{complementOf(), complementOf(), "~[]"},
	{set(1, 2, 3), complementOf(), "~[]"},
	{set(1), set(), "[1]"},
	{set(1), set(1), "[1]"},
	{set(1), set(5), "[1 5]"},
	{set(7, 9), set(7, 8, 9), "[7 8 9]"},
	{set(0, 1, 3), set(1, 2), "[0 1 2 3]"},
	{set(0, 1, 3), complementOf(1), "~[]"},
	{set(0, 1, 3), complementOf(8), "~[8]"},
	{complementOf(3), complementOf(1), "~[]"},
	{complementOf(2, 3), complementOf(1, 2), "~[2]"},
}

func TestMerge(t *testing.T) {
	for _, test := range mergeTests {
		c := container.Merge(test.a, test.b, nil)
		if got := c.String(); got != test.want {
			t.Errorf("Merge(%v, %v) = %v, want: %v", test.a, test.b, got, test.want)
		}

		c = container.Merge(test.b, test.a, nil)
		if got := c.String(); got != test.want {
			t.Errorf("Merge(%v, %v) = %v, want: %v", test.b, test.a, got, test.want)
		}
	}
}

var intBitSetTests = []container.IntSet{
	set(),
	set(1),
	set(1, 5),
	set(0, 1, 2, 99),
	complementOf(),
	complementOf(0, 1, 2, 99),
}

func TestIntBitSets(t *testing.T) {
	for _, test := range intBitSetTests {
		bs := test.BitSet(100)
		if test.Inverse {
			bs.Complement()
		}
		d := container.IntSet{Set: bs.Slice(nil), Inverse: test.Inverse}
		if got := d.String(); got != test.String() {
			t.Errorf("%v.BitSet().Slice() = %v, want: %v", test, got, test.String())
		}
	}
}

func set(ints ...int) container.IntSet {
	return container.IntSet{Set: ints}
}

func complementOf(ints ...int) container.IntSet {
	return container.IntSet{Inverse: true, Set: ints}
}
