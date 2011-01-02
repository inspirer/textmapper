package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class TypeEx extends AstNode {

	private Type type;
	private List<Multiplicity> multiplicityList;

	public TypeEx(Type type, List<Multiplicity> multiplicityList, TextSource input, int start, int end) {
		super(input, start, end);
		this.type = type;
		this.multiplicityList = multiplicityList;
	}

	public Type getType() {
		return type;
	}
	public List<Multiplicity> getMultiplicityList() {
		return multiplicityList;
	}
}
