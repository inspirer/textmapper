/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.templates.objects;

import org.textway.templates.api.EvaluationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

public class DefaultJavaIxObject implements IxAdaptable, IxObject, IxWrapper {

	protected final Object wrapped;

	public DefaultJavaIxObject(Object wrapped) {
		this.wrapped = wrapped;
	}

	public Object castTo(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("cannot cast");
	}

	public String asString() throws EvaluationException {
		if (wrapped instanceof Collection<?> || wrapped instanceof Object[]) {
			throw new EvaluationException("evaluation results in collection, cannot convert to String");
		}
		return wrapped.toString();
	}

	public boolean asBoolean() {
		if (wrapped instanceof Boolean) {
			return ((Boolean) wrapped).booleanValue();
		} else if (wrapped instanceof String) {
			return ((String) wrapped).trim().length() > 0;
		}
		return wrapped != null;
	}

	public Iterator asSequence() {
		if (wrapped instanceof Iterable<?>) {
			return ((Iterable<?>) wrapped).iterator();
		}
		if (wrapped instanceof Object[]) {
			return new JavaArrayAdapter((Object[]) wrapped).iterator();
		}
		return null;
	}

	public Object getProperty(String id) throws EvaluationException {
		String getAccessor = "get" + Character.toUpperCase(id.charAt(0)) + id.substring(1);
		try {
			try {
				Field f = wrapped.getClass().getField(id);
				return f.get(wrapped);
			} catch (NoSuchFieldException ex) {
			}

			Method meth = wrapped.getClass().getMethod(getAccessor);
			return meth.invoke(wrapped);
		} catch (NoSuchMethodException ex) {
			throw new EvaluationException("symbol `" + id + "` is undefined");
		} catch (IllegalAccessException ex) {
			throw new EvaluationException("IllegalAccessException");
		} catch (InvocationTargetException ex) {
			throw new EvaluationException("InvocationTargetException");
		}
	}

	@SuppressWarnings("unchecked")
	public Object callMethod(String methodName, Object... args) throws EvaluationException {
		try {
			Class[] argClasses = null;
			if (args != null) {
				argClasses = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					argClasses[i] = args[i].getClass();
				}
			}
			Method meth;
			try {
				meth = wrapped.getClass().getMethod(methodName, argClasses);
			} catch (NoSuchMethodException ex) {
				meth = searchMethod(wrapped.getClass(), methodName, argClasses);
				if (meth == null) {
					throw new EvaluationException("no method: " + ex.toString());
				}
			}
			meth.setAccessible(true);
			return meth.invoke(wrapped, args);
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

	public Object getByIndex(Object index) throws EvaluationException {
		throw new EvaluationException("do not know how to apply index");
	}

	public boolean is(String pattern) throws EvaluationException {
		return JavaIsInstanceUtil.isInstance(getObject(), pattern);
	}

	public Object getObject() {
		return wrapped;
	}
}
