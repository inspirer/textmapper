package net.sf.lapg.templates;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExecutionEnvironment {
	
	HashMap<String,Object> vars;

	
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
			} catch( IllegalAccessException ex ) {
			} catch( InvocationTargetException ex ) {
			}
		}
		return null;
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
		} catch( IllegalAccessException ex ) {
		} catch( InvocationTargetException ex ) {
		}
		return null;
	}
	
	public Object getByIndex(Object obj, Object index) {
		if( obj instanceof Object[]) { 
			if( index instanceof Integer )
				return ((Object[])obj)[(Integer)index];
			else
				; // fire error
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
}
