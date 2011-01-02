package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class MethodDeclaration extends AstNode implements IMemberDeclaration {

	private TypeEx returnType;
	private String name;
	private List<TypeEx> parametersopt;

	public MethodDeclaration(TypeEx returnType, String name, List<TypeEx> parametersopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.returnType = returnType;
		this.name = name;
		this.parametersopt = parametersopt;
	}

	public TypeEx getReturnType() {
		return returnType;
	}
	public String getName() {
		return name;
	}
	public List<TypeEx> getParametersopt() {
		return parametersopt;
	}
}
