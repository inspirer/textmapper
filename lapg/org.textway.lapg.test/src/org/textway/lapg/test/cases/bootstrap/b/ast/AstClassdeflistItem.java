package org.textway.lapg.test.cases.bootstrap.b.ast;

import org.textway.lapg.test.cases.bootstrap.b.SampleBTree.TextSource;

public class AstClassdeflistItem extends AstNode {

	private AstClassdef classdef;
	private String identifier;

	public AstClassdeflistItem(AstClassdef classdef, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.classdef = classdef;
		this.identifier = identifier;
	}

	public AstClassdef getClassdef() {
		return classdef;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (classdef != null) {
			classdef.accept(v);
		}
		// TODO for identifier
	}
}
