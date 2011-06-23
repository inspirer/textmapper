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
package org.textway.lapg.lex;

import org.textway.lapg.api.Lexem;
import org.textway.lapg.api.NamedPattern;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.common.FormatUtil;
import org.textway.lapg.parser.LiLexem;
import org.textway.lapg.parser.LiNamedPattern;
import org.textway.lapg.regex.RegexPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LexicalBuilder {

	private class State {
		State next, hash;
		int number;
		int[] change;
		int[] set;
	}

	// initial information
	private final Lexem[] lexems;
	private final NamedPattern[] patterns;
	private final ProcessingStatus status;

	// lexical analyzer description
	int nsit, nlexems, totalgroups;
	int characters, charsetSize;
	int[] char2no;
	private int[][] set2symbols;
	private int[] lsym;

	private int[] lnum, lprio, group, lindex, llen, ljmpset;
	private int[][] ljmp;
	private String[] lname;

	// generate-time variables
	int states;
	State[] hash;
	State first, last, current;
	int[] clsr, cset;

	int[] groupset;

	private LexicalBuilder(Lexem[] lexems, NamedPattern[] patterns, ProcessingStatus status) {
		this.status = status;
		this.lexems = lexems;
		this.patterns = patterns;
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
			slen = ljmpset[i];
			Arrays.fill(cset, 0);

			for (base = lindex[i], word = lindex[i + 1]; set[p] >= 0 && set[p] < word; p++) {
				assert set[p] >= base;
				int[] cjmp = ljmp[i];
				n = (set[p] - base) * slen;
				for (l = 0; l < slen; l++) {
					cset[l] |= cjmp[n++];
				}
			}

			if (LEX_CLOSURE_DEBUG) {
				status.debug("\t\t\tcset (" + i + ", base=" + base + ") = ");
				for (l = 0; l < slen * LexConstants.BITS; l++) {
					if ((cset[(l) / LexConstants.BITS] & (1 << ((l) % LexConstants.BITS))) != 0) {
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
						if ((word & (1 << l)) != 0) {
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
			if (llen[lex] == 0) {
				// ignoring lexem
				continue;
			}

			int[] jumps = ljmp[lex];
			int jmpset = ljmpset[lex];
			int cd = -1, len = llen[lex] - 1;
			int sym_index = lindex[lex];

			// generate initial jumps
			for (i = 0; i < len; i++) {
				switch (lsym[sym_index + i] & LexConstants.MASK) {
					case LexConstants.LBR: // (
						stack[++cd] = i;
						jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
						break;

					case LexConstants.RBR: // )
						jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));

						switch ((lsym[sym_index + i] >> 29) & 3) {
							case 1: // +
								jumps[(i + 1) * jmpset + (stack[cd]) / LexConstants.BITS] |= (1 << ((stack[cd]) % LexConstants.BITS));
								break;
							case 2: // *
								jumps[(i + 1) * jmpset + (stack[cd]) / LexConstants.BITS] |= (1 << ((stack[cd]) % LexConstants.BITS));
								jumps[stack[cd] * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
								break;
							case 3: // ?
								jumps[stack[cd] * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
								break;
						}
						cd--;
						break;

					case LexConstants.OR: // |
						k = lsym[sym_index + stack[cd]] & 0xffff;
						jumps[i * jmpset + (k + 1) / LexConstants.BITS] |= (1 << ((k + 1) % LexConstants.BITS));
						jumps[stack[cd] * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
						break;

					case LexConstants.SPL: // forbid two steps back
						jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
						break;

					default: // SYM, ANY, SET
						switch ((lsym[sym_index + i] >> 29) & 3) {
							case 1: // +
								jumps[(i + 1) * jmpset + (i) / LexConstants.BITS] |= (1 << ((i) % LexConstants.BITS));
								break;
							case 2: // *
								jumps[(i + 1) * jmpset + (i) / LexConstants.BITS] |= (1 << ((i) % LexConstants.BITS));
								jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
								break;
							case 3: // ?
								jumps[i * jmpset + (i + 1) / LexConstants.BITS] |= (1 << ((i + 1) % LexConstants.BITS));
								break;
						}
						break;
				}
			}

			// transitive closure of jumps
			int j, e;
			for (i = 0; i <= len; i++) {
				for (j = 0; j <= len; j++) {
					if (((jumps[jmpset * j + (i) / LexConstants.BITS] & (1 << ((i) % LexConstants.BITS))) != 0)) {
						for (e = 0; e <= len; e++) {
							if (((jumps[jmpset * i + (e) / LexConstants.BITS] & (1 << ((e) % LexConstants.BITS))) != 0)) {
								jumps[jmpset * j + (e) / LexConstants.BITS] |= (1 << ((e) % LexConstants.BITS));
							}
						}
					}
				}
			}

			// reflexive
			for (i = 0; i <= len; i++) {
				jumps[jmpset * i + (i) / LexConstants.BITS] |= (1 << ((i) % LexConstants.BITS));
			}

			// extended debug information
			if (status.isDebugMode()) {
				status.debug(FormatUtil.asDecimal(lex, 2, ' ') + ": ");
				for (i = 0; i < len; i++) {
					status.debug(" (" + i + ":");
					for (k = 0; k <= len; k++) {
						if ((jumps[i * jmpset + (k) / LexConstants.BITS] & (1 << ((k) % LexConstants.BITS))) != 0) {
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
					switch ((lsym[sym_index + i] >> 29) & 3) {
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
				status.debug("  [" + lname[lex] + "," + lnum[lex] + "]\n");
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
			if (ljmpset[i] > e) {
				e = ljmpset[i];
			}
		}
		cset = new int[e];

		// create first set
		for (i = 0; i < nlexems; i++) {
			if ((group[i] & 1) != 0 && llen[i] != 0) {
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
			if ((totalgroups & (1 << k)) != 0) {
				for (nnext = i = 0; i < nlexems; i++) {
					if ((group[i] & (1 << k)) != 0 && llen[i] != 0) {
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

						if (lprio[nlex] == lprio[lexnum]) {
							status.report(ProcessingStatus.KIND_ERROR, "two lexems are identical: " + lname[lexnum] + " and " + lname[nlex], lexems[lexnum], lexems[nlex]);
							lexemerrors++;

						} else if (lprio[nlex] > lprio[lexnum]) {
							if (status.isAnalysisMode()) {
								status.debug("fixed: " + lname[nlex] + " > " + lname[lexnum] + "\n");
							}
							lexnum = nlex;

						} else if (status.isAnalysisMode()) {
							status.debug("fixed: " + lname[lexnum] + " > " + lname[nlex] + "\n");
						}

					} else {
						lexnum = nlex;
					}

				} else {
					switch (lsym[csval] & LexConstants.MASK) {
						case LexConstants.SYM:
							toshift[(lsym[csval] & 0xffff) / LexConstants.BITS] |= (1 << ((lsym[csval] & 0xffff) % LexConstants.BITS));
							break;
						case LexConstants.ANY: /* except \n and eof */
							int nl = char2no['\n'];
							for (i = 1; i < charsetSize; i++) {
								if (i != (nl / LexConstants.BITS)) {
									toshift[i] = ~0;
								}
							}
							if (nl < 32) {
								toshift[0] |= ~((1 << nl) + (1));
							} else {
								toshift[0] |= ~1;
								toshift[nl / LexConstants.BITS] |= ~(1 << (nl % LexConstants.BITS));
							}
							break;
						default:
							e = lsym[csval] & ~LexConstants.HIGH_STORAGE;
							int[] used = set2symbols[e];
							for (i = 0; i < used.length; i++) {
								toshift[(used[i]) / LexConstants.BITS] |= (1 << ((used[i]) % LexConstants.BITS));
							}
							break;
					}
				}
			}

			// check for the empty lexem
			if (current == first && lexnum != -1) {
				status.report(ProcessingStatus.KIND_ERROR, lname[lexnum] + ": lexem is empty", lexems[lexnum]);
				lexemerrors++;
			}

			// allocate new change table
			current.change = new int[characters];

			// try to shift all available symbols
			for (int sym = 0; sym < characters; sym++) {
				if ((toshift[(sym) / LexConstants.BITS] & (1 << ((sym) % LexConstants.BITS))) != 0) {
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
					current.change[sym] = (lexnum >= 0) ? -3 - lexnum : -1;
				}
			}

			assert current.change[0] < 0;

			// next state
			current = current.next;
		}

		first.change[0] = -2;

		return lexemerrors == 0;
	}

	/*
	 * Fills initial arrays from lexems descriptions
	 */
	private boolean prepare() {
		RegexpParser rp = new RegexpParser(loadNamedPatterns());
		boolean success = true;

		nsit = 0;
		ArrayList<int[]> syms = new ArrayList<int[]>();
		nlexems = lexems.length;

		if (nlexems >= LexConstants.MAX_LEXEMS) {
			status.report(ProcessingStatus.KIND_ERROR, "too much lexems", lexems[LexConstants.MAX_LEXEMS - 1]);
			return false;
		}

		totalgroups = 0;
		lnum = new int[nlexems];
		lprio = new int[nlexems];
		group = new int[nlexems];
		lindex = new int[nlexems + 1];
		llen = new int[nlexems];
		ljmpset = new int[nlexems];
		ljmp = new int[nlexems][];
		lname = new String[nlexems];

		for (int i = 0; i < nlexems; i++) {
			Lexem l = lexems[i];
			assert i == l.getIndex();
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

			lnum[i] = l.getSymbol().getIndex();
			lname[i] = l.getSymbol().getName();
			lprio[i] = l.getPriority();
			group[i] = l.getGroups();
			lindex[i] = nsit;

			if (!l.isExcluded()) {
				int[] lexem_sym = parseRegexp(rp, l);
				if (lexem_sym == null) {
					success = false;
					continue;
				}

				llen[i] = lexem_sym.length;
				ljmpset[i] = (((lexem_sym.length) + LexConstants.BITS - 1) / LexConstants.BITS);
				ljmp[i] = new int[lexem_sym.length * ljmpset[i]];

				Arrays.fill(ljmp[i], 0);
				syms.add(lexem_sym);
				nsit += lexem_sym.length;
			} else {
				llen[i] = 0;
				ljmpset[i] = 0;
			}
		}
		if (!success) {
			return false;
		}
		lindex[nlexems] = nsit;
		lsym = new int[nsit];
		int e = 0;
		for (int[] from : syms) {
			for (int element : from) {
				lsym[e++] = element;
			}
		}

		if ((totalgroups & 1) == 0) {
			status.report(ProcessingStatus.KIND_ERROR, "no lexems in the first group", lexems.length > 0 ? lexems[0] : null);
			return false;
		}

		rp.buildSets();
		char2no = rp.getCharacterMap();
		characters = rp.getSymbolCount();
		set2symbols = rp.getSetToSymbolsMap();
		charsetSize = (characters + LexConstants.BITS - 1) / LexConstants.BITS;

		if (status.isDebugMode()) {
			status.debug("\nLexems:\n\n");
			for (int i = 0; i < nlexems; i++) {
				status.debug(lname[i] + "," + lnum[i] + ": ");
				for (e = lindex[i]; e < lindex[i + 1]; e++) {
					status.debug(" " + FormatUtil.asHex(lsym[e], 8));
				}

				status.debug(" (" + lexems[i].getRegexp() + ")\n");
			}

			// only for small data
			if (characters * char2no.length < 1000000) {
				status.debug("\nSymbols:\n\n");
				for (int i = 0; i < characters; i++) {
					status.debug(i + ": ");
					for (e = 0; e < char2no.length; e++) {
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
				for (e = 0; e < set2symbols[i].length; e++) {
					status.debug(" " + set2symbols[i][e]);
				}

				status.debug("\n");
			}
		}

		return true;
	}

	private Map<String, RegexPart> loadNamedPatterns() {
		Map<String, RegexPart> result = new HashMap<String, RegexPart>();
		for (NamedPattern p : patterns) {
			String name = p.getName();
			try {
				RegexPart regex = p instanceof LiNamedPattern
						? ((LiNamedPattern) p).getParsedRegexp()
						: RegexMatcher.parse(name, p.getRegexp());
				result.put(name, regex);
			} catch (RegexpParseException ex) {
				status.report(ProcessingStatus.KIND_ERROR, name + ": " + ex.getMessage(), p);
			}
		}
		return result;
	}

	private int[] parseRegexp(RegexpParser rp, Lexem l) {
		try {
			RegexPart parsedRegex = l instanceof LiLexem
					? ((LiLexem) l).getParsedRegexp()
					: RegexMatcher.parse(l.getSymbol().getName(), l.getRegexp());
			return rp.compile(l.getIndex(), parsedRegex);

		} catch (RegexpParseException ex) {
			status.report(ProcessingStatus.KIND_ERROR, l.getSymbol().getName() + ": " + ex.getMessage(), l);
			return null;
		}
	}

	private LexerTables generate() {

		if (lexems.length == 0) {
			status.report(ProcessingStatus.KIND_ERROR, "no lexems");
			return null;
		}

		if (!prepare()) {
			return null;
		}

		buildJumps();
		if (!buildStates()) {
			return null;
		}

		int[][] stateChange = new int[states][];
		for (State s = first; s != null; s = s.next) {
			stateChange[s.number] = s.change;
		}

		return new LexerTables(states, characters, nlexems, lnum, char2no, groupset, stateChange);
	}

	/*
	 * Generates lexer tables from lexems descriptions
	 */
	public static LexerTables compile(Lexem[] lexems, NamedPattern[] patterns, ProcessingStatus status) {
		LexicalBuilder lb = new LexicalBuilder(lexems, patterns, status);
		return lb.generate();
	}
}
