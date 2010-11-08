package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class MapEntriesItem extends AstNode {

	private String identifier;
	private IExpression expression;

	public MapEntriesItem(String identifier, IExpression expression, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.expression = expression;
	}

	public String getIdentifier() {
		return identifier;
	}
	public IExpression getExpression() {
		return expression;
	}
}
