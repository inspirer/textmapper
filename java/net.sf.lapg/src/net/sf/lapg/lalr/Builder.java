package net.sf.lapg.lalr;

import java.util.Vector;

import net.sf.lapg.IError;
import net.sf.lapg.ParserTables;
import net.sf.lapg.api.Grammar;


public class Builder extends Lalr1 {

	private Builder(Grammar g, IError err, int debuglev) {
		super(g, err, debuglev);
	}

	// tables
	private int[] action_index;
	private int nactions;
	private short[] action_table;

	//

//	private final int[] sym_opt;

	private void fix_grammar() {

		// TODO copy types
//		for (int i = 0; i < nsyms; i++) {
//			if (sym_opt[i] != -1) {
//				sym[sym_opt[i]].type = sym[i].getType();
//			}
//
//			if (sym[i].is_attr) {
//				sym[i].type = sym[sym[i].sibling].type;
//			}
//		}
	}

	private void verify_grammar() {
		int i, e, h;
		boolean k;

		// search for symbols which accepts the empty chain
		search_next_empty: for (;;) {
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

		boolean[] sym_good = new boolean[nsyms];

		// terminal and empty symbols are good
		for (i = 0; i < nsyms; i++) {
			if (sym[i].isTerm() || sym_empty[i]) {
				sym_good[i] = true;
			}
		}

		// search for the good symbols
		get_next_good: for (;;) {
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
		sym_temp[input] = true;
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
			if (!sym[i].isTerm() && !sym[i].isDefined()) {
				err.error("no rules for `" + sym[i].getName() + "`\n");
			} else if (!sym_good[i] || !sym_employed[i]) {
				if (!sym[i].getName().startsWith("_skip")) {
					err.warn( "lapg: symbol `" + sym[i].getName() + "` is useless\n");
				}
			}
		}
	}

	// returns 0:unresolved 1:shift 2:reduce
	private int compare_prio( int rule, int next )
	{
		int i, cgroup, assoc = -1, rule_group = -1, next_group = -1, nextassoc = -1;

		if( nprio == 0 ) {
			return 0;
		}

		for( cgroup = i = 0; i < nprio; i++ ) {
			if( priorul[i] < 0 ) {
				assoc = -priorul[i];
				cgroup++;
			} else {
				if( priorul[i] == rprio[rule] ) {
					rule_group = cgroup;
				}
				if( priorul[i] == next ) {
					next_group = cgroup;
					nextassoc = assoc;
				}
			}
		}

		if( rule_group == -1 || next_group == -1 ) {
			return 0;
		}
		if( rule_group > next_group ) {
			return 2;               // reduce
		}
		if( rule_group < next_group ) {
			return 1;               // shift
		}
		if( nextassoc == 1 ) {
			return 2;               // left => reduce
		}
		if( nextassoc == 2 ) {
			return 1;               // right => shift
		}
		return 0;
	}

	private void print_input(int s) {
		if (state[s].number == 0) {
			return;
		}
		print_input(state[s].fromstate);
		err.warn( " " + sym[state[s].symbol].getName());
	}

	private void action() {
		State t;
		Vector<short[]> actionTables = new Vector<short[]>();
		int rr = 0, sr = 0;
		short[] actionset = new short[nterms], next = new short[nterms];
		int setsize;
		int i, e;

		action_index = new int[nstates];
		action_table = null;
		nactions = 0;

		for (t = first; t != null; t = t.next) {
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
				setsize = 0;
				for (i = 0; i < nterms; i++) {
					next[i] = -2;
				}

				// process shifts
				int termSym;
				for (i = 0; i < t.nshifts; i++) {
					termSym = state[t.shifts[i]].symbol;
					if (termSym >= nterms) {
						break;
					}
					assert next[termSym] == -2;
					next[termSym] = -1;
					actionset[setsize++] = (short) termSym;
				}

				// process reduces
				for (i = laindex[t.number]; i < laindex[t.number + 1]; i++) {
					int ai = i * termset;
					int max = ai + termset;
					termSym = 0;
					for (; ai < max; ai++) {
						int bits = LA[ai];
						if (bits == 0) {
							termSym += BITS;
						} else {
							for (e = 0; e < BITS; e++) {
								if ((bits & (1 << e)) != 0) {
									if (next[termSym] == -2) {
										// OK
										next[termSym] = larule[i];
										actionset[setsize++] = (short) termSym;
									} else if (next[termSym] == -1) {
										switch (compare_prio(larule[i], termSym)) {
										case 0: // shift/reduce
											err.warn( "\ninput:");
											print_input(t.number);
											err.warn( "\nconflict: shift/reduce (" + t.number + ", next " + sym[termSym].getName() + ")\n");
											warn_rule(larule[i]);
											sr++;
											break;
										case 1: // shift
											err.warn( "\ninput:");
											print_input(t.number);
											err.warn( "\nfixed: shift: shift/reduce (" + t.number + ", next " + sym[termSym].getName() + ")\n");
											warn_rule(larule[i]);
											break;
										case 2: // reduce
											err.warn( "\ninput:");
											print_input(t.number);
											err.warn( "\nfixed: reduce: shift/reduce (" + t.number + ", next " + sym[termSym].getName() + ")\n");
											warn_rule(larule[i]);
											next[termSym] = larule[i];
											break;
										}
									} else {
										// reduce/reduce
										err.warn( "\ninput:");
										print_input(t.number);
										err.warn( "\nconflict: reduce/reduce (" + t.number + ", next " + sym[termSym].getName() + ")\n");
										warn_rule(next[termSym]);
										warn_rule(larule[i]);
										rr++;
									}
								}
								termSym++;
							}
						}
					}
				}

				// insert into action_table
				short[] stateActions = new short[2*(setsize+1)];
				action_index[t.number] = -3-nactions;
				e = 0;
				for( i = 0; i < setsize; i++ ) {
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
			err.error("conflicts: " + sr + " shift/reduce and " + rr + " reduce/reduce\n");
		}

		e = 0;
		action_table = new short[nactions];
		for( short[] stateActions : actionTables ) {
			for( i = 0; i < stateActions.length; i++ ) {
				action_table[e++] = stateActions[i];
			}
		}
	}


	private ParserTables generate() {
		if (input == -1) {
			err.error("input symbol is not defined\n");
			return null;
		}

		if (eoi == -1) {
			err.error("the end-of-input symbol is not defined\n");
			return null;
		}

		// grammar
		fix_grammar();
		verify_grammar();

		 // engine
		if (!buildLR0()) {
			return null;
		}

		buildLalr();
		action();
		return createResult();
	}

	private ParserTables createResult() {
		ParserTables r = new ParserTables();
		r.sym = this.sym;
		r.rules = this.rules;
		r.nsyms = this.nsyms;
		r.nterms = this.nterms;
		r.nstates = this.nstates;
		r.errorn = this.errorn;
		r.rleft = this.rleft;
		r.rright = this.rright;
		r.rindex = this.rindex;
		r.rprio = this.rprio;
		r.raction = this.raction;
		r.sym_goto = this.term_goto;
		r.sym_from = this.term_from;
		r.sym_to = this.term_to;
		r.action_table = this.action_table;
		r.action_index = this.action_index;
		r.nactions = this.nactions;
		return r;
	}

	public static ParserTables compile(Grammar g, IError err, int debuglev) {
		Builder en = new Builder(g, err, debuglev);
		return en.generate();
	}
}
