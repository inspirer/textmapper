package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class Constraint extends AstNode {

	private StringConstraint stringConstraint;
	private List<Multiplicity> multiplicityList;

	public Constraint(StringConstraint stringConstraint, List<Multiplicity> multiplicityList, TextSource input, int start, int end) {
		super(input, start, end);
		this.stringConstraint = stringConstraint;
		this.multiplicityList = multiplicityList;
	}

	public StringConstraint getStringConstraint() {
		return stringConstraint;
	}
	public List<Multiplicity> getMultiplicityList() {
		return multiplicityList;
	}
}
