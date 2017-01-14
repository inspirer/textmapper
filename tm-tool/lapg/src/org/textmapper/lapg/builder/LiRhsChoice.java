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
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsChoice;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 12/5/12
 */
class LiRhsChoice extends LiRhsPart implements RhsChoice {

	private final LiRhsPart[] parts;

	LiRhsChoice(LiRhsPart[] parts, boolean isRewrite, SourceElement origin) {
		super(origin);
		this.parts = parts;
		register(isRewrite, parts);
	}

	@Override
	public RhsPart[] getParts() {
		return parts;
	}

	@Override
	List<RhsCFPart[]> expand(ExpansionContext context) {
		List<RhsCFPart[]> result = new ArrayList<>();
		for (LiRhsPart part : parts) {
			result.addAll(part.expand(context));
		}
		return result;
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LiRhsChoice that = (LiRhsChoice) o;
		return structurallyEquals(parts, that.parts);
	}

	@Override
	public int structuralHashCode() {
		return structuralHashCode(parts);
	}

	@Override
	public Kind getKind() {
		return Kind.Choice;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append("(");
		toString(sb, parts, " | ");
		sb.append(")");
	}
}
