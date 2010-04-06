package net.sf.lapg.parser;

import net.sf.lapg.api.LocatedEntity;
import net.sf.lapg.parser.ast.IAstNode;
import net.sf.lapg.templates.api.ILocatedEntity;

public class LiEntity implements LocatedEntity, ILocatedEntity {

	private final IAstNode node;

	public LiEntity(IAstNode node) {
		this.node = node;
	}

	public int getOffset() {
		return node == null ? 0 : node.getOffset();
	}

	public int getEndOffset() {
		return node == null ? 0 : node.getEndOffset();
	}

	public int getLine() {
		return node == null ? 1 : node.getLine();
	}

	public String getResourceName() {
		return node == null ? null : node.getInput().getFile();
	}

	public String getLocation() {
		return node == null ? "<unknown>" : getResourceName() + "," + getLine();
	}
}
