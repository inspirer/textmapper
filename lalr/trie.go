package lalr

import (
	"sort"

	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/sparse"
)

// trieBuilder builds a lookahead automaton to resolve LALR conflicts that require
// more than one token of lookahead. It works by following the transition-based
// follow sets computed by buildLA(useTransitions=true).
//
// The automaton is encoded in the Lalr array using the same convention:
// pairs of (terminal, action), terminated by (-1, default). An action < -2
// means "follow to automaton state at Lalr[-3 - action]", recursively.
type trieBuilder struct {
	c      *compiler
	follow []sparse.Set // transition-based follow sets from buildLA

	// Shared across minimize/emit calls for cross-state deduplication.
	cache   *container.IntSliceMap[*trieNode]
	nextID  int
	emitted map[*trieNode]int // node -> offset in lalr
}

func newTrieBuilder(c *compiler) *trieBuilder {
	return &trieBuilder{
		c:      c,
		follow: c.follow,
		cache: container.NewIntSliceMap(func(key []int) *trieNode {
			return new(trieNode)
		}),
		emitted: make(map[*trieNode]int),
	}
}

// trieNode represents a node in the lookahead trie before minimization.
type trieNode struct {
	edges     []trieEdge
	defAction int // default action for terminals not covered by edges (-2 = error)
	id        int // assigned during minimization for dedup
	depth     int // max height of the trie rooted at this node (1 = leaf)
}

// trieEdge represents one edge in the trie: on seeing this terminal, take
// the action or follow to a child node for more lookahead.
type trieEdge struct {
	terminal int
	action   int       // >= 0 reduce, -1 shift, -2 error (only when child == nil)
	child    *trieNode // non-nil means "need more lookahead"
}

// resolve attempts to resolve a reduce/reduce conflict among the given rules
// by building a lookahead trie up to maxDepth levels deep.
//
// ruleGts maps each conflicting rule index to the set of goto indices that
// should be followed to find the next-level terminals. These are the transition
// indices from follow[gt] for the conflict terminal at the previous level.
//
// Returns nil if the conflict cannot be resolved within maxDepth.
func (b *trieBuilder) resolve(ruleGts map[int][]int, maxDepth int) *trieNode {
	if maxDepth <= 0 {
		return nil
	}

	// Collect the next-level terminal transitions for each rule.
	type termGts struct {
		terminal int
		gts      []int
	}
	type ruleInfo struct {
		rule     int
		terms    []termGts
		allTerms bool // if the rule can be followed by any terminal
	}
	var rules []ruleInfo

	for rule, gts := range ruleGts {
		ri := ruleInfo{rule: rule}
		termMap := make(map[int][]int) // terminal -> gts at next level
		for _, gt := range gts {
			if gt == b.c.allTokensMarker {
				ri.allTerms = true
				continue
			}
			for _, gt2 := range b.follow[gt] {
				if gt2 == b.c.allTokensMarker {
					ri.allTerms = true
					continue
				}
				sym := int(b.c.states[b.c.out.FromTo[2*gt2+1]].symbol)
				termMap[sym] = append(termMap[sym], gt2)
			}
		}
		for term, nextGts := range termMap {
			ri.terms = append(ri.terms, termGts{terminal: term, gts: nextGts})
		}
		// Sort for determinism.
		sort.Slice(ri.terms, func(i, j int) bool {
			return ri.terms[i].terminal < ri.terms[j].terminal
		})
		rules = append(rules, ri)
	}
	// Sort rules for determinism.
	sort.Slice(rules, func(i, j int) bool {
		return rules[i].rule < rules[j].rule
	})

	// Collect all next-level terminals across all rules.
	terminalSet := container.NewBitSet(b.c.grammar.Terminals)
	for _, ri := range rules {
		if ri.allTerms {
			// If any rule has allTerms, all terminals up to Terminals are candidates.
			terminalSet.SetAll(b.c.grammar.Terminals)
			break
		}
		for _, tg := range ri.terms {
			terminalSet.Set(tg.terminal)
		}
	}

	ret := &trieNode{defAction: -2, depth: 1}
	for _, term := range terminalSet.Slice(nil) {
		// Find which rules can reach this next-level terminal.
		var candidates []int
		candidateGts := make(map[int][]int) // rule -> gts for this terminal at next level

		for _, ri := range rules {
			var gts []int
			var found bool
			for _, tg := range ri.terms {
				if tg.terminal == term {
					gts = tg.gts
					found = true
					break
				}
			}
			if !found && ri.allTerms {
				// Rule can be followed by any terminal, so it's a candidate
				// but has no specific gts to follow deeper.
				found = true
				gts = nil
			}
			if found {
				candidates = append(candidates, ri.rule)
				candidateGts[ri.rule] = gts
			}
		}

		switch len(candidates) {
		case 0:
			continue
		case 1:
			// Resolved: only one rule can follow this terminal.
			ret.edges = append(ret.edges, trieEdge{
				terminal: term,
				action:   candidates[0],
			})
			continue
		}

		// Still ambiguous among multiple rules — check if we can recurse.
		// All candidates must have gts to follow, otherwise we can't resolve deeper.
		canRecurse := true
		for _, rule := range candidates {
			if len(candidateGts[rule]) == 0 {
				canRecurse = false
				break
			}
		}
		if !canRecurse {
			return nil
		}
		child := b.resolve(candidateGts, maxDepth-1)
		if child == nil {
			return nil // cannot resolve
		}
		ret.edges = append(ret.edges, trieEdge{
			terminal: term,
			child:    child,
		})
	}

	if len(ret.edges) == 0 {
		return nil // not resolved
	}
	for _, e := range ret.edges {
		if e.child != nil {
			ret.depth = max(ret.depth, e.child.depth+1)
		}
	}
	return ret
}

