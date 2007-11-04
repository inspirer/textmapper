package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class TextNode extends Node {
	private String text;

	public TextNode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		sb.append(text);
	}
}
