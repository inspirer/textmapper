package lalr

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
)

// lookaheadPlanner handles ambiguities that cannot be solved via the LALR mechanism
// and need a runtime lookahead.
type lookaheadPlanner struct {
	ruleBase   int
	lookaheads []Lookahead
	index      []int     // rule -> index in lookaheads
	rules      []*laRule // additional lookahead rules
	g          *Grammar
}

type laRule struct {
	index      int
	refCount   int
	refRule    int
	lookaheads []int
}

func (b *lookaheadPlanner) init(g *Grammar) {
	b.g = g
	b.ruleBase = len(g.Rules)
	b.lookaheads = g.Lookaheads

	nonterms := make([]int, len(g.Symbols)-g.Terminals)
	for i := range nonterms {
		nonterms[i] = -1
	}
	for i, la := range g.Lookaheads {
		key := int(la.Nonterminal) - g.Terminals
		if nonterms[key] != -1 {
			log.Fatal("internal error")
		}
		nonterms[key] = i
	}

	for _, rule := range g.Rules {
		if nonterms[int(rule.LHS)-g.Terminals] != -1 && len(rule.RHS) != 0 {
			log.Fatalf("non-empty rule for a lookahead nonterminal")
		}
		b.index = append(b.index, nonterms[int(rule.LHS)-g.Terminals])
	}
}

func (b *lookaheadPlanner) addRule(prev, rule int) int {
	ret := &laRule{
		index:    b.ruleBase + len(b.rules),
		refCount: 1,
	}
	if prev >= b.ruleBase {
		existing := b.rules[prev-b.ruleBase]
		existing.refCount--
		ret.lookaheads = append(ret.lookaheads, existing.lookaheads...)
	} else {
		ret.lookaheads = append(ret.lookaheads, b.index[prev])
	}
	ret.lookaheads = append(ret.lookaheads, b.index[rule])
	ret.refRule = rule
	b.rules = append(b.rules, ret)
	return ret.index
}

func (b *lookaheadPlanner) compile() (ret []LookaheadRule, mapping []int, err error) {
	var s status.Status
	sm := container.NewIntSliceMap(func(key []int) int {
		val := len(ret)
		var input []Lookahead
		for _, k := range key {
			input = append(input, b.lookaheads[k])
		}
		rule, err := newLookaheadRule(input)
		if err != nil {
			var sb strings.Builder
			sb.WriteString("Lookaheads must use mutually exclusive conditions and enumerate disambiguating\n")
			fmt.Fprintf(&sb, "nonterminals in the same order, failed with %v:\n", err)
			for _, la := range input {
				sb.WriteString("\t(?= ")
				for i, pred := range la.Predicates {
					if i > 0 {
						sb.WriteString(" & ")
					}
					if pred.Negated {
						sb.WriteRune('!')
					}
					sb.WriteString(b.g.Symbols[b.g.Inputs[pred.Input].Nonterminal])
				}
				sb.WriteString(")\n")
			}
			msg := sb.String()
			for _, la := range input {
				s.Add(la.Origin.SourceRange(), msg)
			}

			// Avoid cascading failures by having some reasonable rule.
			rule = LookaheadRule{DefaultTarget: input[0].Nonterminal}
		}
		ret = append(ret, rule)
		return val
	})
	for _, r := range b.rules {
		if r.refCount == 0 {
			mapping = append(mapping, -1)
			continue
		}
		sort.Ints(r.lookaheads)
		for i, val := range r.lookaheads[1:] {
			if r.lookaheads[i] == val {
				// By construction, a single laRule contains a set of conflicting production
				// rules, and production rules correspond 1:1 to lookaheads.
				log.Fatal("internal error: no duplicates are allowed")
			}
		}
		r.index = sm.Get(r.lookaheads)
		mapping = append(mapping, b.ruleBase+r.index)
	}
	return ret, mapping, s.Err()
}

func newLookaheadRule(lookaheads []Lookahead) (LookaheadRule, error) {
	type node struct {
		input int32
		prev  []*node
		state uint8
		depth int
	}
	nodes := make(map[int32]*node) // input -> node

	var top node
	for _, la := range lookaheads {
		var prev *node
		for _, pred := range la.Predicates {
			n, ok := nodes[pred.Input]
			if !ok {
				n = &node{input: pred.Input}
				nodes[pred.Input] = n
			}
			if prev != nil {
				n.prev = append(n.prev, prev)
			}
			prev = n
		}
		if prev != nil {
			top.prev = append(top.prev, prev)
		}
	}

	var cycle bool
	var order []int32
	var dfs func(n *node)
	dfs = func(n *node) {
		switch n.state {
		case 1:
			cycle = true
			return
		case 2:
			return
		}
		n.state = 1
		n.depth = 1
		for _, prev := range n.prev {
			dfs(prev)
			if prev.depth >= n.depth {
				n.depth = prev.depth + 1
			}
		}
		n.state = 2
		order = append(order, n.input)
	}
	dfs(&top)
	ok := !cycle && top.depth == len(nodes)+1
	if !ok {
		if cycle {
			return LookaheadRule{}, fmt.Errorf("inconsistent order")
		}
		return LookaheadRule{}, fmt.Errorf("ambiguous order")
	}

	// Note: the algorithm below is O(n^4), but n is small (<= 4 in all practical grammars).
	var ret LookaheadRule
outer:
	for len(lookaheads) > 1 {
		// Lookaheads should be enumerated in the order of increasing costs, so
		// trying the list from the beginning every time.
		for i, next := range order {
			k, negated, ok := pickLookahead(next, lookaheads)
			if ok {
				ret.Cases = append(ret.Cases, LookaheadCase{
					Predicate: Predicate{Input: next, Negated: negated},
					Target:    lookaheads[k].Nonterminal,
				})
				lookaheads[k] = lookaheads[len(lookaheads)-1]
				lookaheads = lookaheads[:len(lookaheads)-1]
				order[i] = order[len(order)-1]
				order = order[:len(order)-1]
				continue outer
			}
		}
		return LookaheadRule{}, fmt.Errorf("cannot decide on the next lookahead")
	}
	ret.DefaultTarget = lookaheads[0].Nonterminal
	return ret, nil
}

func pickLookahead(input int32, lookaheads []Lookahead) (index int, negated, ok bool) {
	pos, neg := -1, -1
	for i, la := range lookaheads {
		negated, ok := la.Accepts(input)
		if !ok {
			// This lookahead is not dependent on "input". Give up.
			return -1, false, false
		}
		switch {
		case !negated && pos == -1:
			pos = i
		case negated && neg == -1:
			neg = i
		case negated:
			neg = -2 // ambiguous
		default:
			pos = -2 // ambiguous
		}
	}
	if pos >= 0 {
		return pos, false, true
	}
	return neg, true, neg >= 0
}
