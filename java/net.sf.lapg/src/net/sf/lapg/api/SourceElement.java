package net.sf.lapg.api;

public interface SourceElement {

	String getResourceName();

	int getOffset();

	int getEndOffset();

	int getLine();
}
