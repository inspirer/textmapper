package org.textway.lapg.test.cases.bootstrap.a.ast;

import org.textway.lapg.test.cases.bootstrap.a.SampleATree.TextSource;

public interface IAstNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	void accept(AstVisitor v);
}
