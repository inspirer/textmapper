/**
 * Copyright 2002-2019 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * LR(0) states generator
 */
class LR0 extends ContextFree {

	protected static final int BITS = 32;
	private static final int STATE_TABLE_SIZE = 1037;

	// LR0 engine internals
	private int nvars;
	private int varset, ruleset;
	private int[] ruleforvar /* nvars: set of rules (closure) */;

	private int[] toreduce, closure /* [items] */;
	private int closureend /* size of closure */;
	private int[][] symbase /* nsyms: array of size symbasesize[i] = items after sym shift */;
	private int[] symbasesize;
	private int[] symcanshift /* list of symbols to shift [nsyms] */;
	private int[] closurebit /* list of rules, added to closure [ruleset] */;
	private State[] table;
	private State current, last;
	private State[] next_to_final;
	private Map<String, Set<Integer>> markerStates = new LinkedHashMap<>();

	// result
	protected int nstates, termset;
	protected int[][] derives /* nvars: list of rules */;   // !! note: derives -= nterms;
	protected State[] state;
	protected State first;
	protected int[] final_states;
	protected Marker[] markers;

	protected LR0(Grammar g, ProcessingStatus status) {
		super(g, status);
	}

	protected boolean buildLR0() {
		allocate_data();
		build_derives();
		build_sets();
		initializeLR0();

		while (current != null) {
			build_closure(current, current.elems);
			if (!process_state()) {
				status.report(ProcessingStatus.KIND_FATAL, "syntax analyzer is too big ...");
				freeLR0();
				return false;
			}
			current = current.next;
		}

		add_final_states();
		show_debug();

		markers = new Marker[markerStates.size()];
		int i = 0;
		for (Entry<String, Set<Integer>> e : markerStates.entrySet()) {
			markers[i++] = new MarkerImpl(e.getKey(), e.getValue());
		}
		freeLR0();
		return true;
	}

	private void allocate_data() {
		nvars = nsyms - nterms;
		ruleset = (((rules) + BITS - 1) / BITS);
		varset = (((nvars) + BITS - 1) / BITS);
		termset = (((nterms) + BITS - 1) / BITS);

		toreduce = new int[rules + 1];
		closure = new int[items];
		closurebit = new int[ruleset];

		table = new State[STATE_TABLE_SIZE];
		Arrays.fill(table, null);

		next_to_final = new State[inputs.length];

		// state transition temporary data
		int[] symnum = new int[nsyms];
		Arrays.fill(symnum, 0);

		int i;

		for (i = 0; i < items; i++) {
			if (rright[i] >= 0) {
				symnum[rright[i]]++;
			}
		}

		symbase = new int[nsyms][];
		symbasesize = new int[nsyms];

		for (i = 0; i < nsyms; i++) {
			symbase[i] = new int[symnum[i]];
		}
		symcanshift = symnum;
	}

	private void build_derives() {
		int i, e;
		int[] q = new int[rules];
		int[] m = new int[nvars];
		int[] count = new int[nvars];

		Arrays.fill(m, -1);
		Arrays.fill(count, 0);

		for (i = rules - 1; i >= 0; i--) {
			e = rleft[i] - nterms;
			q[i] = m[e];
			m[e] = i;
			count[e]++;
		}

		derives = new int[nvars][];

		for (i = 0; i < nvars; i++) {
			int[] current = new int[count[i]];
			derives[i] = current;
			int c = 0;
			e = m[i];
			for (; e != -1; e = q[e]) {
				current[c++] = e;
			}
			assert c == count[i];
		}
	}

	private void build_sets() {
		int i, e, j;

		// firsts [Non-term -> set of(Non-term)]
		int[] firsts = new int[nvars * varset];
		Arrays.fill(firsts, 0);

		for (i = 0; i < nvars; i++) {
			for (int ruleIndex : derives[i]) {
				e = rright[rindex[ruleIndex]];
				if (e >= nterms) {
					firsts[varset * i + (e - nterms) / BITS] |= (1 << ((e - nterms) % BITS));
				}
			}
		}

		// [j,i] && [i,e] => [j,e]
		for (i = 0; i < nvars; i++) {
			for (j = 0; j < nvars; j++) {
				if (((firsts[varset * j + (i) / BITS] & (1 << ((i) % BITS))) != 0)) {
					for (e = 0; e < nvars; e++) {
						if (((firsts[varset * i + (e) / BITS] & (1 << ((e) % BITS))) != 0)) {
							firsts[varset * j + (e) / BITS] |= (1 << ((e) % BITS));
						}
					}
				}
			}
		}

		// set [i,i]
		for (i = 0; i < nvars; i++) {
			firsts[varset * i + (i) / BITS] |= (1 << ((i) % BITS));
		}

		// ruleforvar
		ruleforvar = new int[nvars * ruleset];
		Arrays.fill(ruleforvar, 0);

		for (i = 0; i < nvars; i++) {
			for (e = 0; e < nvars; e++) {
				if (((firsts[varset * i + (e) / BITS] & (1 << ((e) % BITS))) != 0)) {
					for (int p : derives[e]) {
						ruleforvar[ruleset * i + (p) / BITS] |= (1 << ((p) % BITS));
					}
				}
			}
		}
	}

