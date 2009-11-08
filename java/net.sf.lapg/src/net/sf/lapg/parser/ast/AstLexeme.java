package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstLexeme extends Node implements AstLexerPart {

	private AstIdentifier name;
	private String type;
	private AstRegexp regexp;
	private int priority;
	private AstCode code;

	public AstLexeme(AstIdentifier name, String type, AstRegexp regexp, Integer priority, AstCode code,
			TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.regexp = regexp;
		this.priority = priority;
		this.code = code;
	}

	public AstIdentifier getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public AstRegexp getRegexp() {
		return regexp;
	}

	public int getPriority() {
		return priority;
	}

	public AstCode getCode() {
		return code;
	}
}
