package lalr

import (
	"log"
	"strings"

	"github.com/inspirer/textmapper/tm-go/status"
	"github.com/inspirer/textmapper/tm-go/util/container"
	"github.com/inspirer/textmapper/tm-go/util/graph"
)

// Compile generates LALR tables for a given grammar.
func Compile(grammar *Grammar) (*Tables, error) {
	c := &compiler{
		grammar: grammar,
		out:     &Tables{},
		empty:   container.NewBitSet(len(grammar.Symbols)),
	}

	c.init()
	c.computeEmpty()
	c.computeSets()
	c.computeStates()

	c.initLalr()
	c.buildFollow()
	c.buildLA()
	return c.out, c.s.Err()
}

type compiler struct {
	grammar *Grammar
	out     *Tables
	s       status.Status

	index []int // the rule start in "right"
	right []int // all rules flattened into one slice, each position in this slice is an LR(0) item
	empty container.BitSet

	rules  []container.BitSet // nonterminal -> LR(0) items
	shifts [][]int            // symbol -> [<the number of symbol occurrences in "right">]int

	states []*state

	follow []container.BitSet // goto -> set of accepted terminals
}

type state struct {
	index       int
	symbol      Sym
	sourceState int
	core        []int
	shifts      []int // slice of target state indices sorted by state.symbol
	reduce      []int // slice of rule indices (sorted)
	lr0         bool

	// for non-lr0 states
	la       []container.BitSet // set of accepted terminals after each reduction in "reduce"
	lookback [][]int            // slice of gotos per reduction to pull LA from
}

func (c *compiler) init() {
	right := make([]int, 0, len(c.grammar.Rules)*8)
	for i, r := range c.grammar.Rules {
		c.index = append(c.index, len(right))
		for _, sym := range r.RHS {
			if sym.IsStateMarker() {
				// TODO support state markers
				continue
			}
			right = append(right, int(sym))
		}
		right = append(right, -1-i)
	}
	c.right = right

	// Initialize c.shifts.
	count := make([]int, len(c.grammar.Symbols))
	for _, r := range right {
		if r >= 0 {
			count[r]++
		}
	}
	buf := make([]int, len(c.right))
	c.shifts = make([][]int, len(c.grammar.Symbols))
	for i, ln := range count {
		c.shifts[i] = buf[:0:ln] // override the cap
		buf = buf[ln:]
	}
}

// computeEmpty computes the set of nullable nonterminals.
func (c *compiler) computeEmpty() {
	for {
		var keepGoing bool
		for _, r := range c.grammar.Rules {
			if c.empty.Get(int(r.LHS)) {
				continue
			}
			empty := true
			for _, sym := range r.RHS {
				if !sym.IsStateMarker() && !c.empty.Get(int(sym)) {
					empty = false
					break
				}
			}
			if empty {
				c.empty.Set(int(r.LHS))
				keepGoing = true
			}
		}
		if !keepGoing {
			break
		}
	}
}

func (c *compiler) computeSets() {
	d := c.grammar.Terminals
	n := len(c.grammar.Symbols) - d
	first := graph.NewMatrix(n)
	for i, r := range c.grammar.Rules {
		if e := c.right[c.index[i]]; e >= d {
			first.AddEdge(int(r.LHS)-d, e-d)
		}
	}
	var rules []container.BitSet // nonterminal -> LR(0) items
	for i := 0; i < n; i++ {
		rules = append(rules, container.NewBitSet(len(c.right)))
	}
	for i, r := range c.grammar.Rules {
		rules[int(r.LHS)-d].Set(c.index[i])
	}

	g := first.Graph(nil)
	graph.Tarjan(g, func(nonterms []int, _ container.BitSet) {
		set := rules[nonterms[0]]
		for _, nt := range nonterms[1:] {
			set.Or(rules[nt])
			rules[nt] = set
		}
		for _, nt := range nonterms {
			for _, target := range g[nt] {
				set.Or(rules[target])
			}
		}
	})
	c.rules = rules
}

