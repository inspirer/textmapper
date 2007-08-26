package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class IndexNode extends ExpressionNode {

	ExpressionNode objectExpr;
	ExpressionNode indexExpr;

	public IndexNode(ExpressionNode objectExpr, ExpressionNode index) {
		this.objectExpr = objectExpr;
		this.indexExpr = index;
	}

	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object object = env.evaluate(objectExpr, context, false);
		Object index = env.evaluate(indexExpr, context, false);

		return env.getByIndex(object, index);
	}

	public String toString() {
		return objectExpr.toString() + "[" + indexExpr.toString() + "]";
	}
}
