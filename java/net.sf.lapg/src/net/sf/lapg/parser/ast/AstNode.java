package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public abstract class AstNode implements IAstNode {

	private final TextSource source;
	private final int offset, endoffset;

	public AstNode(TextSource source, int offset, int endoffset) {
		this.source = source;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	public String getLocation() {
		return source.getLocation(offset);
	}
	
	public int getLine() {
		return source.lineForOffset(offset);
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getEndOffset() {
		return endoffset;
	}
	
	public TextSource getInput() {
		return source;
	}

	@Override
	public String toString() {
		return source.getText(offset, endoffset);
	}
}
