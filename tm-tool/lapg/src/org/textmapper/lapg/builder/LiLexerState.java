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
import org.textmapper.lapg.api.LexerState;
import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.SourceElement;

/**
 * Gryaznov Evgeny, 9/10/12
 */
public class LiLexerState extends LiNamedElement implements LexerState, DerivedSourceElement {

	private final int index;
	private final Name name;
	private final SourceElement origin;

	public LiLexerState(int index, Name name, SourceElement origin) {
		this.index = index;
		this.name = name;
		this.origin = origin;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Name getName() {
		return name;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return getNameText() + " (state)";
	}
}
