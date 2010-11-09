package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.AstTree.TextSource;

public class CommaNode extends ExpressionNode {

	private final ExpressionNode leftExpr;
	private final ExpressionNode rightExpr;

	public CommaNode(ExpressionNode leftExpr, ExpressionNode rightExpr, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		env.evaluate(leftExpr, context, true);
		return env.evaluate(rightExpr, context, true);
	}

	@Override
	public void toString(StringBuilder sb) {
		leftExpr.toString(sb);
		sb.append(", ");
		rightExpr.toString(sb);
	}
}
