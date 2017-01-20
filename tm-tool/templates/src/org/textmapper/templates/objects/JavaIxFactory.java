/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.api.SourceElement;

import java.util.*;
import java.util.stream.Collectors;

public class JavaIxFactory implements IxFactory {

	@Override
	public IxObject asObject(Object o) {
		if (o instanceof IxObject) {
			return (IxObject) o;
		}
		if (o instanceof IxWrapper) {
			o = ((IxWrapper)o).getObject();
		}
		if (o instanceof Map) {
			return new JavaMapIxObject((Map) o);
		}
		if (o instanceof int[]) {
			return new JavaIntArrayIxObject((int[]) o);
		}
		if (o instanceof short[]) {
			return new JavaShortArrayIxObject((short[]) o);
		}
		if (o instanceof Object[]) {
			return new JavaArrayIxObject((Object[]) o);
		}
		if (o instanceof List) {
			return new JavaListIxObject((List) o);
		}
		if (o instanceof Collection) {
			return new JavaCollectionIxObject((Collection) o);
		}
		return new DefaultJavaIxObject(o);
	}

	@Override
	public IxOperand asOperand(Object o) {
		if (o instanceof IxOperand) {
			return (IxOperand) o;
		}
		if (o instanceof IxWrapper) {
			o = ((IxWrapper)o).getObject();
		}
		if (o instanceof Number) {
			return new JavaNumberIxObject((Number) o);
		}
		if (o instanceof String) {
			return new JavaStringIxObject((String) o);
		}
		return new DefaultIxOperand(o);
	}

	@Override
	public IxAdaptable asAdaptable(Object o) {
		if (o instanceof IxAdaptable) {
			return (IxAdaptable) o;
		}
		if (o instanceof IxWrapper) {
			o = ((IxWrapper)o).getObject();
		}
		if (o instanceof Number) {
			return new JavaNumberIxObject((Number) o);
		}
		if (o instanceof String) {
			return new JavaStringIxObject((String) o);
		}
		if (o instanceof int[]) {
			return new JavaIntArrayIxObject((int[]) o);
		}
		if (o instanceof short[]) {
			return new JavaShortArrayIxObject((short[]) o);
		}
		if (o instanceof Object[]) {
			return new JavaArrayIxObject((Object[]) o);
		}
		if (o instanceof List) {
			return new JavaListIxObject((List) o);
		}
		if (o instanceof Collection) {
			return new JavaCollectionIxObject((Collection) o);
		}
		return new DefaultJavaIxObject(o);
	}

	@Override
	public void setStrategy(IEvaluationStrategy strategy) {
	}

	public static class JavaCollectionIxObject extends DefaultIxObject implements IxWrapper {

		protected Collection<?> collection;

		public JavaCollectionIxObject(Collection<?> collection) {
			this.collection = collection;
		}

		@Override
		protected String getType() {
			return "Collection";
		}

		@Override
		public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
			if (args == null) {
				switch (methodName) {
					case "first":
						return collection.isEmpty() ? null : collection.iterator().next();
					case "last":
						if (collection instanceof List<?>)
							return ((List<?>) collection).get(collection.size() - 1);
						Object last = null;
						for (Object r : collection) {
							last = r;
						}
						return last;
					case "toSet":
						if (collection instanceof Set<?>) {
							return collection;
						}
						return new LinkedHashSet<>(collection);
					case "size":
						return collection.size();
				}
			} else if (args.length == 1) {
				switch (methodName) {
					case "contains":
						return collection.contains(args[0]);
					case "indexOf":
						if (collection instanceof List<?>) {
							return ((List<?>) collection).indexOf(args[0]);
						}
						int i = 0;
						for (Object o : collection) {
							if (o != null && o.equals(args[0])) {
								return i;
							}
							i++;
						}
						return -1;
					case "union":
						if (args[0] != null) {
							Collection<Object> result = collection instanceof Set<?> ? new
									LinkedHashSet<>(collection) : new ArrayList<>(collection);
							if (args[0] instanceof Object[]) {
								Collections.addAll(result, (Object[]) args[0]);
							} else if (args[0] instanceof Collection<?>) {
								result.addAll((Collection<?>) args[0]);
							} else {
								result.add(args[0]);
							}
							return result;
						}
						return collection;
				}
			}
			return super.callMethod(caller, methodName, args);
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if (propertyName.equals("length")) {
				return collection.size();
			}
			throw new EvaluationException("do not know property `" + propertyName + "`");
		}

		@Override
		public boolean is(String qualifiedName) throws EvaluationException {
			if("Collection".equals(qualifiedName)) {
				return true;
			}
			return JavaIsInstanceUtil.isInstance(getObject(), qualifiedName);

		}

		@Override
		public Iterator asSequence() throws EvaluationException {
			return collection.iterator();
		}

		@Override
		public boolean asBoolean() {
			return collection.size() > 0;
		}

