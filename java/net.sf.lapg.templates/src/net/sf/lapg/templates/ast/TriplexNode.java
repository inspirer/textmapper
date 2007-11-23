package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class TriplexNode extends ExpressionNode {

	private final ExpressionNode elsenode;
	private final ExpressionNode thennode;
	private final ExpressionNode condition;

	protected TriplexNode(ExpressionNode condition, ExpressionNode thennode, ExpressionNode elsenode, int line) {
		super(line);
		this.condition = condition;
		this.thennode = thennode;
		this.elsenode = elsenode;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object cond = env.evaluate(condition, context, false);
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
