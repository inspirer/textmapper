package net.sf.lapg.gen.options.ast;

// typedef ::= identifier '=' type ';' (normal)
public class Typedef {

	private String identifier;
	private Type type;

	public String getIdentifier() {
		return identifier;
	}
	public Type getType() {
		return type;
	}
}
