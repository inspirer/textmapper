package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

// string ::= identifier (normal)
// string ::= scon (normal)
public class _String extends AstOptNode {

	private String identifier;
	private String scon;

	public _String(TextSource input, int start, int end) {
		super(input, start, end);
	}

	public String getIdentifier() {
		return identifier;
	}
	public String getScon() {
		return scon;
	}
}
