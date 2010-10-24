package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class _String extends AstOptNode {

	private String identifier;
	private String scon;

	public _String(String identifier, String scon, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.scon = scon;
	}

	public String getIdentifier() {
		return identifier;
	}
	public String getScon() {
		return scon;
	}
}
