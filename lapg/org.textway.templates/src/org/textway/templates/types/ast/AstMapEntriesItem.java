package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class AstMapEntriesItem extends AstNode {

	private String identifier;
	private AstMapSeparator mapSeparator;
	private IAstExpression expression;

	public AstMapEntriesItem(String identifier, AstMapSeparator mapSeparator, IAstExpression expression, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.mapSeparator = mapSeparator;
		this.expression = expression;
	}

	public String getIdentifier() {
		return identifier;
	}
	public AstMapSeparator getMapSeparator() {
		return mapSeparator;
	}
	public IAstExpression getExpression() {
		return expression;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for identifier
		// TODO for mapSeparator
		if (expression != null) {
			expression.accept(v);
		}
	}
}