		@Override
		public Object getObject() {
			return collection;
		}
	}

	public static class JavaListIxObject extends JavaCollectionIxObject {

		public JavaListIxObject(List collection) {
			super(collection);
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				if (i < 0 || i >= collection.size()) {
					throw new EvaluationException(i + " is out of 0.." + (collection.size() - 1));
				}
				return ((List) collection).get(i);
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		@Override
		public boolean is(String qualifiedName) throws EvaluationException {
			if("List".equals(qualifiedName)) {
				return true;
			}

			return super.is(qualifiedName);	//To change body of overridden methods use File | Settings | File Templates.
		}

		@Override
		protected String getType() {
			return "List";
		}
	}

	public static class JavaArrayIxObject extends JavaListIxObject {
		private Object[] array;

		public JavaArrayIxObject(Object[] array) {
			super(new JavaArrayAdapter(array));
			this.array = array;
		}

		@Override
		protected String getType() {
			return "Object[]";
		}

		@Override
		public Object getObject() {
			return array;
		}
	}

	private class JavaMapIxObject extends DefaultIxObject implements IxWrapper {
		private Map map;

		public JavaMapIxObject(Map map) {
			this.map = map;
		}

		@Override
		public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
			return new DefaultJavaIxObject(map).callMethod(caller, methodName, args);
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
			return map.get(index);
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			return map.get(propertyName);
		}

		@Override
		public boolean asBoolean() {
			return map.size() > 0;
		}

		@Override
		public boolean is(String qualifiedName) throws EvaluationException {
			return "Map".equals(qualifiedName) || JavaIsInstanceUtil.isInstance(getObject(), qualifiedName);
		}

		@Override
		protected String getType() {
			return "Map";
		}

		@Override
		public Object getObject() {
			return map;
		}
	}

	private class JavaIntArrayIxObject extends DefaultIxObject implements IxWrapper {
		private int[] array;

		public JavaIntArrayIxObject(int[] array) {
			this.array = array;
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if(propertyName.equals("length")) {
				return array.length;
			}
			return super.getProperty(caller, propertyName);
		}

		@Override
		public boolean asBoolean() {
			return array.length > 0;
		}

		@Override
		public boolean is(String qualifiedName) throws EvaluationException {
			return "int[]".equals(qualifiedName);
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
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

		@Override
		public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
			if (args == null) {
				switch (methodName) {
					case "first":
						return array.length == 0 ? null : array[0];
					case "last":
						return array.length == 0 ? null : array[array.length - 1];
					case "toSet":
						LinkedHashSet<Integer> set = new LinkedHashSet<>();
						for (int i : array) {
							set.add(i);
						}
						return set;
					case "size":
						return array.length;
				}
			} else if (args.length == 1) {
				if (methodName.equals("contains")) {
					for(int i : array) {
						if(new Integer(i).equals(args[0])) {
							return true;
						}
					}
					return false;
				} else if (methodName.equals("indexOf")) {
					for(int i = 0; i < array.length; i++) {
						if(new Integer(array[i]).equals(args[0])) {
							return i;
						}
					}
					return -1;
				}
			}
			return super.callMethod(caller, methodName, args);
		}

		@Override
		protected String getType() {
			return "int[]";
		}

		@Override
		public Object getObject() {
			return array;
		}

		@Override
		public Iterator asSequence() throws EvaluationException {
			return new Iterator() {
				private int pos = 0;

				@Override
				public boolean hasNext() {
					return pos < array.length;
				}

				@Override
				public Object next() {
					return array[pos++];
				}
			};
		}
	}

	private class JavaShortArrayIxObject extends DefaultIxObject implements IxWrapper {
		private short[] array;

		public JavaShortArrayIxObject(short[] array) {
			this.array = array;
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if(propertyName.equals("length")) {
				return array.length;
			}
			return super.getProperty(caller, propertyName);
		}

		@Override
		public boolean asBoolean() {
			return array.length > 0;
		}

		@Override
		public boolean is(String qualifiedName) throws EvaluationException {
			return "short[]".equals(qualifiedName);
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
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

		@Override
		public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
			if (args == null) {
				switch (methodName) {
					case "first":
						return array.length == 0 ? null : array[0];
					case "last":
						return array.length == 0 ? null : array[array.length - 1];
					case "toSet":
						LinkedHashSet<Integer> set = new LinkedHashSet<>();
						for (int i : array) {
							set.add(i);
						}
						return set;
					case "size":
						return array.length;
				}
			} else if (args.length == 1) {
				if (methodName.equals("contains")) {
					for(short i : array) {
						if(new Short(i).equals(args[0])) {
							return true;
						}
					}
					return false;
				} else if (methodName.equals("indexOf")) {
					for(int i = 0; i < array.length; i++) {
						if(new Short(array[i]).equals(args[0])) {
							return i;
						}
					}
					return -1;
				}
			}
			return super.callMethod(caller, methodName, args);
		}

		@Override
		protected String getType() {
			return "short[]";
		}

		@Override
		public Object getObject() {
			return array;
		}
	}
}
