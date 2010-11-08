package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class StringConstraint extends AstNode implements IConstraint {

	public static final int LSET = 1;
	public static final int LCHOICE = 2;

	private int kind;
	private List<_String> strings;
	private String identifier;

	public StringConstraint(int kind, List<_String> strings, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.strings = strings;
		this.identifier = identifier;
	}

	public int getKind() {
		return kind;
	}
	public List<_String> getStrings() {
		return strings;
	}
	public String getIdentifier() {
		return identifier;
	}
}
