package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class MethodCallNode extends ExpressionNode {

	private ExpressionNode objectExpr;
	private String methodName;
	private ExpressionNode[] arguments;

	public MethodCallNode(ExpressionNode objectExpr, String methodName, List<ExpressionNode> arguments, int line) {
		super(line);
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
			object = context;
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
	public void toString(StringBuffer sb) {
		if( objectExpr != null ) {
			objectExpr.toString(sb);
			sb.append('.');
		}
		sb.append(methodName);
		sb.append('(');
		if( arguments != null ) {
			for( int i = 0; i < arguments.length; i++ ) {
				if( i > 0) {
					sb.append(",");
				}
				arguments[i].toString(sb);
			}
		}
		sb.append(')');
	}
}
