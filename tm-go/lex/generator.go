package lex

import (
	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
)

type checkpoint struct {
	targetState int
	action      int
}

type state struct {
	index  int
	set    []int
	action int
}

// generator assembles regex instructions into a lexer DFA.
type generator struct {
	ins      []inst
	states   []*state
	powerset *container.IntSliceMap
	s        status.Status
}

func newGenerator(ins []inst) *generator {
	g := &generator{ins: ins}
	g.powerset = container.NewIntSliceMap(g.allocateState)
	return g
}

func (g *generator) allocateState(key []int) interface{} {
	s := &state{
		index:  len(g.states),
		set:    key,
		action: -1,
	}
	g.states = append(g.states, s)
	return s
}

func (g *generator) addState(set []int) int {
	return g.powerset.Get(set).(*state).index
}

func (g *generator) generate() (dfa []int, backtrack []int, err error) {
	// TODO implement
	return nil, nil, g.s.Err()
}
