/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.templates.objects;

import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.SourceElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

public class DefaultJavaIxObject implements IxAdaptable, IxObject, IxWrapper {

	protected final Object wrapped;

	public DefaultJavaIxObject(Object wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Object castTo(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("cannot cast");
	}

	@Override
	public String asString() throws EvaluationException {
		if (wrapped instanceof Collection<?> || wrapped instanceof Object[]) {
			throw new EvaluationException("evaluation results in collection, cannot convert to String");
		}
		return wrapped.toString();
	}

	@Override
	public boolean asBoolean() {
		if (wrapped instanceof Boolean) {
			return (Boolean) wrapped;
		} else if (wrapped instanceof String) {
			return ((String) wrapped).trim().length() > 0;
		}
		return wrapped != null;
	}

	@Override
	public Iterator asSequence() {
		if (wrapped instanceof Iterable<?>) {
			return ((Iterable<?>) wrapped).iterator();
		}
		if (wrapped instanceof Object[]) {
			return new JavaArrayAdapter((Object[]) wrapped).iterator();
		}
		return null;
	}

	@Override
	public Object getProperty(SourceElement caller, String id) throws EvaluationException {
		try {
			String getAccessor = "get" + Character.toUpperCase(id.charAt(0)) + id.substring(1);
			Method meth = wrapped.getClass().getMethod(getAccessor);
			meth.setAccessible(true);
			return meth.invoke(wrapped);
		} catch (NoSuchMethodException ex) {
			throw new EvaluationException("symbol `" + id + "` is undefined");
		} catch (IllegalAccessException ex) {
			throw new EvaluationException("IllegalAccessException");
		} catch (InvocationTargetException ex) {
			String message = ex.getMessage();
			if (ex.getCause() != null) {
				message = "(caused by " + ex.getCause().getClass().getCanonicalName() + "): " + message;
			}
			throw new EvaluationException(message);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
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
			String message = ex.getMessage();
			if (ex.getCause() != null) {
				message = "(caused by " + ex.getCause().getClass().getCanonicalName() + "): " + message;
			}
			throw new EvaluationException(message);
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
					if (paramTypes[i].isAssignableFrom(argClasses[i])) continue;
					if (paramTypes[i].isPrimitive() && paramTypes[i].getName().equals("int")
							&& argClasses[i].getName().equals("java.lang.Integer")) continue;
					good = false;
					break;
				}
				if (good) {
					return m;
				}
			}
		}
		return null;
	}

	@Override
	public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
		throw new EvaluationException("do not know how to apply index");
	}

	@Override
	public boolean is(String pattern) throws EvaluationException {
		return JavaIsInstanceUtil.isInstance(getObject(), pattern);
	}

	@Override
	public Object getObject() {
		return wrapped;
	}
}
