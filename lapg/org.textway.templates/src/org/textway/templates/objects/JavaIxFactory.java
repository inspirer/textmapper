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

import java.util.*;

public class JavaIxFactory implements IxFactory {

	public IxObject asObject(Object o) {
		return new DefaultIxObject(o);
	}

	public IxOperand asOperand(Object o) {
		if(o instanceof Number) {
			return new JavaNumberIxObject((Number) o);
		}
		if(o instanceof String) {
			return new JavaStringIxObject((String) o);
		}
		return new DefaultIxOperand(o);
	}

	public IxAdaptable asAdaptable(Object o) {
		if(o instanceof Number) {
			return new JavaNumberIxObject((Number) o);
		}
		if(o instanceof String) {
			return new JavaStringIxObject((String) o);
		}
		if(o instanceof Object[]) {
			return new JavaArrayIxObject((Object[]) o);
		}
		if(o instanceof List) {
			return new JavaListIxObject((List) o);
		}
		if(o instanceof Collection) {
			return new JavaCollectionIxObject((Collection) o);
		}
		return new DefaultIxObject(o);
	}

	public static class JavaCollectionIxObject extends BaseIxObject {

		protected Collection collection;

		public JavaCollectionIxObject(Collection collection) {
			this.collection = collection;
		}

		@Override
		protected String getType() {
			return "Collection";
		}

		@Override
		public Object callMethod(String methodName, Object[] args) throws EvaluationException {
			if (args == null) {
				if (methodName.equals("first")) {
					return collection.isEmpty() ? null : collection.iterator().next();
				} else if(methodName.equals("last")) {
					if(collection instanceof List<?>) {
						return ((List<?>)collection).get(((List<?>)collection).size()-1);
					}
					Object last = null;
					for(Object r : collection) {
						last = r;
					}
					return last;
				} else if(methodName.equals("toSet")) {
					if(collection instanceof Set<?>) {
						return collection;
					}
					return new LinkedHashSet<Object>(collection);
				} else if(methodName.equals("size")) {
					return collection.size();
				}
			} else if(args.length == 1) {
				if(methodName.equals("contains")) {
					return collection.contains(args[0]);
				} else if(methodName.equals("indexOf")) {
					if(collection instanceof List<?>) {
						return ((List<?>)collection).indexOf(args[0]);
					}
					int i = 0;
					for(Object o : collection) {
						if(o != null && o.equals(args[0])) {
							return i;
						}
						i++;
					}
					return -1;
				} else if(methodName.equals("union")) {
					if(args[0] != null) {
						Collection<Object> result = collection instanceof Set<?> ? new LinkedHashSet<Object>(collection) : new ArrayList<Object>(collection);
						if(args[0] instanceof Object[]) {
							for(Object o : (Object[])args[0]) {
								result.add(o);
							}
						} else if(args[0] instanceof Collection<?>) {
							result.addAll((Collection<?>)args[0]);
						} else {
							result.add(args[0]);
						}
						return result;
					}
					return collection;
				}
			}
			return super.callMethod(methodName, args);
		}

		@Override
		public Object getProperty(String propertyName) throws EvaluationException {
			if (propertyName.equals("length")) {
				return collection.size();
			}
			throw new EvaluationException("do not know property `" + propertyName + "`");
		}

		@Override
		public Iterator asSequence() throws EvaluationException {
			return collection.iterator();
		}

		public Object getObject() {
			return collection;
		}
	}

	public static class JavaListIxObject extends JavaCollectionIxObject {

		public JavaListIxObject(List collection) {
			super(collection);
		}

		@Override
		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof Integer) {
				return ((List)collection).get((Integer) index);
			} else {
				throw new EvaluationException("index object should be integer");
			}
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
}
