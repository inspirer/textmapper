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

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.ast.AstList;
import org.textmapper.lapg.api.ast.AstType;

class LiAstList implements AstList, DerivedSourceElement {

	private final AstType innerType;
	private final boolean nonEmpty;
	private final SourceElement origin;

	public LiAstList(AstType innerType, boolean nonEmpty, SourceElement origin) {
		this.innerType = innerType;
		this.nonEmpty = nonEmpty;
		this.origin = origin;
	}

	@Override
	public AstType getInner() {
		return innerType;
	}

	@Override
	public boolean isNotEmpty() {
		return nonEmpty;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public boolean isSubtypeOf(AstType another) {
		return equals(another) || another == AstType.ANY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiAstList liAstList = (LiAstList) o;
		return nonEmpty == liAstList.nonEmpty && innerType.equals(liAstList.innerType);
	}

	@Override
	public int hashCode() {
		int result = innerType.hashCode();
		result = 31 * result + (nonEmpty ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "list<" + innerType.toString() + ">";
	}
}
