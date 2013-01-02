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
package org.textmapper.lapg.common;

import org.textmapper.lapg.api.rule.*;

import java.util.*;

/**
 * evgeny, 12/31/12
 */
public class RuleUtil {

	private RuleUtil() {
	}

	public static RhsSymbol[] getAllSymbols(RhsPart p) {
		final List<RhsSymbol> result = new ArrayList<RhsSymbol>();
		p.accept(new RhsSwitch<Object>() {
			@Override
			public Object caseChoice(RhsChoice p) {
				for (RhsPart part : p.getParts()) {
					part.accept(this);
				}
				return null;
			}

			@Override
			public Object caseOptional(RhsOptional p) {
				return p.getPart().accept(this);
			}

			@Override
			public Object caseSequence(RhsSequence p) {
				for (RhsPart part : p.getParts()) {
					part.accept(this);
				}
				return null;
			}

			@Override
			public Object caseSymbol(RhsSymbol p) {
				result.add(p);
				return null;
			}

			@Override
			public Object caseUnordered(RhsUnordered p) {
				for (RhsPart part : p.getParts()) {
					part.accept(this);
				}
				return null;
			}
		});
		return result.toArray(new RhsSymbol[result.size()]);
	}

	public static Set<RhsSymbol> getSymbols(final String name, RhsPart p) {
		final Set<RhsSymbol> all = p.accept(new SymbolResolver(name));
		if (all == null) {
			return Collections.emptySet();
		}
		if (all.size() > 1) {
			boolean withAlias = false;
			boolean woAlias = false;
			for (RhsSymbol s : all) {
				if (s.getAlias() != null) {
					withAlias = true;
				} else {
					woAlias = true;
				}
			}
			if (withAlias && woAlias) {
				// remove symbols without alias
				Iterator<RhsSymbol> it = all.iterator();
				while (it.hasNext()) {
					RhsSymbol s = it.next();
					if (s.getAlias() == null) {
						it.remove();
					}
				}
			}
		}
		return all;
	}

	private static class SymbolResolver extends RhsSwitch<Set<RhsSymbol>> {
		private final String name;

		public SymbolResolver(String name) {
			this.name = name;
		}

		@Override
		public Set<RhsSymbol> caseSymbol(RhsSymbol p) {
			String pName = p.getAlias();
			if (pName == null) {
				pName = p.getTarget().getName();
			}
			if (name.equals(pName)) {
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
		public Set<RhsSymbol> caseSequence(RhsSequence p) {
			return fromList(p.getParts());
		}

		@Override
		public Set<RhsSymbol> caseUnordered(RhsUnordered p) {
			return fromList(p.getParts());
		}
	}
}
