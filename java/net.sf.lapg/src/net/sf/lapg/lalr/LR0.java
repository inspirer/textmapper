/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package net.sf.lapg.lalr;

import java.util.Arrays;

import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.ProcessingStatus;

/**
 *  LR(0) states generator
 */
class LR0 extends ContextFree {

	protected static final int BITS = 32;
	protected static final int MAX_WORD = 0x7ff0;
	private static final int STATE_TABLE_SIZE = 1037;

	// LR0 engine internals
	private int nvars;
	private int varset, ruleset;
	private int[] ruleforvar /* nvars x ruleset */;

	private short[] toreduce, closure /* [situations] */;
	private int closureend /* size of closure */;
	private short[][] symbase /* [nsyms][] */;
	private int[] symbasesize;
	private short[] symcanshift;
	private int[]   closurebit /* ruleset */;
	private int 	ntoreduce, ntoshift;
	private State[] table;
	private State current, last;

	// result
	protected int nstates, termset;
	protected int[][] derives /* nvars: list of rules */;   // !! note: derives -= nterms;
	protected State[] state;
	protected State first;

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
				status.error("syntax analyzer is too big ...\n");
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
		int[] p;

		// firsts
		int[] firsts = new int[nvars * varset];
		Arrays.fill(firsts, 0);

		for (i = 0; i < nvars; i++) {
			p = derives[i];
			for (int element : p) {
				e = rright[rindex[element]];
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
					p = derives[e];
					for (int q = 0; q < p.length; q++) {
						ruleforvar[ruleset * i + (p[q]) / BITS] |= (1 << ((p[q]) % BITS));
					}
				}
			}
		}
	}

	private void initializeLR0() {
		for(nstates = 0; nstates < inputs.length; nstates++) {
			if(nstates == 0) {
				first = last = current = new State();
			} else {
				last = last.next = new State();
			}
			last.number = nstates;
			last.nreduce = last.nshifts = last.symbol = last.fromstate = 0;
			last.next = last.link = null;
			last.shifts = last.reduce = null;
			last.elems = new short[] { -1 };
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

	private State new_state(int from, int by, int hash, int size) {
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

		while (t != null) {
			for (i = 0; i < size; i++) {
				if (new_core[i] != t.elems[i]) {
					break;
				}
			}

			if (i == size) {
				break;
			}

			t = t.link;
		}

		if (t == null) {
			t = new_state(current.number, symbol, hash, size);
			for (i = 0; i < size; i++) {
				t.elems[i] = new_core[i];
			}
			t.elems[size] = -1;
		}

		return t.number;
	}

	private boolean process_state() {
		int i, e, sym;

		ntoreduce = 0;
		Arrays.fill(symbasesize, (short) 0);

		for (i = 0; i < closureend; i++) {
			sym = rright[closure[i]];
			if (sym >= 0) {
				e = symbasesize[sym];
				symbase[sym][e++] = (short) (closure[i] + 1);
				symbasesize[sym] = e;

			} else {
				toreduce[ntoreduce++] = (short) (-1 - rright[closure[i]]);
			}
		}

		ntoshift = 0;
		for (i = 0; i < nsyms; i++) {
			if (symbasesize[i] != 0) {
				symcanshift[ntoshift++] = (short) i;
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
			for (; i < n; i++) {
				t.shifts[e++] = old[i];
			}
			t.nshifts++;

		} else {
			t.nshifts = 1;
			t.shifts = new short[1];
			t.shifts[0] = (short) tostate;
		}
	}

	private void add_final_states() {
		State[] next_to_final = new State[inputs.length];
		boolean[] created = new boolean[inputs.length];
		State[] final_state = new State[inputs.length];

		// search next_to_final
		Arrays.fill(created, false);
		for(State t = first; t != null; t = t.next) {
			if(t.number >= inputs.length && t.fromstate < inputs.length && t.symbol == inputs[t.fromstate]) {
				next_to_final[t.fromstate] = t;
			}
		}

		// if not found then create
		for(int i = 0; i < inputs.length; i++) {
			if (next_to_final[i] == null) {
				next_to_final[i] = new_state(i, inputs[i], 0, 0);
				created[i] = true;
			}
		}

		for(int i = 0; i < inputs.length; i++) {
			final_state[i] = new_state(next_to_final[i].number, eoi, 0, 0);
		}

		// create state array
		state = new State[nstates];
		for (State t = first; t != null; t = t.next) {
			state[t.number] = t;
		}

		// insert shifts
		for(int i = 0; i < inputs.length; i++) {
			if (created[i]) {
				insert_shift(state[i], next_to_final[i].number);
			}
			insert_shift(next_to_final[i], final_state[i].number);
		}
	}

	private void show_debug() {
		if (!status.isAnalysisMode()) {
			return;
		}

		status.debug( "\nStates\n0:\n");

		for (State t = first; t != null; t = t.next) {
			if (t != first) {
				status.debug( "\n" + t.number + ": (from " + t.fromstate + ", " + sym[t.symbol].getName() + ")\n");
			}

			build_closure(t.number, t.elems);

			for (int i = 0; i < closureend; i++) {
				print_situation(closure[i]);
			}
		}
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
	}

	protected class State {
		int  fromstate, symbol, number, nshifts, nreduce;
		State link, next;
		short[] shifts, reduce;
		boolean LR0;
		short[] elems;
	};
}
