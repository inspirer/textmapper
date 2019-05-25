package graph

import (
	"github.com/inspirer/textmapper/tm-go/util/container"
)

// Tarjan calls the provided function for each strongly connected component in the graph in the
// topological order.
func Tarjan(graph [][]int, f func(vertices []int, onStack container.BitSet)) {
	t := tarjan{graph: graph, callback: f}
	t.run()
}

type tarjan struct {
	graph    [][]int
	callback func(vertices []int, onStack container.BitSet)

	stack   []int
	index   []int
	lowLink []int
	onStack container.BitSet
	curr    int
}

func (t *tarjan) run() {
	size := len(t.graph)
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

	for _, w := range t.graph[v] {
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
		t.callback(t.stack[base:], t.onStack)
		for _, v := range t.stack[base:] {
			t.onStack.Clear(v)
		}
		t.stack = t.stack[:base]
	}
}
