package lalr

import (
	"testing"

	"github.com/inspirer/textmapper/util/container"
)

func TestLALRk(t *testing.T) {
	tests := []struct {
		name      string
		input     string
		lookahead int
		wantRR    int // expected reduce/reduce conflicts
		wantSR    int // expected shift/reduce conflicts
		wantLA    int // expected lookahead depth
	}{
		{
			// LALR(2) grammar: S -> Aab | Bac; A -> e; B -> e
			// After 'e', reduce/reduce on terminal 'a': follow(A)=follow(B)={a}.
			// But 2nd token after 'a' is 'b' (for A) vs 'c' (for B).
			name:      "lalr2_conflict_at_1",
			input:     `S -> Aab; S -> Bac; A -> e; B -> e`,
			lookahead: 1,
			wantRR:    1, // reduce/reduce conflict at LALR(1)
		},
		{
			name:      "lalr2_resolved_at_2",
			input:     `S -> Aab; S -> Bac; A -> e; B -> e`,
			lookahead: 2,
			wantRR:    0, // resolved with 2-token lookahead
			wantLA:    2,
		},
		{
			// LALR(1) grammar — no conflicts.
			name:      "already_lalr1",
			input:     `S -> Ca; S -> Db; C -> e; D -> e`,
			lookahead: 1,
			wantRR:    0,
		},
		{
			// LALR(1) grammar with no conflicts.
			name:      "no_conflict",
			input:     `S -> A; S -> B; A -> ab; B -> ac`,
			lookahead: 1,
			wantRR:    0,
		},
		{
			// Grammar with reduce/reduce that cannot be resolved by any finite lookahead.
			// S -> A; S -> B; A -> a; B -> a  (identical RHS, same follow sets forever)
			name:      "unresolvable",
			input:     `S -> A; S -> B; A -> a; B -> a`,
			lookahead: 3,
			wantRR:    1,
		},
		{
			// The classic non-LALR(k) grammar that requires stack context:
			// S -> aEa | bEb | aFb | bFa; E -> e; F -> e
			// This is LR(1) but NOT LALR(k) for any k, because the state merging
			// loses the prefix context needed to distinguish E from F.
			name:      "not_lalrk",
			input:     `S -> aEa; S -> bEb; S -> aFb; S -> bFa; E -> e; F -> e`,
			lookahead: 3,
			wantRR:    2,
		},
		{
			// LALR(3) grammar: S -> Aabc | Babd; A -> e; B -> e
			// After 'e', reduce/reduce on 'a'. 2nd token is 'b' for both.
			// 3rd token is 'c' (A) vs 'd' (B).
			name:      "lalr3_conflict_at_1",
			input:     `S -> Aabc; S -> Babd; A -> e; B -> e`,
			lookahead: 1,
			wantRR:    1,
		},
		{
			name:      "lalr3_not_resolved_at_2",
			input:     `S -> Aabc; S -> Babd; A -> e; B -> e`,
			lookahead: 2,
			wantRR:    1, // still ambiguous at depth 2
		},
		{
			name:      "lalr3_resolved_at_3",
			input:     `S -> Aabc; S -> Babd; A -> e; B -> e`,
			lookahead: 3,
			wantRR:    0, // resolved at depth 3
			wantLA:    3,
		},
		{
			name:      "lalr8_resolved_at_3",
			input:     `S -> Aabc; S -> Babd; A -> e; B -> e`,
			lookahead: 8,
			wantRR:    0, // resolved at depth 3
			wantLA:    3,
		},
		{
			// LALR(2) with lookahead=1 should fail, lookahead=2 should succeed.
			// No conflict when lookahead is sufficient.
			name:      "lalr2_no_conflict_at_2",
			input:     `S -> Aab; S -> Bac; A -> e; B -> e`,
			lookahead: 2,
			wantRR:    0,
			wantLA:    2,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			g, err := parseGrammar(tc.input)
			if err != nil {
				t.Fatalf("parseGrammar(%v) failed with %v", tc.input, err)
			}

			tables, err := Compile(g, Options{
				Lookahead: tc.lookahead,
			})
			if tables.RR != tc.wantRR {
				t.Errorf("reduce/reduce conflicts: got %d, want %d\nerrors: %v", tables.RR, tc.wantRR, err)
			}
			if tables.SR != tc.wantSR {
				t.Errorf("shift/reduce conflicts: got %d, want %d\nerrors: %v", tables.SR, tc.wantSR, err)
			}
			if tables.UsedLADepth != tc.wantLA {
				t.Errorf("lookahead depth: got %d, want %d", tables.UsedLADepth, tc.wantLA)
			}
		})
	}
}

