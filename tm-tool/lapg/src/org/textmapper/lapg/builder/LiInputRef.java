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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.InputRef;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.SourceElement;

/**
 * Gryaznov Evgeny, 3/16/11
 */
class LiInputRef implements InputRef, DerivedSourceElement {

	private final int index;
	private final Nonterminal target;
	private final boolean hasEoi;
	private final SourceElement origin;

	public LiInputRef(int index, Nonterminal target, boolean hasEoi, SourceElement origin) {
		this.index = index;
		this.target = target;
		this.hasEoi = hasEoi;
		this.origin = origin;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Nonterminal getTarget() {
		return target;
	}

	@Override
	public boolean hasEoi() {
		return hasEoi;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiInputRef that = (LiInputRef) o;

		return hasEoi == that.hasEoi && target.equals(that.target);
	}

	@Override
	public int hashCode() {
		int result = target.hashCode();
		result = 31 * result + (hasEoi ? 1 : 0);
		return result;
	}
}
