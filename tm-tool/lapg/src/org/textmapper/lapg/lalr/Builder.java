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

public class Builder extends Lalr1 {

	private Builder(Grammar g, ProcessingStatus status) {
		super(g, status);
	}

	// tables
	private int[] action_index;
	private int nactions;
	private int[] action_table;
	private ExplicitLookaheadBuilder lookaheadBuilder;

	private void verify_grammar() {
		int i, e, h;
		boolean k;

		// TODO use sym[i].isNullable to initialize sym_empty

		// search for symbols which accepts the empty chain
		search_next_empty:
		for (; ; ) {
			for (i = 0; i < rules; i++) {
				if (!sym_empty[rleft[i]]) {

					k = true;
					for (e = rindex[i]; k && (rright[e] >= 0); e++) {
						if (!sym_empty[rright[e]]) {
							k = false;
						}
					}

					if (k) {
						sym_empty[rleft[i]] = true;
						continue search_next_empty;
					}
				}
			}
			break;
		}

		for (i = nterms; i < nsyms; i++) {
			assert sym_empty[i] == ((Nonterminal) sym[i]).isNullable()
					: "old = " + sym_empty[i] + ", new = " + ((Nonterminal) sym[i]).isNullable() +
					" for " + sym[i].getNameText();
		}

		boolean[] sym_good = new boolean[nsyms];

		// terminal and empty symbols are good
		for (i = 0; i < nsyms; i++) {
			if (sym[i].isTerm() || sym_empty[i]) {
				sym_good[i] = true;
			}
		}

		// search for the good symbols
		get_next_good:
		for (; ; ) {
			for (i = 0; i < rules; i++) {
				if (!sym_good[rleft[i]]) {

					k = true;
					for (e = rindex[i]; k && (rright[e] >= 0); e++) {
						if (!sym_good[rright[e]]) {
							k = false;
						}
					}

					if (k) {
						sym_good[rleft[i]] = true;
						continue get_next_good;
					}
				}
			}
			break;
		}

		// search for the employed symbols
		boolean[] sym_employed = new boolean[nsyms];
		boolean[] sym_temp = new boolean[nsyms];
		k = true;
		for (i = 0; i < inputs.length; i++) {
			sym_temp[inputs[i]] = true;
		}
		while (k) {
			k = false;
			for (i = 0; i < nsyms; i++) {
				if (sym_temp[i]) {
					for (h = 0; h < rules; h++) {
						if (rleft[h] == i) {
							for (e = rindex[h]; rright[e] >= 0; e++) {
								if (!sym_temp[rright[e]] && !sym_employed[rright[e]]) {
									if (sym[rright[e]].isTerm()) {
										sym_employed[rright[e]] = true;
									} else {
										k = true;
										sym_temp[rright[e]] = true;
									}
								}
							}
						}
					}

					sym_employed[i] = true;
					sym_temp[i] = false;
				}
			}
		}

		// eoi is very useful token
		sym_good[eoi] = sym_employed[eoi] = true;

		// print out the useless symbols
		for (i = 0; i < nsyms; i++) {
			if (!sym_good[i] || !sym_employed[i]) {
				if (!sym[i].getNameText().startsWith("_skip")) {
					status.report(ProcessingStatus.KIND_WARN, "symbol `" + sym[i].getNameText()
							+ "` is useless", sym[i]);
				}
			}
		}
	}

	// returns 0:unresolved 1:shift 2:reduce 3:error
	private int compare_prio(int rule, int next) {
		int i, cgroup, assoc = -1, rule_group = -1, next_group = -1, nextassoc = -1;

		if (priorul.length == 0) {
			return 0;
		}

		for (cgroup = i = 0; i < priorul.length; i++) {
			if (priorul[i] < 0) {
				assoc = -priorul[i];
				cgroup++;
			} else {
				if (priorul[i] == rprio[rule]) {
					rule_group = cgroup;
				}
				if (priorul[i] == next) {
					next_group = cgroup;
					nextassoc = assoc;
				}
			}
		}

		if (rule_group == -1 || next_group == -1) {
			return 0;
		}
		if (rule_group > next_group) {
			return 2; // reduce
		}
		if (rule_group < next_group) {
			return 1; // shift
		}
		if (nextassoc == 1) {
			return 2; // left => reduce
		}
		if (nextassoc == 2) {
			return 1; // right => shift
		}
		if (nextassoc == 3) {
			return 3; // nonassoc => error
		}
		return 0;
	}