func TestLAAutomaton(t *testing.T) {
	// Test the automaton builder directly with an LALR(2) grammar.
	// S -> Aab | Bac; A -> e; B -> e
	// After 'e', reduce/reduce on 'a'. The 2nd token ('b' vs 'c') resolves it.
	g, err := parseGrammar(`S -> Aab; S -> Bac; A -> e; B -> e`)
	if err != nil {
		t.Fatalf("parseGrammar failed: %v", err)
	}

	c := &compiler{
		grammar:   g,
		lookahead: 2,
		out: &Tables{
			DefaultEnc: &DefaultEnc{},
		},
		empty: container.NewBitSet(len(g.Symbols)),
	}
	c.init()
	c.computeEmpty()
	c.computeSets()
	c.computeStates()
	c.initLalr()
	c.buildLA(true /*useTransitions*/, false /*stats*/)

	if c.follow == nil {
		t.Fatal("expected follow to be stored on compiler when useTransitions=true")
	}
	if !c.useTransitions {
		t.Fatal("expected useTransitions=true")
	}

	c.populateTables()
	c.resolveWithLookahead()

	t.Logf("After resolve: pending=%d, sr=%d, rr=%d, UsedLADepth=%d", len(c.pending), c.sr, c.rr, c.out.UsedLADepth)

	if c.rr > 0 {
		t.Errorf("expected reduce/reduce conflicts to be resolved by LALR(2), but got %d", c.rr)
	}

	// Verify the Lalr array contains a deep reference (automaton pointer).
	hasDeepRef := false
	for i := 1; i < len(c.out.Lalr); i += 2 {
		if c.out.Lalr[i] < -2 {
			hasDeepRef = true
			// Verify the pointer is valid: it should point within the Lalr array.
			offset := -3 - c.out.Lalr[i]
			if offset < 0 || offset >= len(c.out.Lalr) {
				t.Errorf("Lalr[%d] = %d points to invalid offset %d (len=%d)", i, c.out.Lalr[i], offset, len(c.out.Lalr))
			}
		}
	}
	if !hasDeepRef {
		t.Error("expected Lalr array to contain automaton references (values < -2)")
	}

	// Simulate the automaton: for the conflict state, look up action for terminal 'a',
	// then follow the automaton with 'b' and 'c' to verify correct resolution.
	conflictState := 1 // state after 'e'
	action := c.out.Action[conflictState]
	if action >= -2 {
		t.Fatalf("state[%d].action = %d, want: it requires LA", conflictState, action)
	}

	// Find the 'a' terminal index.
	isTerm := func(sym int, name string) bool {
		if sym >= 0 && sym < g.Terminals {
			return g.Symbols[sym] == name
		}
		return false
	}

	// Look up 'a' in the first-level Lalr entries.
	base := -3 - action
	var aAction int
	for i := base; c.out.Lalr[i] >= 0; i += 2 {
		if isTerm(c.out.Lalr[i], "a") {
			aAction = c.out.Lalr[i+1]
			break
		}
	}
	if aAction >= -2 {
		t.Fatalf("expected 'a' action to be an automaton reference, got %d", aAction)
	}

	// Follow the automaton: look up 'b' and 'c' in the second level.
	base2 := -3 - aAction
	var bAction, cAction int
	bAction = -2
	cAction = -2
	for i := base2; c.out.Lalr[i] >= 0; i += 2 {
		if isTerm(c.out.Lalr[i], "b") {
			bAction = c.out.Lalr[i+1]
		}
		if isTerm(c.out.Lalr[i], "c") {
			cAction = c.out.Lalr[i+1]
		}
	}

	// Rule 2 is A -> e (A's rule). Rule 3 is B -> e (B's rule).
	// A is used in S -> Aab, so after 'a' then 'b', we should reduce A.
	// B is used in S -> Bac, so after 'a' then 'c', we should reduce B.
	if bAction != 2 {
		t.Errorf("expected 'b' at level 2 to resolve to rule 2 (A->e), got %d", bAction)
	}
	if cAction != 3 {
		t.Errorf("expected 'c' at level 2 to resolve to rule 3 (B->e), got %d", cAction)
	}
}
