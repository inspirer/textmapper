package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class AssertNode extends Node {

	private ExpressionNode expr;

	public AssertNode(ExpressionNode expr, int line) {
		super(line);
		this.expr = expr;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationEnvironment env) {
		try {
			Object res = env.evaluate(expr, context, true);
			Boolean b = env.toBoolean(res);
			if (!b.booleanValue()) {
				env.fireError(this, "Assertion `" + expr.toString() + "` failed for " + env.getTitle(context.getThisObject()));
			}
		} catch (EvaluationException ex) {
		}
	}
}
