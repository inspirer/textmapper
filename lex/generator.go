package lex

import (
	"errors"
	"fmt"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
)

var (
	errNoStartStates = errors.New("lexer has no start states")
)

type state struct {
	index  int
	set    []int
	action []int // -1 invalid token, -2 eoi, -3... lexer rule #0
	accept *Rule // a matched rule for the current prefix
	next   *state
}

// generator assembles regex instructions into a lexer DFA.
type generator struct {
	ins        []inst
	states     []*state
	powerset   *container.IntSliceMap[*state]
	s          status.Status
	numSymbols int
	closure    container.BitSet
	arena      []int
	shift      container.BitSet
	first      *state
	last       *state
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

func (g *generator) allocateState(key []int) *state {
	s := &state{
		index: len(g.states),
		set:   key,
	}
	g.states = append(g.states, s)
	return s
}

func (g *generator) addState(set []int, after *state) *state {
	g.closure.ClearAll(len(g.ins))
	for _, i := range set {
		if g.ins[i].core() {
			g.closure.Set(i)
		}
		for _, delta := range g.ins[i].links {
			g.closure.Set(i + delta)
		}
	}

	set = g.closure.Slice(g.arena)
	state := g.powerset.Get(set)
	if state.next == nil && state != g.last {
		// This is a newly added state. Insert it after "after".
		if g.first == nil {
			g.first = state
			g.last = state
		} else if after != nil && after != g.last {
			state.next = after.next
			after.next = state
		} else {
			g.last.next = state
			g.last = state
		}
	}
	return state
}

func (g *generator) generate(allowBacktracking bool) (dfa []int, backtrack []Checkpoint, err error) {
	if len(g.states) == 0 {
		return nil, nil, errNoStartStates
	}

	for state := g.first; state != nil; state = state.next {
		// Compute whether we can accept the current prefix, and also collect all transition symbols
		// taking us to some other state.
		var acceptRule *Rule
		g.shift.ClearAll(g.numSymbols)
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
					msg := fmt.Sprintf("two rules are identical: %v and %v", inst.rule.Pattern.Name, acceptRule.Pattern.Name)
					g.s.Add(inst.rule.Origin.SourceRange(), msg)
				}
			}
		}

		// Record all transitions.
		state.action = make([]int, g.numSymbols)
		state.accept = acceptRule
		defAction := -1 // invalid token
		if acceptRule != nil {
			defAction = -1 - acceptRule.Action
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
			state.action[sym] = g.addState(next, state).index
		}
	}

	// Adding backtracking states.
	type checkpointKey struct {
		targetState int
		accept      int
	}
	var bt map[checkpointKey]int
	var numBtStates int
	for state := g.first; state != nil; state = state.next {
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

			if !allowBacktracking {
				var sb strings.Builder
				sb.WriteString("Needs backtracking since the following state(s) are prefixes of valid tokens but are not valid tokens themselves:")
				for _, i := range g.states[val].set {
					inst := g.ins[i]
					if len(inst.consume) == 0 {
						continue
					}
					sb.WriteString("\n\t")
					sb.WriteString(inst.String())
				}
				sb.WriteString("\nConsider removing 'nonBacktracking = true' or reporting these states as 'invalid_token' via separate lexer rules.")
				g.s.Add(state.accept.Origin.SourceRange(), sb.String())
			}

			backtrack = append(backtrack, Checkpoint{
				Action:    key.accept,
				NextState: key.targetState,
				Details:   fmt.Sprintf("in %v", state.accept.Pattern.Name), // TODO print position in regexp
			})
			numBtStates++
			state.action[i] = btState
		}
	}

	// Reorder states for better cache locality, and make room for backtracking states in the action
	// space.
	permutation := make([]int, len(g.states))
	var counter int
	for state := g.first; state != nil; state = state.next {
		permutation[state.index] = counter
		counter++
	}
	for i := range backtrack {
		backtrack[i].NextState = permutation[backtrack[i].NextState]
	}
	for _, state := range g.states {
		state.index = permutation[state.index]
		for i, val := range state.action {
			if val >= len(g.states) {
				state.action[i] = -1 - (val - len(g.states))
			} else if val >= 0 {
				state.action[i] = permutation[val]
			} else {
				state.action[i] -= numBtStates
			}
		}
	}

	// Assemble the DFA table.
	dfa = make([]int, len(g.states)*g.numSymbols)
	var offset int
	for state := g.first; state != nil; state = state.next {
		offset += copy(dfa[offset:], state.action)
	}

	return dfa, backtrack, g.s.Err()
}
