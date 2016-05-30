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
package org.textmapper.lapg.api;

public interface TemplateParameter extends NamedElement, SourceElement, UserDataHolder {

	enum Type {
		Flag,
		Symbol
	}
	Type getType();

	enum Modifier {
		Default,
		Global,
		Lookahead,
		Explicit,
	}
	Modifier getModifier();

	/**
	 * May hold a value of type Boolean, or (reference to) Symbol.
	 * Or null, if the parameter does not have any default value (unset).
	 */
	Object getDefaultValue();

	void appendSuffix(StringBuilder sb, Object value);
}
