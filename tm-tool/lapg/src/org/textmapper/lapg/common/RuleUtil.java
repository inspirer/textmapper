/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
package org.textmapper.lapg.common;

import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.util.RhsUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO remove
 */
public class RuleUtil {

	private RuleUtil() {
	}

	public static boolean hasAliases(RhsPart part, String name) {
		switch (part.getKind()) {
			case Assignment: {
				RhsAssignment a = (RhsAssignment) part;
				RhsSymbol sym = getAssignmentSymbol(a);
				if (sym != null) {
					return name.equals(a.getName());
				}
				return hasAliases(a.getPart(), name);
			}
			case Symbol:
			case Set:
			case StateMarker:
				// cannot contain aliases
				return false;
			case Optional:
				return hasAliases(((RhsOptional) part).getPart(), name);
			case Cast:
				return hasAliases(((RhsCast) part).getPart(), name);
			case Choice:
				for (RhsPart p : ((RhsChoice) part).getParts()) {
					if (hasAliases(p, name)) return true;
				}
				return false;
			case Sequence:
				for (RhsPart p : ((RhsSequence) part).getParts()) {
					if (hasAliases(p, name)) return true;
				}
				return false;
			case List:
			case Conditional:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * @return null if undefined, empty set if ambiguous
	 */
	public static Set<RhsSymbol> getSymbolsByName(final String name, RhsPart p) {
		return new SymbolResolver(name, hasAliases(p, name)).resolve(p);
	}

	private static class SymbolResolver {
		private final String name;
		private final boolean aliasesOnly;

		public SymbolResolver(String name, boolean aliasesOnly) {
			this.name = name;
			this.aliasesOnly = aliasesOnly;
		}

		public Set<RhsSymbol> resolve(RhsPart part) {
			switch (part.getKind()) {
				case Assignment: {
					RhsAssignment a = (RhsAssignment) part;
					RhsSymbol sym = getAssignmentSymbol(a);
					if (sym != null) {
						return name.equals(a.getName()) ? Collections.singleton(sym) : null;
					}
					return resolve(a.getPart());
				}
				case Symbol:
					if (!aliasesOnly && name.equals(((RhsSymbol) part).getTarget().getNameText())) {
						return Collections.singleton((RhsSymbol) part);
					}
					return null;
				case Sequence:
					return fromList(((RhsSequence) part).getParts());
				case Choice:
					return choice((RhsChoice) part);
				case Optional:
					return resolve(((RhsOptional) part).getPart());
				case Cast:
					return resolve(((RhsCast) part).getPart());
				case Set:
				case StateMarker:
					// cannot contain named elements
					return null;
				case List:
				case Conditional:
					throw new UnsupportedOperationException();
				default:
					throw new IllegalStateException();
			}
		}

		public Set<RhsSymbol> choice(RhsChoice p) {
			Set<RhsSymbol> result = null;
			for (RhsPart part : p.getParts()) {
				Set<RhsSymbol> pr = resolve(part);
				if (pr != null) {
					if (pr.size() == 0) {
						return pr;
					} else if (result == null) {
						result = pr;
					} else {
						if (result.size() < 2) {
							result = new HashSet<>(result);
						}
						RhsSymbol first = result.iterator().next();
						for (RhsSymbol n : pr) {
							if (first.getTarget() != n.getTarget()) {
								return Collections.emptySet();
							}
						}
						result.addAll(pr);
					}
				}
			}
			return result;
		}

		private Set<RhsSymbol> fromList(RhsPart[] list) {
			Set<RhsSymbol> result = null;
			for (RhsPart p : list) {
				Set<RhsSymbol> pr = resolve(p);
				if (pr != null) {
					if (pr.size() == 0) {
						return pr;
					} else if (result == null) {
						result = pr;
					} else {
						return Collections.emptySet();
					}
				}
			}
			return result;
		}
	}

	public static RhsSymbol getAssignmentSymbol(RhsAssignment p) {
		final RhsPart part = RhsUtil.unwrapEx(p, true, true, true);
		return part instanceof RhsSymbol ? (RhsSymbol) part : null;
	}

	public static boolean isEmpty(RhsCFPart[] rhs) {
		for (RhsCFPart p : rhs) {
			if (p instanceof RhsSymbol) return false;
		}
		return true;
	}
}
