/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.NamedPattern;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.regex.RegexPart;

/**
 * Gryaznov Evgeny, 6/23/11
 */
class LiNamedPattern extends LiNamedElement implements NamedPattern, DerivedSourceElement {

	private final Name name;
	private final RegexPart regexp;
	private final SourceElement origin;

	LiNamedPattern(Name name, RegexPart regexp, SourceElement origin) {
		this.name = name;
		this.regexp = regexp;
		this.origin = origin;
	}

	@Override
	public Name getName() {
		return name;
	}

	@Override
	public RegexPart getRegexp() {
		return regexp;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return getNameText() + " (pattern)";
	}
}
