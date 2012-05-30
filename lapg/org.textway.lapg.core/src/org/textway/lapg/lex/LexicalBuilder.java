/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.lex;

import org.textway.lapg.api.Lexem;
import org.textway.lapg.api.NamedPattern;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.regex.RegexPart;
import org.textway.lapg.common.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LexicalBuilder {

	private static class LexemData {
		private final int prio;
		private final int group;
		private final int[] sym;
		private final int len;
		private final int jmpset;
		private final int[] jmp;
		private final String name;
		private final Lexem lexem;

		private LexemData(Lexem l, int[] sym) {
			this.lexem = l;
			this.name = l.getSymbol().getName();
			this.prio = l.getPriority();
			this.group = l.getGroups();
			this.sym = sym;
			len = sym.length;
			jmpset = (sym.length + LexConstants.BITS - 1) / LexConstants.BITS;
			jmp = new int[sym.length * jmpset];
			Arrays.fill(jmp, 0);
		}
	}

	private static class State {
		State next, hash;
		int number;
		int[] change;
		int[] set;
	}

	// initial information
	private final ProcessingStatus status;

	// lexical analyzer description
	int nsit, nlexems, totalgroups;
	int characters, charsetSize;
	int[] char2no;
	private int[][] set2symbols;
	private int[] lsym;
	private int[] lindex;
	private LexemData[] ldata;

	// generate-time variables
	int states;
	State[] hash;
	State first, last, current;
	int[] clsr, cset;

	int[] groupset;

	private LexicalBuilder(ProcessingStatus status) {
		this.status = status;
	}

	private int add_set(int[] state, int size, int sum) {
		State n;
		int i;

		assert sum >= 0;

		// search for existing
		for (n = hash[sum]; n != null; n = n.hash) {
			for (i = 0; n.set[i] >= 0 && state[i] >= 0 && n.set[i] == state[i]; i++) {
			}
			if (n.set[i] == -1 && state[i] == -1) {
				return n.number;
			}
		}

		// have we exceed the limits
		if (states >= LexConstants.MAX_WORD) {
			return -1;
		}

		// create new
		n = new State();
		n.set = new int[size + 1];
		last = last.next = n;
		n.hash = hash[sum];
		hash[sum] = n;
		n.next = null;

		n.number = states++;
		n.change = null;
		for (i = 0; i < size; i++) {
			n.set[i] = state[i];
		}
		n.set[size] = -1;

		return n.number;
	}

	private final static boolean LEX_CLOSURE_DEBUG = false;

	// builds closure of the given set (using jumps)
	private int closure(int[] set) {
		int n, p;
		int i = 0, clind = 0, base, slen;
		int k, l, m, word;

		if (LEX_CLOSURE_DEBUG) {
			status.debug("\tclosure of: ");
			for (n = 0; set[n] >= 0; n++) {
				status.debug(" " + set[n]);
			}
			status.debug("\n");
		}

		for (p = 0; set[p] >= 0; ) {

			// search for next terminal
			while (i < nlexems && set[p] >= lindex[i + 1]) {
				i++;
			}
			assert i < nlexems;

			// create closure for it in cset
			slen = ldata[i].jmpset;
			Arrays.fill(cset, 0);

			for (base = lindex[i], word = lindex[i + 1]; set[p] >= 0 && set[p] < word; p++) {
				assert set[p] >= base;
				int[] cjmp = ldata[i].jmp;
				n = (set[p] - base) * slen;
				for (l = 0; l < slen; l++) {
					cset[l] |= cjmp[n++];
				}
			}

			if (LEX_CLOSURE_DEBUG) {
				status.debug("\t\t\tcset (" + i + ", base=" + base + ") = ");
				for (l = 0; l < slen * LexConstants.BITS; l++) {
					if ((cset[l / LexConstants.BITS] & 1 << l % LexConstants.BITS) != 0) {
						status.debug(" " + l + "(" + FormatUtil.asHex(lsym[base + l], 2) + ")");
					}
				}
				status.debug("\n");
			}

			// save cset in closure (exclude LBR, RBR, SPL, OR)
			for (m = base, k = 0; k < slen; k++) {
				if ((word = cset[k]) == 0) {
					m += LexConstants.BITS;
				} else {
					for (l = 0; l < LexConstants.BITS; l++, m++) {
						if ((word & 1 << l) != 0) {
							if ((lsym[m] & LexConstants.MASK) > LexConstants.SPL) {
								clsr[clind++] = m;
							}
						}
					}
				}
			}
		}

		// save closure in initial array
		for (i = 0; i < clind; i++) {
			set[i] = clsr[i];
		}
		set[clind] = -1;

		if (LEX_CLOSURE_DEBUG) {
			status.debug("\t\t\tis: ");
			for (n = 0; set[n] >= 0; n++) {
				status.debug(" " + set[n]);
			}
			status.debug("\n");
		}

		return clind;
	}

	// fills: ljmp
	private void buildJumps() {
		int i, k, lex;
		int[] stack = new int[LexConstants.MAX_DEEP];

		if (status.isDebugMode()) {
			status.debug("\nLexem jumps:\n");
		}

		for (lex = 0; lex < nlexems; lex++) {
			if (ldata[lex].len == 0) {
				// ignoring lexem
				continue;
			}

			int[] jumps = ldata[lex].jmp;
			int jmpset = ldata[lex].jmpset;
			int cd = -1, len = ldata[lex].len - 1;
			int sym_index = lindex[lex];

			// generate initial jumps
			for (i = 0; i < len; i++) {
				switch (lsym[sym_index + i] & LexConstants.MASK) {
					case LexConstants.LBR: // (
						stack[++cd] = i;
						jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
						break;

					case LexConstants.RBR: // )
						jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;

						switch (lsym[sym_index + i] >> 29 & 3) {
							case 1: // +
								jumps[(i + 1) * jmpset + stack[cd] / LexConstants.BITS] |= 1 << stack[cd] % LexConstants.BITS;
								break;
							case 2: // *
								jumps[(i + 1) * jmpset + stack[cd] / LexConstants.BITS] |= 1 << stack[cd] % LexConstants.BITS;
								jumps[stack[cd] * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
								break;
							case 3: // ?
								jumps[stack[cd] * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
								break;
						}
						cd--;
						break;

					case LexConstants.OR: // |
						k = lsym[sym_index + stack[cd]] & 0xffff;
						jumps[i * jmpset + (k + 1) / LexConstants.BITS] |= 1 << (k + 1) % LexConstants.BITS;
						jumps[stack[cd] * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
						break;

					case LexConstants.SPL: // forbid two steps back
						jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
						break;

					default: // SYM, ANY, SET
						switch (lsym[sym_index + i] >> 29 & 3) {
							case 1: // +
								jumps[(i + 1) * jmpset + i / LexConstants.BITS] |= 1 << i % LexConstants.BITS;
								break;
							case 2: // *
								jumps[(i + 1) * jmpset + i / LexConstants.BITS] |= 1 << i % LexConstants.BITS;
								jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
								break;
							case 3: // ?
								jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= 1 << (i + 1) % LexConstants.BITS;
								break;
						}
						break;
				}
			}

			// transitive closure of jumps
			int j, e;
			for (i = 0; i <= len; i++) {
				for (j = 0; j <= len; j++) {
					if ((jumps[jmpset * j + i / LexConstants.BITS] & 1 << i % LexConstants.BITS) != 0) {
						for (e = 0; e <= len; e++) {
							if ((jumps[jmpset * i + e / LexConstants.BITS] & 1 << e % LexConstants.BITS) != 0) {
								jumps[jmpset * j + e / LexConstants.BITS] |= 1 << e % LexConstants.BITS;
							}
						}
					}
				}
			}

			// reflexive
			for (i = 0; i <= len; i++) {
				jumps[jmpset * i + i / LexConstants.BITS] |= 1 << i % LexConstants.BITS;
			}

			// extended debug information
			if (status.isDebugMode()) {
				status.debug(FormatUtil.asDecimal(lex, 2, ' ') + ": ");
				for (i = 0; i < len; i++) {
					status.debug(" (" + i + ":");
					for (k = 0; k <= len; k++) {
						if ((jumps[i * jmpset + k / LexConstants.BITS] & 1 << k % LexConstants.BITS) != 0) {
							status.debug(" " + k);
						}
					}
					status.debug(") ");
					switch (lsym[sym_index + i] & LexConstants.MASK) {
						case LexConstants.LBR:
							status.debug("LEFT(" + (lsym[sym_index + i] & 0xffff) + ")");
							break;
						case LexConstants.RBR:
							status.debug("RIGHT");
							break;
						case LexConstants.OR:
							status.debug("OR");
							break;
						case LexConstants.SPL:
							status.debug("SPLIT");
							break;
						case LexConstants.ANY:
							status.debug("ANY");
							break;
						case LexConstants.SYM:
							if ((lsym[sym_index + i] & 0xffff) < 127 && (lsym[sym_index + i] & 0xffff) > 32) {
								status.debug("\'" + (char) (lsym[sym_index + i] & 0xffff) + "\'");
							} else {
								status.debug("#" + FormatUtil.asHex(lsym[sym_index + i] & 0xffff, 4));
							}
							break;
						default:
							status.debug("SET#" + (lsym[sym_index + i] & ~LexConstants.HIGH_STORAGE));
							break;
					}
					switch (lsym[sym_index + i] >> 29 & 3) {
						case 1:
							status.debug("+");
							break;
						case 2:
							status.debug("*");
							break;
						case 3:
							status.debug("?");
							break;
					}
				}
				status.debug("  [" + ldata[lex].name + "," + ldata[lex].lexem.getSymbol().getIndex() + "]\n");
			}
		}

		if (status.isDebugMode()) {
			status.debug("\n");
		}
	}

	private boolean buildStates() {
		int i, e, k;
		int nnext, lexemerrors = 0;
		int[] toshift = new int[charsetSize];

		// allocate temporary storage
		hash = new State[LexConstants.HASH_SIZE];
		clsr = new int[nsit];
		int[] next = new int[nsit + 1];
		nnext = 0;

		// allocate temporary set
		for (e = 1, i = 0; i < nlexems; i++) {
			if (ldata[i].jmpset > e) {
				e = ldata[i].jmpset;
			}
		}
		cset = new int[e];

		// create first set
		for (i = 0; i < nlexems; i++) {
			if ((ldata[i].group & 1) != 0 && ldata[i].len != 0) {
				next[nnext++] = lindex[i];
			}
		}
		next[nnext] = -1;
		nnext = closure(next);

		for (i = 0, e = 0; next[i] >= 0; i++) {
			e += next[i];
		}
		e = e % LexConstants.HASH_SIZE;

		groupset = new int[LexConstants.BITS];
		groupset[0] = 0;

		// create state
		states = 1;
		Arrays.fill(hash, null);

		current = first = last = new State();
		first.number = 0;
		first.hash = first.next = null;
		hash[e] = first;
		first.set = new int[nnext + 1];
		for (i = 0; i <= nnext; i++) {
			first.set[i] = next[i];
		}

		// create left group states
		for (k = 1; k < LexConstants.BITS; k++) {
			if ((totalgroups & 1 << k) != 0) {
				for (nnext = i = 0; i < nlexems; i++) {
					if ((ldata[i].group & 1 << k) != 0 && ldata[i].len != 0) {
						next[nnext++] = lindex[i];
					}
				}
				next[nnext] = -1;
				nnext = closure(next);

				for (i = 0, e = 0; next[i] >= 0; i++) {
					e += next[i];
				}
				groupset[k] = add_set(next, nnext, e % LexConstants.HASH_SIZE);
			} else {
				groupset[k] = -1;
			}
		}

		// generate states
		while (current != null) {

			// first of all we must search if there any lexem have been read already
			int lexnum = -1;
			Arrays.fill(toshift, 0);
			int[] cset = current.set;

			for (int csi = 0; cset[csi] >= 0; csi++) {
				int csval = cset[csi];
				if (lsym[csval] < 0 && lsym[csval] >= -LexConstants.MAX_LEXEMS) {

					// end of some regexp found
					final int nlex = -1 - lsym[csval];
					if (lexnum != -1 && lexnum != nlex) {

						if (ldata[nlex].prio == ldata[lexnum].prio) {
							status.report(ProcessingStatus.KIND_ERROR, "two lexems are identical: " + ldata[lexnum].name + " and " + ldata[nlex].name, ldata[lexnum].lexem, ldata[nlex].lexem);
							lexemerrors++;

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

				} else {
					switch (lsym[csval] & LexConstants.MASK) {
						case LexConstants.SYM:
							toshift[(lsym[csval] & 0xffff) / LexConstants.BITS] |= 1 << (lsym[csval] & 0xffff) % LexConstants.BITS;
							break;
						case LexConstants.ANY: /* except \n and eof */
							int nl = char2no['\n'];
							for (i = 1; i < charsetSize; i++) {
								if (i != nl / LexConstants.BITS) {
									toshift[i] = ~0;
								}
							}
							if (nl < 32) {
								toshift[0] |= ~((1 << nl) + 1);
							} else {
								toshift[0] |= ~1;
								toshift[nl / LexConstants.BITS] |= ~(1 << nl % LexConstants.BITS);
							}
							break;
						default:
							e = lsym[csval] & ~LexConstants.HIGH_STORAGE;
							int[] used = set2symbols[e];
							for (i = 0; i < used.length; i++) {
								toshift[used[i] / LexConstants.BITS] |= 1 << used[i] % LexConstants.BITS;
							}
							break;
					}
				}
			}

			// check for the empty lexem
			if (current == first && lexnum != -1) {
				status.report(ProcessingStatus.KIND_ERROR, ldata[lexnum].name + ": lexem is empty", ldata[lexnum].lexem);
				lexemerrors++;
			}

			// allocate new change table
			current.change = new int[characters];

			// try to shift all available symbols
			for (int sym = 0; sym < characters; sym++) {
				if ((toshift[sym / LexConstants.BITS] & 1 << sym % LexConstants.BITS) != 0) {
					int l;

					nnext = 0;
					// create new state
					for (int p = 0; cset[p] >= 0; p++) {
						l = lsym[cset[p]];

						if ((l & LexConstants.MASK) == LexConstants.ANY) {
							if (sym != char2no['\n']) {
								next[nnext++] = cset[p] + 1;
							}
						} else if ((l & LexConstants.MASK) == LexConstants.SYM) {
							if (sym == (l & 0xffff)) {
								next[nnext++] = cset[p] + 1;
							}
						} else if (l >= 0) {
							int o = l & ~LexConstants.HIGH_STORAGE;
							int[] used = set2symbols[o];
							if (Arrays.binarySearch(used, sym) >= 0) {
								next[nnext++] = cset[p] + 1;
							}
						}
					}

					// closure
					next[nnext] = -1;
					nnext = closure(next);

					// save new state
					for (l = e = 0; e < nnext; e++) {
						l += next[e];
					}

					current.change[sym] = add_set(next, nnext, l % LexConstants.HASH_SIZE);

					// Have we exceeded the limits?
					if (current.change[sym] == -1) {
						status.report(ProcessingStatus.KIND_FATAL, "lexical analyzer is too big ...");
						return false;
					}

				} else {
					current.change[sym] = lexnum >= 0 ? -3 - lexnum : -1;
				}
			}

			assert current.change[0] < 0;

			// next state
			current = current.next;
		}

		// first group (only) succeeds on EOI
		first.change[0] = -2;

		return lexemerrors == 0;
	}

	/*
	 * Fills initial arrays from lexems descriptions
	 */
	private boolean prepare(Lexem[] lexems, NamedPattern[] patterns) {
		RegexpCompiler rp = new RegexpCompiler(loadNamedPatterns(patterns));
		boolean success = true;

		ArrayList<LexemData> syms = new ArrayList<LexemData>();

		totalgroups = 0;

		int lsym_size = 0;
		for (Lexem l : lexems) {
			if (l.isExcluded()) {
				continue;
			}
			totalgroups |= l.getGroups();

			if (l.getGroups() == 0) {
				status.report(ProcessingStatus.KIND_ERROR, l.getSymbol().getName() + ": defined lexem without a group", l);
				success = false;
			}

			if (l.getSymbol().getName().equals("error")) {
				status.report(ProcessingStatus.KIND_ERROR, "error token must be defined without regular expression", l);
				success = false;
				continue;
			}

			int[] lexem_sym = parseRegexp(rp, l);
			if (lexem_sym == null) {
				success = false;
				continue;
			}

			syms.add(new LexemData(l, lexem_sym));
			lsym_size += lexem_sym.length;
		}
		if (!success) {
			return false;
		}

		nlexems = syms.size();
		if (nlexems >= LexConstants.MAX_LEXEMS) {
			status.report(ProcessingStatus.KIND_ERROR, "too much lexems", syms.get(LexConstants.MAX_LEXEMS - 1).lexem);
			return false;

		} else if (nlexems == 0) {
			status.report(ProcessingStatus.KIND_ERROR, "no lexems");
			return false;
		}

		nsit = 0;
		lsym = new int[lsym_size];
		ldata = syms.toArray(new LexemData[syms.size()]);
		lindex = new int[nlexems + 1];
		int index = 0;
		for (LexemData l : syms) {
			lindex[index++] = nsit;
			System.arraycopy(l.sym, 0, lsym, nsit, l.sym.length);
			nsit += l.sym.length;
		}
		lindex[nlexems] = nsit;
		assert lsym_size == nsit;

		if ((totalgroups & 1) == 0) {
			status.report(ProcessingStatus.KIND_ERROR, "no lexems in the first group", nlexems > 0 ? ldata[0].lexem : null);
			return false;
		}

		rp.buildSets();
		char2no = rp.getCharacterMap();
		characters = rp.getSymbolCount();
		set2symbols = rp.getSetToSymbolsMap();
		charsetSize = (characters + LexConstants.BITS - 1) / LexConstants.BITS;

		if (status.isDebugMode()) {
			debugTables();
		}

		return true;
	}

	private void debugTables() {
		status.debug("\nLexems:\n\n");
		for (int i = 0; i < nlexems; i++) {
			status.debug(ldata[i].name + "," + ldata[i].lexem.getSymbol().getIndex() + ": ");
			for (int e = lindex[i]; e < lindex[i + 1]; e++) {
				status.debug(" " + FormatUtil.asHex(lsym[e], 8));
			}

			status.debug(" (" + ldata[i].lexem.getRegexp().toString() + ")\n");
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

	private Map<String, RegexPart> loadNamedPatterns(NamedPattern[] patterns) {
		Map<String, RegexPart> result = new HashMap<String, RegexPart>();
		for (NamedPattern p : patterns) {
			String name = p.getName();
			RegexPart regex = p.getRegexp();
			result.put(name, regex);
		}
		return result;
	}

	private int[] parseRegexp(RegexpCompiler rp, Lexem l) {
		try {
			RegexPart parsedRegex = l.getRegexp();
			return rp.compile(l.getIndex(), parsedRegex);

		} catch (RegexpParseException ex) {
			status.report(ProcessingStatus.KIND_ERROR, l.getSymbol().getName() + ": " + ex.getMessage(), l);
			return null;
		}
	}

	private LexerTables generate(Lexem[] lexems, NamedPattern[] patterns) {

		if (!prepare(lexems, patterns)) {
			return null;
		}

		buildJumps();
		if (!buildStates()) {
			return null;
		}

		int[] stateChange = new int[states * characters];
		int t = 0;
		for (State s = first; s != null; s = s.next, t += characters) {
			System.arraycopy(s.change, 0, stateChange, t, characters);
		}

		return new LexerTables(characters, char2no, groupset, stateChange);
	}

	/*
	 * Generates lexer tables from lexems descriptions
	 */
	public static LexerTables compile(Lexem[] lexems, NamedPattern[] patterns, ProcessingStatus status) {
		LexicalBuilder lb = new LexicalBuilder(status);
		return lb.generate(lexems, patterns);
	}
}
