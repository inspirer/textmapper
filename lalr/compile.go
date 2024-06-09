package lalr

import (
	"fmt"
	"log"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/graph"
)

// Options parameterizes the LALR table generation.
type Options struct {
	Optimize      bool // compress tables for faster lookups
	DefaultReduce bool // Bison compatibility mode, perform a default reduction in non-LR(0) states instead of reporting an error
	Debug         bool // embed debug information into the tables
}

// Compile generates LALR tables for a given grammar.
func Compile(grammar *Grammar, opts Options) (*Tables, error) {
	c := &compiler{
		grammar: grammar,
		out: &Tables{
			DefaultEnc: &DefaultEnc{},
		},
		empty: container.NewBitSet(len(grammar.Symbols)),
	}

	c.init()
	c.computeEmpty()
	c.computeSets()
	c.computeStates()
	c.checkLR0()

	c.initLalr()
	c.buildFollow()
	c.buildLA()
	c.populateTables()
	c.reportConflicts()

	if opts.Debug {
		c.exportDebugInfo()
	}

	if opts.Optimize {
		numRules := len(c.out.RuleLen) // takes into account runtime lookahead rules
		c.out.Optimized = Optimize(c.out.DefaultEnc, grammar.Terminals, numRules, opts.DefaultReduce)
	}
	return c.out, c.s.Err()
}

type compiler struct {
	grammar *Grammar
	out     *Tables
	s       status.Status
	pending []*Conflict // to be reported if the number of conflicts does not match the expectations

	index   []int // the rule start in "right"
	right   []int // all rules flattened into one slice, each position in this slice is an LR(0) item
	empty   container.BitSet
	markers map[int][]int // index in "right" -> markers

	rules  []container.BitSet // nonterminal -> LR(0) items
	shifts [][]int            // symbol -> [<the number of symbol occurrences in "right">]int

	states []*state

	follow []container.BitSet // goto -> set of accepted terminals

	precGroup map[Sym]int // terminal -> index in grammar.Precedence
	sr, rr    int         // conflicts

	planner lookaheadPlanner
}

type state struct {
	index       int
	symbol      Sym
	sourceState int
	core        []int
	shifts      []int // slice of target state indices sorted by state.symbol
	reduce      []int // slice of rule indices (sorted)
	lr0         bool
	dropped     []int // items dropped from core by .greedy (sorted)

	// for non-lr0 states
	la       []container.BitSet // set of accepted terminals after each reduction in "reduce"
	lookback [][]int            // slice of gotos per reduction to pull LA from
}

