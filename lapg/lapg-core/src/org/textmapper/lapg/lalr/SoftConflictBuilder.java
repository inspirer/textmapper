/**
 * Copyright 2002-2012 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.Symbol;

import java.util.*;

public class SoftConflictBuilder {

	private List<SoftClassConflict> conflicts = new ArrayList<SoftClassConflict>();

	public Collection<SoftClassConflict> getConflicts() {
		return Collections.unmodifiableCollection(conflicts);
	}

	public SoftClassConflict addConflict(int state) {
		SoftClassConflict c = new SoftClassConflict(state);
		conflicts.add(c);
		return c;
	}

	public static class SoftClassConflict {
		private final int state;
		private LinkedHashSet<Rule> rules = new LinkedHashSet<Rule>();
		private ArrayList<Symbol> symbols = new ArrayList<Symbol>();

		public SoftClassConflict(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}

		public Rule[] getRules() {
			return rules.toArray(new Rule[rules.size()]);
		}

		public Symbol[] getSymbols() {
			Symbol[] result = symbols.toArray(new Symbol[symbols.size()]);
			Arrays.sort(result, new Comparator<Symbol>() {
				public int compare(Symbol o1, Symbol o2) {
					return o1.getIndex() < o2.getIndex() ? -1 : (o1.getIndex() == o2.getIndex() ? 0 : 1);
				}
			});
			return result;
		}

		public void addRule(Rule rule) {
			rules.add(rule);
		}

		public void addSymbol(Symbol sym) {
			symbols.add(sym);
		}
	}
}
