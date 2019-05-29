/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsStateMarker;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Collections;
import java.util.List;

public class LiRhsStateMarker extends LiRhsPart implements RhsStateMarker {

	private final String name;

	LiRhsStateMarker(String name, SourceElement origin) {
		super(origin);
		this.name = name;
	}

	@Override
	public Kind getKind() {
		return Kind.StateMarker;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	List<RhsCFPart[]> expand(ExpansionContext context) {
		return Collections.singletonList(new RhsCFPart[]{this});
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		return name.equals(((RhsStateMarker)o).getName());
	}

	@Override
	public int structuralHashCode() {
		return name.hashCode();
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append('.');
		sb.append(name);
	}

	@Override
	public Symbol getTarget() {
		return null;
	}
}
