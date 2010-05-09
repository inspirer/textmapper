package net.sf.lapg.api;


public interface ParserConflict {

	public static final int FIXED = 0;
	public static final int SHIFT_REDUCE = 1;
	public static final int REDUCE_REDUCE = 2;

	int getKind();
	String getKindAsText();
	Rule[] getRules();
	Symbol[] getSymbols();
	Input getInput();

	String getText();

	public interface Input {
		int getState();
		Symbol[] getSymbols();
		String getText();
	}
}
