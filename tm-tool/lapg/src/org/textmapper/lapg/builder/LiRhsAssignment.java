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
import org.textmapper.lapg.api.rule.RhsAssignment;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSwitch;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.List;

/**
 * evgeny, 2/8/13
 */
class LiRhsAssignment extends LiRhsPart implements RhsAssignment {

	private final String name;
	private final LiRhsPart inner;
	private final boolean addition;

	LiRhsAssignment(String name, LiRhsPart inner, boolean isAddition, SourceElement origin) {
		super(origin);
		this.name = name;
		this.inner = inner;
		addition = isAddition;
		register(false, inner);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RhsPart getPart() {
		return inner;
	}

	@Override
	public boolean isAddition() {
		return addition;
	}

	@Override
	List<RhsSymbol[]> expand() {
		return inner.expand();
	}

	@Override
	public boolean structuralEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsAssignment that = (LiRhsAssignment) o;
		if (addition != that.addition) return false;
		if (!name.equals(that.name)) return false;
		return inner.structuralEquals(that.inner);
	}

	@Override
	public int structuralHashCode() {
		int result = inner.structuralHashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (addition ? 1 : 0);
		return result;
	}

	@Override
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseAssignment(this);
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append(name);
		if (addition) {
			sb.append("+");
		}
		sb.append("=");
		inner.toString(sb);
	}
}
