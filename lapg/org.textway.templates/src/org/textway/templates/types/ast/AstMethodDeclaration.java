package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstMethodDeclaration extends AstNode implements IAstMemberDeclaration {

	private AstTypeEx returnType;
	private String name;
	private List<AstTypeEx> parametersopt;

	public AstMethodDeclaration(AstTypeEx returnType, String name, List<AstTypeEx> parametersopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.returnType = returnType;
		this.name = name;
		this.parametersopt = parametersopt;
	}

	public AstTypeEx getReturnType() {
		return returnType;
	}
	public String getName() {
		return name;
	}
	public List<AstTypeEx> getParametersopt() {
		return parametersopt;
	}
}
