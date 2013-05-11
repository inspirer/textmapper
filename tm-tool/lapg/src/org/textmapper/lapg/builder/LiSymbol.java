/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.List;

abstract class LiSymbol extends LiUserDataHolder implements Symbol, DerivedSourceElement {

	private int index;
	private final String name;
	private final SourceElement origin;
	private final List<RhsSymbol> usages = new ArrayList<RhsSymbol>();
	private AstType mapping;

	protected LiSymbol(String name, SourceElement origin) {
		this.name = name;
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
	public String getName() {
		return name;
	}

	@Override
	public boolean isTerm() {
		return false;
	}

	@Override
	public Iterable<RhsSymbol> getUsages() {
		return usages;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	void addUsage(RhsSymbol usage) {
		usages.add(usage);
	}

	@Override
	public String toString() {
		return name;
	}
}
