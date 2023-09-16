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
func Optimize(t *DefaultEnc, terms, rules int, defaultReduce bool) *DisplacementEnc {
	syms := len(t.Goto) - 1
	states := len(t.Action)
	ret := &DisplacementEnc{
		DefGoto: make([]int, syms-terms),
		Goto:    make([]int, syms-terms),
		DefAct:  make([]int, states),
		Action:  make([]int, states),
		Base:    -terms,
	}

	reuse := make([]int, states+1+rules) // for pickDefault
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
			undef := -1
			if defaultReduce {
				undef = -2 - states
				for i := 0; i < rules; i++ {
					reuse[i] = 0
				}
			}
			for i := range next {
				next[i] = undef
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
				case -2: // error, which is caused by a non-assoc precedence
					act = -1
				default:
					if act < 0 || act >= rules {
						log.Fatal("internal invariant violated: rule index out of range")
					}
					reuse[act]++
				}
				next[term] = act
			}
			if defaultReduce {
				// If requested, we replace all errors (except ones caused by non-assoc precedence)
				// with the most common reduction. This reduces the output tables size at the
				// expense or error message quality.

				def := -1 // error, aka no default
				var max int
				for rule, v := range reuse[:rules] {
					if v > max {
						max = v
						def = rule
					}
				}
				for i, v := range next {
					if v == undef {
						next[i] = def
					}
				}
			}
		}

		def := pickDefault(next, reuse)
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

		def := pickDefault(toState, reuse)
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
	var delta int
	for i, l := range input {
		entries = append(entries, entry{index: i, pairs: l.pairs})
		total += len(l.pairs)
		if p := l.pairs[0].pos; p > delta {
			delta = p
		}
	}
	sort.SliceStable(entries, func(i, j int) bool {
		return len(entries[i].pairs) > len(entries[j].pairs)
	})
	allocator := allocator{
		delta: delta,
		prev:  make(map[key]int),
		table: make([]int, total),
		check: make([]int, total),
	}

	// Iterate in the order of decreasing number of pairs.
	for _, e := range entries {
		if len(e.pairs) == 0 {
			log.Fatal("internal invariant violated: empty line")
		}
		base := allocator.place(e.pairs)
		indices[e.index] = base
	}
	table = allocator.table[:allocator.size]
	check = allocator.check[:allocator.size]
	for i, e := range check {
		check[i] = e - 1 // -1 means "unused"
	}
	return indices, table, check
}

type allocator struct {
	size     int
	delta    int
	taken    container.BitSet
	usedBase container.BitSet
	prev     map[key]int
	table    []int
	check    []int
}

type key struct {
	hash int
	len  int
}

func hash(pairs []pair) int {
	var ret int
	for _, p := range pairs {
		ret = ret*31 + p.pos
		ret = ret*31 + p.val
	}
	return ret
}

func (a *allocator) grow(size int) {
	if len(a.table) < size {
		tmp := make([]int, size-len(a.table))
		a.table = append(a.table, tmp...)
		a.check = append(a.check, tmp...)
	}
}

func (a *allocator) place(pairs []pair) (base int) {
	min, max := pairs[0].pos, pairs[len(pairs)-1].pos

	// Attempt to dedupe lines.
	hash := hash(pairs)
	if base, ok := a.prev[key{hash, len(pairs)}]; ok {
		ok := true
		for _, p := range pairs {
			if a.table[base+p.pos] != p.val || a.check[base+p.pos] != p.pos+1 {
				ok = false
			}
		}
		if ok {
			return base
		}
	}

	// Reserve enough bits to allocate these pairs at the very end of the table.
	a.taken.Grow(a.size + max - min + 1)
	a.usedBase.Grow(a.delta + a.size + max - min + 1)
	defer func() {
		for _, p := range pairs {
			a.taken.Set(base + p.pos)
		}
		if max+base >= a.size {
			a.size = max + base + 1
			a.grow(a.size)
		}
		a.usedBase.Set(a.delta + base)
		a.prev[key{hash, len(pairs)}] = base
		for _, p := range pairs {
			a.table[base+p.pos] = p.val
			a.check[base+p.pos] = p.pos + 1
		}
	}()

	// Try to find a free block of the required shape.
outer:
	for i := 0; i < a.size; i++ {
		base = i - min
		if a.usedBase.Get(a.delta + base) {
			continue
		}
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

// pickDefault returns the most common element in a given array.
func pickDefault(arr []int, reuse []int) int {
	min, max := arr[0], arr[0]
	for _, val := range arr {
		if val < min {
			min = val
		} else if val > max {
			max = val
		}
	}
	n := max - min
	for i := 0; i <= n; i++ {
		reuse[i] = 0
	}
	for _, val := range arr {
		reuse[val-min]++
	}
	ret := min
	cnt := reuse[0]
	for i, count := range reuse[:n+1] {
		if count > cnt {
			ret = i + min
			cnt = count
		}
	}
	return ret
}
