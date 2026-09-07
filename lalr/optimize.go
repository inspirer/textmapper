package lalr

import (
	"fmt"
	"log"
	"math/bits"
	"sort"
	"strings"
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
	ruleCount := make([]int, rules)      // for the default reduction, always all-zeros between rows
	var touched []int                    // rules with a non-zero count in ruleCount
	next := make([]int, terms)
	var arena pairArena
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
				touched = touched[:0]
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
					if defaultReduce {
						ruleCount[act]++
						touched = append(touched, act)
					}
				}
				next[term] = act
			}
			if defaultReduce {
				// If requested, we replace all errors (except ones caused by non-assoc precedence)
				// with the most common reduction. This reduces the output tables size at the
				// expense or error message quality.

				// Note: this picks the smallest rule index among the most frequent ones,
				// exactly as a left-to-right scan over all rules would.
				def := -1 // error, aka no default
				var max int
				for _, rule := range touched {
					if v := ruleCount[rule]; v > max || v == max && rule < def {
						max = v
						def = rule
					}
				}
				for _, rule := range touched {
					ruleCount[rule] = 0
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

		var n int
		for _, val := range next {
			if val != def {
				n++
			}
		}
		pairs := arena.alloc(n)
		n = 0
		for term, val := range next {
			if val != def {
				pairs[n] = pair{term, val}
				n++
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
	targets := make([]int, 0, len(t.Action))
	for nt := 0; nt < syms-terms; nt++ {
		min := t.Goto[terms+nt]
		max := t.Goto[terms+nt+1]

		for i := range toState {
			toState[i] = -1 // error
		}
		for i := min; i < max; i += 2 {
			toState[t.FromTo[i]] = t.FromTo[i+1]
		}

		// Note: states without a transition on this nonterminal never query it, so
		// "error" entries do not need to be preserved and the most common
		// real target becomes the default.
		targets = targets[:0]
		for _, to := range toState {
			if to != -1 {
				targets = append(targets, to)
			}
		}
		def := -1
		if len(targets) > 0 {
			def = pickDefault(targets, reuse)
		}
		ret.DefGoto[nt] = def
		for i, to := range toState {
			if to == -1 {
				toState[i] = def
			}
		}

		var n int
		for _, to := range toState {
			if to != def {
				n++
			}
		}
		pairs := arena.alloc(n)
		n = 0
		for from, to := range toState {
			if to != def {
				pairs[n] = pair{from, to}
				n++
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
	lowzero  int // index of the first free slot in "taken"; monotonically non-decreasing
	taken    bitmap
	usedBase bitmap
	prev     map[key]int
	table    []int
	check    []int
	rel      []int // scratch space for relative pair offsets
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
		// Note: base can cause out-of-bound problems in case of hash collisions.
		ok := min+base >= 0 && max+base < a.size
		if ok {
			for _, p := range pairs {
				if a.table[base+p.pos] != p.val || a.check[base+p.pos] != p.pos+1 {
					ok = false
					break
				}
			}
		}
		if ok {
			return base
		}
	}

	// Reserve enough bits to allocate these pairs at the very end of the table.
	// Note: the search reads a 64-bit window at the very last candidate offset, so
	// one extra word is reserved on top of the search range.
	a.taken.grow(a.size + max - min + 1 + 64)
	a.usedBase.grow(a.delta + a.size + max - min + 1 + 64)
	defer func() {
		for _, p := range pairs {
			a.taken.set(base + p.pos)
		}
		a.usedBase.set(a.delta + base)
		if max+base >= a.size {
			a.size = max + base + 1
			a.grow(a.size)
		}
		a.prev[key{hash, len(pairs)}] = base
		for _, p := range pairs {
			a.table[base+p.pos] = p.val
			a.check[base+p.pos] = p.pos + 1
		}
	}()

	return a.search(pairs, min, max)
}

// search finds the smallest base offset at which all the pairs of a line fit into
// the free slots of the table. It tests 64 candidate offsets at a time: for a line
// with relative pair offsets q_k, the set of offsets that are still viable inside a
// 64-bit window of candidates i is
//
//	free = ^usedBase[i-min] & AND_k ^taken[i+q_k]
//
// and the answer is the lowest set bit of "free". This is the very same first-fit
// rule a bit-at-a-time scan implements, only ~64x fewer operations: the AND chain
// also stops as soon as the window has no viable offsets left.
func (a *allocator) search(pairs []pair, min, max int) (base int) {
	rel := a.rel[:0]
	for _, p := range pairs[1:] {
		rel = append(rel, p.pos-min)
	}
	a.rel = rel

	// Bits are only ever set, so the first free slot never moves backwards.
	start := a.taken.nextZero(a.lowzero)
	a.lowzero = start
	ubOff := a.delta - min // usedBase index for candidate i is ubOff+i
	size := a.size
	first := uint64(0)
	if s := uint(start) & 63; s != 0 {
		first = (uint64(1) << s) - 1 // candidates below "start" are already taken
	}
	for i := start &^ 63; i < size; i += 64 {
		m := ^(a.taken.word(i) | first)
		first = 0
		if m == 0 {
			continue
		}
		m &^= a.usedBase.word(ubOff + i)
		if m == 0 {
			continue
		}
		for _, q := range rel {
			m &^= a.taken.word(i + q)
			if m == 0 {
				break
			}
		}
		if m != 0 {
			cand := i + bits.TrailingZeros64(m)
			if cand >= size {
				break
			}
			return cand - min
		}
	}
	return size - min
}

// bitmap is a growable bit set backed by 64-bit words. Unlike container.BitSet it
// supports reading an arbitrarily aligned 64-bit window in two loads.
type bitmap []uint64

func (b *bitmap) grow(size int) {
	// One extra word so that word() can always read b[j+1].
	ln := (size+63)/64 + 1
	if len(*b) < ln {
		*b = append(*b, make([]uint64, ln-len(*b))...)
	}
}

func (b bitmap) set(i int) {
	b[uint(i)/64] |= 1 << (uint(i) % 64)
}

func (b bitmap) get(i int) bool {
	return b[uint(i)/64]&(1<<(uint(i)%64)) != 0
}

// word returns bits [x, x+64) of the bit set.
func (b bitmap) word(x int) uint64 {
	j := uint(x) >> 6
	s := uint(x) & 63
	// Note: in Go a shift by 64 yields 0, so the s==0 case needs no branch.
	return b[j]>>s | b[j+1]<<(64-s)
}

func (b bitmap) nextZero(start int) int {
	index := uint(start) / 64
	val := ^b[index] >> (uint(start) % 64)
	if val != 0 {
		return start + bits.TrailingZeros64(val)
	}
	for index++; index < uint(len(b)); index++ {
		if val := b[index]; val != ^uint64(0) {
			return int(index)*64 + bits.TrailingZeros64(^val)
		}
	}
	return int(index) * 64
}

// pickDefault returns the most common element in a given array. Ties are broken
// in favor of the smallest value.
//
// "reuse" is a scratch counter array indexed by (value - min); only the buckets
// actually used by "arr" are touched, so the cost does not depend on the range of
// the values (which can be in the tens of thousands for parser actions).
func pickDefault(arr []int, reuse []int) int {
	min := arr[0]
	for _, val := range arr {
		if val < min {
			min = val
		}
	}
	for _, val := range arr {
		reuse[val-min] = 0
	}
	for _, val := range arr {
		reuse[val-min]++
	}
	ret, cnt := min, 0
	for _, val := range arr {
		if c := reuse[val-min]; c > cnt || c == cnt && val < ret {
			ret, cnt = val, c
		}
	}
	return ret
}

// pairArena hands out small non-overlapping slices carved out of larger chunks,
// which keeps the number of allocations (and the GC pressure) low.
type pairArena struct {
	free []pair
}

func (a *pairArena) alloc(n int) []pair {
	if n == 0 {
		return nil
	}
	if len(a.free) < n {
		const chunk = 16 << 10
		size := chunk
		if n > size {
			size = n
		}
		a.free = make([]pair, size)
	}
	ret := a.free[:n:n]
	a.free = a.free[n:]
	return ret
}
