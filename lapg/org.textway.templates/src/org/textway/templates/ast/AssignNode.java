package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.AstTree.TextSource;

public class AssignNode extends ExpressionNode {

	private final String identifier;
	private final ExpressionNode valueExpr;

	public AssignNode(String identifier, ExpressionNode value, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.identifier = identifier;
		this.valueExpr = value;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object value = env.evaluate(valueExpr, context, true);
		context.setVariable(identifier, value);
		return value;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(identifier);
		sb.append(" = ");
		valueExpr.toString(sb);
	}
}
