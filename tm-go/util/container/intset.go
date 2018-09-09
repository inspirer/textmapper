package container

import (
	"fmt"
)

// IntSet efficiently stores, merges and intersects integer sets (represented as sorted arrays).
type IntSet struct {
	Inverse bool
	Set     []int // sorted
}

// Empty checks if a set is empty.
func (s IntSet) Empty() bool {
	return len(s.Set) == 0 && !s.Inverse
}

// Complement returns a complement set to this one.
func (s IntSet) Complement() IntSet {
	return IntSet{!s.Inverse, s.Set}
}

func (s IntSet) String() string {
	var prefix string
	if s.Inverse {
		prefix = "~"
	}
	return fmt.Sprintf("%v%v", prefix, s.Set)
}

// Intersect computes an intersection of two sets.
func Intersect(a, b IntSet, reuse []int) IntSet {
	switch {
	case a.Empty() || b.Empty():
		return IntSet{}
	case a.Inverse:
		if b.Inverse {
			return IntSet{Inverse: true, Set: combine(a.Set, b.Set, reuse)}
		}
		return IntSet{Set: subtract(b.Set, a.Set, reuse)}
	case b.Inverse:
		return IntSet{Set: subtract(a.Set, b.Set, reuse)}
	}
	return IntSet{Set: intersect(a.Set, b.Set, reuse)}
}

// Merge computes a union of two sets.
func Merge(a, b IntSet, reuse []int) IntSet {
	switch {
	case a.Empty():
		return b
	case b.Empty():
		return a
	case a.Inverse:
		if b.Inverse {
			return IntSet{Inverse: true, Set: intersect(a.Set, b.Set, reuse)}
		}
		return IntSet{Inverse: true, Set: subtract(a.Set, b.Set, reuse)}
	case b.Inverse:
		return IntSet{Inverse: true, Set: subtract(b.Set, a.Set, reuse)}
	}
	return IntSet{Set: combine(a.Set, b.Set, reuse)}
}

func combine(a, b, reuse []int) []int {
	ret := reuse[:0]
	var e int
	bl := len(b)
	for _, v := range a {
		for e < bl && b[e] < v {
			ret = append(ret, b[e])
			e++
		}
		if e < bl && b[e] == v {
			e++
		}
		ret = append(ret, v)
	}
	for _, v := range b[e:] {
		ret = append(ret, v)
	}
	return ret
}

func intersect(a, b, reuse []int) []int {
	ret := reuse[:0]
	var e int
	bl := len(b)
	for _, v := range a {
		for e < bl && b[e] < v {
			e++
		}
		if e < bl && b[e] == v {
			ret = append(ret, v)
		}
	}
	return ret
}

func subtract(a, b, reuse []int) []int {
	ret := reuse[:0]
	var e int
	bl := len(b)
	for _, v := range a {
		for e < bl && b[e] < v {
			e++
		}
		if e < bl && b[e] == v {
			continue
		}
		ret = append(ret, v)
	}
	return ret
}
