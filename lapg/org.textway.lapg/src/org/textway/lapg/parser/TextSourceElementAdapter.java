package org.textway.lapg.parser;

import org.textway.lapg.parser.ast.IAstNode;

public class TextSourceElementAdapter implements TextSourceElement {

	private final IAstNode node;

	public TextSourceElementAdapter(IAstNode node) {
		this.node = node;
	}

	@Override
	public String getResourceName() {
		return node.getInput().getFile();
	}

	@Override
	public int getOffset() {
		return node.getOffset();
	}

	@Override
	public int getEndOffset() {
		return node.getEndOffset();
	}

	@Override
	public int getLine() {
		return node.getLine();
	}

	@Override
	public String getText() {
		return node.getInput().getText(node.getOffset(), node.getEndOffset());
	}

	@Override
	public String toString() {
		return getText();
	}
}
