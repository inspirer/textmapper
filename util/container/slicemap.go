package container

// allocator is used to allocate IntSliceMap values on the first access.
type allocator[T any] func(key []int) T

type entry[T any] struct {
	key   []int
	value T
}

// IntSliceMap maps slices of ints into values that get automatically allocated upon the
// first access.
type IntSliceMap[T any] struct {
	allocate allocator[T]
	data     map[uint64][]entry[T]
}

// NewIntSliceMap returns an empty map, which will use the provided function to instantiate values.
func NewIntSliceMap[T any](allocate allocator[T]) *IntSliceMap[T] {
	return &IntSliceMap[T]{
		allocate: allocate,
		data:     make(map[uint64][]entry[T]),
	}
}

// Get returns the value corresponding to a given key, instantiating it if needed.
func (m *IntSliceMap[T]) Get(key []int) T {
	var hash uint64
	for _, i := range key {
		hash = hash*31 + uint64(i)
	}

	for _, entry := range m.data[hash] {
		if SliceEqual(key, entry.key) {
			return entry.value
		}
	}

	keyCopy := make([]int, len(key))
	copy(keyCopy, key)
	val := m.allocate(keyCopy)
	m.data[hash] = append(m.data[hash], entry[T]{keyCopy, val})
	return val
}

// SliceEqual compares two slices for equality.
func SliceEqual(a, b []int) bool {
	if len(a) != len(b) {
		return false
	}
	for i, ai := range a {
		if ai != b[i] {
			return false
		}
	}
	return true
}
