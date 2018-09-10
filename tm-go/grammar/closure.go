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
	t := tarjan{buf: c.buf, interner: c.interner, nodes: c.nodes}
	t.run()
	return t.err.err()
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
		s.edges = make([]int, 0, len(sets))
	}
	for _, set := range sets {
		s.edges = append(s.edges, set.index)
	}
}

// ClosureError enumerates all offending complement sets.
type ClosureError []*FutureSet

func (c ClosureError) err() error {
	if len(c) == 0 {
		return nil
	}
	return c
}

// Error implements error
func (c ClosureError) Error() string {
	return "Set complement cannot be part of a dependency cycle."
}

type tarjan struct {
	buf      []int // for reuse
	interner *container.IntSliceMap
	nodes    []*FutureSet

	stack   []int
	index   []int
	lowLink []int
	onStack container.BitSet
	curr    int
	err     ClosureError
}

func (t *tarjan) run() {
	size := len(t.nodes)
	if size < 2 {
		return
	}

	t.stack = nil
	t.index = make([]int, size)
	for i := range t.index {
		t.index[i] = -1
	}
	t.lowLink = make([]int, size)
	t.onStack = container.NewBitSet(size)

	t.curr = 0
	for i := 0; i < size; i++ {
		if t.index[i] == -1 {
			t.strongConnect(i)
		}
	}
}

func (t *tarjan) strongConnect(v int) {
	base := len(t.stack)
	t.index[v] = t.curr
	t.lowLink[v] = t.curr
	t.curr++
	t.stack = append(t.stack, v)
	t.onStack.Set(v)

	for _, w := range t.nodes[v].edges {
		if t.index[w] == -1 {
			t.strongConnect(w)
			if t.lowLink[w] < t.lowLink[v] {
				t.lowLink[v] = t.lowLink[w]
			}
		} else if t.onStack.Get(w) && t.index[w] < t.lowLink[v] {
			t.lowLink[v] = t.index[w]
		}
	}

	if t.lowLink[v] == t.index[v] {
		t.closure(t.stack[base:])
		for _, v := range t.stack[base:] {
			t.onStack.Clear(v)
		}
		t.stack = t.stack[:base]
	}
}

func (t *tarjan) closure(component []int) {
	for _, q := range component {
		if t.nodes[q].op == intersection {
			t.slowClosure(component)
			return
		}
	}

	// Simple union (no intersections).
	var res container.IntSet
	for _, v := range component {
		fs := t.nodes[v]
		res = t.intern(container.Merge(res, fs.IntSet, t.buf))
		for _, w := range t.nodes[v].edges {
			set := t.nodes[w].IntSet
			if fs.op == complement {
				set = set.Complement()
				if t.onStack.Get(w) {
					// Complements cannot be part of a dependency cycle.
					t.err = append(t.err, fs)
					continue
				}
			}
			if !t.onStack.Get(w) {
				res = t.intern(container.Merge(res, set, t.buf))
			}
		}
	}

	if len(t.err) != 0 {
		return
	}

	for _, v := range component {
		t.nodes[v].IntSet = res
	}
}

func (t *tarjan) slowClosure(component []int) {
	for {
		var dirty bool
		for _, v := range component {
			fs := t.nodes[v]
			var res container.IntSet
			switch fs.op {
			case intersection:
				res.Inverse = true
				for _, w := range t.nodes[v].edges {
					res = container.Intersect(res, t.nodes[w].IntSet, t.buf)
				}
			case union:
				res = fs.IntSet
				for _, w := range t.nodes[v].edges {
					res = t.intern(container.Merge(res, t.nodes[w].IntSet, t.buf))
				}
			case complement:
				if len(t.nodes[v].edges) != 1 {
					panic("broken invariant")
				}
				w := t.nodes[v].edges[0]
				if t.onStack.Get(w) {
					// Complements cannot be part of a dependency cycle.
					t.err = append(t.err, fs)
					continue
				}
				res = t.nodes[w].IntSet.Complement()
			}
			res = t.intern(res)
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

func (t *tarjan) intern(set container.IntSet) container.IntSet {
	s := t.interner.Get(set.Set).([]int)
	return container.IntSet{Set: s, Inverse: set.Inverse}
}
