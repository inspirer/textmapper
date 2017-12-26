package container_test

import (
	"fmt"
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
	s.Set(63)

	var got []int
	for i := 0; i < 64; i++ {
		if s.Get(i) {
			got = append(got, i)
		}
	}
	if gotStr := fmt.Sprintf("%v", got); gotStr != "[20 40 63]" {
		t.Errorf("Found %v set bits, want: [20 40 63]", got)
	}
}
