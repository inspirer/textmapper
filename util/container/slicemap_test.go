package container_test

import (
	"testing"

	"github.com/inspirer/textmapper/util/container"
)

func TestSliceMap(t *testing.T) {
	var counter int
	sm := container.NewIntSliceMap(func(key []int) interface{} {
		val := counter
		counter++
		return val
	})

	for _, tc := range []struct {
		input []int
		want  int
	}{
		{[]int{1}, 0},
		{[]int{1}, 0},
		{[]int{1, 2, 3}, 1},
		{[]int{1, 2, 3, 4}, 2},
		{[]int{1, 2, 3}, 1},
		{[]int{1}, 0},
		{[]int{}, 3},
		{[]int{}, 3},
	} {
		got := sm.Get(tc.input).(int)
		if got != tc.want {
			t.Errorf("Get(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}
