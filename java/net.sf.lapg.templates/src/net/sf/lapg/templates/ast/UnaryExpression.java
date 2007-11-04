package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class UnaryExpression extends ExpressionNode {

	public static final int NOT = 1;
	public static final int MINUS = 2;

	private static String[] operators = new String[] { "", "! ", "- " };

	private int kind;
	private ExpressionNode expr;

	public UnaryExpression(int kind, ExpressionNode expr) {
		this.kind = kind;
		this.expr = expr;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env)
			throws EvaluationException {
		if( kind == NOT ) {
			Object value = env.evaluate(expr, context, true);
			return !env.toBoolean(value);
		}

		if( kind == MINUS ) {
			Object value = env.evaluate(expr, context, false);
			if( value instanceof Integer ) {
				return -((Integer)value).intValue();
			} else {
				throw new EvaluationException("unary minus expression should be Integer");
			}
		}

		throw new EvaluationException("internal error: unknown kind");
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append(operators[kind]);
		expr.toString(sb);
	}
}
