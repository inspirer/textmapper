package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

// structural_expression ::= '[' map_entries ']' (normal)
// structural_expression ::= '[' expression_list ']' (normal)
public class StructuralExpression extends AstOptNode implements IExpression {

	private List<MapEntriesItem> mapEntries;
	private List<IExpression> expressionList;

	public StructuralExpression(TextSource input, int start, int end) {
		super(input, start, end);
	}

	public List<MapEntriesItem> getMapEntries() {
		return mapEntries;
	}
	public List<IExpression> getExpressionList() {
		return expressionList;
	}
}
