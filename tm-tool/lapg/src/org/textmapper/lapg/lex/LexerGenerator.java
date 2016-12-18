/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.lex;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.common.FormatUtil;
import org.textmapper.lapg.regex.RegexFacade;

import java.util.*;

public class LexerGenerator {
	private static class RuleData {
		private final LexerRule lexerRule;
		private final RegexInstruction[] pattern;
		private final int prio;
		private final BitSet applicableStates;
		private final int len;
		private final BitSet jumps;
		private final String name;

		private RuleData(LexerRule l, BitSet applicableStates, RegexInstruction[] pattern) {
			this.lexerRule = l;
			this.pattern = pattern;
			this.name = l.getSymbol().getNameText();
			this.prio = l.getPriority();
			this.applicableStates = applicableStates;
			len = pattern.length;
			assert len > 0;
			jumps = new BitSet(len * len);
		}
	}

	private static class State {
		State next, hash;
		int number;
		int[] set;
		// -1 error, -2 succeed, -3... lexer rule #0
		int[] action;
	}

	private static final int TABLE_SIZE = 1024; // should be a power of 2

	// initial information
	private final ProcessingStatus status;

	// lexical analyzer description
	private int nitems, nrules, nlexerStates;
	private int characters, charsetSize;
	private int[] char2no;
	private int[][] set2symbols;
	private int[] lsym;
	private int[] lindex;
	private RuleData[] ldata;

	// generate-time variables
	private int states;
	private State[] hash;
	private State first;
	private State last;
	private int[] clsr;
	private BitSet cset;

	private int[] groupset;

	private LexerGenerator(ProcessingStatus status) {
		this.status = status;
	}

	private int add_set(int[] state, State insertAfter) {
		int hCode = 1;
		int lastIndex = 0;
		while (state[lastIndex] >= 0) {
			hCode = hCode * 31 + state[lastIndex++];
		}

		State n;

		// search for existing
		for (n = hash[hCode & (TABLE_SIZE - 1)]; n != null; n = n.hash) {
			int i = 0;
			while (state[i] >= 0 && n.set[i] == state[i]) {
				i++;
			}
			if (n.set[i] == -1 && state[i] == -1) {
				return n.number;
			}
		}

		// create new
		n = new State();
		n.set = new int[lastIndex + 1];
		System.arraycopy(state, 0, n.set, 0, lastIndex);
		n.set[lastIndex] = -1;
		n.next = null;
		n.number = states++;
		n.action = null;

		n.hash = hash[hCode & (TABLE_SIZE - 1)];
		hash[hCode & (TABLE_SIZE - 1)] = n;

		if (first == null) {
			first = last = n;
		} else if (insertAfter != null) {
			if (insertAfter == last) {
				last = n;
				assert insertAfter.next == null;
			} else {
				n.next = insertAfter.next;
			}
			insertAfter.next = n;
		} else {
			last = last.next = n;
		}
		return n.number;
	}

	// builds closure of the given set (using jumps)
	private void closure(int[] set) {
		int lex = 0;
		int outputSize = 0;
		for (int p = 0; set[p] >= 0; ) {

			// search for next rule
			while (lex < nrules && set[p] >= lindex[lex + 1]) {
				lex++;
			}
			assert lex < nrules;

			// create closure for it in cset
			cset.clear();
			BitSet jumps = ldata[lex].jumps;
			int len = ldata[lex].len;
			int base = lindex[lex];

			for (; set[p] >= 0 && set[p] < lindex[lex + 1]; p++) {
				assert set[p] >= base;
				int n = (set[p] - base) * len;
				for (int l = 0; l < len; l++) {
					if (jumps.get(n + l)) {
						cset.set(l);
					}
				}
			}

			// save cset in closure (without parentheses & quantifiers)
			for (int val = cset.nextSetBit(0); val >= 0; val = cset.nextSetBit(val + 1)) {
				RegexInstructionKind kind = ldata[lex].pattern[val].getKind();
				if (kind == RegexInstructionKind.Set ||
						kind == RegexInstructionKind.Symbol ||
						kind == RegexInstructionKind.Any ||
						kind == RegexInstructionKind.Done) {
					clsr[outputSize++] = base + val;
				}
			}
		}

		// save closure in initial array
		for (lex = 0; lex < outputSize; lex++) {
			set[lex] = clsr[lex];
		}
		set[outputSize] = -1;
	}

