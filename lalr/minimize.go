// Package lalr implements LALR table generation and state compression.
package lalr

import (
	"fmt"
	"log"
	"slices"
	"strings"

	"github.com/inspirer/textmapper/util/container"
)

const (
	sigReduce    = 1
	sigShift     = 2
	sigError     = 3
	sigLookahead = 4
	sigPartition = 5
)

func computeRuleClasses(t *Tables, g *Grammar) []int {
	numRules := len(t.RuleLen) // includes Lookaheads
	ruleClass := make([]int, numRules)

	type ruleKey struct {
		lhs    int
		length int
		action int
		typ    int
		flags  string
	}
	ruleToClass := make(map[ruleKey]int)

	for i, r := range g.Rules {
		key := ruleKey{
			lhs:    int(r.LHS),
			length: t.RuleLen[i],
			action: r.Action,
			typ:    r.Type,
			flags:  strings.Join(r.Flags, ","),
		}
		if class, ok := ruleToClass[key]; ok {
			ruleClass[i] = class
		} else {
			class = len(ruleToClass)
			ruleToClass[key] = class
			ruleClass[i] = class
		}
	}
	for i := len(g.Rules); i < numRules; i++ {
		// ruleClass indexes beyond len(g.Rules) correspond to synthetically introduced lookahead rules.
		// We give those a value outside the range of class ids to prevent accidental merging of
		// lookahead related states.
		ruleClass[i] = len(ruleToClass) + i
	}
	return ruleClass
}

func partitionStatesByAction(t *Tables, ruleClass []int, numStates int) ([]int, *container.IntSliceSet) {
	// Initial partitions based on reductions and actions
	// Signature of a state:
	//    Action[s], plus LALR entries substituting rule -> ruleClass
	stateSignature := func(s int) []int {
		act := t.Action[s]
		if act >= 0 {
			return []int{sigReduce, ruleClass[act]}
		}
		if act == -1 {
			return []int{sigShift}
		}
		if act == -2 {
			return []int{sigError}
		}

		var sigParts []int
		sigParts = append(sigParts, sigLookahead)
		a := -act - 3

		previousTerm := -1 // To assert that the LALR table is sorted by term.
		for ; t.Lalr[a] >= 0; a += 2 {
			term := t.Lalr[a]
			rule := t.Lalr[a+1]
			if term <= previousTerm {
				log.Fatalf("LaLr table is not sorted by term: term %d < lastTerm %d", term, previousTerm)
			}
			if rule >= 0 {
				rule = ruleClass[rule]
			}
			// Note: rule <= -3 means a recursive lookup in the lalr table requiring one more token of
			// lookahead. Those extra states are minimized during construction, so we don't go recursively
			// here.
			sigParts = append(sigParts, term, rule)
		}
		return sigParts
	}

	partition := make([]int, numStates)
	partitions := container.NewIntSliceSet()

	// Create the initial partitions
	for i := 0; i < numStates; i++ {
		sig := stateSignature(i)
		partition[i] = partitions.Insert(sig)
	}
	return partition, partitions
}

