package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRegexp extends AstNode {

	private String regexp;

	public AstRegexp(String regexp, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.regexp = regexp;
	}
	
	public String getRegexp() {
		return regexp;
	}
}
