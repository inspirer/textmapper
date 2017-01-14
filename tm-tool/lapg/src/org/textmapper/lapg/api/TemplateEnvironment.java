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
package org.textmapper.lapg.api;

/**
 * The immutable data structure that maps template parameters to their values.
 * Template parameters can hold values of type Boolean, or (reference to) Symbol.
 * Unset parameters are represented as nulls.
 */
public interface TemplateEnvironment {

	/**
	 * May hold a value of type Boolean, or (reference to) Symbol.
	 * Returns null, if the parameter is unset.
	 */
	Object getValue(TemplateParameter param);

	/**
	 * Creates a new template environment based on the current one with the parameter set to value.
	 *
	 * @param value Boolean, (reference to) Symbol, or null if the parameter
	 *              is to be unset by the argument.
	 */
	TemplateEnvironment extend(TemplateParameter param, Object value);

	/**
	 * Returns a human-readable suffix for nonterminals instantiated for this environment.
	 * The suffix is empty for empty environments and starts with an underscore otherwise.
	 */
	String getNonterminalSuffix();

	/**
	 * Unsets all parameters for which the given predicate returns false.
	 */
	TemplateEnvironment filter(ParameterPredicate predicate);

	interface ParameterPredicate {
		boolean include(TemplateParameter parameter);
	}

	/**
	 * @return true if this environment contains any lookahead parameters.
	 */
	boolean hasLookahead();
}
