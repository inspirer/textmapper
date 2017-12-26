package container

// allocator is used to allocate IntSliceMap values on the first access.
type allocator func(key []int) interface{}

type entry struct {
	key   []int
	value interface{}
}

// IntSliceMap maps slices of ints into values that get automatically allocated upon the
// first access.
type IntSliceMap struct {
	allocate allocator
	data     map[uint64][]entry
}

// NewIntSliceMap returns an empty map, which will use the provided function to instantiate values.
func NewIntSliceMap(allocate allocator) *IntSliceMap {
	return &IntSliceMap{
		allocate: allocate,
		data:     make(map[uint64][]entry),
	}
}

// Get returns the value corresponding to a given key, instantiating it if needed.
func (m *IntSliceMap) Get(key []int) interface{} {
	var hash uint64
	for _, i := range key {
		hash = hash*31 + uint64(i)
	}

	for _, entry := range m.data[hash] {
		if sliceEqual(key, entry.key) {
			return entry.value
		}
	}

	keyCopy := make([]int, len(key))
	copy(keyCopy, key)
	val := m.allocate(key)
	m.data[hash] = append(m.data[hash], entry{keyCopy, val})
	return val
}

func sliceEqual(a, b []int) bool {
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
