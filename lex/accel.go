package lex

// Accelerators describe runs of input bytes that a generated lexer can consume without
// stepping the DFA for every character. They are derived from the tables and do not
// change the produced tokens.
type Accelerators struct {
	// SpaceSkip[startCondition] is a 128-bit set of ASCII bytes that always produce a
	// one-byte token of a "space" rule (with no semantic action) in that start condition.
	// A run of such bytes can be skipped as a whole. Nil if no start condition has any.
	SpaceSkip [][2]uint64

	// SelfLoop[state] is the 1-based index into ByteSets of the set of bytes on which the
	// DFA stays in "state", or 0 if the state has no useful self-loop.
	SelfLoop []int

	// ByteSets[i][b] == 0 iff byte b keeps the DFA in the same state. Bytes >= 0x80 are
	// included only when every non-ASCII rune loops back, which makes it safe to consume
	// UTF-8 sequences byte by byte.
	ByteSets [][256]uint8

	// HasNewline[i] reports whether ByteSets[i] contains '\n', in which case the lexer has
	// to count the skipped newlines.
	HasNewline []bool
}

// HasSelfLoops returns true if at least one state has a self-loop accelerator.
func (a *Accelerators) HasSelfLoops() bool {
	return a != nil && len(a.ByteSets) > 0
}

// HasSpaceSkip returns true if at least one start condition can skip space tokens.
func (a *Accelerators) HasSpaceSkip() bool {
	return a != nil && a.SpaceSkip != nil
}

// SpaceSkipBits returns SpaceSkip as a flat array of 64-bit words (two per start condition).
func (a *Accelerators) SpaceSkipBits() []uint64 {
	var ret []uint64
	for _, s := range a.SpaceSkip {
		ret = append(ret, s[0], s[1])
	}
	return ret
}

// ByteSet returns ByteSets[i] as a slice of ints (for code generation).
func (a *Accelerators) ByteSet(i int) []int {
	ret := make([]int, 256)
	for b, v := range a.ByteSets[i] {
		ret[b] = int(v)
	}
	return ret
}

// minSelfLoopBytes is the minimum number of ASCII bytes in a self-loop set for the
// accelerator to be worth it.
const minSelfLoopBytes = 8

// Accelerators computes lexer accelerators for the tables. skippableAction reports
// whether a given action is a "space" rule without semantic actions, i.e. whether the
// generated lexer would silently restart after matching it.
func (t *Tables) Accelerators(skippableAction func(action int) bool) *Accelerators {
	numStates := len(t.Dfa) / t.NumSymbols
	actionStart := t.ActionStart()
	ret := &Accelerators{SelfLoop: make([]int, numStates)}

	// Classes of the Latin-1 bytes and of everything above 0x7f.
	class := func(b int) int {
		e := 0
		for e+1 < len(t.SymbolMap) && t.SymbolMap[e+1].Start <= rune(b) {
			e++
		}
		return int(t.SymbolMap[e].Target)
	}
	var byteClass [256]int
	for b := range byteClass {
		byteClass[b] = class(b)
	}
	// hiClasses are the classes of all runes >= 0x80 (SymbolMap entries cover consecutive
	// ranges of runes, from Start to the Start of the next entry).
	var hiClasses []int
	if !t.ScanBytes {
		seen := make(map[Sym]bool)
		for i, e := range t.SymbolMap {
			if i+1 < len(t.SymbolMap) && t.SymbolMap[i+1].Start <= 0x80 {
				continue
			}
			if !seen[e.Target] {
				seen[e.Target] = true
				hiClasses = append(hiClasses, int(e.Target))
			}
		}
	}

	// terminal[s] is the action a state produces regardless of the next character, or 0.
	terminal := make([]int, numStates)
	for s := 0; s < numStates; s++ {
		row := t.Dfa[s*t.NumSymbols : (s+1)*t.NumSymbols]
		v := row[0]
		if v > actionStart {
			continue // not an action (a transition or a backtracking checkpoint)
		}
		same := true
		for _, w := range row[1:] {
			if w != v {
				same = false
				break
			}
		}
		if same {
			terminal[s] = v
		}
	}

	// Space skipping.
	var anySpace bool
	ret.SpaceSkip = make([][2]uint64, len(t.StateMap))
	for sc, start := range t.StateMap {
		row := t.Dfa[start*t.NumSymbols : (start+1)*t.NumSymbols]
		for b := 0; b < 128; b++ {
			next := row[byteClass[b]]
			if next < 0 {
				continue
			}
			if v := terminal[next]; v != 0 && skippableAction(actionStart-v) {
				ret.SpaceSkip[sc][b>>6] |= 1 << uint(b&63)
				anySpace = true
			}
		}
	}
	if !anySpace {
		ret.SpaceSkip = nil
	}

	// Self-loops.
	for s := 0; s < numStates; s++ {
		row := t.Dfa[s*t.NumSymbols : (s+1)*t.NumSymbols]
		var set [256]uint8
		for i := range set {
			set[i] = 1
		}
		var n int
		limit := 128
		if t.ScanBytes {
			limit = 256
		}
		for b := 0; b < limit; b++ {
			if row[byteClass[b]] == s {
				set[b] = 0
				n++
			}
		}
		if n < minSelfLoopBytes {
			continue
		}
		if !t.ScanBytes {
			allHi := len(hiClasses) > 0
			for _, c := range hiClasses {
				if row[c] != s {
					allHi = false
					break
				}
			}
			if allHi {
				for b := 128; b < 256; b++ {
					set[b] = 0
				}
			}
		}
		index := -1
		for i := range ret.ByteSets {
			if ret.ByteSets[i] == set {
				index = i
				break
			}
		}
		if index < 0 {
			index = len(ret.ByteSets)
			ret.ByteSets = append(ret.ByteSets, set)
			ret.HasNewline = append(ret.HasNewline, set['\n'] == 0)
		}
		ret.SelfLoop[s] = index + 1
	}
	if len(ret.ByteSets) == 0 {
		ret.SelfLoop = nil
	}
	return ret
}
