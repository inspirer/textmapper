/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.RhsMapping;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Collections;
import java.util.List;

class LiRhsSymbol extends LiRhsPart implements RhsSymbol, DerivedSourceElement {

	private final Symbol target;
	private LiRhsMapping mapping;

	public LiRhsSymbol(Symbol target, SourceElement origin) {
		super(origin);
		this.target = target;
	}

	@Override
	public Symbol getTarget() {
		return target;
	}

	@Override
	public RhsMapping getMapping() {
		return mapping;
	}

	void setMapping(LiRhsMapping mapping) {
		this.mapping = mapping;
	}

	@Override
	List<RhsSymbol[]> expand() {
		return Collections.singletonList(new RhsSymbol[]{this});
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsSymbol that = (LiRhsSymbol) o;
		return target.equals(that.target);
	}

	@Override
	public int structuralHashCode() {
		return target.hashCode();
	}

	@Override
	public Kind getKind() {
		return Kind.Symbol;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append(target.getName());
	}
}
