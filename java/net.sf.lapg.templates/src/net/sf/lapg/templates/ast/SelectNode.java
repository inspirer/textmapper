package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class SelectNode extends ExpressionNode {

	ExpressionNode objectExpr;
	String identifier;

	public SelectNode(ExpressionNode objectExpr, String identifier) {
		this.objectExpr = objectExpr;
		this.identifier = identifier;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object object;
		if( objectExpr != null ) {
			object = env.evaluate(objectExpr, context, false);
		} else {
			object = context;
		}

		return env.getProperty(object, identifier, objectExpr == null);
	}

	@Override
	public String toString() {
		if( objectExpr != null ) {
			return objectExpr.toString() + "." + identifier;
		} else {
			return identifier;
		}
	}
}