	// fills: ljmp
	private void buildJumps() {
		int i, k;
		Stack<Integer> stack = new Stack<>();

		if (status.isDebugMode()) {
			status.debug("\nRegex jumps:\n");
		}

		for (int lex = 0; lex < nrules; lex++) {
			BitSet jumps = ldata[lex].jumps;
			int len = ldata[lex].len;

			// generate initial jumps
			for (i = 0; i < len - 1; i++) {
				switch (ldata[lex].pattern[i].getKind()) {
					case LeftParen: // (
						stack.push(i);
						jumps.set(i * len + i + 1);
						break;

					case RightParen: // )
						jumps.set(i * len + i + 1);
						if (i + 2 < len) {
							switch (ldata[lex].pattern[i + 1].getKind()) {
								case OneOrMore:
									jumps.set((i + 1) * len + stack.peek());
									break;
								case ZeroOrMore:
									jumps.set((i + 1) * len + stack.peek());
									jumps.set(stack.peek() * len + i + 1);
									break;
								case Optional:
									jumps.set(stack.peek() * len + i + 1);
									break;
							}
						}
						stack.pop();
						break;

					case Or: // |
						k = ldata[lex].pattern[stack.peek()].getValue();
						jumps.set(i * len + k + 1);
						jumps.set(stack.peek() * len + i + 1);
						break;

					case ZeroOrMore: // forbid two steps back
					case OneOrMore:
					case Optional:
						jumps.set(i * len + i + 1);
						break;

					default: // SYM, ANY, SET
						if (i + 2 < len) {
							switch (ldata[lex].pattern[i + 1].getKind()) {
								case OneOrMore:
									jumps.set((i + 1) * len + i);
									break;
								case ZeroOrMore:
									jumps.set((i + 1) * len + i);
									jumps.set(i * len + i + 1);
									break;
								case Optional:
									jumps.set(i * len + i + 1);
									break;
							}
						}
						break;
				}
			}

			// transitive closure of jumps
			int j, e;
			for (i = 0; i < len; i++) {
				for (j = 0; j < len; j++) {
					if (jumps.get(j * len + i)) {
						for (e = 0; e < len; e++) {
							if (jumps.get(i * len + e)) {
								jumps.set(j * len + e);
							}
						}
					}
				}
			}

			// reflexive
			for (i = 0; i < len; i++) {
				jumps.set(i * len + i);
			}

			// extended debug information
			if (status.isDebugMode()) {
				status.debug(FormatUtil.asDecimal(lex, 2, ' ') + ": ");
				for (i = 0; i < len - 1; i++) {
					status.debug(" (" + i + ":");
					for (k = 0; k < len; k++) {
						if (jumps.get(i * len + k)) {
							status.debug(" " + k);
						}
					}
					status.debug(") ");
					status.debug(ldata[lex].pattern[i].toString());
				}
				status.debug("  [" + ldata[lex].name + ","
						+ ldata[lex].lexerRule.getSymbol().getIndex() + "]\n");
			}
		}

		if (status.isDebugMode()) {
			status.debug("\n");
		}
	}

