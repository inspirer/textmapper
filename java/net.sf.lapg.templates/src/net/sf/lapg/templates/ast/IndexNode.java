package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class IndexNode extends ExpressionNode {

	private ExpressionNode objectExpr;
	private ExpressionNode indexExpr;

	public IndexNode(ExpressionNode objectExpr, ExpressionNode index, int line) {
		super(line);
		this.objectExpr = objectExpr;
		this.indexExpr = index;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object object = env.evaluate(objectExpr, context, false);
		Object index = env.evaluate(indexExpr, context, false);

		return env.getByIndex(object, index);
	}

	@Override
	public void toString(StringBuffer sb) {
		objectExpr.toString(sb);
		sb.append("[");
		indexExpr.toString(sb);
		sb.append("]");
	}
}
