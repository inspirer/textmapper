package graph

import (
	"fmt"
	"testing"
)

var pathTests = []struct {
	g    [][]int
	want string
}{
	{
		g:    [][]int{},
		want: "[]",
	},
	{
		g: [][]int{
			{},
		},
		want: "[0]",
	},
	{
		g: [][]int{
			{1},
			{0}, // cycle
		},
		want: "[]",
	},
	{
		g: [][]int{
			{1},
			{},
			{0},
			{},
		},
		want: "[2 0 1]",
	},
	{
		g: [][]int{
			{1},
			{2, 3},
			{3},
			{},
		},
		want: "[0 1 2 3]",
	},
}

func TestLongestPath(t *testing.T) {
	for _, tc := range pathTests {
		path := LongestPath(tc.g)
		if got := fmt.Sprintf("%v", path); got != tc.want {
			t.Errorf("LongestPath(%v) = %v, want: %v", tc.g, got, tc.want)
		}
	}
}
