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
import org.textmapper.lapg.api.DerivedSourceElement;

abstract class LiSymbol implements Symbol, DerivedSourceElement {

	private int index;
	private final String name;
	private final String type;
	private final SourceElement origin;

	protected LiSymbol(String name, String type, SourceElement origin) {
		this.name = name;
		this.type = type;
		this.origin = origin;
	}

	@Override
	public int getIndex() {
		return index;
	}

	void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isTerm() {
		return false;
	}

	@Override
	public String kindAsString() {
		switch (getKind()) {
			case KIND_TERM:
				return "terminal";
			case KIND_SOFTTERM:
				return "soft-terminal";
			case KIND_NONTERM:
				return "non-terminal";
		}
		return "<unknown>";
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
