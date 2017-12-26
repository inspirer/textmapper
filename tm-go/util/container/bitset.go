// Package container provides container-like data-structures.
package container

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

// Get tests if the bit at a given index is 1.
func (b BitSet) Get(i int) bool {
	return (b[uint(i)/32] & (1 << (uint(i) % 32))) != 0
}

// Clear sets all bits to 0.
func (b BitSet) Clear() {
	for i := len(b) - 1; i >= 0; i-- {
		b[i] = 0
	}
}
