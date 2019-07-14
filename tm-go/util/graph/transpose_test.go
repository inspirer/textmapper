package graph

import (
	"fmt"
	"testing"
)

var transposeTests = []struct {
	g    [][]int
	want string
}{
	{[][]int{{0}}, "[[0]]"},                   // 0<->0
	{[][]int{{1}, {}}, "[[] [0]]"},            // 0 <- 1
	{[][]int{{1}, {0}}, "[[1] [0]]"},          // 0 <-> 1
	{[][]int{{1}, {2}, {0}}, "[[2] [0] [1]]"}, // 0<-1<-2<-0
	{[][]int{{0, 1, 2}, {2, 1}, {0}}, "[[0 2] [0 1] [0 1]]"},
}

func TestTranspose(t *testing.T) {
	for _, tc := range transposeTests {
		tg := Transpose(tc.g)
		if got := fmt.Sprintf("%v", tg); got != tc.want {
			t.Errorf("Transpose(%v) = %v, want: %v", tc.g, got, tc.want)
		}
	}
}