	private void action() {
		List<int[]> actionTables = new ArrayList<>();
		int rr = 0, sr = 0;
		int[] actionset = new int[nterms];
		int[] next = new int[nterms];
		ConflictBuilder conflicts = new ConflictBuilder(nterms);

		action_index = new int[nstates];
		action_table = null;
		nactions = 0;

		for (State t = first; t != null; t = t.next) {
			if (t.LR0) {
				if (t.nshifts > 0) {
					action_index[t.number] = -1;
				} else if (t.nreduce > 0) {
					action_index[t.number] = t.reduce[0];
				} else {
					action_index[t.number] = -2;
				}
			} else {

				// prepare
				int setsize = 0;
				Arrays.fill(next, -2);

				// process shifts
				for (int i = 0; i < t.nshifts; i++) {
					int termSym = state[t.shifts[i]].symbol;
					if (termSym >= nterms) break;

					assert next[termSym] == -2;
					next[termSym] = -1;
					actionset[setsize++] = termSym;
				}

				// reset conflicts
				conflicts.clear();

				// process reduces
				for (int i = laindex[t.number]; i < laindex[t.number + 1]; i++) {
					int ai = i * termset;
					int max = ai + termset;
					int termSym = 0;
					for (; ai < max; ai++) {
						int bits = LA[ai];
						if (bits == 0) {
							termSym += BITS;
						} else {
							for (int e = 0; e < BITS; e++) {
								if ((bits & (1 << e)) != 0) {
									if (next[termSym] == -2) {
										// OK
										next[termSym] = larule[i];
										actionset[setsize++] = termSym;
									} else {
										addReduce(next, termSym, larule[i], conflicts);
									}
								}
								termSym++;
							}
						}
					}
				}

				// merge conflicts
				List<LalrConflict> mergedConflicts = conflicts.getMergedConflicts(t.number,
						getInput(t.number), next);
				for (ParserConflict conflict : mergedConflicts) {
					status.report(conflict);
					switch (conflict.getKind()) {
						case ParserConflict.REDUCE_REDUCE:
						case ParserConflict.REDUCE_REDUCE_SOFT:
							rr++;
							break;
						case ParserConflict.SHIFT_REDUCE:
						case ParserConflict.SHIFT_REDUCE_SOFT:
							sr++;
							break;
					}
				}

				// process non-assoc syntax errors
				for (int i = 0; i < nterms; i++) {
					if (next[i] == -3) {
						next[i] = -2;
					}
				}

				// insert into action_table
				int[] stateActions = new int[2 * (setsize + 1)];
				action_index[t.number] = -3 - nactions;
				int e = 0;
				for (int i = 0; i < setsize; i++) {
					stateActions[e++] = actionset[i];
					stateActions[e++] = next[actionset[i]];
				}
				stateActions[e++] = -1;
				stateActions[e++] = -2;
				actionTables.add(stateActions);
				nactions += stateActions.length;
			}
		}
		if ((sr + rr) > 0) {
			status.report(ProcessingStatus.KIND_ERROR, "conflicts: " + sr + " shift/reduce and "
					+ rr + " reduce/reduce");
		}

		int e = 0;
		action_table = new int[nactions];
		for (int[] stateActions : actionTables) {
			for (int stateAction : stateActions) {
				action_table[e++] = stateAction;
			}
		}
	}