func (c *compiler) init() {
	// Initialize markers.
	c.markers = make(map[int][]int)
	for _, name := range c.grammar.Markers {
		c.out.Markers = append(c.out.Markers, StateMarker{Name: name})
	}

	right := make([]int, 0, len(c.grammar.Rules)*8)
	for i, r := range c.grammar.Rules {
		c.index = append(c.index, len(right))
		for _, sym := range r.RHS {
			if sym.IsStateMarker() {
				c.markers[len(right)] = append(c.markers[len(right)], sym.AsMarker())
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

	// Initialize runtime lookahead planner.
	c.planner.init(c.grammar)
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
	greedy := container.NewBitSet(len(c.right)) // of item
	for i, m := range c.grammar.Markers {
		if m != "greedy" {
			continue
		}
		for item, v := range c.markers {
			if contains(v, i) {
				greedy.Set(item)
			}
		}
		break
	}

	for i := range c.grammar.Inputs {
		c.states = append(c.states, &state{index: i, sourceState: -1})
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
			for _, marker := range c.markers[item] {
				c.out.mark(i, marker)
			}
		}
		for sym, core := range c.shifts {
			if len(core) == 0 {
				continue
			}

			var hasGreedy bool
			var dropped []int
			for _, item := range core {
				hasGreedy = hasGreedy || greedy.Get(item)
			}
			if hasGreedy {
				// Keep ".greedy" rules only.
				list := core
				core = core[:0]
				for _, item := range list {
					if greedy.Get(item) {
						core = append(core, item)
					} else {
						dropped = append(dropped, item)
					}
				}
			}

			state := stateMap.Get(core).(*state)
			if state.sourceState == -1 {
				state.sourceState = curr.index
			}
			state.dropped = union(state.dropped, dropped)
			curr.shifts = append(curr.shifts, state.index)
			c.shifts[sym] = core[:0]
		}

		nreduce := len(curr.reduce)
		hasShifts := len(curr.shifts) > 0 && int(c.states[curr.shifts[0]].symbol) < c.grammar.Terminals
		curr.lr0 = nreduce == 0 || nreduce == 1 && !hasShifts
	}

	// Add final states (if needed).
	finalStates := make([]int, len(c.grammar.Inputs))
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

		finalStates[i] = last.index
	}
	for i, inp := range c.grammar.Inputs {
		if !inp.Eoi {
			// no-eoi inputs are accepted as soon as they are reduced without the last transition on EOI.
			continue
		}
		last := finalStates[i]
		afterEoi := &state{index: len(c.states), symbol: EOI, sourceState: last, lr0: true}
		c.states = append(c.states, afterEoi)
		c.addShift(c.states[last], afterEoi)
		finalStates[i] = afterEoi.index
	}
	c.out.FinalStates = finalStates
	c.out.NumStates = len(c.states)
}

func (c *compiler) checkLR0() {
	var states []int
	for _, m := range c.out.Markers {
		if m.Name == "lr0" {
			states = m.States
			break
		}
	}
	if len(states) == 0 {
		return
	}

	lr0 := container.NewBitSet(len(c.right)) // of item
	for i, m := range c.grammar.Markers {
		if m != "lr0" {
			continue
		}
		for item, v := range c.markers {
			if contains(v, i) {
				lr0.Set(item)
			}
		}
		break
	}

	seen := make(map[int]bool) // rules
	for _, state := range states {
		if c.states[state].lr0 {
			continue
		}
		for _, item := range c.states[state].core {
			if !lr0.Get(item) {
				continue
			}
			rule := c.rule(item)
			if seen[rule] {
				continue
			}
			seen[rule] = true
			c.s.Errorf(c.grammar.Rules[rule].Origin, "Found an lr0 marker inside a non-LR0 state (%v)", state)
		}
	}
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

func (c *compiler) writeRule(r int, out *strings.Builder) {
	if r >= len(c.grammar.Rules) {
		// This is a runtime lookahead rule.
		nt := c.out.RuleSymbol[r]
		out.WriteString(c.grammar.Symbols[nt])
		out.WriteByte(':')
		return
	}

	rule := c.grammar.Rules[r]
	out.WriteString(c.grammar.Symbols[rule.LHS])
	out.WriteString(":")
	for _, sym := range rule.RHS {
		out.WriteString(" ")
		if sym.IsStateMarker() {
			out.WriteByte('.')
			out.WriteString(c.out.Markers[sym.AsMarker()].Name)
			continue
		}
		out.WriteString(c.grammar.Symbols[sym])
	}
}

func (c *compiler) rule(item int) int {
	i := item
	for c.right[i] >= 0 {
		i++
	}
	return -1 - c.right[i]
}

func (c *compiler) writeItem(item int, out *strings.Builder) {
	i := item
	for c.right[i] >= 0 {
		i++
	}
	rule := c.grammar.Rules[-1-c.right[i]]
	pos := len(rule.RHS) - (i - item)
	for _, sym := range rule.RHS {
		if sym.IsStateMarker() {
			pos--
		}
	}

	out.WriteString(c.grammar.Symbols[rule.LHS])
	out.WriteString(":")
	var index int
	for _, sym := range rule.RHS {
		out.WriteString(" ")
		if sym.IsStateMarker() {
			out.WriteByte('.')
			out.WriteString(c.out.Markers[sym.AsMarker()].Name)
			continue
		}
		if index == pos {
			out.WriteString("_ ")
		}
		out.WriteString(c.grammar.Symbols[sym])
		index++
	}
	if pos == index {
		out.WriteString(" ")
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
	c.out.Goto = make([]int, syms+1)
	c.out.FromTo = make([]int, 2*n)
	n = 0
	for i := range c.grammar.Symbols {
		c.out.Goto[i] = n
		n += count[i] * 2
		count[i] = n
	}
	c.out.Goto[syms] = n
	for i := len(c.states) - 1; i >= 0; i-- {
		state := c.states[i]
		for _, target := range state.shifts {
			sym := c.states[target].symbol
			i := count[sym] - 2
			count[sym] = i
			c.out.FromTo[i] = state.index
			c.out.FromTo[i+1] = target
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
			if !inp.Eoi && c.out.FinalStates[from.index] == to.index {
				follow.SetAll(c.grammar.Terminals)
			}
		}
		for _, shift := range to.shifts {
			sym := c.states[shift].symbol
			if int(sym) < c.grammar.Terminals {
				follow.Set(int(sym))
			} else if c.empty.Get(int(sym)) {
				empties[gt] = append(empties[gt], c.selectGoto(to.index, sym))
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

	states := make([]int, 32)
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
	rules:
		for _, rule := range rules[int(sym)-c.grammar.Terminals] {
			i := c.index[rule]
			states = states[:0]
			curr := state.index

			for ; c.right[i] >= 0; i++ {
				states = append(states, curr)
				curr = c.gotoState(curr, Sym(c.right[i]))
				if curr == -1 {
					// This rule was pruned from the inner chain of transitions.
					continue rules
				}
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
				g[gt] = append(g[gt], c.selectGoto(curr, Sym(sym)))
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

func (c *compiler) addLookback(state int, rule, gt int) {
	s := c.states[state]
	for i, rr := range s.reduce {
		if rr == rule {
			s.lookback[i] = append(s.lookback[i], gt)
			return
		}
	}
	log.Fatal("internal error")
}

func (c *compiler) gotoState(state int, sym Sym) int {
	for _, target := range c.states[state].shifts {
		if c.states[target].symbol == sym {
			return target
		}
	}

	// Note: this happens if we pruned some rules from the state.
	return -1
}

func (c *compiler) selectGoto(state int, sym Sym) int {
	min := c.out.Goto[sym]
	max := c.out.Goto[sym+1]

	if max-min < 32 {
		for e := min; e < max; e += 2 {
			if c.out.FromTo[e] == state {
				return e / 2
			}
		}
	} else {
		for min < max {
			e := (min + max) >> 1 &^ 1
			i := c.out.FromTo[e]
			if i == state {
				return e / 2
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

func (c *compiler) populateTables() {
	c.precGroup = make(map[Sym]int)
	for group, prec := range c.grammar.Precedence {
		for _, term := range prec.Terminals {
			c.precGroup[term] = group
		}
	}

	reuse := make([]int, c.grammar.Terminals)
	actionset := make([]int, c.grammar.Terminals)
	next := make([]int, c.grammar.Terminals)

	c.out.Action = make([]int, len(c.states))
	for _, state := range c.states {
		if state.lr0 {
			switch {
			case len(state.reduce) > 0:
				c.out.Action[state.index] = state.reduce[0]
			case len(state.shifts) > 0:
				c.out.Action[state.index] = -1 // shift
			default:
				c.out.Action[state.index] = -2 // error
			}
			continue
		}

		var conflicts conflictBuilder
		actionset = actionset[:0]
		for i := range next {
			next[i] = -2
		}
		for _, target := range state.shifts {
			term := int(c.states[target].symbol)
			if term >= c.grammar.Terminals {
				break
			}
			next[term] = -1
			actionset = append(actionset, term)
		}
		for i, rule := range state.reduce {
			for _, term := range state.la[i].Slice(reuse) {
				if next[term] == -2 {
					next[term] = rule
					actionset = append(actionset, term)
				} else {
					next[term] = c.ruleAction(next[term], Sym(term), rule, &conflicts)
				}
			}
		}

		for _, conflict := range conflicts.merge(c.grammar, state.index, c.states) {
			if conflict.Resolved {
				continue
			}
			c.pending = append(c.pending, conflict)
			if conflict.CanShift {
				c.sr += len(conflict.Next)
			} else {
				c.rr += len(conflict.Next)
			}
		}

		for i, val := range next {
			if val == -3 {
				next[i] = -2 // non-assoc errors are normal syntax errors
			}
		}
		c.out.Action[state.index] = -3 - len(c.out.Lalr)
		for _, term := range actionset {
			c.out.Lalr = append(c.out.Lalr, term, next[term])
		}
		// Note: all other -2 in c.out.Lalr are caused by non-assoc errros.
		c.out.Lalr = append(c.out.Lalr, -1, -2)
	}

	for _, rule := range c.grammar.Rules {
		var len int
		for _, sym := range rule.RHS {
			if !sym.IsStateMarker() {
				len++
			}
		}
		c.out.RuleLen = append(c.out.RuleLen, len)
		c.out.RuleSymbol = append(c.out.RuleSymbol, int(rule.LHS))
	}

	// Integrating runtime lookahead rules into the tables.
	var mapping []int
	var err error
	c.out.Lookaheads, mapping, err = c.planner.compile()
	if err != nil {
		c.s.AddError(err)
	}
	base := len(c.grammar.Rules)
	for i, val := range c.out.Lalr {
		if i%2 == 1 && val >= base {
			c.out.Lalr[i] = mapping[val-base]
		}
	}
	for _, rule := range c.out.Lookaheads {
		c.out.RuleLen = append(c.out.RuleLen, 0)
		c.out.RuleSymbol = append(c.out.RuleSymbol, int(rule.DefaultTarget))
	}
}

func (c *compiler) reportConflicts() {
	if c.sr == c.grammar.ExpectSR && c.rr == c.grammar.ExpectRR {
		return
	}

	for _, conflict := range c.pending {
		for _, rule := range conflict.Rules {
			c.s.Errorf(c.grammar.Rules[rule].Origin, "%s", conflict)
		}
	}
	c.s.Errorf(c.grammar.Origin, "conflicts: %v shift/reduce and %v reduce/reduce", c.sr, c.rr)
}

func (c *compiler) resolvePrec(rule int, term Sym) resolution {
	rulePrec := c.grammar.Rules[rule].Precedence
	if rulePrec == 0 {
		rhs := c.grammar.Rules[rule].RHS
		for i := len(rhs) - 1; i >= 0; i-- {
			if sym := rhs[i]; sym > 0 && sym < Sym(c.grammar.Terminals) {
				rulePrec = sym
				break
			}
		}
	}
	if rulePrec == 0 || term == 0 {
		return conflict
	}
	reduce, ok1 := c.precGroup[rulePrec]
	shift, ok2 := c.precGroup[term]
	switch {
	case !ok1 || !ok2:
		return conflict
	case reduce > shift:
		return doReduce
	case reduce < shift:
		return doShift
	}
	switch c.grammar.Precedence[shift].Associativity {
	case Left:
		return doReduce
	case Right:
		return doShift
	case NonAssoc:
		return doError
	}
	return conflict
}

func (c *compiler) ruleAction(action int, term Sym, rule int, b *conflictBuilder) int {
	if b.hasConflict(term) || action == -3 {
		// This is already an unresolved conflict. Add "rule" to the upcoming error message.
		b.addRule(term, conflict, rule, true)
		return action
	}
	if action == -1 { // shift
		res := c.resolvePrec(rule, term)
		b.addRule(term, res, rule, true /*canShift*/)
		switch res {
		case doReduce:
			return rule
		case doError:
			return -3
		}
		return -1 // shift
	}
	otherRule := action

	// reduce/reduce conflict
	switch {
	case otherRule >= c.planner.ruleBase:
		if c.planner.index[rule] != -1 {
			// Updating the resolution rule to include a new lookahead.
			return c.planner.addRule(otherRule, rule)
		}

		// We have a partially resolved conflict.
		// Note: Resolution rules are not part of the grammar, report one of the
		// original lookahead rules.
		otherRule = c.planner.rules[otherRule-c.planner.ruleBase].refRule
	case c.planner.index[rule] != -1 && c.planner.index[otherRule] != -1:
		// New resolution rule.
		return c.planner.addRule(otherRule, rule)
	}

	// TODO hint the user about missing lookaheads.

	b.addRule(term, conflict, rule, false /*canShift*/)
	b.addRule(term, conflict, otherRule, false /*canShift*/)
	return action
}

func (c *compiler) exportDebugInfo() {
	final := make(map[int][]int)
	for i, state := range c.out.FinalStates {
		final[state] = append(final[state], i)
	}

	input := func(index int) string {
		inp := c.grammar.Inputs[index]
		ret := c.grammar.Symbols[inp.Nonterminal]
		if !inp.Eoi {
			ret += " no-eoi"
		}
		return ret
	}

	type conflict uint8
	conflicts := make([]conflict, c.grammar.Terminals)

	set := container.NewBitSet(len(c.right))
	reuse := make([]int, len(c.right))
	for _, s := range c.states {
		c.stateClosure(s, set)
		closure := set.Slice(reuse)

		var b strings.Builder
		fmt.Fprintf(&b, "state %v", s.index)

		var attrs []string
		if s.index < len(c.grammar.Inputs) {
			attrs = append(attrs, fmt.Sprintf("input %v", input(s.index)))
		}
		for _, inp := range final[s.index] {
			attrs = append(attrs, fmt.Sprintf("final %v", input(inp)))
		}
		if s.sourceState >= 0 {
			attrs = append(attrs, fmt.Sprintf("from %v on %v", s.sourceState, c.grammar.Symbols[s.symbol]))
		}
		if s.lr0 {
			if len(s.reduce) == 1 {
				attrs = append(attrs, "lr0 -> reduce")
			} else {
				attrs = append(attrs, "lr0 -> shift")
			}

		}
		if len(attrs) > 0 {
			fmt.Fprintf(&b, " (%v)", strings.Join(attrs, ", "))
		}
		b.WriteByte('\n')
		var reduce int
		for _, item := range closure {
			b.WriteString("\t")
			c.writeItem(item, &b)
			if r := c.right[item]; r < 0 {
				rule := c.grammar.Rules[-1-r]
				fmt.Fprintf(&b, " { reduce to %v", c.grammar.Symbols[rule.LHS])
				if !s.lr0 {
					b.WriteString(" lookahead [")
					for i, term := range s.la[reduce].Slice(nil) {
						if i > 0 {
							b.WriteByte(' ')
						}
						b.WriteString(c.grammar.Symbols[term])
					}
					b.WriteByte(']')
				}
				reduce++
				b.WriteString(" }")
			}
			b.WriteString("\n")
		}
		if len(closure) == 0 {
			b.WriteString("\t<empty>\n")
		}

		if len(s.dropped) > 0 {
			b.WriteString("\n\tDropped by .greedy:\n")

			for _, item := range s.dropped {
				b.WriteString("\t  ")
				c.writeItem(item, &b)
				b.WriteString("\n")
			}
		}

		if !s.lr0 || c.out.DefaultEnc.Action[s.index] == -1 /*shift*/ {
			for i := range conflicts {
				conflicts[i] = 0
			}
			for _, target := range s.shifts {
				sym := c.states[target].symbol
				if int(sym) < c.grammar.Terminals {
					conflicts[sym] = 1 // shift
				}
			}
			for i := range s.reduce {
				for _, term := range s.la[i].Slice(nil) {
					if conflicts[term] == 0 {
						conflicts[term] = 2 // reduce
					} else {
						conflicts[term] |= 4 // conflict
					}
				}
			}

			b.WriteString("\n\tAction: ")
			action := c.out.DefaultEnc.Action[s.index]
			switch {
			case action >= 0:
				b.WriteString("reduce `")
				c.writeRule(action, &b)
				b.WriteString("`\n")
			case action == -2:
				b.WriteString("error\n")
			case action == -1:
				b.WriteString("shift\n")
				for _, target := range s.shifts {
					t := c.states[target]
					fmt.Fprintf(&b, "\t\t%v => go to state %v\n", c.grammar.Symbols[t.symbol], target)
				}
			default:
				b.WriteString("lookahead\n")
				a := -action - 3
				for ; c.out.Lalr[a] >= 0; a += 2 {
					term := c.out.Lalr[a]
					fmt.Fprintf(&b, "\t\t%v => ", c.grammar.Symbols[term])
					switch action := c.out.Lalr[a+1]; {
					case action >= 0:
						b.WriteString("reduce `")
						c.writeRule(action, &b)
						b.WriteByte('`')
					case action == -1:
						fmt.Fprintf(&b, "shift, go to state %v", c.out.gotoState(s.index, term))
					case action == -2:
						b.WriteString("error")
					}
					switch conflicts[term] {
					case 5: // shift/reduce
						b.WriteString(" (resolved shift/reduce conflict)")
					case 6: // reduce/reduce
						b.WriteString(" (resolved reduce/reduce conflict)")
					}
					b.WriteByte('\n')
				}
			}
		}
		c.out.DebugInfo = append(c.out.DebugInfo, b.String())
	}
}

func contains(slice []int, val int) bool {
	for _, v := range slice {
		if v == val {
			return true
		}
	}
	return false
}

// union computes the union of two sets represented as sorted slices.
func union(a, b []int) []int {
	switch {
	case len(b) == 0:
		return a
	case len(a) == 0:
		return b
	}

	var ret []int
	var i, j int

	for i < len(a) && j < len(b) {
		if a[i] < b[j] {
			ret = append(ret, a[i])
			i++
			continue
		}
		ret = append(ret, b[j])
		if a[i] == b[j] {
			i++
		}
		j++
	}
	ret = append(ret, a[i:]...)
	ret = append(ret, b[j:]...)
	return ret
}
