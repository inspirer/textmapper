package lex

import (
	"errors"
	"fmt"

	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
)

var (
	errNoStartStates = errors.New("lexer has no start states")
)

type checkpoint struct {
	targetState int
	action      int
}

type state struct {
	index  int
	set    []int
	action []int // -1 invalid token, -2 eoi, -3... lexer rule #0
}

// generator assembles regex instructions into a lexer DFA.
type generator struct {
	ins        []inst
	states     []*state
	powerset   *container.IntSliceMap
	s          status.Status
	numSymbols int
	closure    container.BitSet
	arena      []int
	shift      container.BitSet
}

func newGenerator(ins []inst, numSymbols int) *generator {
	g := &generator{
		ins:        ins,
		numSymbols: numSymbols,
		closure:    container.NewBitSet(len(ins)),
		arena:      make([]int, len(ins)),
		shift:      container.NewBitSet(numSymbols),
	}
	g.powerset = container.NewIntSliceMap(g.allocateState)
	return g
}

func (g *generator) allocateState(key []int) interface{} {
	s := &state{
		index: len(g.states),
		set:   key,
	}
	g.states = append(g.states, s)
	return s
}

func (g *generator) addState(set []int) int {
	g.closure.ClearAll()
	for _, i := range set {
		if g.ins[i].core() {
			g.closure.Set(i)
		}
		for _, delta := range g.ins[i].links {
			g.closure.Set(i + delta)
		}
	}

	set = g.closure.Slice(g.arena)
	return g.powerset.Get(set).(*state).index
}

func (g *generator) generate() (dfa []int, backtrack []int, err error) {
	if len(g.states) == 0 {
		return nil, nil, errNoStartStates
	}

	for si := 0; si < len(g.states); si++ {
		state := g.states[si]

		// Compute whether we can accept the current prefix, and also collect all transition symbols
		// taking us to some other state.
		var acceptRule *Rule
		accept := -1
		g.shift.ClearAll()
		for _, i := range state.set {
			inst := g.ins[i]
			for _, sym := range inst.consume {
				g.shift.Set(int(sym))
			}
			if inst.rule != nil && accept != inst.rule.Action {
				switch {
				case accept == -1 || acceptRule.Precedence < inst.rule.Precedence:
					accept = inst.rule.Action
					acceptRule = inst.rule
				case acceptRule.Precedence == inst.rule.Precedence:
					msg := fmt.Sprintf("two rules are identical: %v and %v", inst.rule.OriginName, acceptRule.OriginName)
					g.s.Add(inst.rule.Origin.SourceRange(), msg)
				}
			}
		}

		// Record all transitions.
		state.action = make([]int, g.numSymbols)
		defAction := -1
		if accept >= 0 {
			defAction = -3 - accept
		}
		for sym := 0; sym < g.numSymbols; sym++ {
			if !g.shift.Get(sym) {
				state.action[sym] = defAction
				continue
			}
			next := g.arena[:0]
			for _, i := range state.set {
				if g.ins[i].consume.contains(Sym(sym)) {
					next = append(next, i+1)
				}
			}
			state.action[sym] = g.addState(next)
		}
	}

	// First group (only) succeeds on EOI, unless there is an explicit EOI rule.
	if first := g.states[0]; first.action[0] == -1 {
		first.action[0] = -2
	}

	// TODO implement backtracking

	// Assemble the DFA table.
	dfa = make([]int, len(g.states)*g.numSymbols)
	var offset int
	for _, state := range g.states {
		offset += copy(dfa[offset:], state.action)
	}

	return dfa, nil, g.s.Err()
}
