package graph

import (
	"github.com/inspirer/textmapper/tm-go/util/container"
)

// Matrix stores a simple directed graph as an adjacency matrix in a bit set.
type Matrix struct {
	n   int
	set container.BitSet
}

// NewMatrix creates a new n x n matrix.
func NewMatrix(n int) Matrix {
	return Matrix{n, container.NewBitSet(n * n)}
}

// AddEdge creates a link from i to e.
func (m Matrix) AddEdge(i, e int) {
	m.set.Set(i*m.n + e)
}

// HasEdge checks if there is a link from i to e.
func (m Matrix) HasEdge(i, e int) bool {
	return m.set.Get(i*m.n + e)
}

// Closure adds links for each path available in the graph.
func (m Matrix) Closure() {
	// [j,i] && [i,e] => [j,e]
	for i := 0; i < m.n; i++ {
		for j := 0; j < m.n; j++ {
			if !m.HasEdge(j, i) {
				continue
			}
			for e := 0; e < m.n; e++ {
				if m.HasEdge(i, e) {
					m.AddEdge(j, e)
				}
			}
		}
	}
}

// Graph returns the adjacency list representation of the graph.
func (m Matrix) Graph(reuse []int) [][]int {
	n := m.n
	ret := make([][]int, n)
	slice := m.set.Slice(reuse)
	var start, index int
	for i, val := range slice {
		e := val / n
		slice[i] = val % n
		if e > index {
			ret[index] = slice[start:i]
			start = i
			index = e
		}
	}
	ret[index] = slice[start:]
	return ret
}