	private void initializeLR0() {
		for (nstates = 0; nstates < inputs.length; nstates++) {
			if (nstates == 0) {
				first = last = current = new State();
			} else {
				last = last.next = new State();
			}
			last.number = nstates;
			last.nreduce = last.nshifts = last.symbol = last.fromstate = 0;
			last.next = last.link = null;
			last.shifts = last.reduce = null;
			last.elems = new int[]{-1};
		}
	}

	private void build_closure(State state, int[] prev) {
		int e, i;

		if (state.number < inputs.length) {
			int from = (inputs[state.number] - nterms) * ruleset;
			for (i = 0; i < ruleset; i++) {
				closurebit[i] = ruleforvar[from++];
			}

		} else {
			Arrays.fill(closurebit, 0);

			for (i = 0; prev[i] >= 0; i++) {
				e = rright[prev[i]];
				if (e >= nterms) {
					int from = (e - nterms) * ruleset;
					for (int x = 0; x < ruleset; x++) {
						closurebit[x] |= ruleforvar[from++];
					}
				}
			}
		}

		int rule = 0, prev_index = 0;
		closureend = 0;

		for (i = 0; i < ruleset; i++) {
			int rulebit = closurebit[i];
			if (rulebit == 0) {
				rule += BITS;
			} else {
				for (e = 0; e < BITS; e++) {
					if ((rulebit & (1 << e)) != 0) {
						int index = rindex[rule];
						while (prev[prev_index] >= 0 && prev[prev_index] < index) {
							closure[closureend++] = prev[prev_index++];
						}
						closure[closureend++] = index;
					}
					rule++;
				}
			}
		}

		while (prev[prev_index] >= 0) {
			closure[closureend++] = prev[prev_index++];
		}
	}

	private State new_state(int from, int by, int hash, int size, int inputsign) {
		last = last.next = new State();
		last.elems = new int[size + 1];
		last.link = table[hash % STATE_TABLE_SIZE];
		table[hash % STATE_TABLE_SIZE] = last;
		last.fromstate = from;
		last.symbol = by;
		last.number = nstates++;
		last.nshifts = last.nreduce = 0;
		last.next = null;
		last.reduce = last.shifts = null;
		last.LR0 = true;
		last.elems[size] = -1;
		last.inputsign = inputsign;
		if (inputsign >= 0) {
			next_to_final[inputsign] = last;
		}
		return last;
	}

	private int goto_state(int symbol) {
		int[] new_core = symbase[symbol];
		int size = symbasesize[symbol];
		int i, hash;

		for (hash = i = 0; i < size; i++) {
			hash = 31 * hash + new_core[i];
		}
		hash = Math.abs(hash);
		State t = table[hash % STATE_TABLE_SIZE];
		int inputsign = current.number < inputs.length && inputs[current.number] == symbol ? current.number : -1;

		while (t != null) {
			for (i = 0; i < size; i++) {
				if (new_core[i] != t.elems[i]) {
					break;
				}
			}

			if (i == size && t.elems[size] == -1 && inputsign == t.inputsign) {
				break;
			}

			t = t.link;
		}

		if (t == null) {
			t = new_state(current.number, symbol, hash, size, inputsign);
			System.arraycopy(new_core, 0, t.elems, 0, size);
			t.elems[size] = -1;
		}

		return t.number;
	}

