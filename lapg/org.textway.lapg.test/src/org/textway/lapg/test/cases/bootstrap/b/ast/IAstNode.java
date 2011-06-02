package org.textway.lapg.test.cases.bootstrap.b.ast;

import org.textway.lapg.test.cases.bootstrap.b.SampleBTree.TextSource;

public interface IAstNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	void accept(AstVisitor v);
}
