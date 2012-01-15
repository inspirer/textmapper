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

import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.DerivedSourceElement;

class LiSymbol implements Symbol, DerivedSourceElement {

	private int index;
	private final int kind;
	private final String name;
	private final String type;
	private final Symbol softClass;
	private final SourceElement origin;

	public LiSymbol(int kind, String name, String type, SourceElement origin) {
		this.kind = kind;
		this.name = name;
		this.type = type;
		this.softClass = null;
		this.origin = origin;
	}

	public LiSymbol(String name, Symbol softClass, SourceElement origin) {
		this.kind = KIND_SOFTTERM;
		this.name = name;
		this.type = softClass.getType();
		this.softClass = softClass;
		this.origin = origin;
	}

	@Override
	public int getKind() {
		return kind;
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
		return kind == KIND_TERM || kind == KIND_SOFTTERM;
	}

	@Override
	public boolean isSoft() {
		return kind == KIND_SOFTTERM;
	}

	@Override
	public Symbol getSoftClass() {
		return softClass;
	}

	@Override
	public String kindAsString() {
		switch (kind) {
			case KIND_TERM:
				return "terminal";
			case KIND_SOFTTERM:
				return "soft-terminal";
			case KIND_NONTERM:
				return "non-terminal";
			case KIND_LAYOUT:
				return "layout";
		}
		return "<unknown>";
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
