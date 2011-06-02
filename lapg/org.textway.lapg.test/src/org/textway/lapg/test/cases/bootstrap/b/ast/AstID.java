package org.textway.lapg.test.cases.bootstrap.b.ast;

import org.textway.lapg.test.cases.bootstrap.b.SampleBTree.TextSource;

public class AstID extends AstNode {

	private String identifier;

	public AstID(String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for identifier
	}
}
