package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;


public class IndexNode extends ExpressionNode {

	ExpressionNode objectExpr;
	ExpressionNode indexExpr;

	public IndexNode(ExpressionNode objectExpr, ExpressionNode index) {
		this.objectExpr = objectExpr;
		this.indexExpr = index;
	}

	public Object resolve(Object context, ExecutionEnvironment env) {
		Object object = objectExpr.resolve(context, env);
		if( object == null )
			return null;

		Object index = indexExpr.resolve(context, env);

		return env.getByIndex(context, index);
	}
}
