package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class StructuralExpression extends AstNode implements IExpression {

	private String identifier;
	private List<MapEntriesItem> mapEntries;
	private List<IExpression> expressionList;

	public StructuralExpression(String identifier, List<MapEntriesItem> mapEntries, List<IExpression> expressionList, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.mapEntries = mapEntries;
		this.expressionList = expressionList;
	}

	public String getIdentifier() {
		return identifier;
	}
	public List<MapEntriesItem> getMapEntries() {
		return mapEntries;
	}
	public List<IExpression> getExpressionList() {
		return expressionList;
	}
}
