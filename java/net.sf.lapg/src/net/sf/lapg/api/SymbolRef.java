package net.sf.lapg.api;

public interface SymbolRef extends Annotated {
	Symbol getTarget();
	String getAlias();
}
