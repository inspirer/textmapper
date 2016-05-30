/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.api.rule;

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.TemplateParameter;

/**
 * An expression which, given a template environment, evaluates to true or false.
 */
public interface RhsPredicate extends SourceElement {

	enum Operation {
		/* Template parameter checks */
		Equals,
		/* Logical expressions */
		Or,
		And,
		Not,
	}

	Operation getOperation();

	/**
	 * For logical expressions
	 */
	RhsPredicate[] getChildren();

	/**
	 * A parameter to check against the value
	 */
	TemplateParameter getParameter();

	/**
	 * Holds a value of type Integer, String, Boolean, or (reference to) Symbol.
	 * Cannot be null.
	 */
	Object getValue();

	/**
	 * Evaluates the predicate.
	 */
	boolean apply(TemplateEnvironment env);
}
