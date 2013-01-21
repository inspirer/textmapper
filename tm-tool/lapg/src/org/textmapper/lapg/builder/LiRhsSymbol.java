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
import org.textmapper.lapg.api.NegativeLookahead;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.rule.RhsSwitch;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Collections;
import java.util.List;

class LiRhsSymbol extends LiRhsPart implements RhsSymbol, DerivedSourceElement {

	private final Symbol target;
	private final String alias;
	private final NegativeLookahead negLA;
	private AstField mapping;

	public LiRhsSymbol(Symbol target, String alias, NegativeLookahead negLA, SourceElement origin) {
		super(origin);
		this.target = target;
		this.alias = alias;
		this.negLA = negLA;

	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public NegativeLookahead getNegativeLA() {
		return negLA;
	}

	@Override
	public Symbol getTarget() {
		return target;
	}

	@Override
	public AstField getMapping() {
		return mapping;
	}

	void setMapping(AstField mapping) {
		this.mapping = mapping;
	}

	@Override
	List<RhsSymbol[]> expand() {
		return Collections.singletonList(new RhsSymbol[]{this});
	}

	@Override
	public boolean structuralEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsSymbol that = (LiRhsSymbol) o;
		if (alias != null ? !alias.equals(that.alias) : that.alias != null) return false;
		if (negLA != null ? !negLA.equals(that.negLA) : that.negLA != null) return false;
		return target.equals(that.target);
	}

	@Override
	public int structuralHashCode() {
		int result = target.hashCode();
		result = 31 * result + (alias != null ? alias.hashCode() : 0);
		result = 31 * result + (negLA != null ? negLA.hashCode() : 0);
		return result;
	}

	@Override
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseSymbol(this);
	}
}
