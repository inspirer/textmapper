package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.TextSource;

public class AstOption extends Node {

	private final String key;
	private final String value;

	public AstOption(String key, String value, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
