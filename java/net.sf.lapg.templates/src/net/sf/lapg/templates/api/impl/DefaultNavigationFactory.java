package net.sf.lapg.templates.api.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.INavigationStrategy;

public class DefaultNavigationFactory implements INavigationStrategy.Factory {

	protected IEvaluationStrategy evaluationStrategy;

	public void setEvaluationStrategy(IEvaluationStrategy strategy) {
		this.evaluationStrategy = strategy;
	}

	public INavigationStrategy<?> getStrategy(Object o) {

		if (o instanceof Object[]) {
			return arrayNavigation;
		}

		if (o instanceof int[]) {
			return intArrayNavigation;
		}

		if (o instanceof Map<?, ?>) {
			return mapNavigation;
		}

		if (o instanceof String) {
			// return stringNavigation;
		}

		if (o instanceof Collection<?>) {
			return collectionNavigation;
		}

		return javaNavigation;
	}

	protected INavigationStrategy<Object> javaNavigation = new INavigationStrategy<Object>() {

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
		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			try {
				Class[] argClasses = null;
				if (args != null) {
					argClasses = new Class[args.length];
					for (int i = 0; i < args.length; i++) {
						argClasses[i] = args[i].getClass();
					}
				}
				Method meth = null;
				try {
					meth = obj.getClass().getMethod(methodName, argClasses);
				} catch (NoSuchMethodException ex) {
					meth = searchMethod(obj.getClass(), methodName, argClasses);
					if (meth == null) {
						throw new EvaluationException("no method: " + ex.toString());
					}
				}
				return meth.invoke(obj, args);
			} catch (IllegalAccessException ex) {
				throw new EvaluationException("IllegalAccessException");
			} catch (InvocationTargetException ex) {
				throw new EvaluationException("InvocationTargetException");
			}
		}

		private Method searchMethod(Class<?> class1, String methodName, Class<?>[] argClasses) {
			for (Method m : class1.getMethods()) {
				if (m.getName().equals(methodName)) {
					Class<?>[] paramTypes = m.getParameterTypes();
					if (paramTypes.length != argClasses.length) {
						continue;
					}
					boolean good = true;
					for (int i = 0; i < paramTypes.length; i++) {
						if (!paramTypes[i].isAssignableFrom(argClasses[i])) {
							good = false;
							break;
						}
					}
					if (good) {
						return m;
					}
				}
			}
			return null;
		}

		public Object getByIndex(Object obj, Object index) throws EvaluationException {
			throw new EvaluationException("do not know how to apply index");
		}
	};

	private final INavigationStrategy<Map<?, ?>> mapNavigation = new INavigationStrategy<Map<?, ?>>() {

		public Object callMethod(Map<?, ?> obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Map<?, ?> obj, Object index) throws EvaluationException {
			return obj.get(index);
		}

		public Object getProperty(Map<?, ?> obj, String id) throws EvaluationException {
			return obj.get(id);
		}
	};

	private final INavigationStrategy<Collection<?>> collectionNavigation = new INavigationStrategy<Collection<?>>() {

		public Object callMethod(Collection<?> cl, String methodName, Object[] args) throws EvaluationException {
			if (args == null) {
				if (methodName.equals("first")) {
					return cl.isEmpty() ? null : cl.iterator().next();
				}
			}
			return javaNavigation.callMethod(cl, methodName, args);
		}

		public Object getByIndex(Collection<?> cl, Object index) throws EvaluationException {
			ArrayList<?> array = (ArrayList<?>) cl; // FIXME support collection
													// by index
			if (index instanceof Integer) {
				return array.get((Integer) index);
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Collection<?> cl, String id) throws EvaluationException {
			if (id.equals("length")) {
				return cl.size();
			}
			throw new EvaluationException("do not know property `" + id + "`");
		}
	};

	private final INavigationStrategy<Object[]> arrayNavigation = new INavigationStrategy<Object[]>() {

		public Object callMethod(Object[] array, String methodName, Object[] args) throws EvaluationException {
			if (args == null) {
				if (methodName.equals("first")) {
					return array[0];
				} else if (methodName.equals("last")) {
					return array[array.length - 1];
				} else if (methodName.equals("size")) {
					return array.length;
				}
			} else if(args.length == 1) {
				if (methodName.equals("contains")) {
					return Arrays.asList(array).contains(args[0]);
				}
			}
			throw new EvaluationException("do not know method `" + methodName + "`");
		}

		public Object getByIndex(Object[] array, Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				if (i < 0 || i >= array.length) {
					throw new EvaluationException(i + " is out of 0.." + (array.length - 1));
				}
				return array[i];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Object[] array, String id) throws EvaluationException {
			if (id.equals("length")) {
				return array.length;
			}
			throw new EvaluationException("do not know property `" + id + "`");
		}
	};

	private final INavigationStrategy<int[]> intArrayNavigation = new INavigationStrategy<int[]>() {

		public Object callMethod(int[] array, String methodName, Object[] args) throws EvaluationException {
			if (args == null) {
				if (methodName.equals("first")) {
					return array[0];
				} else if (methodName.equals("last")) {
					return array[array.length - 1];
				} else if (methodName.equals("size")) {
					return array.length;
				}
			}
			throw new EvaluationException("do not know method `" + methodName + "`");
		}

		public Object getByIndex(int[] array, Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				if (i < 0 || i >= array.length) {
					throw new EvaluationException(i + " is out of 0.." + (array.length - 1));
				}
				return array[i];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(int[] array, String id) throws EvaluationException {
			if (id.equals("length")) {
				return array.length;
			}
			throw new EvaluationException("do not know property `" + id + "`");
		}
	};
}
