/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
import org.textmapper.lapg.api.TemplateParameter;

public interface RhsArgument extends SourceElement {

	TemplateParameter getParameter();

	/**
	 * May hold a value of type Boolean, or (reference to) Symbol.
	 * null, if the parameter is to be unset by the argument.
	 */
	Object getValue();
}
