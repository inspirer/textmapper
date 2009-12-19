package net.sf.lapg.api;

public interface Symbol {

	int getIndex();
	String getName();
	String getType();
	boolean isTerm();
	boolean isDefined();
}