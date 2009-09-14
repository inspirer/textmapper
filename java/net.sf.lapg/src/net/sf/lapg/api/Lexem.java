package net.sf.lapg.api;

/**
 * Lexem rule.
 */
public interface Lexem {

	public Symbol getSymbol();
	public String getRegexp();

	public int getPriority();
	public int getGroups();

	public Action getAction();
}
