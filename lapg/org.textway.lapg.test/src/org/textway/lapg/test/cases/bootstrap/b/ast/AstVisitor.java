package org.textway.lapg.test.cases.bootstrap.b.ast;

public abstract class AstVisitor {

	protected boolean visit(AstID n) {
		return true;
	}

	protected boolean visit(AstClassdef n) {
		return true;
	}

	protected boolean visit(AstClassdeflistItem n) {
		return true;
	}
}