// minimize deduplicates trie nodes bottom-up, returning a potentially shared DAG.
func (b *trieBuilder) minimize(root *trieNode) *trieNode {
	var key []int
	var visit func(n *trieNode) *trieNode
	visit = func(n *trieNode) *trieNode {
		// First, minimize children.
		for i, val := range n.edges {
			if val.child == nil {
				continue
			}
			n.edges[i].child = visit(val.child)
		}

		// Pack the canonical key for this node into a slice of ints.
		key = append(key[:0], n.defAction)
		for _, e := range n.edges {
			key = append(key, e.terminal)
			if e.child != nil {
				key = append(key, -3-e.child.id)
			} else {
				key = append(key, e.action)
			}
		}
		ret := b.cache.Get(key)
		if ret.edges != nil {
			// Reuse an existing node.
			return ret
		}
		n.id = b.nextID
		b.nextID++
		*ret = *n
		return ret
	}
	return visit(root)
}

// emit writes the trie into the lalr array (b.c.out.Lalr) and returns the
// starting offset of the root node.
//
// Nodes are emitted in post-order so children are emitted before parents.
func (b *trieBuilder) emit(node *trieNode) int {
	lalr := b.c.out.Lalr
	var postOrder func(n *trieNode)
	postOrder = func(n *trieNode) {
		if n == nil {
			return
		}
		if _, ok := b.emitted[n]; ok {
			return // already emitted (shared via minimization)
		}
		// Emit children first.
		for _, e := range n.edges {
			postOrder(e.child)
		}
		// Emit this node.
		offset := len(lalr)
		for _, e := range n.edges {
			lalr = append(lalr, e.terminal)
			if e.child != nil {
				childOffset := b.emitted[e.child]
				lalr = append(lalr, -3-childOffset) // pointer to child
			} else {
				lalr = append(lalr, e.action)
			}
		}
		lalr = append(lalr, -1, n.defAction) // terminator
		b.emitted[n] = offset
	}
	postOrder(node)
	b.c.out.Lalr = lalr
	return b.emitted[node]
}
