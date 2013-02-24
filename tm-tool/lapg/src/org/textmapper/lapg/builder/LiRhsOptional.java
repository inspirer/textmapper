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

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsOptional;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSwitch;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 12/5/12
 */
class LiRhsOptional extends LiRhsPart implements RhsOptional {

	private final LiRhsPart inner;

	public LiRhsOptional(LiRhsPart inner, SourceElement origin) {
		super(origin);
		this.inner = inner;
		register(inner);
	}

	@Override
	public RhsPart getPart() {
		return inner;
	}

	@Override
	List<RhsSymbol[]> expand() {
		List<RhsSymbol[]> result = inner.expand();
		for (RhsSymbol[] p : result) {
			if (p.length == 0) {
				return result;
			}
		}
		if (result.size() < 2) {
			result = new ArrayList<RhsSymbol[]>(result);
		}
		result.add(RhsSymbol.EMPTY_LIST);
		return result;
	}

	@Override
	public boolean structuralEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LiRhsOptional that = (LiRhsOptional) o;
		return inner.structuralEquals(that.inner);
	}

	@Override
	public int structuralHashCode() {
		return inner.structuralHashCode();
	}

	@Override
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseOptional(this);
	}

	@Override
	public LiRhsOptional copy() {
		return new LiRhsOptional(inner.copy(), getOrigin());
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append("(");
		inner.toString(sb);
		sb.append(")?");
	}

}