	private void addReduce(int[] next, int termSym, int rule, ConflictBuilder builder) {
		if (builder.hasConflict(termSym)) {
			builder.addReduce((Terminal) sym[termSym], ConflictBuilder.CONFLICT, wrules[rule],
					null);

		} else if (next[termSym] == -1) {
			switch (compare_prio(rule, termSym)) {
				case 0: // shift/reduce
					builder.addReduce((Terminal) sym[termSym], ConflictBuilder.CONFLICT,
							wrules[rule], null);
					break;
				case 1: // shift
					builder.addReduce((Terminal) sym[termSym], ConflictBuilder.SHIFT,
							wrules[rule], null);
					break;
				case 2: // reduce
					builder.addReduce((Terminal) sym[termSym], ConflictBuilder.REDUCE,
							wrules[rule], null);
					next[termSym] = rule;
					break;
				case 3: // error (non-assoc)
					builder.addReduce((Terminal) sym[termSym], ConflictBuilder.SYNTAXERR,
							wrules[rule], null);
					next[termSym] = -3;
					break;
			}
		} else if (next[termSym] == -3) {
			builder.addReduce((Terminal) sym[termSym], ConflictBuilder.CONFLICT, wrules[rule],
					null);
		} else {
			// reduce/reduce
			int prevRule = next[termSym];
			if (lookaheadBuilder.isResolutionRule(prevRule)) {
				if (sym[rleft[rule]] instanceof Lookahead) {
					// Updating the resolution rule to include a new lookahead.
					next[termSym] = lookaheadBuilder.addResolutionRule(prevRule,
							(Lookahead) sym[rleft[rule]]);
					return;
				} else {
					// Resolution rules are not part of the grammar, report one of the
					// original lookahead rules.
					prevRule = lookaheadBuilder.getRefRule(prevRule);
				}
			} else if (sym[rleft[rule]] instanceof Lookahead
					&& sym[rleft[prevRule]] instanceof Lookahead) {

				// Conflicting lookaheads need a new resolution rule.
				Set<Lookahead> set = new LinkedHashSet<>();
				set.add((Lookahead) sym[rleft[prevRule]]);
				set.add((Lookahead) sym[rleft[rule]]);
				next[termSym] = lookaheadBuilder.addResolutionRule(set, prevRule);
				return;
			}
			builder.addReduce((Terminal) sym[termSym], ConflictBuilder.CONFLICT, wrules[rule],
					wrules[prevRule]);
		}
	}

	private ParserTables generate() {
		if (inputs == null || inputs.length == 0) {
			status.report(ProcessingStatus.KIND_ERROR, "input symbol is not defined");
			return null;
		}

		for (int input : inputs) {
			if (input == -1) {
				status.report(ProcessingStatus.KIND_ERROR, "input symbol is not defined"); // FIXME
				return null;
			}
		}

		if (eoi == -1) {
			status.report(ProcessingStatus.KIND_ERROR, "the end-of-input symbol is not defined");
			return null;
		}

		// grammar
		verify_grammar();

		// engine
		if (!buildLR0()) {
			return null;
		}

		lookaheadBuilder = new ExplicitLookaheadBuilder(rules, status);
		buildLalr();
		action();
		return createResult();
	}

	private ParserTables createResult() {
		// Compacting resolution rules.
		lookaheadBuilder.assignIndices();
		for (int i = 1; i < action_table.length; i += 2) {
			if (action_table[i] >= this.rules) {
				action_table[i] = lookaheadBuilder.getRuleIndex(action_table[i]);
			}
		}
		lookaheadBuilder.compact();
		LookaheadRule[] resolutionRules = lookaheadBuilder.extractRules();

		int[] rlen = new int[this.rules + resolutionRules.length];
		for (int i = 0; i < this.rules; i++) {
			int e = 0;
			while (rright[rindex[i] + e] >= 0) e++;
			rlen[i] = e;
		}
		int nrules = this.rules;
		int[] rleft = Arrays.copyOf(this.rleft, nrules + resolutionRules.length);
		for (LookaheadRule r : resolutionRules) {
			rleft[nrules] = r.getDefaultTarget().getIndex();
			rlen[nrules++] = 0;
		}
		int[] goto_ = Arrays.copyOf(term_goto, term_goto.length);
		for (int i = 0; i < goto_.length; i++) {
			goto_[i] *= 2;
		}

		return new ParserTables(sym,
				nrules, nsyms, nterms, nstates,
				rleft, rlen,
				goto_, interleave(term_from, term_to),
				action_table, action_index, final_states,
				markers, resolutionRules);
	}

	public static ParserData compile(Grammar g, ProcessingStatus status) {
		Builder en = new Builder(g, status);
		return en.generate();
	}

	private static int[] interleave(int[] arr1, int[] arr2) {
		if (arr1 == null || arr2 == null || arr1.length != arr2.length) {
			throw new IllegalArgumentException("cannot interleave arrays of different length");
		}
		int[] result = new int[arr1.length * 2];
		int idx = 0;
		for (int i = 0; i < arr1.length; i++) {
			result[idx++] = arr1[i];
			result[idx++] = arr2[i];
		}
		return result;
	}
}
