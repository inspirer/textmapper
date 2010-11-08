package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public interface IAstNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	//void accept(Visitor v);
}
