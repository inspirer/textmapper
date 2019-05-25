package grammar

import (
	"log"

	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
	"github.com/inspirer/textmapper/tm-go/util/graph"
)

// Closure takes multiple integer sets and expands them according to the provided rules: sets can
// include each other, be complements or intersections of other sets.
type Closure struct {
	buf      []int // for reuse
	interner *container.IntSliceMap
	nodes    []*FutureSet
	err      ClosureError
}

// NewClosure returns a new Closure instance.
func NewClosure(bufSize int) *Closure {
	return &Closure{
		buf:      make([]int, bufSize),
		interner: container.NewIntSliceMap(func(key []int) interface{} { return key }),
	}
}

// FutureSet represents a not-yet computed integer set.
type FutureSet struct {
	index  int
	op     op
	edges  []int
	Origin status.SourceNode

	container.IntSet
}

type op uint8

const (
	union op = iota
	intersection
	complement
)

// Include adds all elements from the given sets into this one (transitively).
func (s *FutureSet) Include(sets ...*FutureSet) {
	if s.op != union {
		log.Fatal("cannot expand a non-union FutureSet")
	}
	if s.edges == nil {
		s.edges = make([]int, 0, len(sets))
	}
	for _, set := range sets {
		s.edges = append(s.edges, set.index)
	}
}

// ClosureError enumerates all offending complement sets.
type ClosureError []*FutureSet

// Error implements error
func (c ClosureError) Error() string {
	return "Set complement cannot be part of a dependency cycle."
}

func (c ClosureError) err() error {
	if len(c) == 0 {
		return nil
	}
	return c
}

// Add registers a new (possibly empty) set.
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
// Example: A = ~B; B = ~C ; C = A (there exist an infinite number of solutions)
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
	var g [][]int
	for _, n := range c.nodes {
		g = append(g, n.edges)
	}
	graph.Tarjan(g, c.closure)
	return c.err.err()
}

func (c *Closure) closure(component []int, onStack container.BitSet) {
	for _, q := range component {
		if c.nodes[q].op == intersection {
			c.slowClosure(component, onStack)
			return
		}
	}

	// Simple union (no intersections).
	var res container.IntSet
	for _, v := range component {
		fs := c.nodes[v]
		res = c.intern(container.Merge(res, fs.IntSet, c.buf))
		for _, w := range c.nodes[v].edges {
			set := c.nodes[w].IntSet
			if fs.op == complement {
				set = set.Complement()
				if onStack.Get(w) {
					// Complements cannot be part of a dependency cycle.
					c.err = append(c.err, fs)
					continue
				}
			}
			if !onStack.Get(w) {
				res = c.intern(container.Merge(res, set, c.buf))
			}
		}
	}

	if len(c.err) != 0 {
		return
	}

	for _, v := range component {
		c.nodes[v].IntSet = res
	}
}

func (c *Closure) slowClosure(component []int, onStack container.BitSet) {
	for {
		var dirty bool
		for _, v := range component {
			fs := c.nodes[v]
			var res container.IntSet
			switch fs.op {
			case intersection:
				res.Inverse = true
				for _, w := range c.nodes[v].edges {
					res = container.Intersect(res, c.nodes[w].IntSet, c.buf)
				}
			case union:
				res = fs.IntSet
				for _, w := range c.nodes[v].edges {
					res = c.intern(container.Merge(res, c.nodes[w].IntSet, c.buf))
				}
			case complement:
				if len(c.nodes[v].edges) != 1 {
					log.Fatal("broken invariant")
				}
				w := c.nodes[v].edges[0]
				if onStack.Get(w) {
					// Complements cannot be part of a dependency cycle.
					c.err = append(c.err, fs)
					continue
				}
				res = c.nodes[w].IntSet.Complement()
			}
			res = c.intern(res)
			if !res.Equals(fs.IntSet) {
				fs.IntSet = res
				dirty = true
			}
		}
		if !dirty {
			break
		}
	}
}

func (c *Closure) intern(set container.IntSet) container.IntSet {
	s := c.interner.Get(set.Set).([]int)
	return container.IntSet{Set: s, Inverse: set.Inverse}
}
