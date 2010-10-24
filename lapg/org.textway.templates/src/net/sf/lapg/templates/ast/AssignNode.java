package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.ast.AstTree.TextSource;

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
	public void toString(StringBuffer sb) {
		sb.append(identifier);
		sb.append(" = ");
		valueExpr.toString(sb);
	}
}
