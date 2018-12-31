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

type state struct {
	index  int
	set    []int
	action []int // -1 invalid token, -2 eoi, -3... lexer rule #0
	accept *Rule // a matched rule for the current prefix
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

func (g *generator) generate() (dfa []int, backtrack []Checkpoint, err error) {
	if len(g.states) == 0 {
		return nil, nil, errNoStartStates
	}

	for si := 0; si < len(g.states); si++ {
		state := g.states[si]

		// Compute whether we can accept the current prefix, and also collect all transition symbols
		// taking us to some other state.
		var acceptRule *Rule
		g.shift.ClearAll()
		for _, i := range state.set {
			inst := g.ins[i]
			for _, sym := range inst.consume {
				g.shift.Set(int(sym))
			}
			if inst.rule != nil {
				switch {
				case acceptRule == nil || acceptRule.Precedence < inst.rule.Precedence:
					acceptRule = inst.rule
				case acceptRule.Precedence == inst.rule.Precedence && acceptRule.Action != inst.rule.Action:
					msg := fmt.Sprintf("two rules are identical: %v and %v", inst.rule.OriginName, acceptRule.OriginName)
					g.s.Add(inst.rule.Origin.SourceRange(), msg)
				}
			}
		}

		// Record all transitions.
		state.action = make([]int, g.numSymbols)
		state.accept = acceptRule
		defAction := -1
		if acceptRule != nil {
			defAction = -3 - acceptRule.Action
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

	// Adding backtracking states.
	type checkpointKey struct {
		targetState int
		accept      int
	}
	var bt map[checkpointKey]int
	var numBtStates int
	for _, state := range g.states {
		if state.accept == nil {
			continue
		}
		for i, val := range state.action {
			if val < 0 || g.states[val].accept != nil {
				continue
			}

			// This transition requires backtracking.
			key := checkpointKey{val, state.accept.Action}
			if btState, ok := bt[key]; ok {
				state.action[i] = btState
				continue
			}
			if bt == nil {
				bt = make(map[checkpointKey]int)
			}
			btState := len(g.states) + numBtStates
			bt[key] = btState

			// Note: adding 2 to make room for accept EOI and accept InvalidToken actions.
			backtrack = append(backtrack, Checkpoint{
				Action:    key.accept + 2, // TODO get rid of +2 and align the ranges with dfa
				NextState: key.targetState,
				Details:   "in " + state.accept.OriginName,
			})
			numBtStates++
			state.action[i] = btState
		}
	}

	// Make room for backtracking states in the action space.
	for _, state := range g.states {
		for i, val := range state.action {
			if val >= len(g.states) {
				state.action[i] = -1 - (val - len(g.states))
			} else if val < 0 {
				state.action[i] -= numBtStates
			}
		}
	}

	// Assemble the DFA table.
	dfa = make([]int, len(g.states)*g.numSymbols)
	var offset int
	for _, state := range g.states {
		offset += copy(dfa[offset:], state.action)
	}

	return dfa, backtrack, g.s.Err()
}
