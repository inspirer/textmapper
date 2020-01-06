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
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.AstType;

abstract class LiSymbol extends LiNamedElement implements Symbol, DerivedSourceElement {

	private Name name;
	// OR
	private String nameHint;

	private int index;
	protected final SourceElement origin;
	private AstType mapping;
	private boolean unused;

	protected LiSymbol(Name name, String nameHint, SourceElement origin) {
		this.name = name;
		this.nameHint = nameHint;
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
	public AstType getType() {
		return mapping;
	}

	void setType(AstType mapping) {
		this.mapping = mapping;
	}

	@Override
	public Name getName() {
		return name;
	}

	@Override
	public void setName(String value) {
		if (name != null) throw new IllegalStateException();

		name = LiName.raw(value);
	}

	@Override
	public void updateNameHint(String nameHint) {
		this.nameHint = nameHint;
	}

	@Override
	public String getNameHint() {
		return nameHint;
	}

	@Override
	public boolean isTerm() {
		return false;
	}

	@Override
	public boolean isUnused() {
		return unused;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return LiUtil.getSymbolName(this) + (isTerm() ? " (terminal)" : " (nonterminal)");
	}

	void setUnused() {
		unused = true;
	}
}
