/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.builder;

import org.textway.lapg.api.InputRef;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.DerivedSourceElement;

/**
 * Gryaznov Evgeny, 3/16/11
 */
class LiInputRef implements InputRef, DerivedSourceElement {

	private final Symbol target;
	private final boolean hasEoi;
	private final SourceElement origin;

	public LiInputRef(Symbol target, boolean hasEoi, SourceElement origin) {
		this.target = target;
		this.hasEoi = hasEoi;
		this.origin = origin;
	}

	@Override
	public Symbol getTarget() {
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
}
