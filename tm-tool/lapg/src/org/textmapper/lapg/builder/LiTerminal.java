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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;

/**
 * evgeny, 11/16/12
 */
public class LiTerminal extends LiSymbol implements Terminal {

	private final Symbol softClass;

	public LiTerminal(String name, String type, SourceElement origin) {
		super(name, type, origin);
		this.softClass = null;
	}

	public LiTerminal(String name, Terminal softClass, SourceElement origin) {
		super(name, softClass.getType(), origin);
		this.softClass = softClass;
	}

	@Override
	public int getKind() {
		return softClass == null ? KIND_TERM : KIND_SOFTTERM;
	}

	@Override
	public boolean isTerm() {
		return true;
	}

	@Override
	public Symbol getSoftClass() {
		return softClass;
	}

	@Override
	public boolean isSoft() {
		return softClass != null;
	}
}

