package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

// typedef ::= identifier '=' type ';' (normal)
public class Typedef extends AstOptNode {

	private String identifier;
	private Type type;

	public Typedef(TextSource input, int start, int end) {
		super(input, start, end);
	}

	public String getIdentifier() {
		return identifier;
	}
	public Type getType() {
		return type;
	}
}
