package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstOption extends AstNode {

	private final String key;
	private final Object value;

	public AstOption(String key, Object value, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}
}
