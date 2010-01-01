package net.sf.lapg.api;


public interface Rule extends Annotated {

	public int getIndex();
	public Symbol getLeft();
	public SymbolRef[] getRight();
	public int getPriority();
	public Action getAction();
}