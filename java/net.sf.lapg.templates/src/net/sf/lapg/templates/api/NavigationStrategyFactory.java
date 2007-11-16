package net.sf.lapg.templates.api;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class NavigationStrategyFactory {

	public INavigationStrategy getStrategy(Object o) {

		if( o instanceof Object[] ) {
			return arrayNavigation;
		}

		if( o instanceof Map ) {
			return mapNavigation;
		}

		if( o instanceof String ) {
			//return stringNavigation;
		}

		if( o instanceof Collection ) {
			//return collectionNavigation;
		}

		return javaNavigation;
	}

	private INavigationStrategy javaNavigation = new INavigationStrategy() {

		public Object getProperty(Object obj, String id) throws EvaluationException {
			String getAccessor = "get" + Character.toUpperCase(id.charAt(0)) + id.substring(1);
			try {
				try {
					Field f = obj.getClass().getField(id);
					return f.get(obj);
				} catch (NoSuchFieldException ex) {
				}

				Method meth = obj.getClass().getMethod(getAccessor);
				return meth.invoke(obj);
			} catch (NoSuchMethodException ex) {
				throw new EvaluationException("nomethod: " + ex.toString());
			} catch (IllegalAccessException ex) {
				throw new EvaluationException("IllegalAccessException");
			} catch (InvocationTargetException ex) {
				throw new EvaluationException("InvocationTargetException");
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
			throw new EvaluationException("do not know how to apply index");
		}

		public Object getByQuery(Object obj, String query) throws EvaluationException {
			throw new EvaluationException("do not know how to execute query");
		}
	};

	private INavigationStrategy mapNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Object obj, Object index) throws EvaluationException {
			return ((Map<?,?>)obj).get(index);
		}

		public Object getByQuery(Object obj, String query) throws EvaluationException {
			return ((Map<?,?>)obj).get(query);
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			return ((Map<?, ?>)obj).get(id);
		}
	};

	private INavigationStrategy arrayNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			Object[] array = (Object[]) obj;
			if( args == null ) {
				if( methodName.equals("first") ) {
					return array[0];
				} else if( methodName.equals("last") ) {
					return array[array.length-1];
				} else if( methodName.equals("size") ) {
					return array.length;
				}
			}
			throw new EvaluationException("do not know method `"+methodName+"`");
		}

		public Object getByIndex(Object obj, Object index) throws EvaluationException {
			Object[] array = (Object[])obj;
			if( index instanceof Integer ) {
				return array[(Integer)index];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getByQuery(Object obj, String query) throws EvaluationException {
			throw new EvaluationException("do not know how to execute query");
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			if( id.equals("length") ) {
				return ((Object[])obj).length;
			}
			throw new EvaluationException("do not know property `"+id+"`");
		}
	};
}
