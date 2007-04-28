package net.sf.lapg;

public class ParserTables {
	public Symbol[] sym;
	public int rules, nsyms, nterms, nstates, errorn;
	public int[] rleft, rright, rindex, rprio, rlines;
	public String[] raction;
	public short[] sym_goto, sym_from, sym_to, action_table;
	public int[] action_index;
	public int nactions;
}