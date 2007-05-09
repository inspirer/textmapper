package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;
import net.sf.lapg.templates.EvaluationException;

public class SelectNode extends ExpressionNode {

	ExpressionNode objectExpr;
	String identifier;
	
	public SelectNode(ExpressionNode objectExpr, String identifier) {
		this.objectExpr = objectExpr;
		this.identifier = identifier;
	}

	public Object evaluate(Object context, ExecutionEnvironment env) throws EvaluationException {
		Object object;
		if( objectExpr != null ) {
			object = env.evaluate(objectExpr, context);
			if( object == null )
				return null;
		} else {
			object = context;
		}

		return env.getProperty(object, identifier, objectExpr == null);
	}

	public String toString() {
		if( objectExpr != null )
			return objectExpr.toString() + "." + identifier;
		else
			return identifier;
	}
}
