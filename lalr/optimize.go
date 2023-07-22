package lalr

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/util/container"
)

// DisplacementEnc is a less compact but faster representation of the parser tables.
type DisplacementEnc struct {
	DefGoto []int // nonterm -> new state (or -1)
	Goto    []int // nonterm -> offset in Table

	DefAct []int // state -> rule to reduce, -1 for an error, -2-state for a shift
	Action []int // state -> a) val==Base => use DefAct, b) val>Base => use Table[val+next]

	Base  int
	Table []int // storage for actions and target states
	Check []int // == j, if Table[D(i)+j] is a stored value
}

func (e *DisplacementEnc) TableStats() string {
	var b strings.Builder

	kb := func(size int) string {
		return fmt.Sprintf("%.1f KB", float64(size)/1024.)
	}

	var zero int
	for _, val := range e.Check {
		if val == -1 {
			zero++
		}
	}

	fmt.Fprintf(&b, "Faster encoding:\n\tTable+Check = %v, zero = %.2f%%\n", kb(len(e.Table)*4*2), float64(zero*100)/float64(len(e.Check)))
	fmt.Fprintf(&b, "\tDefAct+Action = %v\n", kb(len(e.Action)*4*2))
	fmt.Fprintf(&b, "\tDefGoto+Goto = %v\n", kb(len(e.DefGoto)*4*2))

	return b.String()
}

// Optimize converts parsing tables into a faster displacement encoding.
func Optimize(t *DefaultEnc, terms, syms int) *DisplacementEnc {
	ret := &DisplacementEnc{
		DefGoto: make([]int, syms-terms),
		Goto:    make([]int, syms-terms),
		DefAct:  make([]int, len(t.Action)),
		Action:  make([]int, len(t.Action)),
		Base:    -terms,
	}

	next := make([]int, terms)
	var lines []line
	var target []int // of each line: >= for Action, -1-nonterm for Goto

	for state, act := range t.Action {
		switch {
		case act >= 0:
			ret.DefAct[state] = act
			ret.Action[state] = ret.Base
			continue
		case act == -1: // shift
			for i := 0; i < terms; i++ {
				state := t.gotoState(state, i)
				if state >= 0 {
					next[i] = -2 - state
				} else {
					next[i] = -1 // error
				}
			}
		case act == -2: // error
			ret.DefAct[state] = -1
			ret.Action[state] = ret.Base
			continue
		default:
			for i := range next {
				next[i] = -1
			}
			a := -act - 3
			for ; t.Lalr[a] >= 0; a += 2 {
				term := t.Lalr[a]
				act := t.Lalr[a+1]
				switch act {
				case -1: // shift
					state := t.gotoState(state, term)
					if state < 0 {
						log.Fatal("internal invariant violated: shift action without goto")
					}
					act = -2 - state
				case -2: // error
					act = -1
				}
				next[term] = act
			}
		}

		def := pickDefault(next, -1) // error as the backup
		ret.DefAct[state] = def

		var pairs []pair
		for term, val := range next {
			if val != def {
				pairs = append(pairs, pair{term, val})
			}
		}
		if len(pairs) > 0 {
			lines = append(lines, line{pairs})
			target = append(target, state)
		} else {
			ret.Action[state] = ret.Base
		}
	}

	toState := make([]int, len(t.Action))
	for nt := 0; nt < syms-terms; nt++ {
		min := t.Goto[terms+nt]
		max := t.Goto[terms+nt+1]

		for i := range toState {
			toState[i] = -1 // error
		}
		for i := min; i < max; i += 2 {
			toState[t.FromTo[i]] = t.FromTo[i+1]
		}

		def := pickDefault(toState, -1)
		ret.DefGoto[nt] = def

		var pairs []pair
		for from, to := range toState {
			if to != def {
				pairs = append(pairs, pair{from, to})
			}
		}
		if len(pairs) > 0 {
			lines = append(lines, line{pairs})
			target = append(target, -1-nt)
		} else {
			ret.Goto[nt] = -syms // guaranteed to fall back to the default
		}
	}

	indices, table, check := pack(lines)
	for i, target := range target {
		val := indices[i]
		if target >= 0 {
			ret.Action[target] = val
		} else {
			ret.Goto[-1-target] = val
		}
	}

	ret.Table = table
	ret.Check = check
	return ret
}

type line struct {
	pairs []pair // non-empty, sorted by pos
}

type pair struct {
	pos, val int
}

func pack(input []line) (indices, table, check []int) {
	type entry struct {
		index int
		pairs []pair
	}
	entries := make([]entry, 0, len(input))
	indices = make([]int, len(input))

	var total int
	for i, l := range input {
		entries = append(entries, entry{index: i, pairs: l.pairs})
		total += len(l.pairs)
	}
	sort.SliceStable(entries, func(i, j int) bool {
		return len(entries[i].pairs) > len(entries[j].pairs)
	})
	table = make([]int, total)
	check = make([]int, total)
	var allocator allocator

	// Iterate in the order of decreasing number of pairs.
	for _, e := range entries {
		if len(e.pairs) == 0 {
			log.Fatal("internal invariant violated: empty line")
		}
		base := allocator.place(e.pairs)
		last := e.pairs[len(e.pairs)-1].pos
		minSize := base + last + 1
		if len(table) < minSize {
			tmp := make([]int, minSize-len(table))
			table = append(table, tmp...)
			check = append(check, tmp...)
		}
		for _, p := range e.pairs {
			table[base+p.pos] = p.val
			check[base+p.pos] = p.pos + 1
		}
		indices[e.index] = base
	}
	for i, e := range check {
		check[i] = e - 1 // -1 means "unused"
	}
	return indices, table[:allocator.size], check[:allocator.size]
}

type allocator struct {
	size  int
	taken container.BitSet
}

func (a *allocator) place(pairs []pair) (base int) {
	min, max := pairs[0].pos, pairs[len(pairs)-1].pos

	// Reserve enough bits to allocate these pairs at the very end of the table.
	a.taken.Grow(a.size + max - min + 1)
	defer func() {
		for _, p := range pairs {
			a.taken.Set(base + p.pos)
		}
		if max+base >= a.size {
			a.size = max + base + 1
		}
	}()

	if len(pairs) > 16 {
		// This is a large block, allocate it at the end of the table.
		base = a.size - min
		return
	}

	// Try to find a free block of the required shape.
outer:
	for i := 0; i < a.size; i++ {
		base = i - min
		for _, p := range pairs {
			if a.taken.Get(base + p.pos) {
				continue outer
			}
		}
		// Found space.
		return
	}

	// No better solution than allocate at the end of the table.
	base = a.size - min
	return
}

// pickDefault returns the "default" element for a given array.
func pickDefault(arr []int, backup int) int {
	// Try to find the majority element (if any).
	// See https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_majority_vote_algorithm
	var count int
	var ret int
	for _, val := range arr {
		if count == 0 {
			ret = val
			count = 1
		} else {
			if val == ret {
				count++
			} else {
				count--
			}
		}
	}

	count = 0
	for _, val := range arr {
		if val == ret {
			count++
		}
	}
	if count > len(arr)/2 {
		// This is the majority element.
		return ret
	}

	// No majority element, try the first, last and "backup" values.
	first := arr[0]
	last := arr[len(arr)-1]
	var cf, cl, cb int
	for _, val := range arr {
		switch {
		case val == first:
			cf++
		case val == last:
			cl++
		case val == backup:
			cb++
		}
	}
	switch {
	case cf >= cl && cf >= cb:
		return first
	case cl >= cf && cl >= cb:
		return last
	}
	return backup
}