func (c *compiler) computeStates() {
	for i := range c.grammar.Inputs {
		c.states = append(c.states, &state{index: i})
	}
	stateMap := container.NewIntSliceMap(func(core []int) interface{} {
		s := &state{
			index:       len(c.states),
			symbol:      Sym(c.right[core[0]-1]),
			sourceState: -1,
			core:        core,
		}
		c.states = append(c.states, s)
		return s
	})

	set := container.NewBitSet(len(c.right))
	reuse := make([]int, len(c.right))
	for i := 0; i < len(c.states); i++ {
		curr := c.states[i]
		c.stateClosure(curr, set)
		closure := set.Slice(reuse)

		for _, item := range closure {
			r := c.right[item]
			if r >= 0 {
				c.shifts[r] = append(c.shifts[r], item+1)
			} else {
				curr.reduce = append(curr.reduce, -1-r)
			}
		}
		for sym, core := range c.shifts {
			if len(core) == 0 {
				continue
			}
			state := stateMap.Get(core).(*state)
			if state.sourceState == -1 {
				state.sourceState = curr.index
			}
			curr.shifts = append(curr.shifts, state.index)
			c.shifts[sym] = core[:0]
		}

		nreduce := len(curr.reduce)
		curr.lr0 = nreduce == 0 || nreduce == 1 && len(curr.shifts) == 0
	}

	// Add final states (if needed).
	finalStates := make([]int32, len(c.grammar.Inputs))
	for i, inp := range c.grammar.Inputs {
		var last *state
		for _, target := range c.states[i].shifts {
			if state := c.states[target]; state.symbol == inp.Nonterminal {
				last = state
				break
			}
		}
		if last == nil {
			last = &state{index: len(c.states), symbol: inp.Nonterminal, sourceState: i, lr0: true}
			c.states = append(c.states, last)
			c.addShift(c.states[i], last)
		}

		if !inp.Eoi {
			// no-eoi inputs are accepted as soon as they are reduced without the last transition on EOI.
			finalStates[i] = int32(last.symbol)
			continue
		}
		afterEoi := &state{index: len(c.states), symbol: EOI, sourceState: last.index, lr0: true}
		c.states = append(c.states, afterEoi)
		c.addShift(last, afterEoi)
		finalStates[i] = int32(afterEoi.symbol)
	}
	c.out.FinalStates = finalStates
}

func (c *compiler) addShift(from, to *state) {
	if len(from.shifts) == 0 && len(from.reduce) > 0 {
		from.lr0 = false
	}
	from.shifts = append(from.shifts, to.index)
	if len(from.shifts) == 1 {
		return
	}
	var i int
	for i < len(from.shifts) && c.states[from.shifts[i]].symbol < to.symbol {
		i++
	}
	if i+1 < len(from.shifts) {
		copy(from.shifts[i+1:], from.shifts[i:])
		from.shifts[i] = to.index
	}
}

func (c *compiler) stateClosure(state *state, out container.BitSet) {
	out.ClearAll(len(c.right))
	if state.index < len(c.grammar.Inputs) {
		inp := c.grammar.Inputs[state.index]
		out.Or(c.rules[int(inp.Nonterminal)-c.grammar.Terminals])
		return
	}

	for _, item := range state.core {
		out.Set(item)
		if sym := c.right[item]; sym >= c.grammar.Terminals {
			out.Or(c.rules[sym-c.grammar.Terminals])
		}
	}
}

func (c *compiler) writeItem(item int, out *strings.Builder) {
	i := item
	for c.right[i] >= 0 {
		i++
	}
	rule := c.grammar.Rules[-1-c.right[i]]
	pos := len(rule.RHS) - (i - item)
	out.WriteString(c.grammar.Symbols[rule.LHS])
	out.WriteString(" : ")
	for i, sym := range rule.RHS {
		if i > 0 {
			out.WriteString(" ")
		}
		if i == pos {
			out.WriteString("_ ")
		}
		out.WriteString(c.grammar.Symbols[sym])
	}
	if pos == len(rule.RHS) {
		if pos > 0 {
			out.WriteString(" ")
		}
		out.WriteString("_")
	}
}

func (c *compiler) initLalr() {
	// Initialize per-state variables.
	var n int
	for _, state := range c.states {
		if !state.lr0 {
			n += len(state.reduce)
		}
	}
	la := container.NewBitSetSlice(c.grammar.Terminals, n)
	pool := make([][]int, n)
	for _, state := range c.states {
		if state.lr0 {
			continue
		}
		ln := len(state.reduce)
		state.la = la[:ln]
		la = la[ln:]
		state.lookback = pool[:ln]
		pool = pool[ln:]
	}

	// Initialize goto.
	syms := len(c.grammar.Symbols)
	count := make([]int, syms)
	n = 0
	for _, state := range c.states {
		for _, target := range state.shifts {
			count[c.states[target].symbol]++
			n++
		}
	}
	c.out.Goto = make([]int32, syms+1)
	c.out.FromTo = make([]int32, 2*n)
	n = 0
	for i := range c.grammar.Symbols {
		c.out.Goto[i] = int32(n)
		n += count[i] * 2
		count[i] = n
	}
	c.out.Goto[syms] = int32(n)
	for i := len(c.states) - 1; i >= 0; i-- {
		state := c.states[i]
		for _, target := range state.shifts {
			sym := c.states[target].symbol
			i := count[sym] - 2
			count[sym] = i
			c.out.FromTo[i] = int32(state.index)
			c.out.FromTo[i+1] = int32(target)
		}
	}
}

