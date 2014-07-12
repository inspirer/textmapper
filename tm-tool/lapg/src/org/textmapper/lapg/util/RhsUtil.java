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
package org.textmapper.lapg.util;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.rule.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * evgeny, 12/7/12
 */
public class RhsUtil {

	public static RhsPart unwrap(RhsPart part) {
		if (part instanceof RhsChoice && ((RhsChoice) part).getParts().length == 1) {
			return unwrap(((RhsChoice) part).getParts()[0]);
		}
		if (part instanceof RhsSequence && ((RhsSequence) part).getParts().length == 1) {
			return unwrap(((RhsSequence) part).getParts()[0]);
		}
		if (part instanceof RhsUnordered && ((RhsUnordered) part).getParts().length == 1) {
			return unwrap(((RhsUnordered) part).getParts()[0]);
		}
		return part;
	}

	public static RhsPart unwrapOpt(RhsPart part) {
		if (part instanceof RhsOptional) {
			return unwrap(((RhsOptional) part).getPart());
		}
		return part;
	}

	public static RhsPart unwrapEx(RhsPart part, boolean opts, boolean casts, boolean assignments) {
		part = unwrap(part);
		if (opts && part instanceof RhsOptional) {
			return unwrapEx(((RhsOptional) part).getPart(), opts, casts, assignments);
		}
		if (casts && part instanceof RhsCast) {
			return unwrapEx(((RhsCast) part).getPart(), opts, casts, assignments);
		}
		if (assignments && part instanceof RhsAssignment && !(((RhsAssignment) part).isAddition())) {
			return unwrapEx(((RhsAssignment) part).getPart(), opts, casts, assignments);
		}
		return part;
	}

	public static Symbol getRepresentative(RhsPart part) {
		part = unwrapEx(part, true, true, true);
		if (part instanceof RhsSymbol) {
			return ((RhsSymbol) part).getTarget();
		}
		return null;
	}

	public static AstType getCastType(RhsPart part) {
		part = unwrapEx(part, true, false, true);
		return part instanceof RhsCast ? ((RhsCast) part).getTarget().getType() : null;
	}

	public static RhsAssignment getAssignment(RhsPart part) {
		part = unwrapEx(part, true, true, false);
		return part instanceof RhsAssignment ? (RhsAssignment) part : null;
	}

	public static boolean isConstant(RhsPart part) {
		part = unwrapEx(part, false, false, true);
		return part instanceof RhsSymbol && isConstant((RhsSymbol) part);
	}

	public static boolean isConstant(RhsSymbol part) {
		Symbol s = part.getTarget();
		return s instanceof Terminal && ((Terminal) s).isConstant();
	}

	public static boolean isEmpty(RhsPart p) {
		return p instanceof RhsSequence && ((RhsSequence) p).getParts().length == 0
				|| p instanceof RhsChoice && ((RhsChoice) p).getParts().length == 0
				|| p instanceof RhsUnordered && ((RhsUnordered) p).getParts().length == 0;
	}

	public static boolean hasMapping(RhsSequence seq) {
		RhsMapping mapping = seq.getMapping();
		if (mapping == null) return false;

		if (mapping.getField() == null
				&& mapping.getValue() == null
				&& !mapping.isAddition()
				&& seq.getType() == null) {
			// identity mapping => ignore
			return false;
		}
		return true;
	}

	public static Iterable<RhsPart> getChildren(RhsPart part) {
		switch (part.getKind()) {
			case Sequence:
				return Arrays.asList(((RhsSequence) part).getParts());
			case Optional:
				return Arrays.asList(((RhsOptional) part).getPart());
			case Cast:
				return Arrays.asList(((RhsCast) part).getPart());
			case Assignment:
				return Arrays.asList(((RhsAssignment) part).getPart());
			case Choice:
				return Arrays.asList(((RhsChoice) part).getParts());
			case Unordered:
				return Arrays.asList(((RhsUnordered) part).getParts());
		}
		return null;
	}

	public static boolean containsRef(RhsPart part, final Symbol ref) {
		part = unwrapEx(part, true, true, true);
		if (part instanceof RhsSymbol) {
			return ref == ((RhsSymbol) part).getTarget();
		}
		final Iterable<RhsPart> children = getChildren(part);
		if (children == null) return false;

		for (RhsPart child : children) {
			if (containsRef(child, ref)) {
				return true;
			}
		}
		return false;
	}

	public static RhsSymbol[] getRhsSymbols(RhsPart p) {
		final List<RhsSymbol> result = new ArrayList<RhsSymbol>();
		collectRhsSymbols(p, result);
		return result.toArray(new RhsSymbol[result.size()]);
	}

	private static void collectRhsSymbols(RhsPart part, List<RhsSymbol> result) {
		part = unwrapEx(part, true, true, true);
		if (part instanceof RhsSymbol) {
			result.add((RhsSymbol) part);
			return;
		}
		final Iterable<RhsPart> children = getChildren(part);
		if (children == null) return;

		for (RhsPart child : children) {
			collectRhsSymbols(child, result);
		}
	}

	/**
	 * Returns true, if an empty string can be derived from "part".
	 * "dependencies" list will contain all nonterminals that prevent "part" to be nullable.
	 */
	public static boolean isNullable(RhsPart part, final List<Nonterminal> dependencies) {
		switch (part.getKind()) {
			case Choice:
				for (RhsPart inner : ((RhsChoice) part).getParts()) {
					if (isNullable(inner, dependencies)) return true;
				}
				return false;
			case Optional:
				return true;
			case Sequence: {
				boolean isNullable = true;
				for (RhsPart inner : ((RhsSequence) part).getParts()) {
					// Note: we do not return immediately to collect all the dependencies.
					if (!isNullable(inner, dependencies)) isNullable = false;
				}
				return isNullable;
			}
			case Symbol: {
				if (((RhsSymbol) part).getTarget().isTerm()) return false;
				Nonterminal n = (Nonterminal) ((RhsSymbol) part).getTarget();

				if (n.isNullable()) return true;
				if (dependencies != null) dependencies.add(n);
				return false;
			}
			case Unordered: {
				boolean isNullable = true;
				for (RhsPart inner : ((RhsUnordered) part).getParts()) {
					// Note: we do not return immediately to collect all the dependencies.
					if (!isNullable(inner, dependencies)) isNullable = false;
				}
				return isNullable;
			}

			case List: {
				RhsList list = (RhsList) part;
				if (!list.isNonEmpty()) return true;
				RhsPart first = list.getCustomInitialElement() != null
						? list.getCustomInitialElement()
						: list.getElement();
				return isNullable(first, dependencies);
			}
			case Set:
				// Sets have at least one element.
				return false;
			case Assignment:
				return isNullable(((RhsAssignment) part).getPart(), dependencies);
			case Cast:
				return isNullable(((RhsCast) part).getPart(), dependencies);
			case Ignored:
				return isNullable(((RhsIgnored) part).getInner(), dependencies);
		}

		throw new IllegalStateException();
	}

	public static RhsChoice asChoice(final RhsPart... parts) {
		return new RhsChoice() {
			@Override
			public RhsPart[] getParts() {
				return parts;
			}

			@Override
			public Kind getKind() {
				return Kind.Choice;
			}

			@Override
			public Nonterminal getLeft() {
				throw new UnsupportedOperationException();
			}

			@Override
			public RhsSequence getContext() {
				return null;
			}

			@Override
			public Object structuralNode() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Object getUserData(String key) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void putUserData(String key, Object value) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
