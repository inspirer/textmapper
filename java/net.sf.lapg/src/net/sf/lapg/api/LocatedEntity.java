package net.sf.lapg.api;

public interface LocatedEntity {

	String getResourceName();

	int getOffset();

	int getEndOffset();

	int getLine();
}
