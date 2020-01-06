/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
import org.textmapper.lapg.api.NamedSet;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsSet;

public class LiNamedSet extends LiNamedElement implements NamedSet, DerivedSourceElement {
	private final Name name;
	private final RhsSet set;
	private int[] resolvedElements;
	private final SourceElement origin;

	public LiNamedSet(Name name, RhsSet set, SourceElement origin) {
		this.name = name;
		this.set = set;
		this.origin = origin;
	}

	@Override
	public Name getName() {
		return name;
	}

	@Override
	public RhsSet getSet() {
		return set;
	}

	@Override
	public int[] getElements() {
		return resolvedElements;
	}

	void setElements(int[] value) {
		resolvedElements = value;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return getNameText() + " (set)";
	}
}
