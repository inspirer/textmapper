package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstType extends AstNode {

	public static final int LINT = 1;
	public static final int LSTRING = 2;
	public static final int LBOOL = 3;

	private int kind;
	private boolean isReference;
	private boolean isClosure;
	private List<String> name;
	private List<AstTypeEx> parametersopt;

	public AstType(int kind, boolean isReference, boolean isClosure, List<String> name, List<AstTypeEx> parametersopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.isReference = isReference;
		this.isClosure = isClosure;
		this.name = name;
		this.parametersopt = parametersopt;
	}

	public int getKind() {
		return kind;
	}
	public boolean getIsReference() {
		return isReference;
	}
	public boolean getIsClosure() {
		return isClosure;
	}
	public List<String> getName() {
		return name;
	}
	public List<AstTypeEx> getParametersopt() {
		return parametersopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for name
		if (parametersopt != null) {
			for (AstTypeEx it : parametersopt) {
				it.accept(v);
			}
		}
	}
}
