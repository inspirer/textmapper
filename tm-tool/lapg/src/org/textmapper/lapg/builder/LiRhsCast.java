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
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.rule.RhsCast;
import org.textmapper.lapg.api.rule.RhsSwitch;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.List;

/**
 * evgeny, 2/11/13
 */
class LiRhsCast extends LiRhsPart implements RhsCast {

	private final Symbol asSymbol;
	private final LiRhsPart inner;

	LiRhsCast(Symbol asSymbol, LiRhsPart inner, SourceElement origin) {
		super(origin);
		this.asSymbol = asSymbol;
		this.inner = inner;
		register(false, inner);
	}

	@Override
	public Symbol getTarget() {
		return asSymbol;
	}

	@Override
	public LiRhsPart getPart() {
		return inner;
	}

	@Override
	List<RhsSymbol[]> expand() {
		return inner.expand();
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsCast that = (LiRhsCast) o;
		if (!asSymbol.equals(that.asSymbol)) return false;
		return inner.structurallyEquals(that.inner);
	}

	@Override
	public int structuralHashCode() {
		int result = inner.structuralHashCode();
		result = 31 * result + asSymbol.hashCode();
		return result;
	}

	@Override
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseCast(this);
	}

	@Override
	protected void toString(StringBuilder sb) {
		inner.toString(sb);
		sb.append(" = ");
		sb.append(asSymbol.getName());
	}
}
