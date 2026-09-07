package lex

import (
	"testing"
)

func TestAccelerators(t *testing.T) {
	rules := []*Rule{
		rule("eoi", `{eoi}`, 1),
		rule("space", `[ \t\n]`, 2),   // one-character space token, skippable
		rule("id", `[a-zA-Z_]\w*`, 3), // self-loop on identifier characters
		rule("num", `[0-9]+`, 4),      // self-loop on digits
		rule("str", `"[^"\n]*"`, 5),   // self-loop inside the string (including non-ASCII)
		rule("comment", `#[^\n]*`, 6), // self-loop up to a newline
		rule("crlf", `\r\n`, 7),       // '\r' must not be skippable (two-character token)
		rule("lt", `<`, 8),
		rule("lte", `<=`, 9),
	}
	tables, err := Compile(rules, false /*scanBytes*/, false /*allowBacktracking*/)
	if err != nil {
		t.Fatal(err)
	}
	accel := tables.Accelerators(func(action int) bool { return action == 2 })

	if !accel.HasSpaceSkip() || len(accel.SpaceSkip) != 1 {
		t.Fatalf("no space skip: %+v", accel)
	}
	skip := accel.SpaceSkip[0]
	in := func(b byte) bool { return skip[b>>6]&(1<<(b&63)) != 0 }
	for _, b := range []byte{' ', '\t', '\n'} {
		if !in(b) {
			t.Errorf("%q should be skippable", b)
		}
	}
	for _, b := range []byte{'\r', 'a', '0', '"', '#', '<', 0} {
		if in(b) {
			t.Errorf("%q should not be skippable", b)
		}
	}

	if !accel.HasSelfLoops() {
		t.Fatalf("no self loops")
	}
	// Scan through the DFA for each token and check the accelerator of the state we end
	// up in after the first character.
	stateAfter := func(input string) int {
		state := tables.StateMap[0]
		for _, r := range input {
			i := 0
			for i+1 < len(tables.SymbolMap) && tables.SymbolMap[i+1].Start <= r {
				i++
			}
			state = tables.Dfa[state*tables.NumSymbols+int(tables.SymbolMap[i].Target)]
			if state < 0 {
				t.Fatalf("unexpected action while scanning %q", input)
			}
		}
		return state
	}
	set := func(state int) *[256]uint8 {
		idx := accel.SelfLoop[state]
		if idx == 0 {
			t.Fatalf("state %v has no self-loop accelerator", state)
		}
		return &accel.ByteSets[idx-1]
	}
	loops := func(set *[256]uint8, b byte) bool { return set[b] == 0 }

	id := set(stateAfter("a"))
	for _, b := range []byte{'a', 'Z', '_', '5'} {
		if !loops(id, b) {
			t.Errorf("identifier state should loop on %q", b)
		}
	}
	for _, b := range []byte{' ', '"', '#', 0xc3} {
		if loops(id, b) {
			t.Errorf("identifier state should not loop on %q", b)
		}
	}

	str := set(stateAfter(`"`))
	for _, b := range []byte{'a', ' ', '#', 0x80, 0xc3, 0xff} {
		if !loops(str, b) {
			t.Errorf("string state should loop on %#x", b)
		}
	}
	for _, b := range []byte{'"', '\n'} {
		if loops(str, b) {
			t.Errorf("string state should not loop on %q", b)
		}
	}
	if accel.HasNewline[accel.SelfLoop[stateAfter("#")]-1] {
		t.Errorf("comment state must not skip over newlines")
	}
	if accel.HasNewline[accel.SelfLoop[stateAfter("0")]-1] {
		t.Errorf("number state must not skip over newlines")
	}
	if accel.SelfLoop[stateAfter("<")] != 0 {
		t.Errorf("'<' state has fewer than %v loop bytes and should have no accelerator", minSelfLoopBytes)
	}
}
