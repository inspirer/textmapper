package graph

import (
	"testing"

	"github.com/inspirer/textmapper/util/container"
)

var tarjanTests = [][][]int{
	{{1}, {}},                          // 0 -> 1
	{{1}, {0}},                         // 0 <-> 1
	{{}, {0}},                          // 0 <- 1
	{{1}, {2}, {0}},                    // 0->1->2->0
	{{1, 2, 3, 4}, {0}, {0}, {4}, {3}}, // (0<->1,2) -> (3<->4)
	{{}, {}, {}, {}, {}},               // 5 separate nodes
	{{}, {3}, {4}, {}, {}},
	{{3}, {3}, {3}, {4}, {}},
	{{3}, {3}, {3}, {4}, {0}},
}

func TestTarjan(t *testing.T) {
	for _, g := range tarjanTests {
		seen := container.NewBitSet(len(g))
		Tarjan(g, func(vs []int, onStack container.BitSet) {
			stack := onStack.Slice(nil)
			for _, n := range stack {
				if seen.Get(n) {
					t.Fatalf("Tarjan(%v) calls the callback with (%v, %v), seen=%v", g, vs, stack, seen.Slice(nil))
				}
			}
			for _, n := range vs {
				if !onStack.Get(n) {
					t.Fatalf("Tarjan(%v) calls the callback with (%v, %v), %v is not onStack", g, vs, stack, n)
				}
				seen.Set(n)
			}
		})
		if got := len(seen.Slice(nil)); got != len(g) {
			t.Errorf("Tarjan(%v) called the callback for %v nodes, want: %v", g, got, len(g))
		}
	}
}
