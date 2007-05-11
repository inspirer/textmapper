package net.sf.lapg.templates.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.ast.ExpressionNode;

public class DefaultEnvironment implements IEvaluationEnvironment {
	
	HashMap<String,Object> vars = new HashMap<String,Object>();

	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#getVariable(java.lang.String)
	 */
	public Object getVariable(String id) {
		return vars.get(id);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#setVariable(java.lang.String, java.lang.Object)
	 */
	public void setVariable(String id, Object value) {
		vars.put(id, value);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#getProperty(java.lang.Object, java.lang.String, boolean)
	 */
	public Object getProperty( Object obj, String id, boolean searchVars ) {
		if( searchVars ) {
			Object res = vars.get(id);
			if( res != null )
				return res;
		}

		if( obj instanceof Map) {
			return ((Map)obj).get(id);
		} else if( id.equals("length") && obj instanceof Object[]) {
			return ((Object[])obj).length;
		} else {
			String getAccessor = "get" + Character.toUpperCase(id.charAt(0)) + id.substring(1);
			try {
				Method meth = obj.getClass().getMethod(getAccessor);
				return meth.invoke(obj);
			} catch( NoSuchMethodException ex ) {
				throw new RuntimeException("NoSuchMethodException - " + getAccessor);
			} catch( IllegalAccessException ex ) {
				throw new RuntimeException("IllegalAccessException");
			} catch( InvocationTargetException ex ) {
				throw new RuntimeException("InvocationTargetException");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#callMethod(java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	public Object callMethod( Object obj, String methodName, Object[] args ) {
		try {
			Class[] argClasses = null;
			if( args != null ) {
				argClasses = new Class[args.length];
				for( int i = 0; i < args.length; i++ ) {
					argClasses[i] = args[i].getClass();			
				}				
			}
			Method meth = obj.getClass().getMethod(methodName, argClasses);
			return meth.invoke(obj, args);
		} catch( NoSuchMethodException ex ) {
			throw new RuntimeException("NoSuchMethodException - " + methodName);
		} catch( IllegalAccessException ex ) {
			throw new RuntimeException("IllegalAccessException");
		} catch( InvocationTargetException ex ) {
			throw new RuntimeException("InvocationTargetException");
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#getByIndex(java.lang.Object, java.lang.Object)
	 */
	public Object getByIndex(Object obj, Object index) {
		if( obj instanceof Object[]) {
			Object[] array = (Object[])obj;
			if( index instanceof Integer ) { 
				return array[(Integer)index];
			} else {
				throw new RuntimeException("index object should be integer");
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#toBoolean(java.lang.Object)
	 */
	public boolean toBoolean(Object o) {
		if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		} else if( o instanceof String ) {
			return ((String)o).length() > 0;
		}
		return o != null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#evaluate(net.sf.lapg.templates.ast.ExpressionNode, java.lang.Object)
	 */
	public Object evaluate(ExpressionNode expr, Object context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if( result == null && !permitNull ) {
				EvaluationException ex = new EvaluationException(expr, context, "null", null);
				fireError(ex.getMessage());
				throw ex;
			}
			return result;
		} catch( EvaluationException ex ) {
			throw ex;
		} catch( Throwable th ) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			EvaluationException ex = new EvaluationException(expr, context, cause.getMessage(), cause);
			fireError(ex.getMessage());
			throw ex;
		}
	}

	public String executeTemplate(String name, Object context, Object[] arguments) {
		return "";
	}

	/* (non-Javadoc)
	 * @see net.sf.lapg.templates.IEvaluationEnvironment#fireError(java.lang.String)
	 */
	public void fireError(String error) {
		System.err.println(error);
	}
}
