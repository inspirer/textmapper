package gen

import (
	"fmt"
	"math/rand"
	"strings"
	"testing"
)

func TestHashing(t *testing.T) {
	tests := []struct {
		input string
		want  string
	}{
		{"", "0"},
		{" ", "0x20"},
		{"__", "0xbe0"},
		{"shift", "0x6856c82"},
		{"inline", "0xb96da299"},
		{"verylongstring", "0x1200fb43"},
	}

	for _, tc := range tests {
		if got := hex(stringHash(tc.input)); got != tc.want {
			t.Errorf("stringHash(%v) = %v, want: %v", tc.input, got, tc.want)
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
		{[]int{0}, 0, "\n  0,\n"},
		{[]int{-128, 12}, 12, "\n  -128, 12,\n"},
		{[]int{-128, 128}, 12, "\n  -128,\n  128,\n"},
		{[]int{1, 2, 3, 42, 5, 6, 77, 888, 9, 10, 11}, 13, "\n  1, 2, 3,\n  42, 5, 6,\n  77, 888,\n  9, 10, 11,\n"},
		{[]int{1, 2, 3}, 2, "\n  1,\n  2,\n  3,\n"}, // not enough width
	}

	for _, tc := range tests {
		if got := intArray(tc.input, "  ", tc.width); got != tc.want {
			t.Errorf("intArray(%v) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}

func TestStringSwitch(t *testing.T) {
	swtch := asStringSwitch(map[string]int{
		"abc":  1,
		"def":  2,
		"a":    3,
		"z":    4,
		"abba": 5,
		"e":    6,
		"y":    7,
	})
	const want = "/8=>[0=>[(0x2d9420,abba)=>5] 1=>[(0x61,a)=>3 (0x79,y)=>7] 2=>[(0x17862,abc)=>1 (0x7a,z)=>4] 5=>[(0x18405,def)=>2 (0x65,e)=>6]]"
	if got := switchString(swtch); got != want {
		t.Errorf("stringSwitch() = %v, want: %v", got, want)
	}
}

func switchString(s stringSwitch) string {
	var buf strings.Builder
	fmt.Fprintf(&buf, "/%v=>[", s.Size)
	for i, c := range s.Cases {
		if i > 0 {
			buf.WriteRune(' ')
		}
		fmt.Fprintf(&buf, "%v=>[", hex(c.Value))
		for i, s := range c.Subcases {
			if i > 0 {
				buf.WriteRune(' ')
			}
			fmt.Fprintf(&buf, "(%v,%v)=>%v", hex(s.Hash), s.Str, s.Action)
		}
		buf.WriteRune(']')
	}
	buf.WriteRune(']')
	return buf.String()
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
