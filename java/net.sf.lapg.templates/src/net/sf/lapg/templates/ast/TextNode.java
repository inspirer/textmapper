package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.ITemplatesFacade;

public class TextNode extends Node {
	private String text;

	public TextNode(String text, String input, int line) {
		super(input, line);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, ITemplatesFacade env) {
		sb.append(text);
	}
}
