package lex

import (
	"encoding/binary"
	"sort"
)

// symlist is a sorted list of DFA input symbols.
type symlist []Sym

// compressCharsets combines unicode runes into equivalence classes that become input symbols for
// the generated DFA. All characters mapped into one symbol are either a subset or do not belong
// to any of the given charsets.
func compressCharsets(sets []charset) (out []symlist, inputMap []RangeEntry) {
	type rng struct {
		index      int
		start, end rune // inclusive
		delta      int
	}

	var ranges []rng
	for index, cs := range sets {
		for i := 0; i < len(cs); i += 2 {
			ranges = append(ranges, rng{
				index: index,
				start: cs[i],
				end:   cs[i+1],
				delta: 1,
			})
		}
	}

	if len(ranges) == 0 {
		return nil, nil
	}

	sort.Slice(ranges, func(i, j int) bool {
		if ranges[i].start != ranges[j].start {
			return ranges[i].start < ranges[j].start
		}
		if ranges[i].end != ranges[j].end {
			return ranges[i].end < ranges[j].end
		}
		return ranges[i].index < ranges[j].index
	})

	out = make([]symlist, len(sets))
	chunk := make([]int, 0, len(sets))
	b := make([]byte, 4*len(sets))

	var start rune
	var first int

	counter := Sym(2)
	m := make(map[string]Sym)
	dd := make(map[[2]int]bool)
	l := len(ranges)
	for {
		for first < l && ranges[first].end < start {
			first += ranges[first].delta
		}
		if first == l {
			break
		}
		if ranges[first].start > start {
			start = ranges[first].start
		}

		prev := &first
		chunk = chunk[:0]
		end := ranges[first].end
		var i int
		for i = first; i < l && ranges[i].start <= start; i += ranges[i].delta {
			if ranges[i].end < start {
				*prev += ranges[i].delta
				continue
			}
			if ranges[i].end < end {
				end = ranges[i].end
			}
			chunk = append(chunk, ranges[i].index)
			prev = &ranges[i].delta
		}
		if i < l && ranges[i].start-1 < end {
			end = ranges[i].start - 1
		}
		sort.Ints(chunk)
		var size int
		for _, index := range chunk {
			binary.LittleEndian.PutUint32(b[size:], uint32(index))
			size += 4
		}
		key := string(b[:size]) // allocates key
		id, ok := m[key]
		if !ok {
			id = counter
			counter++
			m[key] = id
		}

		for _, index := range chunk {
			key := [2]int{index, int(id)}
			if dd[key] {
				continue
			}
			out[index] = append(out[index], id)
			dd[key] = true
		}
		inputMap = append(inputMap, RangeEntry{start, end, id})
		start = end + 1
	}
	return
}
