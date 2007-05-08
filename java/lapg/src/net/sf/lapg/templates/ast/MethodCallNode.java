package net.sf.lapg.templates.ast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodCallNode extends ExpressionNode {
	
	ExpressionNode object;
	String methodName;
	Object[] arguments;
	
	public MethodCallNode(ExpressionNode object, String methodName, List arguments) {
		this.object = object;
		this.methodName = methodName;
		this.arguments = arguments != null && arguments.size() > 0 ? arguments.toArray() : null;
	}

	public Object resolve(Object context) {
		if( object != null )
			context = object.resolve(context);
		if( context == null )
			return null;
		
		Object[] args = null;
		Class[] argClasses = null;
		if( arguments != null ) {
			args = new Object[arguments.length];
			argClasses = new Class[arguments.length];
			for( int i = 0; i < arguments.length; i++ ) {
				if( arguments[i] instanceof ExpressionNode ) {
					args[i] = ((ExpressionNode)arguments[i]).resolve(context);
				} else {
					args[i] = arguments[i];
				}
				if( args[i] == null )
					return null;
				argClasses[i] = args[i].getClass();			
			}
			
		}
		try {
			Method meth = context.getClass().getMethod(methodName, argClasses);
			return meth.invoke(context, args);
		} catch( NoSuchMethodException ex ) {
		} catch( IllegalAccessException ex ) {
		} catch( InvocationTargetException ex ) {
		}
		return null;
	}
	
	
}
