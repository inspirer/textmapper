package net.sf.lapg.api;


public interface Rule {

	public int getIndex();
	public Symbol getLeft();
	public Symbol[] getRight();
	public int getPriority();
	public Action getAction();
}