// Package lex implements a lexer generator.
package lex

import (
	"fmt"
	"sort"
	"unicode/utf8"

	"github.com/inspirer/textmapper/status"
)

// Rule is a lexer generator rule.
type Rule struct {
	Pattern         *Pattern
	Resolver        Resolver
	StartConditions []int
	Precedence      int // Precedence disambiguates between rules that match the same prefix.
	Action          int // non-negative; 0 fail and try backtrack, 1 return eoi
	Origin          status.SourceNode
}

// Sym is a DFA input symbol, which represents zero or more runes indistinguishable by the
// generated lexer.
type Sym int

// EOI is an end of stream indicator.
const EOI Sym = 0

// Initial is the name of the default lexer start condition.
const Initial = "initial"

// RangeEntry translates a segment of runes into a DFA input symbol.
type RangeEntry struct {
	Start  rune // inclusive
	Target Sym
}

func (re RangeEntry) String() string {
	return fmt.Sprintf("%v=>%v", charset{re.Start, re.Start}.String(), re.Target)
}

// Tables holds generated lexer tables.
type Tables struct {
	// If true, the lexer will scan bytes instead of runes.
	ScanBytes bool
	// SymbolMap translates runes into DFA symbols. This slice is sorted by "Start" and covers all
	// unicode runes (or bytes if ScanBytes is true).
	SymbolMap []RangeEntry
	// The maximum value in SymbolMap.Target plus one.
	NumSymbols int
	// Maps start conditions into DFA states.
	StateMap []int
	// A matrix of NumSymbols x NumStates. Positive values denote transitions to other states. Low
	// negative values [-1 .. -len(Backtrack)] are reserved for backtracking rules. Other
	// negative values accept the current prefix, leaving the current character for the next token.
	Dfa []int
	// Backtracking rules the dfa might resort to.
	Backtrack []Checkpoint
}

// Checkpoint describes one backtracking situation.
type Checkpoint struct {
	Action    int // The default action to be accepted if a longer token is not found.
	NextState int
	Details   string
}

// LastMapEntry returns the last entry of the symbol map.
func (t *Tables) LastMapEntry() RangeEntry {
	return t.SymbolMap[len(t.SymbolMap)-1]
}

// SymbolArr returns a simple array representation of the symbol map (except the last segment).
func (t *Tables) SymbolArr(maxRune rune) []int {
	if len(t.SymbolMap) == 1 {
		return nil
	}
	size := t.LastMapEntry().Start
	if maxRune != 0 && maxRune < size {
		size = maxRune
	}
	ret := make([]int, size)
	var index rune
	var target int
	for _, e := range t.SymbolMap {
		for ; index < e.Start && index < size; index++ {
			ret[index] = target
		}
		if index == size {
			break
		}
		target = int(e.Target)
	}
	return ret
}

// CompressedEntry is a single entry of a compressed map from runes to DFA symbols.
type CompressedEntry struct {
	Lo, Hi     rune
	DefaultVal int
	Vals       []int
}

func (e CompressedEntry) String() string {
	return fmt.Sprintf("[%v,%v]=%v,default=%v", e.Lo, e.Hi, e.Vals, e.DefaultVal)
}

