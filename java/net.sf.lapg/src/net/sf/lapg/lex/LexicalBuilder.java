package net.sf.lapg.lex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.sf.lapg.INotifier;
import net.sf.lapg.LexerTables;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.common.FormatUtil;

public class LexicalBuilder {

	private class State {
		State next, hash;
		int number;
		int[] change;
		int[] set;
	};

	// initial information
	private Lexem[] myLexems;
	private INotifier err;
	int debuglev;

	// lexical analyzer description
	int nsit, nterms, totalgroups;
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

	private LexicalBuilder(Lexem[] lexems, INotifier err, int debuglev) {
		this.err = err;
		this.debuglev = debuglev;
		this.myLexems = lexems;
	}

	private int add_set(int[] state, int size, int sum) {
		State n;
		int i;

		assert sum >= 0;

		// search for existing
		for (n = hash[sum]; n != null; n = n.hash) {
			for (i = 0; n.set[i] >= 0 && state[i] >= 0 && n.set[i] == state[i]; i++) {
				;
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
			err.debug("\tclosure of: ");
			for (n = 0; set[n] >= 0; n++) {
				err.debug(" " + set[n]);
			}
			err.debug("\n");
		}

		for (p = 0; set[p] >= 0;) {

			// search for next terminal
			while (i < nterms && set[p] >= lindex[i + 1]) {
				i++;
			}
			assert i < nterms;

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
				err.debug("\t\t\tcset (" + i + ", base=" + base + ") = ");
				for (l = 0; l < slen * LexConstants.BITS; l++) {
					if ((cset[(l) / LexConstants.BITS] & (1 << ((l) % LexConstants.BITS))) != 0) {
						err.debug(" " + l + "(" + FormatUtil.asHex(lsym[base + l], 2) + ")");
					}
				}
				err.debug("\n");
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
			err.debug("\t\t\tis: ");
			for (n = 0; set[n] >= 0; n++) {
				err.debug(" " + set[n]);
			}
			err.debug("\n");
		}

		return clind;
	}

	// fills: ljmp
	private void buildJumps() {
		int i, k, lex;
		int[] stack = new int[LexConstants.MAX_DEEP];

		if (debuglev >= 2) {
			err.debug("\nLexem jumps:\n");
		}

		for (lex = 0; lex < nterms; lex++) {

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
			if (debuglev >= 2) {
				err.debug(FormatUtil.asDecimal(lex, 2, ' ') + ": ");
				for (i = 0; i < len; i++) {
					err.debug(" (" + i + ":");
					for (k = 0; k <= len; k++) {
						if ((jumps[i * jmpset + (k) / LexConstants.BITS] & (1 << ((k) % LexConstants.BITS))) != 0) {
							err.debug(" " + k);
						}
					}
					err.debug(") ");
					switch (lsym[sym_index + i] & LexConstants.MASK) {
					case LexConstants.LBR:
						err.debug("LEFT(" + (lsym[sym_index + i] & 0xffff) + ")");
						break;
					case LexConstants.RBR:
						err.debug("RIGHT");
						break;
					case LexConstants.OR:
						err.debug("OR");
						break;
					case LexConstants.SPL:
						err.debug("SPLIT");
						break;
					case LexConstants.ANY:
						err.debug("ANY");
						break;
					case LexConstants.SYM:
						if ((lsym[sym_index + i] & 0xffff) < 127 && (lsym[sym_index + i] & 0xffff) > 32) {
							err.debug("\'" + (char) (lsym[sym_index + i] & 0xffff) + "\'");
						} else {
							err.debug("#" + FormatUtil.asHex(lsym[sym_index + i] & 0xffff, 4));
						}
						break;
					default:
						err.debug("SET#" + (lsym[sym_index + i] & ~LexConstants.HIGH_STORAGE));
						break;
					}
					switch ((lsym[sym_index + i] >> 29) & 3) {
					case 1:
						err.debug("+");
						break;
					case 2:
						err.debug("*");
						break;
					case 3:
						err.debug("?");
						break;
					}
				}
				err.debug("  [" + lname[lex] + "," + lnum[lex] + "]\n");
			}
		}

		if (debuglev >= 2) {
			err.debug("\n");
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
		for (e = 1, i = 0; i < nterms; i++) {
			if (ljmpset[i] > e) {
				e = ljmpset[i];
			}
		}
		cset = new int[e];

		// create first set
		for (i = 0; i < nterms; i++) {
			if ((group[i] & 1) != 0) {
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
				for (nnext = i = 0; i < nterms; i++) {
					if ((group[i] & (1 << k)) != 0) {
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
							err.error("lex: two lexems are identical: " + lname[lexnum] + " and " + lname[nlex] + "\n");
							lexemerrors++;

						} else if (lprio[nlex] > lprio[lexnum]) {
							if (debuglev != 0) {
								err.warn("fixed: " + lname[nlex] + " > " + lname[lexnum] + "\n");
							}
							lexnum = nlex;

						} else if (debuglev != 0) {
							err.warn("fixed: " + lname[lexnum] + " > " + lname[nlex] + "\n");
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
				err.error("lex: lexem is empty: `" + lname[lexnum] + "`\n");
				lexemerrors++;
			}

			// allocate new change table
			current.change = new int[characters];

			// try to shift all available symbols
			for (i = 0; i < characters; i++) {
				if ((toshift[(i) / LexConstants.BITS] & (1 << ((i) % LexConstants.BITS))) != 0) {
					int l, sym = i;

					nnext = 0;
					// create new state
					for (int p = 0; cset[p] >= 0; p++) {
						l = lsym[cset[p]];

						if ((l & LexConstants.MASK) == LexConstants.ANY) {
							if (sym != '\n') {
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

					current.change[i] = add_set(next, nnext, l % LexConstants.HASH_SIZE);

					// Have we exceeded the limits?
					if (current.change[i] == -1) {
						err.error("lex: lexical analyzer is too big ...\n");
						return false;
					}

				} else {
					current.change[i] = (lexnum >= 0) ? -2 - lnum[lexnum] : -1;
				}
			}

			assert current.change[0] < 0;

			// next state
			current = current.next;
		}

		first.change[0] = -2;

		return lexemerrors == 0;
	}

	/**
	 * Fills initial arrays from lexems descriptions
	 */
	private boolean prepare() {
		RegexpParser rp = new RegexpParser(err);
		nsit = 0;
		ArrayList<int[]> syms = new ArrayList<int[]>();
		nterms = myLexems.length;

		if (nterms >= LexConstants.MAX_LEXEMS) {
			err.error("lex: too much lexems\n");
			return false;
		}

		totalgroups = 0;
		lnum = new int[nterms];
		lprio = new int[nterms];
		group = new int[nterms];
		lindex = new int[nterms + 1];
		llen = new int[nterms];
		ljmpset = new int[nterms];
		ljmp = new int[nterms][];
		lname = new String[nterms];

		for (int i = 0; i < nterms; i++) {
			Lexem l = myLexems[i];
			totalgroups |= l.getGroups();

			if (l.getGroups() == 0) {
				err.error("lex: lexem `" + l.getSymbol().getName() + "` does not belong to groups\n");
				return false;
			}

			if (l.getSymbol().getName().equals("error")) {
				err.error("lex: error token must be defined without regular expression\n");
				return false;
			}

			int[] lexem_sym = rp.compile(i, l.getSymbol().getName(), l.getRegexp());

			lnum[i] = l.getSymbol().getIndex();
			lprio[i] = l.getPriority();
			group[i] = l.getGroups();
			lindex[i] = nsit;
			llen[i] = lexem_sym.length;
			ljmpset[i] = (((lexem_sym.length) + LexConstants.BITS - 1) / LexConstants.BITS);
			ljmp[i] = new int[lexem_sym.length * ljmpset[i]];
			lname[i] = l.getSymbol().getName();

			Arrays.fill(ljmp[i], 0);
			syms.add(lexem_sym);
			nsit += lexem_sym.length;
		}
		lindex[nterms] = nsit;
		lsym = new int[nsit];
		int e = 0;
		for (Iterator<int[]> it = syms.iterator(); it.hasNext();) {
			int[] from = it.next();
			for (int i = 0; i < from.length; i++) {
				lsym[e++] = from[i];
			}
		}

		if ((totalgroups & 1) == 0) {
			err.error("lex: no lexems in the first group\n");
			return false;
		}

		rp.buildSets();
		char2no = rp.getCharacterMap();
		characters = rp.getSymbolCount();
		set2symbols = rp.getSetToSymbolsMap();
		charsetSize = (characters + LexConstants.BITS - 1) / LexConstants.BITS;

		if (debuglev >= 2) {
			err.debug("\nLexems:\n\n");
			for (int i = 0; i < nterms; i++) {
				err.debug(lname[i] + "," + lnum[i] + ": ");
				for (e = lindex[i]; e < lindex[i + 1]; e++) {
					err.debug(" " + FormatUtil.asHex(lsym[e], 8));
				}

				err.debug(" (" + myLexems[i].getRegexp() + ")\n");
			}

			// only for small data
			if (characters * char2no.length < 1000000) {
				err.debug("\nSymbols:\n\n");
				for (int i = 0; i < characters; i++) {
					err.debug(i + ": ");
					for (e = 0; e < char2no.length; e++) {
						if (char2no[e] == i) {
							if (e > 32 && e < 128) {
								err.debug(Character.toString((char) e));
							} else {
								err.debug("\\x" + FormatUtil.asHex(e, 4));
							}
						}
					}
					err.debug("\n");
				}
			}

			err.debug("\nSets:\n\n");
			for (int i = 0; i < set2symbols.length; i++) {
				err.debug(i + ": ");
				for (e = 0; e < set2symbols[i].length; e++) {
					err.debug(" " + set2symbols[i][e]);
				}

				err.debug("\n");
			}
		}

		return true;
	}

	private LexerTables generate() {

		if (myLexems.length == 0) {
			err.error("lex: no lexems\n");
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

		return new LexerTables(states, characters, nterms, lnum, char2no, groupset, stateChange);
	}

	/**
	 * Generates lexer tables from lexems descriptions
	 */
	public static LexerTables compile(Lexem[] lexems, INotifier err, int debuglev) {
		LexicalBuilder lb = new LexicalBuilder(lexems, err, debuglev);
		return lb.generate();
	}
}
