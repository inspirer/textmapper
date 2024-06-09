package lalr

import (
	"fmt"
	"strings"

	"github.com/inspirer/textmapper/util/container"
)

// Conflict represents a grammar ambiguity.
type Conflict struct {
	Resolved bool
	CanShift bool
	Kind     string
	Next     []Sym
	Rules    []int // in g.Rules

	State int
	Input []Sym
	g     *Grammar
}

func (c *Conflict) String() string {
	g := c.g
	var sb strings.Builder
	sb.WriteString("input:")
	for _, sym := range c.Input {
		sb.WriteByte(' ')
		sb.WriteString(g.Symbols[sym])
	}
	sb.WriteByte('\n')
	sb.WriteString(c.Kind)
	sb.WriteString(" conflict (next:")
	sep := " "
	for _, sym := range c.Next {
		sb.WriteString(sep)
		sb.WriteString(g.Symbols[sym])
		sep = ", "
	}
	sb.WriteString(")\n")
	for _, ruleID := range c.Rules {
		rule := g.Rules[ruleID]
		fmt.Fprintf(&sb, "    %v :", g.Symbols[rule.LHS])
		for _, sym := range rule.RHS {
			sb.WriteByte(' ')
			if sym.IsStateMarker() {
				sb.WriteString(".")
				sb.WriteString(g.Markers[-sym-1])
			} else {
				sb.WriteString(g.Symbols[sym])
			}
		}
		if rule.Precedence != 0 {
			sb.WriteString(" %prec ")
			sb.WriteString(g.Symbols[rule.Precedence])
		}
		sb.WriteByte('\n')
	}
	return sb.String()
}

type resolution uint8

const (
	none resolution = iota
	doShift
	doReduce
	doError
	conflict
)

type ambiguity struct {
	terminal Sym
	canShift bool
	rules    []int
	res      resolution
}

func (a *ambiguity) add(res resolution, rule int) {
	if a.res != none && a.res != res {
		res = conflict
	}
	a.res = res
	a.rules = append(a.rules, rule)
}

func (a *ambiguity) key() []int {
	val := int(a.res)
	if a.canShift {
		val += 16
	}
	ret := make([]int, 0, len(a.rules)+1)
	ret = append(ret, val)
	ret = append(ret, a.rules...)
	return ret
}

type conflictBuilder struct {
	m    map[Sym]*ambiguity
	list []*ambiguity
}

func (b *conflictBuilder) hasConflict(term Sym) bool {
	a, ok := b.m[term]
	return ok && a.res == conflict
}

func (b *conflictBuilder) addRule(term Sym, res resolution, rule int, canShift bool) {
	if b.m == nil {
		b.m = make(map[Sym]*ambiguity)
	}
	a, ok := b.m[term]
	if !ok {
		a = &ambiguity{terminal: term, canShift: canShift}
		b.m[term] = a
		b.list = append(b.list, a)
	}
	a.add(res, rule)
}

func (b *conflictBuilder) merge(g *Grammar, state int, states []*state) []*Conflict {
	var ret []*Conflict
	m := container.NewIntSliceMap(func(key []int) interface{} {
		res := resolution(key[0] & 15)
		canShift := key[0]&16 != 0
		c := &Conflict{Rules: key[1:], Resolved: true, CanShift: canShift, State: state, g: g}
		switch res {
		case conflict:
			c.Resolved = false
			if canShift {
				c.Kind = "shift/reduce"
			} else {
				c.Kind = "reduce/reduce"
			}
		case doShift:
			c.Kind = "resolved as shift"
		case doReduce:
			c.Kind = "resolved as reduce"
		case doError:
			c.Kind = "resolved as syntax error"
		}
		ret = append(ret, c)
		return c
	})
	for _, a := range b.list {
		conflict := m.Get(a.key()).(*Conflict)
		conflict.Next = append(conflict.Next, a.terminal)
	}
	if len(ret) > 0 {
		var input []Sym
		for s := state; s >= 0 && states[s].sourceState >= 0; s = states[s].sourceState {
			input = append(input, states[s].symbol)
		}
		for i := len(input)/2 - 1; i >= 0; i-- {
			opp := len(input) - 1 - i
			input[i], input[opp] = input[opp], input[i]
		}
		for _, c := range ret {
			c.Input = input
		}
	}
	return ret
}
