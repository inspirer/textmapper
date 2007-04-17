package net.sf.lapg.lalr;


public class Engine extends EngineLalr {

	public class Result {
		Symbol sym;
		int rules, nsyms, nterms, nstates, errorn;
		int[] rleft, rright, rindex, rprio, rlines;
		String[] raction;
		short[] sym_goto, sym_from, sym_to, action_table;
		int[] action_index;
		int nactions;
	};

	public Engine(Grammar g, IError err, int debuglev) {
		super(g, err, debuglev);
	}

	// last generation
	private int[] action_index;
	private int nactions, nactions_used;
	private short[] action_table;

	
	private void fix_grammar() {

		for (int i = 0; i < nsyms; i++) {
			Symbol curr = sym[i];
			if (curr.opt != -1)
				sym[curr.opt].type = curr.type;

			if (curr.is_attr)
				curr.type = sym[curr.sibling].type;
		}
	}

	private void verify_grammar() {
		int i, e, h;
		boolean k;

		// search for symbols which accepts the empty chain
		search_next_empty: for (;;) {
			for (i = 0; i < rules; i++) {
				if (!sym[rleft[i]].empty) {

					k = true;
					for (e = rindex[i]; k && (rright[e] >= 0); e++)
						if (!sym[rright[e]].empty)
							k = false;

					if (k) {
						sym[rleft[i]].empty = true;
						continue search_next_empty;
					}
				}
			}
			break;
		}

		// terminal and empty symbols are good
		for (i = 0; i < nsyms; i++)
			if (sym[i].term || sym[i].empty)
				sym[i].good = true;

		// search for the good symbols
		get_next_good: for (;;) {
			for (i = 0; i < rules; i++) {
				if (!sym[rleft[i]].good) {

					k = true;
					for (e = rindex[i]; k && (rright[e] >= 0); e++)
						if (!sym[rright[e]].good)
							k = false;

					if (k) {
						sym[rleft[i]].good = true;
						continue get_next_good;
					}
				}
			}
			break;
		}

		// search for the employed symbols
		k = true;
		sym[input].temp = true;
		while (k) {
			k = false;
			for (i = 0; i < nsyms; i++) {
				if (sym[i].temp) {
					for (h = 0; h < rules; h++) {
						if (rleft[h] == (int) i) {
							for (e = rindex[h]; rright[e] >= 0; e++) {
								if (!sym[rright[e]].temp && !sym[rright[e]].employed) {
									if (sym[rright[e]].term) {
										sym[rright[e]].employed = true;
									} else {
										k = true;
										sym[rright[e]].temp = true;
									}
								}
							}
						}
					}

					sym[i].employed = true;
					sym[i].temp = false;
				}
			}
		}

		// eoi is very useful token
		sym[eoi].good = sym[eoi].employed = true;

		// print out the useless symbols
		for (i = 0; i < nsyms; i++) {
			if (!sym[i].term && !sym[i].defed) {
				err.error(0, "no rules for `" + sym[i].name + "`\n");
			} else if (!sym[i].good || !sym[i].employed) {
				if (!sym[i].name.startsWith("_skip"))
					err.error(1, "lapg: symbol `" + sym[i].name + "` is useless\n");
			}
		}
	}

	public Result generate() {
		if (input == -1) {
			err.error(0, "input symbol is not defined\n");
			return null;
		}

		if (eoi == -1) {
			err.error(0, "the end-of-input symbol is not defined\n");
			return null;
		}

		if (errors != 0) {
			return null;
		}

		// grammar
		fix_grammar();
		verify_grammar();
		
		 // engine
		if (!LR0()) {
			clear();
			return null;
		}

		lalr();
		//		action();
		//		cleanup();

		//		return 1;
		return null;
	}

}
