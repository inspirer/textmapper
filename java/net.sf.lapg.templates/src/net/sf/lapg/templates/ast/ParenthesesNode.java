package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ITemplatesFacade;

public class ParenthesesNode extends ExpressionNode {

	private ExpressionNode expr;

	public ParenthesesNode(ExpressionNode expr, String input, int line) {
		super(input, line);
		this.expr = expr;
	}

	@Override
	public Object evaluate(EvaluationContext context, ITemplatesFacade env)
			throws EvaluationException {
		return env.evaluate(expr, context, true);
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append('(');
		expr.toString(sb);
		sb.append(')');
	}

}
