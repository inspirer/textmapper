package net.sf.lapg.api;

public interface Symbol {

	public int getIndex();
	public String getName();
	public String getType();
	public boolean isTerm();
	public boolean isDefined();
}