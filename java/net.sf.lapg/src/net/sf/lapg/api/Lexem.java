package net.sf.lapg.api;


public interface Lexem {
	public Symbol getSymbol();
	public String getRegexp();
	public int getPriority();
	public int getGroups();
	public String getAction();
}
