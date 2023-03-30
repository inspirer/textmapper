package set_test

import (
	"strings"
	"testing"

	"github.com/inspirer/textmapper/util/set"
)

func TestClosureEmpty(t *testing.T) {
	c := set.NewClosure(0)
	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}
}

func TestClosureSingle(t *testing.T) {
	c := set.NewClosure(0)
	fs := c.Add([]int{2, 42})
	fs.Include(fs)
	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}
	if got, want := fs.IntSet.String(), "[2 42]"; got != want {
		t.Errorf("fs = %v, want: %v", got, want)
	}
}

func TestClosureUnion(t *testing.T) {
	c := set.NewClosure(100)
	fs1 := c.Add([]int{1, 2, 3})
	fs2 := c.Add([]int{7, 8})
	fs3 := c.Add([]int{13})
	fs4 := c.Add([]int{7, 13, 22})

	fs1.Include(fs2, fs3)
	fs3.Include(fs2)
	fs4.Include(fs3)

	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}
	var want = []struct {
		*set.FutureSet
		want string
	}{
		{fs1, "[1 2 3 7 8 13]"},
		{fs2, "[7 8]"},
		{fs3, "[7 8 13]"},
		{fs4, "[7 8 13 22]"},
	}
	for i, exp := range want {
		if got, want := exp.FutureSet.IntSet.String(), exp.want; got != want {
			t.Errorf("fs%v = %v, want: %v", i+1, got, want)
		}
	}
}

func TestClosureUnionCycle(t *testing.T) {
	c := set.NewClosure(2)
	fs1 := c.Add([]int{78})
	fs2 := c.Add([]int{1, 11, 12})
	fs3 := c.Add([]int{7, 8})
	fs4 := c.Add([]int{4, 5, 27})
	fs5 := c.Add([]int{89})

	fs2.Include(fs1, fs3)
	fs3.Include(fs4)
	fs4.Include(fs2)
	fs5.Include(fs4)

	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}
	var want = []struct {
		*set.FutureSet
		want string
	}{
		{fs1, "[78]"},
		{fs2, "[1 4 5 7 8 11 12 27 78]"},
		{fs3, "[1 4 5 7 8 11 12 27 78]"},
		{fs4, "[1 4 5 7 8 11 12 27 78]"},
		{fs5, "[1 4 5 7 8 11 12 27 78 89]"},
	}
	for i, exp := range want {
		if got, want := exp.FutureSet.IntSet.String(), exp.want; got != want {
			t.Errorf("fs%v = %v, want: %v", i+1, got, want)
		}
	}
}

func TestClosureIntersection(t *testing.T) {
	c := set.NewClosure(0)
	fs1 := c.Add([]int{1, 2, 3, 4, 5, 6, 9})
	fs2 := c.Add([]int{0, 3, 5, 9, 12})
	fs3 := c.Add([]int{3, 7, 8})
	fs4 := c.Add([]int{9, 11, 12})
	fs5 := c.Add([]int{89})

	fs3.Include(fs4)
	fs4.Include(fs5)
	fs5.Include(fs3)

	i1 := c.Intersect(fs1, fs2) // [3 5 9]
	i2 := c.Intersect(fs2, fs4) // [3 9 12]
	i3 := c.Intersect(i1, i2)   // [3 9]

	i4 := c.Intersect(fs2, c.Complement(i3, nil)) // [0 5 12]
	i5 := c.Intersect(i4, c.Complement(fs1, nil)) // [0 12]

	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}
	var want = []struct {
		*set.FutureSet
		want string
	}{
		{fs1, "[1 2 3 4 5 6 9]"},
		{fs2, "[0 3 5 9 12]"},
		{fs3, "[3 7 8 9 11 12 89]"},
		{fs4, "[3 7 8 9 11 12 89]"},
		{fs5, "[3 7 8 9 11 12 89]"},
		{i1, "[3 5 9]"},
		{i2, "[3 9 12]"},
		{i3, "[3 9]"},
		{i4, "[0 5 12]"},
		{i5, "[0 12]"},
	}
	for i, exp := range want {
		if got, want := exp.FutureSet.IntSet.String(), exp.want; got != want {
			t.Errorf("#%v = %v, want: %v", i+1, got, want)
		}
	}
}

