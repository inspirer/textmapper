package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class Ast_String extends AstNode {

	private String identifier;
	private String scon;

	public Ast_String(String identifier, String scon, TextSource input, int start, int end) {
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
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for identifier
		// TODO for scon
	}
}
