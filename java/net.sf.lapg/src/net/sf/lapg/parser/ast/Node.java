package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.templates.api.ILocatedEntity;

public abstract class Node implements ILocatedEntity {

	private final TextSource source;
	private final int offset, endoffset;

	public Node(TextSource source, int offset, int endoffset) {
		this.source = source;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	public String getLocation() {
		return source.getLocation(offset);
	}

	@Override
	public String toString() {
		return source.getText(offset, endoffset);
	}

	public void accept(Visitor v) { };
}