func TestClosureComplement(t *testing.T) {
	c := set.NewClosure(0)
	fs1 := c.Add([]int{1, 2, 3, 4, 5, 6, 9})
	fs2 := c.Add([]int{0, 3, 5, 9, 12})

	fs2.Include(c.Complement(fs1, nil))
	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}

	var want = []struct {
		*set.FutureSet
		want string
	}{
		{fs1, "[1 2 3 4 5 6 9]"},
		{fs2, "~[1 2 4 6]"},
	}
	for i, exp := range want {
		if got, want := exp.FutureSet.IntSet.String(), exp.want; got != want {
			t.Errorf("#%v = %v, want: %v", i+1, got, want)
		}
	}
}

func TestClosureIntersectionCycle(t *testing.T) {
	c := set.NewClosure(0)
	fs1 := c.Add([]int{1, 2, 3, 4, 5, 6, 9})
	fs2 := c.Add([]int{0, 3, 5, 9, 12})

	i1 := c.Intersect(fs1, fs2)
	fs3 := c.Add([]int{1, 48})
	fs2.Include(i1, fs3)

	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}

	var want = []struct {
		*set.FutureSet
		want string
	}{
		{fs1, "[1 2 3 4 5 6 9]"},
		{fs2, "[0 1 3 5 9 12 48]"},
		{i1, "[1 3 5 9]"},
	}
	for i, exp := range want {
		if got, want := exp.FutureSet.IntSet.String(), exp.want; got != want {
			t.Errorf("#%v = %v, want: %v", i+1, got, want)
		}
	}
}

func TestClosureError(t *testing.T) {
	c := set.NewClosure(0)
	fs1 := c.Add([]int{1, 2, 3, 4, 5, 6, 9})
	fs2 := c.Add([]int{0, 3, 5, 9, 12})

	fs1.Include(fs2)
	compl := c.Complement(fs1, nil)
	fs2.Include(compl)

	err := c.Compute()
	ce, ok := err.(set.ClosureError)
	if !ok {
		t.Errorf("Compute() returned %v, want: an error", err)
		return
	}

	if got, want := err.Error(), "complement cannot be part of a dependency cycle"; !strings.Contains(got, want) {
		t.Errorf("Compute() failed with %v, want: .. %v", got, want)
	}

	if len(ce) != 1 || ce[0] != compl {
		t.Errorf("Compute() returned %v, want: [complement node]", err)
	}
}

func TestClosureComplexCycle(t *testing.T) {
	c := set.NewClosure(0)
	fs1 := c.Add([]int{})
	fs2 := c.Add([]int{})
	fs3 := c.Add([]int{})
	fs4 := c.Add([]int{0})
	fs5 := c.Add([]int{})
	fs6 := c.Add([]int{0})
	c1 := c.Complement(fs6, nil)
	i1 := c.Intersect(fs4, c1)
	fs7 := c.Add([]int{})
	fs8 := c.Add([]int{})

	fs1.Include(fs5)
	fs5.Include(fs2)
	fs2.Include(i1)
	fs3.Include(fs2)
	fs4.Include(fs7, fs8)
	fs7.Include(fs3)
	fs8.Include(fs3)

	if err := c.Compute(); err != nil {
		t.Errorf("Compute() failed with %v", err)
	}

	var want = []struct {
		*set.FutureSet
		want string
	}{
		{fs1, "[]"},
		{fs2, "[]"},
		{fs3, "[]"},
		{fs4, "[0]"},
		{fs5, "[]"},
		{fs6, "[0]"},
		{c1, "~[0]"},
		{i1, "[]"},
		{fs7, "[]"},
		{fs8, "[]"},
	}
	for i, exp := range want {
		if got, want := exp.FutureSet.IntSet.String(), exp.want; got != want {
			t.Errorf("#%v = %v, want: %v", i+1, got, want)
		}
	}
}
