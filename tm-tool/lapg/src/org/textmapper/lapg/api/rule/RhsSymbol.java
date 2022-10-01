/**
 * Copyright 2002-2022 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateParameter;

/**
 * evgeny, 11/24/12
 */
public interface RhsSymbol extends RhsPart, RhsCFPart {

	RhsSymbol[] EMPTY_LIST = new RhsSymbol[0];
	String UD_REWRITTEN = "rewrittenTo";

	/**
	 * Instead of referencing some symbol directly, RhsSymbol can reference a template parameter value.
	 * Only one of getTarget() or getTemplateTarget() is not null.
	 */
	Symbol getTarget();
	TemplateParameter getTemplateTarget();

	/**
	 * Both symbol and templated symbol may have arguments.
	 */
	RhsArgument[] getArgs();

	/**
	 * Forwards all template arguments, including non-globals.
	 */
	boolean isFwdAll();

	RhsMapping getMapping();
}
