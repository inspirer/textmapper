package net.sf.lapg.api;


public interface Rule {

	public Symbol getLeft();
	public Symbol[] getRight();
	public int getPriority();
	public String getAction();

	public int getLine(); // TODO remove
}