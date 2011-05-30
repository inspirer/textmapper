/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.lalr;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ParserConflict.Input;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.lalr.LalrConflict.InputImpl;
import org.textway.lapg.lalr.SoftConflictBuilder.SoftClassConflict;

import java.util.Arrays;
import java.util.Stack;

/**
 * LR(0) states generator
 */
class LR0 extends ContextFree {

	protected static final int BITS = 32;
	protected static final int MAX_WORD = 0x7ff0;
	private static final int STATE_TABLE_SIZE = 1037;

	// LR0 engine internals
	private int nvars;
	private int varset, ruleset;
	private int[] ruleforvar /* nvars: set of rules (closure) */;

	private short[] toreduce, closure /* [situations] */;
	private int closureend /* size of closure */;
	private short[][] symbase /* nsyms: array of size symbasesize[i] = situations after sym shift */;
	private int[] symbasesize;
	private short[] symcanshift /* list of symbols to shift [nsyms] */;
	private int[] closurebit /* list of rules, added to closure [ruleset] */;
	private State[] table;
	private State current, last;
	private State[] next_to_final;
	private SoftConflictBuilder softconflicts;

	// result
	protected int nstates, termset;
	protected int[][] derives /* nvars: list of rules */;   // !! note: derives -= nterms;
	protected State[] state;
	protected State first;
	protected int[] final_states;

	protected LR0(Grammar g, ProcessingStatus status) {
		super(g, status);
	}

	protected boolean buildLR0() {
		allocate_data();
		build_derives();
		build_sets();
		initializeLR0();

		while (current != null) {
			build_closure(current.number, current.elems);
			if (!process_state()) {
				status.report(ProcessingStatus.KIND_FATAL, "syntax analyzer is too big ...");
				freeLR0();
				return false;
			}
			current = current.next;
		}

		add_final_states();
		show_debug();
		freeLR0();
		return true;
	}

