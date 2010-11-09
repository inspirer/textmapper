package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class StructuralExpression extends AstNode implements IExpression {

	private List<String> name;
	private List<MapEntriesItem> mapEntries;
	private List<IExpression> expressionList;

	public StructuralExpression(List<String> name, List<MapEntriesItem> mapEntries, List<IExpression> expressionList, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.mapEntries = mapEntries;
		this.expressionList = expressionList;
	}

	public List<String> getName() {
		return name;
	}
	public List<MapEntriesItem> getMapEntries() {
		return mapEntries;
	}
	public List<IExpression> getExpressionList() {
		return expressionList;
	}
}
