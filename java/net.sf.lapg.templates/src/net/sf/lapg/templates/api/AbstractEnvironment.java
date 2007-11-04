package net.sf.lapg.templates.api;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.ast.ExpressionNode;

public abstract class AbstractEnvironment implements IEvaluationEnvironment {

	HashMap<String,Object> vars = new HashMap<String,Object>();

	public Object getVariable(String id) {
		return vars.get(id);
	}

	public void setVariable(String id, Object value) {
		vars.put(id, value);
	}

	public Object getProperty( Object obj, String id, boolean searchVars ) throws EvaluationException {
		if( searchVars ) {
			Object res = vars.get(id);
			if( res != null ) {
				return res;
			}
		}

		if( obj instanceof Map) {
			return ((Map)obj).get(id);
		} else if( id.equals("length") && obj instanceof Object[]) {
			return ((Object[])obj).length;
		} else {
			String getAccessor = "get" + Character.toUpperCase(id.charAt(0)) + id.substring(1);
			try {
				try {
					Field f = obj.getClass().getField(id);
					return f.get(obj);
				} catch( NoSuchFieldException ex ) {
				}

				Method meth = obj.getClass().getMethod(getAccessor);
				return meth.invoke(obj);
			} catch( NoSuchMethodException ex ) {
				throw new EvaluationException("nomethod: " + ex.toString() );
			} catch( IllegalAccessException ex ) {
				throw new EvaluationException("IllegalAccessException");
			} catch( InvocationTargetException ex ) {
				throw new EvaluationException("InvocationTargetException");
			}
		}
	}

	public Object callMethod( Object obj, String methodName, Object[] args ) throws EvaluationException {
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
			throw new EvaluationException("nomethod: " + ex.toString() );
		} catch( IllegalAccessException ex ) {
			throw new EvaluationException("IllegalAccessException");
		} catch( InvocationTargetException ex ) {
			throw new EvaluationException("InvocationTargetException");
		}
	}

	public Object getByIndex(Object obj, Object index) throws EvaluationException {
		if( obj instanceof Object[]) {
			Object[] array = (Object[])obj;
			if( index instanceof Integer ) {
				return array[(Integer)index];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		} else if( obj instanceof Map) {
			return ((Map)obj).get(index);
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

	public Object evaluate(ExpressionNode expr, Object context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if( result == null && !permitNull ) {
				String message = "Evaluation of `"+expr.toString()+"` failed for " + getContextTitle(context) + ": null";
				EvaluationException ex = new HandledEvaluationException(message);
				fireError(message);
				throw ex;
			}
			return result;
		} catch( HandledEvaluationException ex ) {
			throw ex;
		} catch( Throwable th ) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String message = "Evaluation of `"+expr.toString()+"` failed for " + getContextTitle(context) + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(message);
			throw ex;
		}
	}

	public String executeTemplate(String name, Object context, Object[] arguments) {
		return "";
	}

	public String getContextTitle(Object context) {
		if( context == null ) {
			return "<unknown>";
		}
		return context.getClass().getCanonicalName();
	}

	public void fireError(String error) {
		System.err.println(error);
	}

	public IStaticMethods getStaticMethods() {
		return null;
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}
}
