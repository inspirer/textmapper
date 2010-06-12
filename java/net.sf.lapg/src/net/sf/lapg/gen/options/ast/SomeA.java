package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

// someA ::= map_entries (normal)
// someA ::= structural_expression (normal)
public class SomeA extends AstOptNode {

	private List<MapEntriesItem> mapEntries;
	private StructuralExpression structuralExpression;

	public SomeA(List<MapEntriesItem> mapEntries, StructuralExpression structuralExpression, TextSource input, int start, int end) {
		super(input, start, end);
		this.mapEntries = mapEntries;
		this.structuralExpression = structuralExpression;
	}

	public List<MapEntriesItem> getMapEntries() {
		return mapEntries;
	}
	public StructuralExpression getStructuralExpression() {
		return structuralExpression;
	}
}
