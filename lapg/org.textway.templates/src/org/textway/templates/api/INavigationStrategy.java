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
package org.textway.templates.api;

/**
 * Navigation strategy specifies how to take a property or call a method of an object. It can be registered for classes
 * or concrete instances.
 */
public interface INavigationStrategy<T> {

	/**
	 * Returns value of <propertyName> property of <obj>.
	 */
	public Object getProperty(T obj, String propertyName) throws EvaluationException;

	/**
	 * Returns a result of <obj>.methodName(args) call.
	 */
	public Object callMethod(T obj, String methodName, Object[] args) throws EvaluationException;

	/**
	 * Returns indexed value.
	 */
	public Object getByIndex(T obj, Object index) throws EvaluationException;

	/**
	 * Factory returns strategy for objects.
	 */
	public static interface Factory {

		/**
		 * Connects factory to the evaluation strategy.
		 */
		public void setEvaluationStrategy(IEvaluationStrategy strategy);

		/**
		 * Returns navigation strategy for object instance.
		 */
		public INavigationStrategy<?> getStrategy(Object o);
	}
}
