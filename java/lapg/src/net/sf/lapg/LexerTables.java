package net.sf.lapg;

public class LexerTables {
	public final int nstates, nchars, nterms;
	public final String[] actions;
	public final int[] lnum, char2no, groupset;
	public final int[][] change;
	
	public LexerTables(int nstates, int nchars, int nterms, String[] lact, int[] lnum, int[] char2no, int[] groupset, int[][] change) {
		this.nstates = nstates;
		this.nchars = nchars;
		this.nterms = nterms;
		this.actions = lact;
		this.lnum = lnum;
		this.char2no = char2no;
		this.groupset = groupset;
		this.change = change;
	}
	
	public boolean hasActions() {
		if( actions != null ) {
			for( int i = 0; i < actions.length; i++ ) {
				if( actions[i] != null )
					return true;
			}
		}
		return false;
	}
}
