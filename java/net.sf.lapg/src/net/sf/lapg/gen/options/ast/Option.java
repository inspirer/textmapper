package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

// option ::= Ltitle scon (normal)
public class Option extends AstOptNode {

	private String scon;

	public Option(TextSource input, int start, int end) {
		super(input, start, end);
	}

	public String getScon() {
		return scon;
	}
}
