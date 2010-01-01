package net.sf.lapg.api;

public interface Symbol extends Annotated {

	int getIndex();
	String getName();
	String getType();
	boolean isTerm();
	boolean isDefined();
}