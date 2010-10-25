package org.textway.lapg.gen.options.ast;

import org.textway.lapg.gen.options.OptdefTree.TextSource;

public interface IAstOptNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	//void accept(Visitor v);
}
