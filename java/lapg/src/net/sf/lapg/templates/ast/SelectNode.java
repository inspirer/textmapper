package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;

public class SelectNode extends ExpressionNode {

	ExpressionNode objectExpr;
	String identifier;
	
	public SelectNode(ExpressionNode objectExpr, String identifier) {
		this.objectExpr = objectExpr;
		this.identifier = identifier;
	}

	public Object resolve(Object context, ExecutionEnvironment env) {
		Object object;
		if( objectExpr != null ) {
			object = objectExpr.resolve(context, env);
			if( object == null )
				return null;
		} else {
			object = context;
		}

		return env.getProperty(object, identifier, objectExpr == null);
	}
}