	private boolean buildStates() {
		int i, k;
		int nnext, errors = 0;
		BitSet toshift = new BitSet(characters);

		// allocate temporary storage
		hash = new State[TABLE_SIZE];
		Arrays.fill(hash, null);
		clsr = new int[nitems];
		int[] next = new int[nitems + 1];
		states = 0;

		// allocate temporary set
		int maxPatternLength = 1;
		for (i = 0; i < nrules; i++) {
			if (ldata[i].len > maxPatternLength) {
				maxPatternLength = ldata[i].len;
			}
		}
		cset = new BitSet(maxPatternLength);

		// create first group states
		groupset = new int[nlexerStates];
		for (k = 0; k < nlexerStates; k++) {
			for (nnext = i = 0; i < nrules; i++) {
				if (ldata[i].applicableStates.get(k) && ldata[i].len != 0) {
					next[nnext++] = lindex[i];
				}
			}
			if (nnext > 0) {
				next[nnext] = -1;
				closure(next);
				groupset[k] = add_set(next, null);
			} else {
				groupset[k] = -1;
			}
		}

		// generate states
		for (State current = first; current != null; current = current.next) {

			// first of all we must search if there any lexeme have been read already
			int lexnum = -1;
			toshift.clear();
			int[] cset = current.set;

			for (int csi = 0; cset[csi] >= 0; csi++) {
				int csval = cset[csi];
				int lex = lsym[csval];
				RegexInstruction instruction = ldata[lex].pattern[csval - lindex[lex]];
				switch (instruction.getKind()) {
					case Done:
						// end of some regexp found
						final int nlex = instruction.getValue();
						if (lexnum != -1 && lexnum != nlex) {

							if (ldata[nlex].prio == ldata[lexnum].prio) {
								status.report(ProcessingStatus.KIND_ERROR,
										"two rules are identical: " + ldata[lexnum].name + " and "
												+ ldata[nlex].name, ldata[lexnum].lexerRule,
										ldata[nlex].lexerRule);
								errors++;

							} else if (ldata[nlex].prio > ldata[lexnum].prio) {
								if (status.isAnalysisMode()) {
									status.debug("fixed: " + ldata[nlex].name + " > "
											+ ldata[lexnum].name + "\n");
								}
								lexnum = nlex;

							} else if (status.isAnalysisMode()) {
								status.debug("fixed: " + ldata[lexnum].name + " > "
										+ ldata[nlex].name + "\n");
							}

						} else {
							lexnum = nlex;
						}
						break;
					case Symbol:
						int symValue = instruction.getValue();
						toshift.set(symValue);
						break;
					case Any: /* except \n and eof */
						int nl = char2no['\n'];
						boolean nlVal = toshift.get(nl);
						toshift.set(1, characters);
						toshift.set(nl, nlVal);
						break;
					case Set: {
						int e = instruction.getValue();
						int[] used = set2symbols[e];
						for (i = 0; i < used.length; i++) {
							toshift.set(used[i]);
						}
						break;
					}
				}
			}

			// nullable?
			if (current == first && lexnum != -1) {
				status.report(ProcessingStatus.KIND_ERROR, "`" + ldata[lexnum].name
						+ "' accepts empty text", ldata[lexnum].lexerRule);
				errors++;
			}

			// allocate new change table
			current.action = new int[characters];

			// try to shift all available symbols
			for (int sym = 0; sym < characters; sym++) {
				if (toshift.get(sym)) {
					nnext = 0;
					// create new state
					for (int p = 0; cset[p] >= 0; p++) {
						int lex = lsym[cset[p]];
						RegexInstruction instruction = ldata[lex].pattern[cset[p] - lindex[lex]];
						switch (instruction.getKind()) {
							case Any:
								if (sym != char2no['\n']) {
									next[nnext++] = cset[p] + 1;
								}
								break;
							case Symbol:
								if (sym == instruction.getValue()) {
									next[nnext++] = cset[p] + 1;
								}
								break;
							case Set:
								int[] used = set2symbols[instruction.getValue()];
								if (Arrays.binarySearch(used, sym) >= 0) {
									next[nnext++] = cset[p] + 1;
								}
								break;
						}
					}

					// closure
					next[nnext] = -1;
					closure(next);
					current.action[sym] = add_set(next, current);

					// Have we exceeded the limits?
					if (current.action[sym] == -1) {
						status.report(ProcessingStatus.KIND_FATAL,
								"lexical analyzer is too big ...");
						return false;
					}

				} else {
					current.action[sym] =
							lexnum >= 0 ? -3 - ldata[lexnum].lexerRule.getIndex() : -1;
				}
			}
		}

		// First group (only) succeeds on EOI, unless there is an explicit EOI rule.
		if (first.action[0] == -1) first.action[0] = -2;

		// Re-ordering states for better cache locality.
		int[] permutationMap = new int[states];
		Arrays.fill(permutationMap, -1);
		int counter = 0;
		for (State s = first; s != null; s = s.next) {
			assert permutationMap[s.number] == -1;
			permutationMap[s.number] = counter++;
		}
		assert counter == states;

		for (i = 0; i < groupset.length; i++) {
			if (groupset[i] == -1) continue;
			groupset[i] = permutationMap[groupset[i]];
		}
		for (State s = first; s != null; s = s.next) {
			s.number = permutationMap[s.number];
			for (i = 0; i < s.action.length; i++) {
				if (s.action[i] < 0) continue;
				s.action[i] = permutationMap[s.action[i]];
			}
		}
		return errors == 0;
	}

