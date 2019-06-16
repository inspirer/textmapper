// Package container provides container-like data-structures.
package container

import (
	"log"
	"math/bits"
)

// BitSet is a fixed-size bit set.
type BitSet []uint32

// NewBitSet creates a bit set of a given size.
func NewBitSet(size int) BitSet {
	return make([]uint32, (31+size)/32)
}

// Set sets the bit at a given index to 1.
func (b BitSet) Set(i int) {
	b[uint(i)/32] |= 1 << (uint(i) % 32)
}

// Clear resets the bit at a given index to 0.
func (b BitSet) Clear(i int) {
	b[uint(i)/32] &^= 1 << (uint(i) % 32)
}

// Get tests if the bit at a given index is 1.
func (b BitSet) Get(i int) bool {
	return (b[uint(i)/32] & (1 << (uint(i) % 32))) != 0
}

// Slice returns a sorted slice of set bits indices.
func (b BitSet) Slice(reuse []int) []int {
	ret := reuse[:0]
	for i, n := range b {
		// Note: using the bits package gives up to 20x speed-up on very sparse bit sets (1% of ones).
		max := bits.Len32(n)
		for e := bits.TrailingZeros32(n); e < max; e++ {
			if (n & (1 << uint(e))) != 0 {
				ret = append(ret, (i<<5)+e)
			}
		}
	}
	return ret
}

// SetAll sets all bits to 1.
func (b BitSet) SetAll() {
	for i := len(b) - 1; i >= 0; i-- {
		b[i] = ^uint32(0)
	}
}

// ClearAll sets all bits to 0.
func (b BitSet) ClearAll() {
	for i := len(b) - 1; i >= 0; i-- {
		b[i] = 0
	}
}

// Complement reverses the value of each bit (in-place).
func (b BitSet) Complement() {
	for i := len(b) - 1; i >= 0; i-- {
		b[i] = ^b[i]
	}
}

// Or sets all bits that are set in "other".
func (b BitSet) Or(other BitSet) {
	if len(b) != len(other) {
		log.Fatalf("incompatible bitsets")
	}
	for i, oth := range other {
		b[i] |= oth
	}
}
