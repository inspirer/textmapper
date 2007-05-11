package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class TextNode extends Node {
	String text;

	public TextNode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		sb.append(text);
	}
}
