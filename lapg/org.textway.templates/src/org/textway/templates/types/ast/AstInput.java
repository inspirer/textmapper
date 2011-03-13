package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstInput extends AstNode {

	private List<AstTypeDeclaration> declarations;

	public AstInput(List<AstTypeDeclaration> declarations, TextSource input, int start, int end) {
		super(input, start, end);
		this.declarations = declarations;
	}

	public List<AstTypeDeclaration> getDeclarations() {
		return declarations;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (declarations != null) {
			for (AstTypeDeclaration it : declarations) {
				it.accept(v);
			}
		}
	}
}
