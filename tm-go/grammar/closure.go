package grammar

import (
	"log"

	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
)

// Closure takes multiple integer sets and expands them according to the provided rules: sets can
// include each other, be complements or intersections of other sets.
type Closure struct {
	buf      []int // for reuse
	interner *container.IntSliceMap
	nodes    []*FutureSet
}

// NewClosure returns a new Closure instance.
func NewClosure(bufSize int) *Closure {
	return &Closure{
		buf:      make([]int, bufSize),
		interner: container.NewIntSliceMap(func(key []int) interface{} { return key }),
	}
}

// Add registers a new, possibly empty set.
func (c *Closure) Add(set []int) *FutureSet {
	set = c.interner.Get(set).([]int) // R/O and unique copy
	ret := &FutureSet{index: len(c.nodes), IntSet: container.IntSet{Set: set}}
	c.nodes = append(c.nodes, ret)
	return ret
}

// Intersect produces a new FutureSet that will be an intersection of the given sets.
func (c *Closure) Intersect(sets ...*FutureSet) *FutureSet {
	ret := c.Add(nil)
	ret.Include(sets...)
	ret.op = intersection
	return ret
}

// Complement produces the complement of a given set. This computation might fail if "set"
// transitively depends on itself.
// Example: A = ~B; B = ~C ; C = A (there exist an infinite number of solution)
func (c *Closure) Complement(set *FutureSet, origin status.SourceNode) *FutureSet {
	ret := c.Add(nil)
	ret.op = complement
	ret.edges = []int{set.index}
	ret.Origin = origin
	return ret
}

// Compute populates IntSet in all future sets returned by this Closure.
// Returns an error (ClosureError) if there exists a complement set transitively depending on
// itself.
func (c *Closure) Compute() error {
	// TODO implement
	return nil
}

type op uint8

const (
	union op = iota
	intersection
	complement
)

// FutureSet represents a not-yet computed integer set.
type FutureSet struct {
	index  int
	op     op
	edges  []int
	Origin status.SourceNode

	container.IntSet
}

// Include adds all elements from the given sets into this one (transitively).
func (s *FutureSet) Include(sets ...*FutureSet) {
	if s.op != union {
		log.Fatal("cannot expand a non-union FutureSet")
	}
	if s.edges == nil {
		s.edges = make([]int, len(sets))
	}
	for _, set := range sets {
		s.edges = append(s.edges, set.index)
	}
}

// ClosureError enumerates all offending complement sets.
type ClosureError []*FutureSet

// Error implements error
func (c *ClosureError) Error() string {
	return "Set complement cannot be part of a dependency cycle."
}
