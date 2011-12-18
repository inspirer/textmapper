/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
import org.textway.lapg.api.Symbol;

/**
 * Gryaznov Evgeny, 3/16/11
 */
class LiInputRef implements InputRef {

	private final Symbol target;
	private final boolean hasEoi;

	public LiInputRef(Symbol target, boolean hasEoi) {
		this.target = target;
		this.hasEoi = hasEoi;
	}

	@Override
	public Symbol getTarget() {
		return target;
	}

	@Override
	public boolean hasEoi() {
		return hasEoi;
	}
}
