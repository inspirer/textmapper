package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.TemplatesTree.TextSource;

public class InstanceOfNode extends ExpressionNode {

	private final ExpressionNode expr;
	private final String pattern;

	public InstanceOfNode(ExpressionNode node, String pattern, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.expr = node;
		this.pattern = pattern;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object element = env.evaluate(expr, context, true);
		if(element != null) {
			return env.asObject(element).is(pattern);
		}
		return Boolean.FALSE;
	}


	@Override
	public void toString(StringBuilder sb) {
		expr.toString(sb);
		sb.append(" is ");
		sb.append(pattern);
	}
}
