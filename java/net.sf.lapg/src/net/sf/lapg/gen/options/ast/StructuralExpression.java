package net.sf.lapg.gen.options.ast;

// structural_expression ::= '[' map_entries ']' (normal)
// structural_expression ::= '[' expression_list ']' (normal)
public class StructuralExpression implements IExpression {

	private Object mapEntries;
	private Object expressionList;

	public Object getMapEntries() {
		return mapEntries;
	}
	public Object getExpressionList() {
		return expressionList;
	}
}
