package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstReference extends AstNode {

	private final String name;

	public AstReference(String name, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void accept(AbstractVisitor v) {
		v.visit(this);
	}
}
