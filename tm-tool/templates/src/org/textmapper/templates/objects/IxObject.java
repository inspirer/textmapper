/**
 * Copyright 2002-2019 Evgeny Gryaznov
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

public interface IxObject {

	/*
	 * Returns value of <propertyName> property of <obj>.
	 */
	Object getProperty(SourceElement caller, String propertyName) throws EvaluationException;

	/*
	 * Returns a result of <obj>.methodName(args) call.
	 */
	Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException;

	/*
	 * Returns indexed value.
	 */
	Object getByIndex(SourceElement caller, Object index) throws EvaluationException;

	/*
	 * "is" expression
	 */
	boolean is(String qualifiedName) throws EvaluationException;
}
