package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.templates.api.ILocatedEntity;

public interface IAstNode extends ILocatedEntity {

	int getLine();

	int getOffset();

	int getEndOffset();

	TextSource getInput();

	void accept(AbstractVisitor v);
}
