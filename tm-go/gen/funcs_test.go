package gen

import (
	"math/rand"
	"testing"
)

func TestHashing(t *testing.T) {
	tests := []struct {
		input     string
		want      string
		wantIndex uint32
	}{
		{"", "0x0", 0},
		{" ", "0x20", 0},
		{"__", "0xbe0", 0},
		{"shift", "0x6856c82", 2},
		{"inline", "0xb96da299", 25},
		{"verylongstring", "0x1200fb43", 3},
	}

	for _, tc := range tests {
		if got := stringHash(tc.input); got != tc.want {
			t.Errorf("stringHash(%v) = %v, want: %v", tc.input, got, tc.want)
		}
		if got := rangedHash(tc.input, 32); got != tc.wantIndex {
			t.Errorf("rangedHash(%v) = %v, want: %v", tc.input, got, tc.wantIndex)
		}
	}
}

func TestBits(t *testing.T) {
	tests := []struct {
		input []int
		want  int
	}{
		{[]int{}, 8},
		{[]int{-128}, 8},
		{[]int{-128, 128}, 16},
		{[]int{65536}, 32},
		{[]int{1 << 31}, 32},
	}

	for _, tc := range tests {
		if got := bitsPerElement(tc.input); got != tc.want {
			t.Errorf("bitsPerElement(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}

func TestIntArray(t *testing.T) {
	tests := []struct {
		input []int
		width int
		want  string
	}{
		{[]int{-128, 128}, 12, "  -128, 128"},
		{[]int{-128, 1280}, 12, "  -128,\n  1280"},
		{[]int{1, 2, 3, 42, 5, 6, 77, 888, 9, 10, 11}, 12, "  1, 2, 3,\n  42, 5, 6,\n  77, 888,\n  9, 10, 11"},
		{[]int{1, 2, 3}, 2, "  1,\n  2,\n  3"}, // not enough width
	}

	for _, tc := range tests {
		if got := intArray(tc.input, "  ", tc.width); got != tc.want {
			t.Errorf("intArray(%v) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}

func BenchmarkIntArray(b *testing.B) {
	r := rand.New(rand.NewSource(42))
	var data [8192]int
	for i := 0; i < len(data); i++ {
		data[i] = r.Intn(1<<16) - 1<<14
	}
	str := intArray(data[:], "  ", 80)
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		intArray(data[:], "  ", 80)
	}
	b.SetBytes(int64(len(str)))
}