	/**
	 * Fills initial arrays from lexemes definitions
	 */
	private boolean prepare(LexerState[] lexerStates, LexerRule[] rules, NamedPattern[] patterns) {
		RegexpCompiler rp = new RegexpCompiler(createContext(patterns));
		boolean success = true;

		ArrayList<RuleData> syms = new ArrayList<>();

		nlexerStates = lexerStates.length;

		int lsym_size = 0;
		for (LexerRule l : rules) {
			if (l.isExcluded()) {
				continue;
			}

			if (l.getSymbol().getNameText().equals(Symbol.ERROR.text())) {
				status.report(ProcessingStatus.KIND_ERROR,
						"error token must be defined without regular expression", l);
				success = false;
				continue;
			}

			RegexInstruction[] pattern = parseRegexp(rp, l, syms.size());
			if (pattern == null) {
				success = false;
				continue;
			}

			BitSet applicableStates = new BitSet(nlexerStates);
			for (LexerState lexerState : l.getStates()) {
				applicableStates.set(lexerState.getIndex());
			}

			syms.add(new RuleData(l, applicableStates, pattern));
			lsym_size += pattern.length;
		}
		if (!success) {
			return false;
		}

		nrules = syms.size();
		if (nrules == 0) {
			status.report(ProcessingStatus.KIND_ERROR, "no lexical rules");
			return false;
		}

		nitems = 0;
		lsym = new int[lsym_size];
		ldata = syms.toArray(new RuleData[syms.size()]);
		lindex = new int[nrules + 1];
		int index = 0;
		for (RuleData l : syms) {
			Arrays.fill(lsym, nitems, nitems + l.pattern.length, index);
			lindex[index++] = nitems;
			nitems += l.pattern.length;
		}
		lindex[nrules] = nitems;
		assert lsym_size == nitems;

		boolean initialStateIsEmpty = true;
		for (RuleData l : syms) {
			if (l.applicableStates.get(0)) {
				initialStateIsEmpty = false;
				break;
			}
		}

		if (initialStateIsEmpty) {
			status.report(ProcessingStatus.KIND_ERROR, "no rules in the `initial' state",
					nrules > 0 ? ldata[0].lexerRule : null);
			return false;
		}

		LexerInputSymbols inputSymbols = rp.getInputSymbols();
		char2no = inputSymbols.getCharacterMap();
		characters = inputSymbols.getSymbolCount();
		set2symbols = inputSymbols.getSetToSymbolsMap();

		if (status.isDebugMode()) {
			debugTables();
		}

		return true;
	}

	private void debugTables() {
		status.debug("\nLexical rules:\n\n");
		for (int i = 0; i < nrules; i++) {
			status.debug(ldata[i].name + "," + ldata[i].lexerRule.getSymbol().getIndex() + ": ");
			for (RegexInstruction instruction : ldata[i].pattern) {
				status.debug(" ");
				status.debug(instruction.toString());
			}
			status.debug(" (" + ldata[i].lexerRule.getRegexp().toString() + ")\n");
		}

		// only for small data
		if (characters * char2no.length < 1000000) {
			status.debug("\nSymbols:\n\n");
			for (int i = 0; i < characters; i++) {
				status.debug(i + ": ");
				for (int e = 0; e < char2no.length; e++) {
					if (char2no[e] == i) {
						if (e > 32 && e < 128) {
							status.debug(Character.toString((char) e));
						} else {
							status.debug("\\x" + FormatUtil.asHex(e, 4));
						}
					}
				}
				status.debug("\n");
			}
		}

		status.debug("\nSets:\n\n");
		for (int i = 0; i < set2symbols.length; i++) {
			status.debug(i + ": ");
			for (int e = 0; e < set2symbols[i].length; e++) {
				status.debug(" " + set2symbols[i][e]);
			}

			status.debug("\n");
		}
	}

	private RegexContext createContext(NamedPattern[] patterns) {
		final Map<String, RegexPart> result = new HashMap<>();
		for (NamedPattern p : patterns) {
			String name = p.getNameText();
			RegexPart regex = p.getRegexp();
			result.put(name, regex);
		}
		return RegexFacade.createContext(result);
	}

	private RegexInstruction[] parseRegexp(RegexpCompiler rp, LexerRule l, int index) {
		try {
			RegexPart parsedRegex = l.getRegexp();
			return rp.compile(index, parsedRegex);

		} catch (RegexParseException ex) {
			status.report(ProcessingStatus.KIND_ERROR,
					l.getSymbol().getNameText() + ": " + ex.getMessage(), l);
			return null;
		}
	}

	private LexerTables generate(LexerState[] lexerStates, LexerRule[] rules,
								 NamedPattern[] patterns) {

		if (!prepare(lexerStates, rules, patterns)) {
			return null;
		}

		buildJumps();
		if (!buildStates()) {
			return null;
		}

		int[] stateChange = new int[states * characters];
		int t = 0;
		for (State s = first; s != null; s = s.next, t += characters) {
			System.arraycopy(s.action, 0, stateChange, t, characters);
		}

		return new LexerTables(characters, char2no, groupset, stateChange);
	}

	/*
	 * Generates lexer tables.
	 */
	public static LexerData generate(LexerState[] states, LexerRule[] lexerRules,
									 NamedPattern[] patterns, ProcessingStatus status) {
		LexerGenerator lb = new LexerGenerator(status);
		return lb.generate(states, lexerRules, patterns);
	}
}
