/**
 * Copyright 2002-2011 Evgeny Gryaznov
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

public interface IxObject {

	/**
	 * Returns value of <propertyName> property of <obj>.
	 */
	public Object getProperty(String propertyName) throws EvaluationException;

	/**
	 * Returns a result of <obj>.methodName(args) call.
	 */
	public Object callMethod(String methodName, Object... args) throws EvaluationException;

	/**
	 * Returns indexed value.
	 */
	public Object getByIndex(Object index) throws EvaluationException;

	/**
	 * "is" expression
	 */
	public boolean is(String qualifiedName) throws EvaluationException;
}
