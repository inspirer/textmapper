/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

	private static final int BITS = 32;
	private static final int MAX_RULES = 0x100000 - 1;
	private static final int MAX_WORD = 0x7ff0;

	private static class RuleData {
		private final LexerRule lexerRule;
		private final RegexInstruction[] pattern;
		private final int prio;
		private final int[] applicableStates;
		private final int len;
		private final int jmpset;
		private final int[] jmp;
		private final String name;

		private RuleData(LexerRule l, int[] applicableStates, RegexInstruction[] pattern) {
			this.lexerRule = l;
			this.pattern = pattern;
			this.name = l.getSymbol().getName();
			this.prio = l.getPriority();
			this.applicableStates = applicableStates;
			len = pattern.length;
			assert len > 0;
			jmpset = (len + BITS - 1) / BITS;
			jmp = new int[len * jmpset];
			Arrays.fill(jmp, 0);
		}
	}

	private static class State {
		State next, hash;
		int number;
		int[] set;
		// -1 error, -2 succeed, -3... lexer rule #0
		int[] action;
	}

	private static final int TABLE_SIZE = 1024; // should be power of 2

	// initial information
	private final ProcessingStatus status;

	// lexical analyzer description
	int nitems, nrules, nlexerStates;
	int characters, charsetSize;
	int[] char2no;
	private int[][] set2symbols;
	private int[] lsym;
	private int[] lindex;
	private RuleData[] ldata;

	// generate-time variables
	int states;
	State[] hash;
	State first, last, current;
	int[] clsr, cset;

	int[] groupset;

	private LexerGenerator(ProcessingStatus status) {
		this.status = status;
	}

	private int add_set(int[] state) {
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

		// have we exceed the limits
		if (states >= MAX_WORD) {
			// TODO throw new ???
			return -1;
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
			int slen = ldata[lex].jmpset;
			Arrays.fill(cset, 0, slen, 0);
			int base = lindex[lex];

			for (; set[p] >= 0 && set[p] < lindex[lex + 1]; p++) {
				assert set[p] >= base;
				int[] cjmp = ldata[lex].jmp;
				int n = (set[p] - base) * slen;
				for (int l = 0; l < slen; l++) {
					cset[l] |= cjmp[n++];
				}
			}

			// save cset in closure (without parentheses & quantifiers)
			int m = 0;
			for (int k = 0; k < slen; k++) {
				int word = cset[k];
				if (word == 0) {
					m += BITS;
				} else {
					for (int l = 0; l < BITS; l++, m++) {
						if ((word & 1 << l) != 0) {
							RegexInstructionKind kind = ldata[lex].pattern[m].getKind();
							if (kind == RegexInstructionKind.Set ||
									kind == RegexInstructionKind.Symbol ||
									kind == RegexInstructionKind.Any ||
									kind == RegexInstructionKind.Done) {
								clsr[outputSize++] = base + m;
							}
						}
					}
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
			int[] jumps = ldata[lex].jmp;
			int jmpset = ldata[lex].jmpset;
			int len = ldata[lex].len - 1;

			// generate initial jumps
			for (i = 0; i < len; i++) {
				switch (ldata[lex].pattern[i].getKind()) {
					case LeftParen: // (
						stack.push(i);
						jumps[i * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
						break;

					case RightParen: // )
						jumps[i * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;

						if (i + 1 < len) {
							switch (ldata[lex].pattern[i + 1].getKind()) {
								case OneOrMore:
									jumps[(i + 1) * jmpset + stack.peek() / BITS] |= 1 << stack.peek() % BITS;
									break;
								case ZeroOrMore:
									jumps[(i + 1) * jmpset + stack.peek() / BITS] |= 1 << stack.peek() % BITS;
									jumps[stack.peek() * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
									break;
								case Optional:
									jumps[stack.peek() * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
									break;
							}
						}
						stack.pop();
						break;

					case Or: // |
						k = ldata[lex].pattern[stack.peek()].getValue();
						jumps[i * jmpset + (k + 1) / BITS] |= 1 << (k + 1) % BITS;
						jumps[stack.peek() * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
						break;

					case ZeroOrMore: // forbid two steps back
					case OneOrMore:
					case Optional:
						jumps[i * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
						break;

					default: // SYM, ANY, SET
						if (i + 1 < len) {
							switch (ldata[lex].pattern[i + 1].getKind()) {
								case OneOrMore:
									jumps[(i + 1) * jmpset + i / BITS] |= 1 << i % BITS;
									break;
								case ZeroOrMore:
									jumps[(i + 1) * jmpset + i / BITS] |= 1 << i % BITS;
									jumps[i * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
									break;
								case Optional:
									jumps[i * jmpset + (i + 1) / BITS] |= 1 << (i + 1) % BITS;
									break;
							}
						}
						break;
				}
			}

			// transitive closure of jumps
			int j, e;
			for (i = 0; i <= len; i++) {
				for (j = 0; j <= len; j++) {
					if ((jumps[jmpset * j + i / BITS] & 1 << i % BITS) != 0) {
						for (e = 0; e <= len; e++) {
							if ((jumps[jmpset * i + e / BITS] & 1 << e % BITS) != 0) {
								jumps[jmpset * j + e / BITS] |= 1 << e % BITS;
							}
						}
					}
				}
			}

			// reflexive
			for (i = 0; i <= len; i++) {
				jumps[jmpset * i + i / BITS] |= 1 << i % BITS;
			}

			// extended debug information
			if (status.isDebugMode()) {
				status.debug(FormatUtil.asDecimal(lex, 2, ' ') + ": ");
				for (i = 0; i < len; i++) {
					status.debug(" (" + i + ":");
					for (k = 0; k <= len; k++) {
						if ((jumps[i * jmpset + k / BITS] & 1 << k % BITS) != 0) {
							status.debug(" " + k);
						}
					}
					status.debug(") ");
					status.debug(ldata[lex].pattern[i].toString());
				}
				status.debug("  [" + ldata[lex].name + "," + ldata[lex].lexerRule.getSymbol().getIndex() + "]\n");
			}
		}

		if (status.isDebugMode()) {
			status.debug("\n");
		}
	}

	private boolean buildStates() {
		int i, e, k;
		int nnext, errors = 0;
		int[] toshift = new int[charsetSize];

		// allocate temporary storage
		hash = new State[TABLE_SIZE];
		Arrays.fill(hash, null);
		clsr = new int[nitems];
		int[] next = new int[nitems + 1];
		nnext = 0;
		states = 0;

		// allocate temporary set
		for (e = 1, i = 0; i < nrules; i++) {
			if (ldata[i].jmpset > e) {
				e = ldata[i].jmpset;
			}
		}
		cset = new int[e];

		// create first set
		for (i = 0; i < nrules; i++) {
			if ((ldata[i].applicableStates[0] & 1) != 0 && ldata[i].len != 0) {
				next[nnext++] = lindex[i];
			}
		}
		next[nnext] = -1;
		closure(next);
		add_set(next);

		groupset = new int[nlexerStates];
		groupset[0] = 0;

		// create state
		current = first;

		// create left group states
		for (k = 1; k < nlexerStates; k++) {
			for (nnext = i = 0; i < nrules; i++) {
				if ((ldata[i].applicableStates[k / BITS] & (1 << (k % BITS))) != 0 && ldata[i].len != 0) {
					next[nnext++] = lindex[i];
				}
			}
			if (nnext > 0) {
				next[nnext] = -1;
				closure(next);
				groupset[k] = add_set(next);
			} else {
				groupset[k] = -1;
			}
		}

		// generate states
		while (current != null) {

			// first of all we must search if there any lexeme have been read already
			int lexnum = -1;
			Arrays.fill(toshift, 0);
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
										"two rules are identical: " + ldata[lexnum].name + " and " +
												ldata[nlex].name, ldata[lexnum].lexerRule,
										ldata[nlex].lexerRule);
								errors++;

							} else if (ldata[nlex].prio > ldata[lexnum].prio) {
								if (status.isAnalysisMode()) {
									status.debug("fixed: " + ldata[nlex].name + " > " + ldata[lexnum].name + "\n");
								}
								lexnum = nlex;

							} else if (status.isAnalysisMode()) {
								status.debug("fixed: " + ldata[lexnum].name + " > " + ldata[nlex].name + "\n");
							}

						} else {
							lexnum = nlex;
						}
						break;
					case Symbol:
						int symValue = instruction.getValue();
						toshift[symValue / BITS] |= 1 << symValue % BITS;
						break;
					case Any: /* except \n and eof */
						int nl = char2no['\n'];
						for (i = 1; i < charsetSize; i++) {
							if (i != nl / BITS) {
								toshift[i] = ~0;
							}
						}
						if (nl < 32) {
							toshift[0] |= ~((1 << nl) + 1);
						} else {
							toshift[0] |= ~1;
							toshift[nl / BITS] |= ~(1 << nl % BITS);
						}
						break;
					case Set:
						e = instruction.getValue();
						int[] used = set2symbols[e];
						for (i = 0; i < used.length; i++) {
							toshift[used[i] / BITS] |= 1 << used[i] % BITS;
						}
						break;
				}
			}

			// check for the empty lexeme
			if (current == first && lexnum != -1) {
				status.report(ProcessingStatus.KIND_ERROR, "`" + ldata[lexnum].name + "' can produce empty lexeme",
						ldata[lexnum].lexerRule);
				errors++;
			}

			// allocate new change table
			current.action = new int[characters];

			// try to shift all available symbols
			for (int sym = 0; sym < characters; sym++) {
				if ((toshift[sym / BITS] & 1 << sym % BITS) != 0) {

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
					current.action[sym] = add_set(next);

					// Have we exceeded the limits?
					if (current.action[sym] == -1) {
						status.report(ProcessingStatus.KIND_FATAL, "lexical analyzer is too big ...");
						return false;
					}

				} else {
					current.action[sym] = lexnum >= 0 ? -3 - ldata[lexnum].lexerRule.getIndex() : -1;
				}
			}

			// next state
			current = current.next;
		}

		// first group (only) succeeds on EOI, unless there is an explicit EOI rule
		if (first.action[0] == -1) first.action[0] = -2;

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

			if (l.getSymbol().getName().equals("error")) {
				status.report(ProcessingStatus.KIND_ERROR, "error token must be defined without regular expression", l);
				success = false;
				continue;
			}

			RegexInstruction[] pattern = parseRegexp(rp, l, syms.size());
			if (pattern == null) {
				success = false;
				continue;
			}

			int[] applicableStates = new int[(nlexerStates + BITS - 1) / BITS];
			for (LexerState lexerState : l.getStates()) {
				int index = lexerState.getIndex();
				applicableStates[index / BITS] |= 1 << (index % BITS);
			}

			syms.add(new RuleData(l, applicableStates, pattern));
			lsym_size += pattern.length;
		}
		if (!success) {
			return false;
		}

		nrules = syms.size();
		if (nrules > MAX_RULES) {
			status.report(ProcessingStatus.KIND_ERROR, "too many lexical rules", syms.get(MAX_RULES).lexerRule);
			return false;

		} else if (nrules == 0) {
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
			if ((l.applicableStates[0] & 1) != 0) {
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
		charsetSize = (characters + BITS - 1) / BITS;

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
			String name = p.getName();
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
			status.report(ProcessingStatus.KIND_ERROR, l.getSymbol().getName() + ": " + ex.getMessage(), l);
			return null;
		}
	}

	private LexerTables generate(LexerState[] lexerStates, LexerRule[] rules, NamedPattern[] patterns) {

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
	 * Generates lexer tables
	 */
	public static LexerData generate(LexerState[] states, LexerRule[] lexerRules, NamedPattern[] patterns,
									 ProcessingStatus status) {
		LexerGenerator lb = new LexerGenerator(status);
		return lb.generate(states, lexerRules, patterns);
	}
}
