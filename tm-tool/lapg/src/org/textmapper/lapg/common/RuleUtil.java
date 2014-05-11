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
package org.textmapper.lapg.common;

import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;

/**
 * TODO remove
 */
public class RuleUtil {

	private RuleUtil() {
	}

	/**
	 * @return null if undefined, empty set if ambiguous
	 */
	public static Set<RhsSymbol> getSymbolsByName(final String name, RhsPart p) {
		boolean aliasesOnly = p.accept(new AnySwitchBase() {
			@Override
			public Boolean caseAssignment(RhsAssignment p1) {
				RhsSymbol sym = getAssignmentSymbol(p1);
				if (sym != null) {
					return name.equals(p1.getName());
				}
				return super.caseAssignment(p1);
			}

			@Override
			public Boolean caseIgnored(RhsIgnored p) {
				// cannot contain named elements
				return false;
			}
		});
		return p.accept(new SymbolResolver(name, aliasesOnly));
	}

	private static class SymbolResolver extends RhsSwitch<Set<RhsSymbol>> {
		private final String name;
		private final boolean aliasesOnly;

		public SymbolResolver(String name, boolean aliasesOnly) {
			this.name = name;
			this.aliasesOnly = aliasesOnly;
		}

		@Override
		public Set<RhsSymbol> caseAssignment(RhsAssignment p) {
			RhsSymbol sym = getAssignmentSymbol(p);
			if (sym != null) {
				return name.equals(p.getName()) ? Collections.singleton(sym) : null;
			}
			return p.getPart().accept(this);
		}

		@Override
		public Set<RhsSymbol> caseSymbol(RhsSymbol p) {
			if (name.equals(p.getTarget().getName()) && !aliasesOnly) {
				return Collections.singleton(p);
			}
			return null;
		}

		@Override
		public Set<RhsSymbol> caseChoice(RhsChoice p) {
			Set<RhsSymbol> result = null;
			for (RhsPart part : p.getParts()) {
				Set<RhsSymbol> pr = part.accept(this);
				if (pr != null) {
					if (pr.size() == 0) {
						return pr;
					} else if (result == null) {
						result = pr;
					} else {
						if (result.size() < 2) {
							result = new HashSet<RhsSymbol>(result);
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
				Set<RhsSymbol> pr = p.accept(this);
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

		@Override
		public Set<RhsSymbol> caseOptional(RhsOptional p) {
			return p.getPart().accept(this);
		}

		@Override
		public Set<RhsSymbol> caseCast(RhsCast p) {
			return p.getPart().accept(this);
		}

		@Override
		public Set<RhsSymbol> caseSet(RhsSet p) {
			// cannot contain named elements
			return null;
		}

		@Override
		public Set<RhsSymbol> caseIgnored(RhsIgnored p) {
			// cannot contain named elements
			return null;
		}

		@Override
		public Set<RhsSymbol> caseSequence(RhsSequence p) {
			return fromList(p.getParts());
		}

		@Override
		public Set<RhsSymbol> caseUnordered(RhsUnordered p) {
			return fromList(p.getParts());
		}

		@Override
		public Set<RhsSymbol> caseList(RhsList p) {
			throw new UnsupportedOperationException();
		}
	}

	private static RhsSymbol getAssignmentSymbol(RhsAssignment p) {
		final RhsPart part = RhsUtil.unwrapEx(p, true, true, true);
		return part instanceof RhsSymbol ? (RhsSymbol) part : null;
	}


	private static class AnySwitchBase extends RhsSwitch<Boolean> {
		private Boolean caseAny(RhsPart[] parts) {
			for (RhsPart part : parts) {
				if (part.accept(this)) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}

		@Override
		public Boolean caseChoice(RhsChoice p) {
			return caseAny(p.getParts());
		}

		@Override
		public Boolean caseAssignment(RhsAssignment p) {
			return p.getPart().accept(this);
		}

		@Override
		public Boolean caseOptional(RhsOptional p) {
			return p.getPart().accept(this);
		}

		@Override
		public Boolean caseSequence(RhsSequence p) {
			return caseAny(p.getParts());
		}

		@Override
		public Boolean caseSymbol(RhsSymbol p) {
			return false;
		}

		@Override
		public Boolean caseUnordered(RhsUnordered p) {
			return caseAny(p.getParts());
		}

		@Override
		public Boolean caseList(RhsList p) {
			throw new IllegalStateException();
		}

		@Override
		public Boolean caseCast(RhsCast p) {
			return p.getPart().accept(this);
		}

		@Override
		public Boolean caseSet(RhsSet p) {
			return false;
		}

		@Override
		public Boolean caseIgnored(RhsIgnored p) {
			return p.getInner().accept(this);
		}
	}
}
