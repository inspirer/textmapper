package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstStringConstraint extends AstNode {

	public static final int LSET = 1;
	public static final int LCHOICE = 2;

	private int kind;
	private List<Ast_String> strings;
	private String identifier;

	public AstStringConstraint(int kind, List<Ast_String> strings, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.strings = strings;
		this.identifier = identifier;
	}

	public int getKind() {
		return kind;
	}
	public List<Ast_String> getStrings() {
		return strings;
	}
	public String getIdentifier() {
		return identifier;
	}
}
