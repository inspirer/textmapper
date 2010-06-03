package net.sf.lapg.gen.options.ast;

// typedef ::= identifier '=' type ';' (normal)
public class Typedef {

	private String identifier;
	private Object type;

	public String getIdentifier() {
		return identifier;
	}
	public Object getType() {
		return type;
	}
}
