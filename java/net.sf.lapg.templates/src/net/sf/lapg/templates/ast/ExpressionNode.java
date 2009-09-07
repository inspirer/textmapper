package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ITemplatesFacade;

public abstract class ExpressionNode extends Node {

	protected ExpressionNode(String input, int line) {
		super(input, line);
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, ITemplatesFacade env) {
		try {
			sb.append(env.toString(env.evaluate(this, context, false), this));
		} catch( EvaluationException ex ) {
		}
	}

	public abstract Object evaluate(EvaluationContext context, ITemplatesFacade env) throws EvaluationException;

	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	public abstract void toString(StringBuffer sb);
}