	private boolean process_state() {
		int i, ntoreduce = 0;
		Arrays.fill(symbasesize, 0);

		for (i = 0; i < closureend; i++) {
			int sym = rright[closure[i]];
			if (sym >= 0) {
				int e = symbasesize[sym];
				symbase[sym][e++] = closure[i] + 1;
				symbasesize[sym] = e;

			} else {
				toreduce[ntoreduce++] = -1 - sym;
			}

			Set<String> markers = itemMarkers.get(closure[i]);
			if (markers == null) continue;

			for (String s : markers) {
				Set<Integer> set = markerStates.get(s);
				if (set == null) {
					markerStates.put(s, set = new HashSet<>());
				}
				set.add(current.number);
			}
		}

		int ntoshift = 0;
		for (i = 0; i < nsyms; i++) {
			if (symbasesize[i] != 0) {
				symcanshift[ntoshift++] = i;
			}
		}

		current.nshifts = ntoshift;
		current.shifts = (ntoshift != 0) ? new int[ntoshift] : null;

		current.nreduce = ntoreduce;
		current.reduce = (ntoreduce != 0) ? new int[ntoreduce] : null;

		for (i = 0; i < ntoshift; i++) {
			current.shifts[i] = goto_state(symcanshift[i]);
		}

		for (i = 0; i < ntoreduce; i++) {
			current.reduce[i] = toreduce[i];
		}

		current.LR0 = !(ntoreduce > 1 || (ntoshift != 0 && ntoreduce != 0));
		return true;
	}

	private void insert_shift(State t, int tostate) {
		if (t.shifts != null) {
			final int symbol = state[tostate].symbol;
			int[] old = t.shifts;
			int i, e, n = t.nshifts;

			t.shifts = new int[n + 1];
			e = 0;
			for (i = 0; i < n && state[old[i]].symbol < symbol; i++) {
				t.shifts[e++] = old[i];
			}
			t.shifts[e++] = tostate;
			assert i == n || state[old[i]].symbol != symbol : "internal error: cannot insert shift";
			for (; i < n; i++) {
				t.shifts[e++] = old[i];
			}
			t.nshifts++;

		} else {
			t.nshifts = 1;
			t.shifts = new int[1];
			t.shifts[0] = tostate;

			if (t.nreduce > 0) {
				t.LR0 = false;
			}
		}
	}

	private void add_final_states() {
		boolean[] created = new boolean[inputs.length];
		final_states = new int[inputs.length];

		// search next_to_final
		Arrays.fill(created, false);

		// if not found then create
		for (int i = 0; i < inputs.length; i++) {
			if (next_to_final[i] == null) {
				next_to_final[i] = new_state(i, inputs[i], 0, 0, -1);
				created[i] = true;
			}
		}

		for (int i = 0; i < inputs.length; i++) {
			if (noEoiInput[i]) {
				final_states[i] = next_to_final[i].number;
			} else {
				final_states[i] = new_state(next_to_final[i].number, eoi, 0, 0, -1).number;
			}
		}

		// create state array
		state = new State[nstates];
		for (State t = first; t != null; t = t.next) {
			state[t.number] = t;
		}

		// insert shifts
		for (int i = 0; i < inputs.length; i++) {
			if (created[i]) {
				insert_shift(state[i], next_to_final[i].number);
			}
			if (!noEoiInput[i]) {
				insert_shift(next_to_final[i], final_states[i]);
			}
		}
	}

	private void show_debug() {
		if (!status.isAnalysisMode()) {
			return;
		}

		status.debug("\nStates\n0:\n");

		for (State t = first; t != null; t = t.next) {
			if (t != first) {
				status.debug("\n" + t.number + ": (from " + t.fromstate + ", " + sym[t.symbol].getNameText() + ")\n");
			}

			build_closure(t, t.elems);

			List<String> items = new ArrayList<>();
			for (int i = 0; i < closureend; i++) {
				items.add(debugText(closure[i]));
			}
			Collections.sort(items);
			for (String s : items) {
				status.debug("  ");
				status.debug(s);
				status.debug("\n");
			}

			status.debug("  (Transitions): ");
			for (int i = t.nshifts - 1; i >= 0; i--) {
				int newstate = t.shifts[i];
				int symnum = state[newstate].symbol;
				status.debug(sym[symnum].getNameText() + " -> " + newstate);
				status.debug(i > 0 ? ", " : "\n");
			}
		}
	}

	protected final Symbol[] getInput(int s) {
		Stack<Symbol> stack = new Stack<>();
		while (state[s].number != 0) {
			stack.push(sym[state[s].symbol]);
			s = state[s].fromstate;
		}
		Symbol[] result = new Symbol[stack.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = stack.pop();
		}
		return result;
	}

	private void freeLR0() {
		ruleforvar = null;
		markerStates = null;
		toreduce = closure = null;
		symbase = null;
		symbasesize = null;
		symcanshift = null;
		closurebit = null;
		table = null;
		current = last = null;
	}

	protected static class State {
		int fromstate, symbol, number, nshifts, nreduce;
		int inputsign;
		State link, next;
		int[] shifts;
		int[] reduce;
		boolean LR0;
		int[] elems;
	}
}
