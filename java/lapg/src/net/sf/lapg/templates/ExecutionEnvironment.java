package net.sf.lapg.templates;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.ast.ExpressionNode;

public class ExecutionEnvironment {
	
	HashMap<String,Object> vars = new HashMap<String,Object>();

	public Object getVariable(String id) {
		return vars.get(id);
	}
	
	public void setVariable(String id, Object value) {
		vars.put(id, value);
	}
	
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
	
	public boolean toBoolean(Object o) {
		if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		} else if( o instanceof String ) {
			return ((String)o).length() > 0;
		}
		return o != null;
	}
	
	public Object evaluate(ExpressionNode expr, Object context) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if( result == null ) {
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
	
	public void fireError(String error) {
		System.err.println(error);
	}
}
