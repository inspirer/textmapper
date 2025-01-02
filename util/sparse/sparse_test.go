package sparse_test

import (
	"fmt"
	"testing"

	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/sparse"
)

func TestSparse(t *testing.T) {
	tests := []struct {
		sets      []sparse.Set
		want      string
		wantBuild string
	}{
		{
			sets:      []sparse.Set{},
			want:      "[]",
			wantBuild: "[]",
		},
		{
			sets: []sparse.Set{
				{1, 0, 3},
				{2, 1, 0},
			},
			want:      "[1 0 3 2]",
			wantBuild: "[1 0 3 2]",
		},
		{
			sets: []sparse.Set{
				{1, 0},
				{2, 1, 0},
			},
			want:      "[2 1 0]", // reusing an existing set
			wantBuild: "[1 0 2]",
		},
		{
			sets: []sparse.Set{
				{2, 3, 4},
				{1, 2, 3},
				{3, 4, 5},
			},
			want:      "[2 3 4 1 5]",
			wantBuild: "[2 3 4 1 5]",
		},
	}

	b := sparse.NewBuilder(16)
	for _, tc := range tests {
		aux := container.NewBitSet(16)
		got := sparse.Union(tc.sets, aux, nil /*reuse*/)
		if got := fmt.Sprintf("%v", got); got != tc.want {
			t.Errorf("Union(%v) = %v, want: %v", tc.sets, got, tc.want)
		}

		// Builder should produce (almost) the same result.
		for _, set := range tc.sets {
			for _, v := range set {
				b.Add(v)
			}
		}
		got = b.Build()
		if got := fmt.Sprintf("%v", got); got != tc.wantBuild {
			t.Errorf("Build(%v) = %v, want: %v", tc.sets, got, tc.wantBuild)
		}
	}
}
