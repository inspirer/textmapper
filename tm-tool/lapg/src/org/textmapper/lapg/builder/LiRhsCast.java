/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.RhsArgument;
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsCast;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Arrays;
import java.util.List;

/**
 * evgeny, 2/11/13
 */
class LiRhsCast extends LiRhsPart implements RhsCast, TemplatedSymbolRef {

	private Symbol asSymbol;
	private LiRhsArgument[] args;
	private final LiRhsPart inner;

	LiRhsCast(Symbol asSymbol, LiRhsArgument[] args, LiRhsPart inner, SourceElement origin) {
		super(origin);
		this.asSymbol = asSymbol;
		this.args = args;
		this.inner = inner;
		register(false, inner);
	}

	@Override
	public Symbol getTarget() {
		return asSymbol;
	}

	@Override
	public RhsArgument[] getArgs() {
		return args;
	}

	@Override
	public LiRhsPart getPart() {
		return inner;
	}

	@Override
	List<RhsCFPart[]> expand(ExpansionContext context) {
		return inner.expand(context);
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsCast that = (LiRhsCast) o;
		if (!asSymbol.equals(that.asSymbol)) return false;
		if (!Arrays.equals(args, that.args)) return false;
		return inner.structurallyEquals(that.inner);
	}

	@Override
	public int structuralHashCode() {
		int result = inner.structuralHashCode();
		result = 31 * result + asSymbol.hashCode();
		result = 31 * result + Arrays.hashCode(args);
		return result;
	}

	@Override
	public Kind getKind() {
		return Kind.Cast;
	}

	@Override
	protected void toString(StringBuilder sb) {
		inner.toString(sb);
		sb.append(" as ");
		sb.append(LiUtil.getSymbolName(asSymbol));
		LiUtil.appendArguments(sb, args);
	}

	@Override
	public void setResolvedSymbol(Symbol symbol) {
		this.args = null;
		this.asSymbol = symbol;
	}
}
