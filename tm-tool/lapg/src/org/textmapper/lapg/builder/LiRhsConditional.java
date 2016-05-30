/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsConditional;
import org.textmapper.lapg.api.rule.RhsPredicate;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.List;

class LiRhsConditional extends LiRhsPart implements RhsConditional {

	private final LiRhsPredicate predicate;
	private final LiRhsSequence inner;

	public LiRhsConditional(LiRhsPredicate predicate, LiRhsSequence inner, SourceElement origin) {
		super(origin);
		this.predicate = predicate;
		this.inner = inner;
	}

	@Override
	public RhsPredicate getPredicate() {
		return predicate;
	}

	@Override
	public LiRhsSequence getInner() {
		return inner;
	}

	@Override
	List<RhsCFPart[]> expand(ExpansionContext context) {
		// TODO if (!predicate(context)) return <empty>;
		throw new UnsupportedOperationException();
		//return inner.expand(context);
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsConditional that = (LiRhsConditional) o;
		if (!predicate.equals(that.predicate)) return false;
		return inner.structurallyEquals(that.inner);
	}

	@Override
	public int structuralHashCode() {
		int result = inner.structuralHashCode();
		result = 31 * result + predicate.hashCode();
		return result;
	}

	@Override
	public Kind getKind() {
		return Kind.Conditional;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append("[");
		predicate.toString(sb);
		sb.append("] ");
		inner.toString(sb);
	}
}
