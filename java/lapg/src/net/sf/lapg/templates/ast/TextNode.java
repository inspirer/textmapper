package net.sf.lapg.templates.ast;

public class TextNode extends Node {
	String text;

	public TextNode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	protected void emit(StringBuffer sb, Object context) {
		sb.append(text);
	}
}
