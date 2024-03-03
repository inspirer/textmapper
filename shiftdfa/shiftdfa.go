// Package shiftdfa contains data structures for fast shift-based automata.
//
// See https://gist.github.com/pervognsen/218ea17743e1442e59bb60d29b1aa725#file-shift_dfa-md
// for more details.
package shiftdfa

import (
	"errors"
	"fmt"

	"github.com/inspirer/textmapper/lex"
	"github.com/inspirer/textmapper/status"
)

// InvalidToken is a token ID that is returned by the DFA in case of .
const InvalidToken = 0

// Rule is a fast lexer generator rule.
type Rule struct {
	Pattern    string
	Token      int // 0..31 (0 is also used by InvalidToken)
	Precedence int // Precedence disambiguates between rules that match the same string.
}

func (r Rule) String() string {
	var prec string
	if r.Precedence != 0 {
		prec = fmt.Sprintf(" (%v)", r.Precedence)
	}
	return fmt.Sprintf("/%v/ => %v%v", r.Pattern, r.Token, prec)
}

// Scanner is a fast shift-based automaton for ASCII input with up to 11 states and 32 tokens.
type Scanner struct {
	table [256]uint64
	onEoi [11]uint8
}

// Scan returns the number of bytes consumed and the found token ID.
// Zero token ID means that the parsed token is invalid.
func (d *Scanner) Scan(input string) (size int, token uint8) {
	var state uint64
	var i int
	for ; i < len(input) && (state&1) == 0; i++ {
		row := d.table[input[i]]
		state = row >> (state & 63)
	}
	if (state & 1) == 0 {
		return len(input), d.onEoi[(state&63)/6]
	}
	return i - 1, uint8(state&63) / 2
}

// Pack attempts to pack lexer tables into a fast shift-based automaton.
func Pack(t *lex.Tables) (*Scanner, error) {
	states := len(t.Dfa) / t.NumSymbols
	if states > 10 { // TODO: update to 11 states
		return nil, errors.New("too many states")
	}
	if len(t.Backtrack) != 0 {
		return nil, errors.New("backtracking not supported")
	}
	if len(t.StateMap) != 1 || t.StateMap[0] != 0 {
		return nil, errors.New("multiple start states are not supported")
	}
	if t.SymbolMap[len(t.SymbolMap)-1].Start > 0xff {
		return nil, errors.New("only ASCII automatons are supported")
	}

	symBytes := make([][]uint8, t.NumSymbols)
	var e int
	for i := uint8(0); i < 128; i++ {
		if e+1 < len(t.SymbolMap) && t.SymbolMap[e+1].Start == rune(i) {
			e++
		}
		target := t.SymbolMap[e].Target
		symBytes[target] = append(symBytes[target], i)
	}
	uniSym := t.SymbolMap[len(t.SymbolMap)-1].Target

	ret := new(Scanner)
	for state := 0; state < states; state++ {
		offset := state * t.NumSymbols
		for sym := 0; sym < t.NumSymbols; sym++ {
			target := t.Dfa[offset+sym]
			if target < 0 {
				action := -1 - target
				if action >= 32 {
					return nil, errors.New("too many actions")
				}
				target = action*2 + 1
			} else {
				target *= 6
			}
			for _, b := range symBytes[sym] {
				ret.table[b] |= uint64(target) << uint(state*6)
			}
			if sym == 0 {
				if target&1 == 0 {
					return nil, fmt.Errorf("invalid transition on end of input in state #%v", state)
				}
				ret.onEoi[state] = uint8(target) / 2
			}
			if sym != int(uniSym) {
				continue
			}
			// Note: here we consume unicode runes byte by byte.
			for b := 128; b < 256; b++ {
				ret.table[b] |= uint64(target) << uint(state*6)
			}
		}
	}

	return ret, nil
}

// Options contains parameters of the DFA compilation.
type Options struct {
	// Extra name patterns.
	Patterns map[string]string
}

// Compile instantiates a shift DFA from a set of rules.
func Compile(rules []Rule, opts Options) (*Scanner, error) {
	var resolver resolver
	for name, pattern := range opts.Patterns {
		re, err := lex.ParseRegexp(pattern, lex.CharsetOptions{ScanBytes: true})
		if err != nil {
			return nil, fmt.Errorf("cannot parse /%v/: %v", pattern, err)
		}
		if resolver.patterns == nil {
			resolver.patterns = make(map[string]*lex.Pattern)
		}
		resolver.patterns[name] = &lex.Pattern{
			Name:   name,
			RE:     re,
			Text:   pattern,
			Origin: virtualNode{name: name},
		}
	}

	var in []*lex.Rule
	for i, r := range rules {
		re, err := lex.ParseRegexp(r.Pattern, lex.CharsetOptions{ScanBytes: true})
		if err != nil {
			return nil, fmt.Errorf("cannot parse /%v/: %v", r.Pattern, err)
		}
		in = append(in, &lex.Rule{
			Pattern: &lex.Pattern{
				Name:   fmt.Sprintf("rule%v", i),
				RE:     re,
				Text:   r.Pattern,
				Origin: virtualNode{"rules", i},
			},
			Resolver:        resolver,
			Precedence:      r.Precedence,
			Action:          r.Token,
			StartConditions: defaultSCs,
			Origin:          virtualNode{"rules", i},
		})
	}

	tables, err := lex.Compile(in, true /*scanBytes*/, false /*allowBacktracking*/)
	if err != nil {
		return nil, fmt.Errorf("cannot compile rules: %v", err)
	}

	dfa, err := Pack(tables)
	if err != nil {
		return nil, err
	}
	return dfa, nil
}

// MustCompile is like Compile but panics if the automaton cannot be created.
// It simplifies safe initialization of global variables holding compiled DFAs.
func MustCompile(rules []Rule, opts Options) *Scanner {
	dfa, err := Compile(rules, opts)
	if err != nil {
		panic(err)
	}
	return dfa
}

var defaultSCs = []int{0}

type virtualNode struct {
	name  string
	index int
}

func (n virtualNode) SourceRange() status.SourceRange {
	return status.SourceRange{
		Filename: n.name,
		Line:     n.index + 1,
		Column:   1,
	}
}

type resolver struct {
	patterns map[string]*lex.Pattern
}

func (r resolver) Resolve(name string) *lex.Pattern {
	return r.patterns[name]
}
