package org.textway.templates.types.ast;

public abstract class AstVisitor {

	protected boolean visit(AstConstraint n) {
		return true;
	}

	protected boolean visit(AstFeatureDeclaration n) {
		return true;
	}

	protected boolean visit(AstInput n) {
		return true;
	}

	protected boolean visit(AstLiteralExpression n) {
		return true;
	}

	protected boolean visit(AstMapEntriesItem n) {
		return true;
	}

	protected boolean visit(AstMethodDeclaration n) {
		return true;
	}

	protected boolean visit(AstMultiplicity n) {
		return true;
	}

	protected boolean visit(Ast_String n) {
		return true;
	}

	protected boolean visit(AstStringConstraint n) {
		return true;
	}

	protected boolean visit(AstStructuralExpression n) {
		return true;
	}

	protected boolean visit(AstType n) {
		return true;
	}

	protected boolean visit(AstTypeDeclaration n) {
		return true;
	}

	protected boolean visit(AstTypeEx n) {
		return true;
	}
}
