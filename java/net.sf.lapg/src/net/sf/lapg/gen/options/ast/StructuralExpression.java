package net.sf.lapg.gen.options.ast;

import java.util.List;

// structural_expression ::= '[' map_entries ']' (normal)
// structural_expression ::= '[' expression_list ']' (normal)
public class StructuralExpression implements IExpression {

	private List<Object> mapEntries;
	private List<Object> expressionList;

	public List<Object> getMapEntries() {
		return mapEntries;
	}
	public List<Object> getExpressionList() {
		return expressionList;
	}
}
