package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstCode extends AstNode {

	public AstCode(TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
	}
}
