package net.sf.lapg.templates.api.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.INavigationStrategy;

public class DefaultNavigationFactory implements INavigationStrategy.Factory {

	protected IEvaluationStrategy templatesFacade;

	public void setTemplatesFacade(IEvaluationStrategy facade) {
		this.templatesFacade = facade;
	}

	public INavigationStrategy getStrategy(Object o) {

		if( o instanceof Object[] ) {
			return arrayNavigation;
		}

		if( o instanceof int[] ) {
			return intArrayNavigation;
		}
		
		if( o instanceof Map<?, ?> ) {
			return mapNavigation;
		}

		if( o instanceof String ) {
			//return stringNavigation;
		}

		if( o instanceof Collection<?> ) {
			return collectionNavigation;
		}

		return javaNavigation;
	}

	protected INavigationStrategy javaNavigation = new INavigationStrategy() {

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

		@SuppressWarnings("unchecked")
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
	};

	private INavigationStrategy mapNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Object obj, Object index) throws EvaluationException {
			return ((Map<?,?>)obj).get(index);
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			return ((Map<?, ?>)obj).get(id);
		}
	};

	private INavigationStrategy collectionNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			Collection<?> cl = (Collection<?>) obj;
			if( args == null ) {
				if( methodName.equals("first") ) {
					return cl.isEmpty() ? null : cl.iterator().next();
				}
			}
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Object obj, Object index) throws EvaluationException {
			ArrayList<?> array = (ArrayList<?>)obj;
			if( index instanceof Integer ) {
				return array.get((Integer)index);
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			if( id.equals("length") ) {
				return ((Collection<?>)obj).size();
			}
			throw new EvaluationException("do not know property `"+id+"`");
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
				int i = (Integer)index;
				if( i < 0 || i >= array.length ) {
					throw new EvaluationException(i + " is out of 0.." + (array.length-1));
				}
				return array[i];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			if( id.equals("length") ) {
				return ((Object[])obj).length;
			}
			throw new EvaluationException("do not know property `"+id+"`");
		}
	};

	private INavigationStrategy intArrayNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			int[] array = (int[]) obj;
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
			int[] array = (int[])obj;
			if( index instanceof Integer ) {
				int i = (Integer)index;
				if( i < 0 || i >= array.length ) {
					throw new EvaluationException(i + " is out of 0.." + (array.length-1));
				}
				return array[i];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			if( id.equals("length") ) {
				return ((int[])obj).length;
			}
			throw new EvaluationException("do not know property `"+id+"`");
		}
	};
}
