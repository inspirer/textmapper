package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class Type extends AstNode {

	public static final int LINT = 1;
	public static final int LSTRING = 2;
	public static final int LBOOL = 3;

	private int kind;
	private boolean isReference;
	private List<String> name;

	public Type(int kind, boolean isReference, List<String> name, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.isReference = isReference;
		this.name = name;
	}

	public int getKind() {
		return kind;
	}
	public boolean getIsReference() {
		return isReference;
	}
	public List<String> getName() {
		return name;
	}
}
