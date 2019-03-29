package lalr

import (
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
	"github.com/inspirer/textmapper/tm-go/util/graph"
)

// Compile generates LALR tables for a given grammar.
func Compile(grammar *Grammar) (*Tables, error) {
	c := &compiler{
		grammar: grammar,
		out:     &Tables{},
		empty:   container.NewBitSet(grammar.Symbols),
	}

	c.init()
	c.computeEmpty()
	c.computeSets()
	c.computeStates()
	// TODO
	return c.out, c.s.Err()
}

type compiler struct {
	grammar *Grammar
	out     *Tables
	s       status.Status

	index []int // the rule start in "right"
	right []int // all rules flattened into one slice, each position in this slice is an LR(0) item
	empty container.BitSet

	states []*state
}

type state struct {
	index       int
	symbol      Sym
	sourceState int
	core        []int
	shifts      []int
	reduce      []int
	lr0         bool
}

func (c *compiler) init() {
	right := make([]int, 0, len(c.grammar.Rules)*8)
	for _, r := range c.grammar.Rules {
		c.index = append(c.index, len(right))
		for _, sym := range r.RHS {
			if sym.IsStateMarker() {
				// TODO support state markers
				continue
			}
			right = append(right, int(sym))
		}
		right = append(right, -1-int(r.LHS))
	}
	c.right = right

}

// computeEmpty computes the set of nullable nonterminals.
func (c *compiler) computeEmpty() {
	for {
		var keepGoing bool
		for _, r := range c.grammar.Rules {
			if c.empty.Get(int(r.LHS)) {
				continue
			}
			empty := true
			for _, sym := range r.RHS {
				if !sym.IsStateMarker() && !c.empty.Get(int(sym)) {
					empty = false
					break
				}
			}
			if empty {
				c.empty.Set(int(r.LHS))
				keepGoing = true
			}
		}
		if !keepGoing {
			break
		}
	}
}

func (c *compiler) computeSets() {
	d := c.grammar.Terminals
	n := c.grammar.Symbols - d
	first := graph.NewMatrix(n)
	for i, r := range c.grammar.Rules {
		if e := c.right[c.index[i]]; e >= d {
			first.AddEdge(int(r.LHS)-d, e-d)
		}
	}
	first.Closure()
	for i := 0; i < n; i++ {
		first.AddEdge(i, i)
	}
	var rules []container.BitSet // nonterminal -> available rules
	// TODO
	_ = rules
}

func (c *compiler) computeStates() {
	container.NewIntSliceMap(func(key []int) interface{} { return new(state) })

}