// CompressedMap returns the compressed representation of the symbol map (except the last segment).
func (t *Tables) CompressedMap(start rune) []CompressedEntry {
	defaultVal := t.LastMapEntry().Target
	var curr CompressedEntry
	curr.Lo = -1

	var ret []CompressedEntry
	emit := func() {
		if curr.Lo == -1 {
			return
		}
		curr.DefaultVal = curr.Vals[len(curr.Vals)-1]
		size := len(curr.Vals)
		for ; size > 0 && curr.Vals[size-1] == curr.DefaultVal; size-- {
		}
		if size == 0 {
			curr.Vals = nil
		} else {
			curr.Vals = curr.Vals[:size]
		}
		ret = append(ret, curr)
		curr.Lo = -1
	}
	consume := func(lo, hi rune, target Sym, strike int) {
		count := int(hi - lo)
		if curr.Lo == -1 {
			if target == defaultVal {
				return
			}
			curr = CompressedEntry{Lo: lo, Hi: hi}
		} else {
			if target == defaultVal && strike+count > 8 {
				emit()
				return
			}
			curr.Hi = hi
		}
		for i := lo; i < hi; i++ {
			curr.Vals = append(curr.Vals, int(target))
		}
		if count > 8 {
			emit()
			return
		}
	}

	var strike int
	startIdx := sort.Search(len(t.SymbolMap), func(i int) bool { return i+1 == len(t.SymbolMap) || t.SymbolMap[i+1].Start > start })
	index := start
	target := t.SymbolMap[startIdx].Target
	for _, e := range t.SymbolMap[startIdx+1:] {
		consume(index, e.Start, target, strike)
		target = e.Target
		strike = int(e.Start - index)
		index = e.Start
	}
	emit()
	return ret
}

// ActionStart returns the index of the very first action.
func (t *Tables) ActionStart() int {
	return -1 - len(t.Backtrack)
}

// Scan applies the lexer to a given string and returns the first discovered token.
func (t *Tables) Scan(start int, text string) (size, action int) {
	state := t.StateMap[start]
	actionStart := t.ActionStart()
	var index int
	for index < len(text) {
		var r rune
		start := index
		if t.ScanBytes {
			r = rune(text[index])
			index++
		} else {
			var w int
			r, w = utf8.DecodeRuneInString(text[index:])
			index += w
		}

		i := sort.Search(len(t.SymbolMap), func(i int) bool { return i+1 == len(t.SymbolMap) || t.SymbolMap[i+1].Start > r })
		ch := int(t.SymbolMap[i].Target)
		state = t.Dfa[state*t.NumSymbols+ch]
		if state < 0 {
			if state > actionStart {
				bt := t.Backtrack[-1-state]
				// Checkpoint.
				action, state = bt.Action, bt.NextState
				size = start
				continue
			}
			if actionStart == state && size > 0 {
				// Backtrack.
				return
			}
			return start, actionStart - state
		}
	}
	state = t.Dfa[state*t.NumSymbols] // end-of-input transition
	if actionStart == state && size > 0 {
		// Backtrack.
		return
	}
	return len(text), actionStart - state
}

// Resolver retrieves named regular expressions.
type Resolver interface {
	Resolve(name string) *Pattern
}

// Compile combines a set of rules into a lexer.
func Compile(rules []*Rule, scanBytes, allowBacktracking bool) (*Tables, error) {
	var s status.Status
	var index []int
	var maxSC int
	c := newCompiler()
	for _, r := range rules {
		i, err := c.addPattern(r.Pattern, r)
		if err != nil {
			s.Add(r.Origin.SourceRange(), err.Error())
		}
		for _, sc := range r.StartConditions {
			if sc > maxSC {
				maxSC = sc
			}
		}
		index = append(index, i)
	}
	ins, inputMap := c.compile(scanBytes)

	var maxSym Sym
	for _, re := range inputMap {
		if re.Target > maxSym {
			maxSym = re.Target
		}
	}
	numSymbols := int(maxSym) + 1

	startStates := make([][]int, maxSC+1)
	for i, r := range rules {
		for _, sc := range r.StartConditions {
			startStates[sc] = append(startStates[sc], index[i])
		}
	}

	g := newGenerator(ins, numSymbols)
	stateMap := make([]*state, maxSC+1)
	for i, states := range startStates {
		stateMap[i] = g.addState(states, nil /*after*/)
	}
	dfa, backtrack, err := g.generate(allowBacktracking)
	s.AddError(err)

	ret := &Tables{
		ScanBytes:  scanBytes,
		SymbolMap:  inputMap,
		NumSymbols: numSymbols,
		Dfa:        dfa,
		Backtrack:  backtrack,
	}
	for _, state := range stateMap {
		ret.StateMap = append(ret.StateMap, state.index)
	}
	return ret, s.Err()
}
