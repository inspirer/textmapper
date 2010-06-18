package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.ast.AstTree.TextSource;

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
	public void toString(StringBuffer sb) {
		leftExpr.toString(sb);
		sb.append(", ");
		rightExpr.toString(sb);
	}
}
