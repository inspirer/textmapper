package org.textway.lapg.test.cases.bootstrap.b.ast;

import org.textway.lapg.test.cases.bootstrap.b.SampleATree.TextSource;

public interface IAstNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	void accept(AstVisitor v);
}
