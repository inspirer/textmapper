package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.TextSource;

public class AstIdentifier extends Node {

	private String name;

	public AstIdentifier(String name, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