	private void allocate_data() {
		nvars = nsyms - nterms;
		ruleset = (((rules) + BITS - 1) / BITS);
		varset = (((nvars) + BITS - 1) / BITS);
		termset = (((nterms) + BITS - 1) / BITS);

		toreduce = new short[rules + 1];
		closure = new short[situations];
		closurebit = new int[ruleset];

		table = new State[STATE_TABLE_SIZE];
		Arrays.fill(table, null);

		next_to_final = new State[inputs.length];

		// state transition temporary data
		short[] symnum = new short[nsyms];
		Arrays.fill(symnum, (short) 0);

		int i;

		for (i = 0; i < situations; i++) {
			if (rright[i] >= 0) {
				symnum[rright[i]]++;
			}
		}

		symbase = new short[nsyms][];
		symbasesize = new int[nsyms];

		for (i = 0; i < nsyms; i++) {
			symbase[i] = new short[symnum[i]];
		}
		symcanshift = symnum;

		softconflicts = new SoftConflictBuilder();
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
			last.elems = new short[]{-1};
		}
	}

	private void build_closure(int state, short[] prev) {
		int e, i;

		if (state < inputs.length) {
			int from = (inputs[state] - nterms) * ruleset;
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
						closure[closureend++] = (short) index;
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
		last.elems = new short[size + 1];
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
		short[] new_core = symbase[symbol];
		int size = symbasesize[symbol];
		int i, hash;
		State t;

		for (hash = i = 0; i < size; i++) {
			hash += new_core[i];
		}
		t = table[hash % STATE_TABLE_SIZE];
		int inputsign = current.number < inputs.length && inputs[current.number] == symbol ? current.number : -1;

		while (t != null) {
			for (i = 0; i < size; i++) {
				if (new_core[i] != t.elems[i]) {
					break;
				}
			}

			if (i == size && inputsign == t.inputsign) {
				break;
			}

			t = t.link;
		}

		if (t == null) {
			t = new_state(current.number, symbol, hash, size, inputsign);
			for (i = 0; i < size; i++) {
				t.elems[i] = new_core[i];
			}
			t.elems[size] = -1;
		}

		return t.number;
	}

	private boolean process_state() {
		int i, ntoreduce = 0;
		Arrays.fill(symbasesize, (short) 0);

		for (i = 0; i < closureend; i++) {
			int sym = rright[closure[i]];
			if (sym >= 0) {
				int e = symbasesize[sym];
				symbase[sym][e++] = (short) (closure[i] + 1);
				symbasesize[sym] = e;

			} else {
				toreduce[ntoreduce++] = (short) (-1 - sym);
			}
		}

		int ntoshift = 0;
		for (i = 0; i < nsyms; i++) {
			if (symbasesize[i] != 0) {
				symcanshift[ntoshift++] = (short) i;

				if (i < nterms && classterm[i] == -1) {
					checkSoftTerms(i);
				}
			}
		}

		current.nshifts = ntoshift;
		current.shifts = (ntoshift != 0) ? new short[ntoshift] : null;

		current.nreduce = ntoreduce;
		current.reduce = (ntoreduce != 0) ? new short[ntoreduce] : null;

		for (i = 0; i < ntoshift; i++) {
			current.shifts[i] = (short) goto_state(symcanshift[i]);
			if (current.shifts[i] >= MAX_WORD) {
				return false;
			}
		}

		for (i = 0; i < ntoreduce; i++) {
			current.reduce[i] = toreduce[i];
		}

		current.LR0 = !(ntoreduce > 1 || (ntoshift != 0 && ntoreduce != 0));
		return true;
	}

	private void checkSoftTerms(int classTerm) {
		SoftClassConflict conflict = null;

		for (int soft = softterms[classTerm]; soft != -1; soft = softterms[soft]) {
			assert soft < nterms && classterm[soft] == classTerm;
			if (symbasesize[soft] != 0) {
				// soft lexem conflict
				short[] core;
				if (conflict == null) {
					conflict = softconflicts.addConflict(current.number);
					conflict.addSymbol(sym[classTerm]);
					core = symbase[classTerm];
					for(int i = 0; i < symbasesize[classTerm]; i++) {
						conflict.addRule(wrules[ruleIndex(core[i])]);
					}
				}
				conflict.addSymbol(sym[soft]);
				core = symbase[soft];
				for(int i = 0; i < symbasesize[soft]; i++) {
					conflict.addRule(wrules[ruleIndex(core[i])]);
				}
			}
		}
	}

	private void insert_shift(State t, int tostate) {
		if (t.shifts != null) {
			final int symbol = state[tostate].symbol;
			short[] old = t.shifts;
			int i, e, n = t.nshifts;

			t.shifts = new short[n + 1];
			e = 0;
			for (i = 0; i < n && state[old[i]].symbol < symbol; i++) {
				t.shifts[e++] = old[i];
			}
			t.shifts[e++] = (short) tostate;
			assert i == n || state[old[i]].symbol != symbol : "internal error: cannot insert shift";
			for (; i < n; i++) {
				t.shifts[e++] = old[i];
			}
			t.nshifts++;

		} else {
			t.nshifts = 1;
			t.shifts = new short[1];
			t.shifts[0] = (short) tostate;

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

		// report conflicts
		for (SoftClassConflict conflict : softconflicts.getConflicts()) {
			Input input = new InputImpl(conflict.getState(), getInput(conflict.getState()));
			status.report(new LalrConflict(
					ParserConflict.SHIFT_SOFT, "shift soft/class",
					input, conflict.getSymbols(), conflict.getRules()));
		}
	}

	private void show_debug() {
		if (!status.isAnalysisMode()) {
			return;
		}

		status.debug("\nStates\n0:\n");

		for (State t = first; t != null; t = t.next) {
			if (t != first) {
				status.debug("\n" + t.number + ": (from " + t.fromstate + ", " + sym[t.symbol].getName() + ")\n");
			}

			build_closure(t.number, t.elems);

			for (int i = 0; i < closureend; i++) {
				print_situation(closure[i]);
			}
		}
	}

	protected final Symbol[] getInput(int s) {
		Stack<Symbol> stack = new Stack<Symbol>();
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
		toreduce = closure = null;
		symbase = null;
		symbasesize = null;
		symcanshift = null;
		closurebit = null;
		table = null;
		current = last = null;
		softconflicts = null;
	}

	protected static class State {
		int fromstate, symbol, number, nshifts, nreduce;
		int inputsign;
		State link, next;
		short[] shifts, reduce;
		boolean LR0;
		short[] elems;
	}
}
