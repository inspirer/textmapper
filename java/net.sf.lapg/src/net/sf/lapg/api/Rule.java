package net.sf.lapg.api;


public interface Rule {

	public int getIndex();
	public Symbol getLeft();
	public SymbolRef[] getRight();
	public int getPriority();
	public Action getAction();
}