// refinePartitions improves the state partitioning by ensuring that states in the same
// partition transition to the same partition for any given symbol.
// State A and State B may be in the same initial partition, but if State A transitions to
// State C on symbol x, while State B transitions to State D on symbol x, we can only
// merge A and B if C and D are also mergeable (i.e., they are in the same partition).
// If C and D are in different partitions, A and B must be split into separate partitions.
func refinePartitions(partition []int, partitions *container.IntSliceSet, t *Tables) ([]int, *container.IntSliceSet) {
	numStates := len(partition)
	numSymbols := len(t.Goto) - 1
	// Build a state -> []{symbol, target_state} index to avoid expensive gotoState
	// calls during refinement.
	stateTransitions := make([][]int, numStates)
	for sym := 0; sym < numSymbols; sym++ {
		min := t.Goto[sym]
		max := t.Goto[sym+1]
		for i := min; i < max; i += 2 {
			from := t.FromTo[i]
			to := t.FromTo[i+1]
			stateTransitions[from] = append(stateTransitions[from], sym, to)
		}
	}
	for {
		newPartitions := container.NewIntSliceSet()
		newPartition := make([]int, numStates)

		for i := 0; i < numStates; i++ {
			var sigParts []int
			// Use sigPartition as prefix to distinguish from state signatures (which use 1..4),
			// though state signatures and partition signatures are used in different maps.
			sigParts = append(sigParts, sigPartition, partition[i])
			trans := stateTransitions[i]
			for j := 0; j < len(trans); j += 2 {
				sym := trans[j]
				tgt := trans[j+1]
				sigParts = append(sigParts, sym, partition[tgt])
			}
			newPartition[i] = newPartitions.Insert(sigParts)
		}

		// Partitions converged.
		if newPartitions.Len() == partitions.Len() {
			break
		}
		partition = newPartition
		partitions = newPartitions
	}
	return partition, partitions
}

// minimize tries to compact the automaton by merging equivalent states (lead to the
// same set of parsing actions for any given input).
func minimize(t *Tables, g *Grammar) {
	numStates := t.NumStates
	ruleClass := computeRuleClasses(t, g)
	partition, partitions := partitionStatesByAction(t, ruleClass, numStates)
	partition, partitions = refinePartitions(partition, partitions, t)

	if partitions.Len() == numStates {
		return // No states to merge
	}

	// Remap states
	remap := partition
	newNumStates := partitions.Len()

	// Rebuild Tables
	newAction := make([]int, newNumStates)
	for i := 0; i < numStates; i++ {
		newAction[remap[i]] = t.Action[i]
	}
	t.Action = newAction

	// Rebuild FinalStates
	for i, s := range t.FinalStates {
		t.FinalStates[i] = remap[s]
	}

	// Rebuild Markers
	for i := range t.Markers {
		newStates := t.Markers[i].States[:0]
		seen := make(map[int]bool)
		for _, s := range t.Markers[i].States {
			ns := remap[s]
			if !seen[ns] {
				seen[ns] = true
				newStates = append(newStates, ns)
			}
		}
		t.Markers[i].States = newStates
	}

	numSymbols := len(t.Goto) - 1
	// Rebuild FromTo
	var newFromTo []int
	newGoto := make([]int, numSymbols+1)

	type edge struct{ from, to int }
	var edges []edge

	for sym := 0; sym < numSymbols; sym++ {
		newGoto[sym] = len(newFromTo)
		min := t.Goto[sym]
		max := t.Goto[sym+1]

		// To avoid duplicates, we track which "from" states we've already added an edge for this
		// symbol. Since we merge states, multiple old `from` states might map to the same new `from`
		// state. Also `to` states will map to the new `to` state. We need to deduplicate edges so that
		// there's only one (from, to) pair per symbol.

		edges = edges[:0]
		for i := min; i < max; i += 2 {
			from := remap[t.FromTo[i]]
			to := remap[t.FromTo[i+1]]
			edges = append(edges, edge{from, to})
		}

		// Sort the edges to ensure that gotoState binary search works correctly
		slices.SortFunc(edges, func(a, b edge) int {
			return a.from - b.from
		})
		// Deduplicate edges by from state.
		// If we merged two states, they should have an identical set of outgoing edges.
		edges = slices.CompactFunc(edges, func(a, b edge) bool {
			return a.from == b.from
		})

		for _, e := range edges {
			newFromTo = append(newFromTo, e.from, e.to)
		}
	}
	newGoto[numSymbols] = len(newFromTo)
	t.Goto = newGoto
	t.FromTo = newFromTo
	t.NumStates = newNumStates

	// Update DebugInfo if necessary, but we can just append a note.
	t.DebugInfo = append(t.DebugInfo, fmt.Sprintf("Compressed states from %d to %d", numStates, newNumStates))
}
