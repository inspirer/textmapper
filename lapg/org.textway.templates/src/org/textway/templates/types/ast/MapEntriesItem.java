package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class MapEntriesItem extends AstNode {

	private String identifier;
	private MapSeparator mapSeparator;
	private IExpression expression;

	public MapEntriesItem(String identifier, MapSeparator mapSeparator, IExpression expression, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.mapSeparator = mapSeparator;
		this.expression = expression;
	}

	public String getIdentifier() {
		return identifier;
	}
	public MapSeparator getMapSeparator() {
		return mapSeparator;
	}
	public IExpression getExpression() {
		return expression;
	}
}
