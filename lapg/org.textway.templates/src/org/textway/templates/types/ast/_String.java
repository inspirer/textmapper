package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class _String extends AstNode {

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
