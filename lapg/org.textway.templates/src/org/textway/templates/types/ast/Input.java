package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class Input extends AstNode {

	private List<TypeDeclaration> declarations;

	public Input(List<TypeDeclaration> declarations, TextSource input, int start, int end) {
		super(input, start, end);
		this.declarations = declarations;
	}

	public List<TypeDeclaration> getDeclarations() {
		return declarations;
	}
}
