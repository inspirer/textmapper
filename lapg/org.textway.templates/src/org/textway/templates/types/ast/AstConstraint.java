package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstConstraint extends AstNode {

	private AstStringConstraint stringConstraint;
	private List<AstMultiplicity> multiplicityList;

	public AstConstraint(AstStringConstraint stringConstraint, List<AstMultiplicity> multiplicityList, TextSource input, int start, int end) {
		super(input, start, end);
		this.stringConstraint = stringConstraint;
		this.multiplicityList = multiplicityList;
	}

	public AstStringConstraint getStringConstraint() {
		return stringConstraint;
	}
	public List<AstMultiplicity> getMultiplicityList() {
		return multiplicityList;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (stringConstraint != null) {
			stringConstraint.accept(v);
		}
		if (multiplicityList != null) {
			for (AstMultiplicity it : multiplicityList) {
				it.accept(v);
			}
		}
	}
}
