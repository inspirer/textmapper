package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class ListNode extends ExpressionNode {

	private ExpressionNode[] expressions;

	public ListNode(List<ExpressionNode> expressions, String input, int line) {
		super(input, line);
		this.expressions = expressions != null && expressions.size() > 0 ? (ExpressionNode[]) expressions
				.toArray(new ExpressionNode[expressions.size()]) : null;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object[] result = null;
		if( expressions != null ) {
			result = new Object[expressions.length];
			for( int i = 0; i < expressions.length; i++ ) {
				result[i] = env.evaluate(expressions[i], context, false);
			}
		} else {
			result = new Object[0];
		}
		return result;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append('[');
		if( expressions != null ) {
			for( int i = 0; i < expressions.length; i++ ) {
				if( i > 0) {
					sb.append(",");
				}
				expressions[i].toString(sb);
			}
		}
		sb.append(']');
	}
}
