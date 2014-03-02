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
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsRoot;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.List;

/**
 * evgeny, 12/5/12
 */
abstract class LiRhsPart extends LiUserDataHolder implements RhsPart, DerivedSourceElement {

	private LiRhsPart parent;
	private final SourceElement origin;

	protected LiRhsPart(SourceElement origin) {
		this.origin = origin;
	}

	abstract List<RhsSymbol[]> expand();

	@Override
	public final SourceElement getOrigin() {
		return origin;
	}

	protected final void register(boolean isRewrite, LiRhsPart... children) {
		for (LiRhsPart part : children) {
			if (part == null) continue;

			part.setParent(this, isRewrite);
		}
	}

	protected void setParent(LiRhsPart parent, boolean force) {
		if (!force && this.parent != null && this.parent != parent) {
			throw new IllegalStateException("passed right-hand side entity is already used somewhere else");
		}
		this.parent = parent;
	}

	public abstract boolean structurallyEquals(LiRhsPart o);

	public abstract int structuralHashCode();

	@Override
	public Nonterminal getLeft() {
		RhsPart part = parent;
		while (part != null) {
			if (part instanceof RhsRoot) {
				return part.getLeft();
			}
			part = ((LiRhsPart) part).parent;
		}
		return null;
	}

	@Override
	public RhsSequence getContext() {
		RhsPart part = parent;
		while (part != null) {
			if (part instanceof RhsSequence) {
				return (RhsSequence) part;
			}
			part = ((LiRhsPart) part).parent;
		}
		return null;
	}

	protected static boolean structurallyEquals(LiRhsPart[] left, LiRhsPart[] right) {
		if (left == right) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}

		int length = left.length;
		if (right.length != length) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			LiRhsPart l = left[i];
			LiRhsPart r = right[i];
			if (l == null ? r != null : !l.structurallyEquals(r))
				return false;
		}

		return true;
	}

	public static int structuralHashCode(LiRhsPart a[]) {
		if (a == null) {
			return 0;
		}
		int result = 1;
		for (LiRhsPart element : a) {
			result = 31 * result + (element == null ? 0 : element.structuralHashCode());
		}
		return result;
	}

	@Override
	public final Object structuralNode() {
		return new StructuralObject(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	protected abstract void toString(StringBuilder sb);

	protected final void toString(StringBuilder sb, LiRhsPart[] parts, String separator) {
		boolean first = true;
		for (LiRhsPart p : parts) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			p.toString(sb);
		}
	}

	private static class StructuralObject {
		private final LiRhsPart part;

		private StructuralObject(LiRhsPart part) {
			this.part = part;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			StructuralObject that = (StructuralObject) o;
			return part.structurallyEquals(that.part);
		}

		@Override
		public int hashCode() {
			return part.structuralHashCode();
		}
	}
}
