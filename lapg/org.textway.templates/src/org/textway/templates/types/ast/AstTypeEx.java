package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstTypeEx extends AstNode {

	private AstType type;
	private List<AstMultiplicity> multiplicityList;

	public AstTypeEx(AstType type, List<AstMultiplicity> multiplicityList, TextSource input, int start, int end) {
		super(input, start, end);
		this.type = type;
		this.multiplicityList = multiplicityList;
	}

	public AstType getType() {
		return type;
	}
	public List<AstMultiplicity> getMultiplicityList() {
		return multiplicityList;
	}
}
