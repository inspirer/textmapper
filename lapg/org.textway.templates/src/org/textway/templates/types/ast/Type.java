package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class Type extends AstNode {

	public static final int LINT = 1;
	public static final int LSTRING = 2;
	public static final int LBOOL = 3;

	private int kind;
	private boolean isReference;
	private String identifier;

	public Type(int kind, boolean isReference, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.isReference = isReference;
		this.identifier = identifier;
	}

	public int getKind() {
		return kind;
	}
	public boolean getIsReference() {
		return isReference;
	}
	public String getIdentifier() {
		return identifier;
	}
}
