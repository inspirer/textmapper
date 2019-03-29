package graph

import (
	"testing"
)

func TestMatrixClosure(t *testing.T) {
	m := NewMatrix(20)
	count := func() int {
		var ret int
		for i := 0; i < 20*20; i++ {
			if m.set.Get(i) {
				ret++
			}
		}
		return ret
	}

	m.AddEdge(1, 8)
	m.AddEdge(8, 3)
	m.AddEdge(3, 4)
	m.AddEdge(4, 5)
	m.AddEdge(5, 0)
	m.Closure() // 5 edges + 1+2+3+4

	if got := count(); got != 15 {
		t.Errorf("count() = %v, want: 15", got)
	}

	m.AddEdge(0, 1)
	m.Closure() // 6 nodes, fully connected
	if got := count(); got != 36 {
		t.Errorf("count() = %v, want: 36", got)
	}
}
