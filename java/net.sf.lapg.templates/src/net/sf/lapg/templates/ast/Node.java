package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.ITemplatesFacade;
import net.sf.lapg.templates.api.ILocatedEntity;

public abstract class Node implements ILocatedEntity {

	private String input;
	private int line;

	protected Node(String input, int line) {
		this.input = input;
		this.line = line;
	}

	protected abstract void emit( StringBuffer sb, EvaluationContext context, ITemplatesFacade env);

	public String getLocation() {
		return input + "," + line;
	}
}
