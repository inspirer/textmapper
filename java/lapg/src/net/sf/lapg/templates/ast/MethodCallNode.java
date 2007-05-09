package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.ExecutionEnvironment;

public class MethodCallNode extends ExpressionNode {
	
	ExpressionNode objectExpr;
	String methodName;
	ExpressionNode[] arguments;
	
	public MethodCallNode(ExpressionNode objectExpr, String methodName, List<ExpressionNode> arguments) {
		this.objectExpr = objectExpr;
		this.methodName = methodName;
		this.arguments = arguments != null && arguments.size() > 0 ? (ExpressionNode[]) arguments
				.toArray(new ExpressionNode[arguments.size()]) : null;
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

		Object[] args = null;
		if( arguments != null ) {
			args = new Object[arguments.length];
			for( int i = 0; i < arguments.length; i++ ) {
				args[i] = arguments[i].resolve(context, env);
				if( args[i] == null )
					return null;
			}
		}
		return env.callMethod(object, methodName, args);
	}
	
	
}
