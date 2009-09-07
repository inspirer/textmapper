package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ITemplatesFacade;

public class UnaryExpression extends ExpressionNode {

	public static final int NOT = 1;
	public static final int MINUS = 2;

	private static String[] operators = new String[] { "", "! ", "- " };

	private int kind;
	private ExpressionNode expr;

	public UnaryExpression(int kind, ExpressionNode expr, String input, int line) {
		super(input, line);
		this.kind = kind;
		this.expr = expr;
	}

	@Override
	public Object evaluate(EvaluationContext context, ITemplatesFacade env)
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
