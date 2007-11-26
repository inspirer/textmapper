package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class IndexNode extends ExpressionNode {

	private ExpressionNode objectExpr;
	private ExpressionNode indexExpr;

	public IndexNode(ExpressionNode objectExpr, ExpressionNode index, String input, int line) {
		super(input, line);
		this.objectExpr = objectExpr;
		this.indexExpr = index;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
		Object object = objectExpr != null ? env.evaluate(objectExpr, context, false) : context.getThisObject();
		Object index = env.evaluate(indexExpr, context, false);

		return env.getByIndex(object, index);
	}

	@Override
	public void toString(StringBuffer sb) {
		if( objectExpr == null ) {
			sb.append("this");
		} else {
			objectExpr.toString(sb);
		}
		sb.append("[");
		indexExpr.toString(sb);
		sb.append("]");
	}
}
