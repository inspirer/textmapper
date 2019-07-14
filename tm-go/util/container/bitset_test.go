package container_test

import (
	"fmt"
	"math/rand"
	"testing"

	"github.com/inspirer/textmapper/tm-go/util/container"
)

func TestBitSet(t *testing.T) {
	for i := 0; i < 100; i++ {
		s := container.NewBitSet(i)
		if i > 0 {
			s.Set(i - 1)
		}
	}

	s := container.NewBitSet(64)
	s.Set(20)
	s.Set(40)
	s.Set(42)
	s.Set(63)
	s.Clear(42)

	var got []int
	for i := 0; i < 64; i++ {
		if s.Get(i) {
			got = append(got, i)
		}
	}
	if gotStr := fmt.Sprintf("%v", got); gotStr != "[20 40 63]" {
		t.Errorf("Found %v set bits, want: [20 40 63]", got)
	}

	got = s.Slice(nil)
	if gotStr := fmt.Sprintf("%v", got); gotStr != "[20 40 63]" {
		t.Errorf("Slice() = %v, want: [20 40 63]", got)
	}

	s = container.NewBitSet(128)
	s.SetAll(74)
	s.ClearAll(72)

	got = s.Slice(nil)
	if gotStr := fmt.Sprintf("%v", got); gotStr != "[72 73]" {
		t.Errorf("Slice() = %v, want: [72 73]", got)
	}

	s = container.NewBitSet(32)
	s.SetAll(32)
	if s[0] != ^uint32(0) {
		t.Errorf("SetAll(32) = %b, want: 32x ones", s[0])
	}

	s.Complement(30)
	if s[0] != 3<<30 {
		t.Errorf("Complement(32) = %b, want: 110000..00 (30x zeroes)", s[0])
	}

	s.ClearAll(32)
	if s[0] != 0 {
		t.Errorf("ClearAll(32) = %b, want: 0", s[0])
	}
}

func BenchmarkBitSetSlice(b *testing.B) {
	s := container.NewBitSet(1024)
	r := rand.New(rand.NewSource(99))
	for i := 0; i < 20; i++ {
		s.Set(r.Intn(1024))
	}
	val := s.Slice(nil)

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		s.Slice(val)
	}
}
