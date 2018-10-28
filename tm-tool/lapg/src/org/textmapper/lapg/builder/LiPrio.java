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
import org.textmapper.lapg.api.Prio;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Terminal;

class LiPrio implements Prio, DerivedSourceElement {

	private final int prio;
	private final Terminal[] symbols;
	private final SourceElement origin;

	public LiPrio(int prio, Terminal[] symbols, SourceElement origin) {
		this.prio = prio;
		this.symbols = symbols;
		this.origin = origin;
	}

	@Override
	public int getPrio() {
		return prio;
	}

	@Override
	public Terminal[] getSymbols() {
		return symbols;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
