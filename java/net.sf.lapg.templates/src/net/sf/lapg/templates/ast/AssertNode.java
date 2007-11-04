package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class AssertNode extends Node {

	private ExpressionNode expr;

	public AssertNode(ExpressionNode expr) {
		this.expr = expr;
	}

	@Override
	protected void emit(StringBuffer sb, Object context,
			IEvaluationEnvironment env) {
		try {
			Object res = env.evaluate(expr, context, true);
			Boolean b = env.toBoolean(res);
			if( !b.booleanValue() ) {
				env.fireError("Assertion `"+expr.toString()+"` failed for " + env.getContextTitle(context));
			}
		} catch( EvaluationException ex ) {
		}
	}
}
