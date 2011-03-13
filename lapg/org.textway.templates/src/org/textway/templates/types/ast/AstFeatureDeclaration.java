package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstFeatureDeclaration extends AstNode implements IAstMemberDeclaration {

	private String name;
	private AstTypeEx typeEx;
	private List<AstConstraint> modifiersopt;
	private IAstExpression defaultvalopt;

	public AstFeatureDeclaration(String name, AstTypeEx typeEx, List<AstConstraint> modifiersopt, IAstExpression defaultvalopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.typeEx = typeEx;
		this.modifiersopt = modifiersopt;
		this.defaultvalopt = defaultvalopt;
	}

	public String getName() {
		return name;
	}
	public AstTypeEx getTypeEx() {
		return typeEx;
	}
	public List<AstConstraint> getModifiersopt() {
		return modifiersopt;
	}
	public IAstExpression getDefaultvalopt() {
		return defaultvalopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for name
		if (typeEx != null) {
			typeEx.accept(v);
		}
		if (modifiersopt != null) {
			for (AstConstraint it : modifiersopt) {
				it.accept(v);
			}
		}
		if (defaultvalopt != null) {
			defaultvalopt.accept(v);
		}
	}
}
