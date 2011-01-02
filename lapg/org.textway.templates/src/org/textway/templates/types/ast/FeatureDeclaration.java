package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class FeatureDeclaration extends AstNode implements IMemberDeclaration {

	private String name;
	private TypeEx typeEx;
	private List<Constraint> modifiersopt;
	private IExpression defaultvalopt;

	public FeatureDeclaration(String name, TypeEx typeEx, List<Constraint> modifiersopt, IExpression defaultvalopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.typeEx = typeEx;
		this.modifiersopt = modifiersopt;
		this.defaultvalopt = defaultvalopt;
	}

	public String getName() {
		return name;
	}
	public TypeEx getTypeEx() {
		return typeEx;
	}
	public List<Constraint> getModifiersopt() {
		return modifiersopt;
	}
	public IExpression getDefaultvalopt() {
		return defaultvalopt;
	}
}
