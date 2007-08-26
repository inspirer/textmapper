package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.api.IStaticMethods;

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

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object object;
		if( objectExpr != null ) {
			object = env.evaluate(objectExpr, context, false);
		} else {
			IStaticMethods staticMethods = env.getStaticMethods();
			if( staticMethods != null && staticMethods.isSupported(methodName) ) {
				object = staticMethods;
			} else {
				object = context;
			}
		}

		Object[] args = null;
		if( arguments != null ) {
			args = new Object[arguments.length];
			for( int i = 0; i < arguments.length; i++ ) {
				args[i] = env.evaluate(arguments[i], context, false);
			}
		}
		return env.callMethod(object, methodName, args);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if( objectExpr != null ) {
			sb.append(objectExpr.toString());
			sb.append('.');
		}
		sb.append(methodName);
		sb.append('(');
		if( arguments != null ) {
			for( int i = 0; i < arguments.length; i++ ) {
				if( i > 0) {
					sb.append(",");
				}
				sb.append(arguments[i].toString());
			}
		}
		sb.append(')');
		return sb.toString();
	}
}