func (c *compiler) buildFollow() {
	c.follow = container.NewBitSetSlice(c.grammar.Terminals, len(c.out.FromTo)/2)

	// Step 1. Build in-rule follow set and process empty symbols.
	empties := make([][]int, len(c.follow))
	for gt, follow := range c.follow {
		from, to := c.states[c.out.FromTo[2*gt]], c.states[c.out.FromTo[2*gt+1]]
		if int(to.symbol) < c.grammar.Terminals {
			continue
		}

		if from.index < len(c.grammar.Inputs) {
			inp := c.grammar.Inputs[from.index]
			if !inp.Eoi && c.out.FinalStates[from.index] == int32(to.index) {
				follow.SetAll(c.grammar.Terminals)
			}
		}
		for _, shift := range to.shifts {
			sym := c.states[shift].symbol
			if int(sym) < c.grammar.Terminals {
				follow.Set(int(sym))
			} else if c.empty.Get(int(sym)) {
				empties[gt] = append(empties[gt], c.selectGoto(int32(to.index), int32(sym)))
			}
		}
	}
	c.updateFollow(empties)

	// Step 2. Build cross-rule follow set and populate the lookback slice.
	rules := make([][]int, len(c.grammar.Symbols)-c.grammar.Terminals)
	for i, rule := range c.grammar.Rules {
		nt := int(rule.LHS) - c.grammar.Terminals
		rules[nt] = append(rules[nt], i)
	}

	states := make([]int32, 32)
	g := empties
	for i := range g {
		g[i] = g[i][:0]
	}
	for gt := range c.follow {
		state := c.states[c.out.FromTo[2*gt]]
		sym := c.states[c.out.FromTo[2*gt+1]].symbol
		if int(sym) < c.grammar.Terminals {
			continue
		}
		for _, rule := range rules[int(sym)-c.grammar.Terminals] {
			i := c.index[rule]
			states = states[:0]
			curr := int32(state.index)

			for ; c.right[i] >= 0; i++ {
				states = append(states, curr)
				curr = c.gotoState(curr, Sym(c.right[i]))
			}

			if !c.states[curr].lr0 {
				// Rule's lookahead symbols include follow set for the current goto (gt).
				c.addLookback(curr, rule, gt)
			}

			i--
			for is := len(states) - 1; is >= 0; is, i = is-1, i-1 {
				curr, sym := states[is], c.right[i]
				if sym < c.grammar.Terminals {
					break
				}
				// Inner rule's goto inherits outer follow set.
				g[gt] = append(g[gt], c.selectGoto(curr, int32(sym)))
				if !c.empty.Get(sym) {
					break
				}
			}
		}
	}
	g = graph.Transpose(g)
	c.updateFollow(g)
}

func (c *compiler) updateFollow(g [][]int) {
	graph.Tarjan(g, func(gotos []int, _ container.BitSet) {
		set := c.follow[gotos[0]]
		for _, nt := range gotos[1:] {
			set.Or(c.follow[nt])
			c.follow[nt] = set
		}
		for _, nt := range gotos {
			for _, target := range g[nt] {
				set.Or(c.follow[target])
			}
		}
	})
}

func (c *compiler) addLookback(state int32, rule, gt int) {
	s := c.states[state]
	for i, rr := range s.reduce {
		if rr == rule {
			s.lookback[i] = append(s.lookback[i], gt)
			return
		}
	}
	log.Fatal("internal error")
}

func (c *compiler) gotoState(state int32, sym Sym) int32 {
	for _, target := range c.states[state].shifts {
		if c.states[target].symbol == sym {
			return int32(target)
		}
	}

	log.Fatal("internal error")
	return -1
}

func (c *compiler) selectGoto(state, sym int32) int {
	min := c.out.Goto[sym]
	max := c.out.Goto[sym+1]

	if max-min < 32 {
		for e := min; e < max; e += 2 {
			if c.out.FromTo[e] == state {
				return int(e / 2)
			}
		}
	} else {
		for min < max {
			e := (min + max) >> 1 &^ int32(1)
			i := c.out.FromTo[e]
			if i == state {
				return int(e / 2)
			} else if i < state {
				min = e + 2
			} else {
				max = e
			}
		}
	}
	return -1
}

func (c *compiler) buildLA() {
	for _, state := range c.states {
		for i, lookback := range state.lookback {
			for _, from := range lookback {
				state.la[i].Or(c.follow[from])
			}
		}
	}
}
