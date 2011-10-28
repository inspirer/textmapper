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
	private int[] nla_vars /* set of vars with nla in rules */;

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
	private int[] var_nla;		/* nvars -> -2 = not processed; -1 = no la restrictions; index in nla otherwise */
	private int[] var_used;	 /* set of vars */
	private int[] var_templist;

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

		// rebuild ruleforvar if lookahead is required
		if (nla_rules == null) return;
		nla_vars = new int[varset];
		Arrays.fill(nla_vars, 0);

		var_nla = new int[nvars];
		var_used = new int[varset];
		var_templist = new int[nvars];
		for (i = 0; i < nvars; i++) {
			boolean hasNLA = false;
			for (int rule : nla_rules) {
				assert sit_nla[rindex[rule]] >= 0;
				if ((ruleforvar[ruleset * i + rule / BITS] & (1 << (rule % BITS))) != 0) {
					hasNLA = true;
					break;
				}
			}
			if (!hasNLA) continue;
			nla_vars[i / BITS] |= 1 << (i % BITS);

			Arrays.fill(ruleforvar, ruleset * i, ruleset * (i + 1), 0);
			nla_closure(i, -1);
			nla_apply(ruleforvar, ruleset * i);
		}
	}

	/**
	 * Converts restrictions (var_nla & var_used) to set of rules. Adds the result into array at offset.
	 *
	 * @param array			array, containing set of rules ([ruleset])
	 * @param startIndex	offset in the array where the set starts
	 */
	public void nla_apply(int[] array, int startIndex) {
		for (int var = 0; var < nvars; var++) {
			int inherited_nla = var_nla[var];
			if (inherited_nla == -2) {
				continue;
			}
			for (int ruleIndex : derives[var]) {
				int e = rright[rindex[ruleIndex]];
				boolean add;
				if (e < 0) {
					add = true;
				} else if (e < nterms) {
					int composite_nla = nla == null ? -1 : nla.mergeSets(sit_nla[rindex[ruleIndex]], inherited_nla);
					add = composite_nla == -1 || !nla.contains(composite_nla, e);
				} else {
					add = (var_used[(e - nterms) / BITS] & (1 << ((e - nterms) % BITS))) != 0;
				}
				if (add) {
					array[startIndex + ruleIndex / BITS] |= (1 << (ruleIndex % BITS));
				}
			}
		}
	}

	/**
	 * Build looakahead restrictions (var_nla) and derived vars set (var_used).
	 *
	 * @param startVar	  start non-terminal: 0..nvars-1
	 * @param inherited_nla index in nla; inherited negative lookahead
	 */
	public void nla_closure(int startVar, int inherited_nla) {
		Arrays.fill(var_nla, -2);
		Arrays.fill(var_used, 0);
		nla_prepare(startVar, inherited_nla);

		int head = -1;

		for (int var = 0; var < nvars; var++) {
			if (var_nla[var] > -2) {
				var_templist[var] = head;
				head = var;
			}
		}

		while (true) {
			boolean oneMore = false;
			for (int var = head; var != -1; var = var_templist[var]) {
				assert var_nla[var] > -2;
				if ((var_used[var / BITS] & (1 << (var % BITS))) != 0) {
					continue;
				}
				inherited_nla = var_nla[var];

				boolean isUsed = false;
				for (int ruleIndex : derives[var]) {
					int e = rright[rindex[ruleIndex]];
					if (e < 0) {
						isUsed = true;
						break;
					}
					if (e < nterms) {
						int composite_nla = nla == null ? -1 : nla.mergeSets(sit_nla[rindex[ruleIndex]], inherited_nla);
						if (composite_nla == -1 || !nla.contains(composite_nla, e)) {
							isUsed = true;
							break;
						}
					} else {
						int firstVar = e - nterms;
						if ((var_used[firstVar / BITS] & (1 << (firstVar % BITS))) != 0) {
							isUsed = true;
							break;
						}
					}
				}
				if (isUsed) {
					oneMore = true;
					var_used[var / BITS] |= (1 << (var % BITS));
				}
			}
			if (!oneMore) {
				break;
			}
		}
	}

	/**
	 * Calculates lookahead restrictions for all derived non-terminals (in var_nla).
	 * Pre-condition: var_nla should be filled with -2.
	 *
	 * @param startVar	  start non-terminal: 0..nvars-1
	 * @param inherited_nla index in nla; inherited negative lookahead
	 */
	private void nla_prepare(int startVar, int inherited_nla) {
		int existing = var_nla[startVar];
		if (existing > -2 && nla.isSubset(existing, inherited_nla)) {
			return;
		}
		var_nla[startVar] = existing == -2 ? inherited_nla : nla.intersectSet(existing, inherited_nla);

		for (int ruleIndex : derives[startVar]) {
			int e = rright[rindex[ruleIndex]];
			if (e >= nterms) {
				int composite_nla = nla.mergeSets(sit_nla[rindex[ruleIndex]], inherited_nla);
				nla_prepare(e - nterms, composite_nla);
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

	private void build_closure(State state, short[] prev) {
		int e, i;
		boolean need_closure = false;

		if (state.number < inputs.length) {
			int inputVar = inputs[state.number] - nterms;
			int from = inputVar * ruleset;
			if (nla_vars != null && (nla_vars[inputVar / BITS] & (1 << (inputVar % BITS))) != 0) {
				need_closure = true;
			}
			for (i = 0; i < ruleset; i++) {
				closurebit[i] = ruleforvar[from++];
			}

		} else {
			Arrays.fill(closurebit, 0);

			for (i = 0; prev[i] >= 0; i++) {
				e = rright[prev[i]];
				if (e >= nterms) {
					if (sit_nla == null || sit_nla[prev[i]] == -1) {
						int from = (e - nterms) * ruleset;
						if (nla_vars != null && (nla_vars[(e - nterms) / BITS] & (1 << ((e - nterms) % BITS))) != 0) {
							need_closure = true;
						}
						for (int x = 0; x < ruleset; x++) {
							closurebit[x] |= ruleforvar[from++];
						}
					} else {
						nla_closure(e - nterms, sit_nla[prev[i]]);
						nla_apply(closurebit, 0);
						need_closure = true;
					}
				}
			}
		}

		if (need_closure) {
			state.closure = Arrays.copyOf(closurebit, ruleset);
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
		last.softConflicts = false;
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
					current.softConflicts = true;
					conflict = softconflicts.addConflict(current.number);
					conflict.addSymbol(sym[classTerm]);
					core = symbase[classTerm];
					for (int i = 0; i < symbasesize[classTerm]; i++) {
						conflict.addRule(wrules[ruleIndex(core[i])]);
					}
				}
				conflict.addSymbol(sym[soft]);
				core = symbase[soft];
				for (int i = 0; i < symbasesize[soft]; i++) {
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

			build_closure(t, t.elems);

			for (int i = 0; i < closureend; i++) {
				print_situation(closure[i]);
			}
		}

		if(var_nla == null) return;
		status.debug("\nRules for var:\n\n");

		for (int var = 0; var < nvars; var++) {
			status.debug(sym[nterms + var].getName() + " ::\n");
			int[] a = new int[ruleset];
			nla_closure(var, -1);
			nla_apply(a, 0);
			if (nla_vars != null && (nla_vars[(var) / BITS] & (1 << ((var) % BITS))) != 0) {
				status.debug("\tconstraints =");
				for (int derivedVar = 0; derivedVar < nvars; derivedVar++) {
					if ((var_used[derivedVar / BITS] & (1 << (derivedVar % BITS))) != 0) {
						status.debug(" ");
						if (var_nla[derivedVar] >= 0) {
							status.debug("(?!");
							for (int i : nla.sets[var_nla[derivedVar]]) {
								if (i != nla.sets[var_nla[derivedVar]][0]) {
									status.debug(", ");
								}
								status.debug(sym[i].getName());
							}
							status.debug(")");
						}
						status.debug(sym[derivedVar + nterms].getName());
					}
				}
				status.debug("\n");
			}
			for (int ruleIndex = 0; ruleIndex < rules; ruleIndex++) {
				if ((a[ruleIndex / BITS] & (1 << (ruleIndex % BITS))) != 0) {
					status.debug("\t");
					status.debug(sym[rleft[ruleIndex]].getName() + " ::=");
					for (int i = rindex[ruleIndex]; rright[i] >= 0; i++) {
						status.debug(" " + sym[rright[i]].getName());
					}
					status.debug("\n");
				}
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
		boolean softConflicts;
		int[] closure; /* ruleset: outgoing rules */
	}
}
