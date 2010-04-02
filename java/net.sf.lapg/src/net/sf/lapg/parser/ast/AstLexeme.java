package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstLexeme extends AstNode implements AstLexerPart {

	private final AstIdentifier name;
	private final String type;
	private final AstRegexp regexp;
	private final int priority;
	private final AstCode code;

	public AstLexeme(AstIdentifier name, String type, AstRegexp regexp,
			Integer priority, AstCode code, TextSource source, int offset,
			int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.regexp = regexp;
		this.priority = priority != null ? priority : 0;
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

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (name != null) {
			name.accept(v);
		}
		if (regexp != null) {
			regexp.accept(v);
		}
		if (code != null) {
			code.accept(v);
		}
	}
}
