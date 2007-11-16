package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class EvalNode extends Node {

	private ExpressionNode expr;

	public EvalNode(ExpressionNode expr, int line) {
		super(line);
		this.expr = expr;
	}

	@Override
	protected void emit(StringBuffer sb, Object context,
			IEvaluationEnvironment env) {
		try {
			String templateCode = env.evaluate(expr, context, false).toString();
			sb.append(env.evaluateTemplate(this, templateCode, context));
		} catch( EvaluationException ex ) {
		}
	}
}
