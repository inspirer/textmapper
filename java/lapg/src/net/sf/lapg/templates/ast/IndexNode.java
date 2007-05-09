package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;
import net.sf.lapg.templates.EvaluationException;


public class IndexNode extends ExpressionNode {

	ExpressionNode objectExpr;
	ExpressionNode indexExpr;

	public IndexNode(ExpressionNode objectExpr, ExpressionNode index) {
		this.objectExpr = objectExpr;
		this.indexExpr = index;
	}

	public Object evaluate(Object context, ExecutionEnvironment env) throws EvaluationException {
		Object object = env.evaluate(objectExpr, context);
		if( object == null )
			return null;

		Object index = env.evaluate(indexExpr, context);

		return env.getByIndex(context, index);
	}

	public String toString() {
		return objectExpr.toString() + "[" + indexExpr.toString() + "]";
	}
}
