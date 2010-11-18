package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class StructuralExpression extends AstNode implements IExpression {

	private List<String> name;
	private List<MapEntriesItem> mapEntriesopt;
	private List<IExpression> expressionListopt;

	public StructuralExpression(List<String> name, List<MapEntriesItem> mapEntriesopt, List<IExpression> expressionListopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.mapEntriesopt = mapEntriesopt;
		this.expressionListopt = expressionListopt;
	}

	public List<String> getName() {
		return name;
	}
	public List<MapEntriesItem> getMapEntriesopt() {
		return mapEntriesopt;
	}
	public List<IExpression> getExpressionListopt() {
		return expressionListopt;
	}
}
