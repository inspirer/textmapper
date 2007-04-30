package net.sf.lapg;

public class LexerTables {
	public final int nstates, nchars, nterms;
	public final String[] lact;
	public final int[] lnum, char2no, groupset;
	public final int[][] change;
	
	public LexerTables(int nstates, int nchars, int nterms, String[] lact, int[] lnum, int[] char2no, int[] groupset, int[][] change) {
		this.nstates = nstates;
		this.nchars = nchars;
		this.nterms = nterms;
		this.lact = lact;
		this.lnum = lnum;
		this.char2no = char2no;
		this.groupset = groupset;
		this.change = change;
	}
}
