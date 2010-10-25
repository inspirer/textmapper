package org.textway.lapg.gen.options.ast;

import org.textway.lapg.gen.options.OptdefTree.TextSource;

public class MapEntriesItem extends AstOptNode {

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
