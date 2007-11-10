package net.sf.lapg.api;


public interface Prio {

	public static final int NOPRIO = -1;

	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int NONASSOC = 3;

	public int getPrio();
	public Symbol[] getSymbols();
}
