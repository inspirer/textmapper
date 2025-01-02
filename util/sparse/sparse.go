// Package sparse introduces a notion of sparse sets.
package sparse

import (
	"slices"

	"github.com/inspirer/textmapper/util/container"
)

// Set is an immutable collection of distinct numbers, typically coming from a certain [0, n) range.
//
// Warning: this list is *unordered*.
type Set []int

// Union computes a union of multiple sets in linear time.
//
// "aux" must have enough bits to capture all the values in "sets". It must contains all zeroes on
// entry and will remain zeroed after this function returns. "reuse" can be provided to reduce
// memory allocations.
func Union(sets []Set, aux container.BitSet, reuse []int) Set {
	if len(sets) == 0 {
		return nil
	}

	var largest, maxSize int
	ret := Set(reuse[:0])
	for i, set := range sets {
		if len(set) > maxSize {
			largest = i
			maxSize = len(set)
		}
		for _, v := range set {
			if aux.Get(int(v)) {
				continue
			}
			aux.Set(v)
			ret = append(ret, v)
		}
	}
	for _, v := range ret {
		aux.Clear(v)
	}
	if len(ret) == maxSize {
		return sets[largest]
	}
	if cap(reuse) >= len(ret) {
		ret = slices.Clone(ret)
	}
	return ret
}

// Builder is a reusable type for easy and safe Set instantiation.
type Builder struct {
	result Set
	seen   container.BitSet
}

// NewBuilder creates a new builder for sets containing numbers in the range of [0, n).
func NewBuilder(n int) *Builder {
	return &Builder{
		seen: container.NewBitSet(n),
	}
}

// Add inserts a value into the set deduplicating values as necessary.
func (b *Builder) Add(val int) {
	if b.seen.Get(val) {
		return
	}
	b.result = append(b.result, val)
	b.seen.Set(val)
}

// Build returns the constructed set and resets this instance for further use.
func (b *Builder) Build() Set {
	for _, v := range b.result {
		b.seen.Clear(v)
	}
	ret := b.result
	b.result = nil
	return ret
}
