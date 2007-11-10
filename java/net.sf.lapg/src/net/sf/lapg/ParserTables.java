package net.sf.lapg;

import net.sf.lapg.api.Symbol;

public class ParserTables {
	public Symbol[] sym;
	public int rules, nsyms, nterms, nstates, errorn;
	public int[] rleft, rright, rindex, rprio, rlines;
	public String[] raction;
	public short[] sym_goto, sym_from, sym_to, action_table;
	public int[] action_index;
	public int nactions;

	public boolean hasError() {
		return errorn != -1;
	}

	public boolean hasActions() {
		if( raction != null ) {
			for( int i = 0; i < raction.length; i++ ) {
				if( raction[i] != null ) {
					return true;
				}
			}
		}
		return false;
	}

	public int[] getRuleLength() {
		int[] result = new int[rules];
		for( int i = 0; i < rules; i++ ) {
			int e = 0;
			for(; rright[ rindex[i]+e ] >= 0; e++) {
			}
			result[i] = e;
		}
		return result;
	}
}