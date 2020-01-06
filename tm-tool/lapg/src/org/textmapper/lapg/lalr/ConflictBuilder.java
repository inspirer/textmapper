/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.ParserConflict;
import org.textmapper.lapg.api.ParserConflict.Input;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.common.SymbolUtil;
import org.textmapper.lapg.lalr.LalrConflict.InputImpl;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictBuilder {

	public static final int NONE = 0;
	public static final int SHIFT = 1;
	public static final int REDUCE = 2;
	public static final int SYNTAXERR = 3;
	public static final int CONFLICT = 4;

	private final ConflictData[] nextconfl;
	private final List<ConflictData> conflicts;

	public ConflictBuilder(int nterms) {
		nextconfl = new ConflictData[nterms];
		conflicts = new ArrayList<>();
	}

	public void clear() {
		conflicts.clear();
		Arrays.fill(nextconfl, null);
	}

	public boolean hasConflict(int termSym) {
		return nextconfl[termSym] != null && nextconfl[termSym].isConflict();
	}

	public void addReduce(Terminal sym, int status, Rule rule, Rule originalRule) {
		int termSym = sym.getIndex();
		if (nextconfl[termSym] == null) {
			conflicts.add(nextconfl[termSym] = new ConflictData(sym, originalRule == null));
			if (originalRule != null) {
				nextconfl[termSym].addReduce(ConflictBuilder.CONFLICT, originalRule);
			}
		}
		nextconfl[termSym].addReduce(status, rule);
	}

	public List<LalrConflict> getMergedConflicts(int state, Symbol[] input,
												 int[] next) {
		if (conflicts.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Object, ConflictData> map = new HashMap<>();
		for (ConflictData c : conflicts) {
			Object key = c.getRulesAndKindKey();
			ConflictData data = map.get(key);
			if (data == null) {
				map.put(key, c);
			} else {
				data.addLinked(c);
			}
		}
		Input inp = new InputImpl(state, input);
		Collection<ConflictData> values = map.values();

		return values.stream()
				.map(data -> new LalrConflict(data.getKind(), data.getKindAsText(), inp,
						data.getSymbols(), data.getRules()))
				.sorted()
				.collect(Collectors.toList());
	}

	public static class ConflictData {

		private final Terminal termSym;
		private final boolean canShift;
		private boolean isSoft;
		private final List<Rule> rules = new ArrayList<>();
		private int status = NONE;

		private ConflictData linked = null;

		public ConflictData(Terminal termSym, boolean canShift) {
			this.termSym = termSym;
			this.canShift = canShift;
			this.isSoft = false;
		}

		public int getConflictingTerm() {
			return termSym.getIndex();
		}

		public void setSoft() {
			isSoft = true;
		}

		public Rule[] getRules() {
			return rules.toArray(new Rule[rules.size()]);
		}

		public Terminal[] getSymbols() {
			int len = 0;
			for (ConflictData curr = this; curr != null; curr = curr.linked) {
				len++;
			}
			Terminal[] result = new Terminal[len];
			len = 0;
			for (ConflictData curr = this; curr != null; curr = curr.linked) {
				result[len++] = curr.termSym;
			}
			Arrays.sort(result, SymbolUtil.COMPARATOR);
			return result;
		}

		public int getKind() {
			if (status == CONFLICT) {
				if (isSoft) {
					return canShift
							? ParserConflict.SHIFT_REDUCE_SOFT
							: ParserConflict.REDUCE_REDUCE_SOFT;
				} else {
					return canShift ? ParserConflict.SHIFT_REDUCE : ParserConflict.REDUCE_REDUCE;
				}
			}
			return ParserConflict.FIXED;
		}

		public String getKindAsText() {
			switch (status) {
				case SHIFT:
					return "resolved as shift";
				case REDUCE:
					return "resolved as reduce";
				case SYNTAXERR:
					return "resolved as syntax error";
				case CONFLICT:
					if (isSoft) {
						return canShift ? "soft shift/reduce" : "soft reduce/reduce";
					} else {
						return canShift ? "shift/reduce" : "reduce/reduce";
					}
			}
			return "<no conflict>";
		}

		public void addReduce(int newstatus, Rule rule) {
			if (status == NONE) {
				status = newstatus;
			} else if (status != CONFLICT && newstatus != status) {
				status = CONFLICT; // shift + reduce = conflict
			}
			rules.add(rule);
		}

		public void addLinked(ConflictData conflict) {
			conflict.linked = linked;
			linked = conflict;
		}

		public boolean isConflict() {
			return status == CONFLICT;
		}

		public Object getRulesAndKindKey() {
			return new RulesAndKindKey();
		}

		private class RulesAndKindKey {
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + (canShift ? 1231 : 1237);
				result = prime * result + (isSoft ? 1231 : 1237);
				result = prime * result + rules.hashCode();
				result = prime * result + status;
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (!(obj instanceof RulesAndKindKey)) {
					return false;
				}
				ConflictData other = ((RulesAndKindKey) obj).getConflictData();
				return canShift == other.canShift &&
						isSoft == other.isSoft &&
						status == other.status &&
						rules.equals(other.rules);
			}

			public ConflictData getConflictData() {
				return ConflictData.this;
			}
		}
	}
}
