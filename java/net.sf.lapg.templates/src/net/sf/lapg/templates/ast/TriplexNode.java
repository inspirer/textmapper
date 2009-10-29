package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;


public class TriplexNode extends ExpressionNode {

	private final ExpressionNode elsenode;
	private final ExpressionNode thennode;
	private final ExpressionNode condition;

	protected TriplexNode(ExpressionNode condition, ExpressionNode thennode, ExpressionNode elsenode, String input, int line) {
		super(input, line);
		this.condition = condition;
		this.thennode = thennode;
		this.elsenode = elsenode;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object cond = env.evaluate(condition, context, true);
		if( env.toBoolean(cond) ) {
			return env.evaluate(thennode, context, false);
		} else {
			return env.evaluate(elsenode, context, false);
		}
	}

	@Override
	public void toString(StringBuffer sb) {
		condition.toString(sb);
		sb.append(" ? ");
		thennode.toString(sb);
		sb.append(" : ");
		elsenode.toString(sb);
	}

}
