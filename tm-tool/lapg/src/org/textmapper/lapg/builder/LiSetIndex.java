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

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsSet.Operation;

import java.util.*;

/**
 * SetIndex maps all known sets that can be computed for symbols as well as those RhsSets
 * explicitly mentioned in the grammar into an integer interval.
 * <p/>
 * Supported symbol sets are: first, last, all, precede and follow.
 */
class LiSetIndex {

	private Map<RhsSet, Integer> sets;
	private List<RhsSet> sortedSets;
	private List<RhsSet> topLevelSets;
	private Set<RhsSet> namedSets;
	private LiSymbol[] symbols;
	private int terminals;
	private int nonterminals;
	private int size;

	LiSetIndex(LiSymbol[] symbols, int terminals, LiNamedSet[] namedSets) {
		this.symbols = symbols;
		this.terminals = terminals;
		this.nonterminals = symbols.length - terminals;
		this.namedSets = new HashSet<RhsSet>();
		sets = new HashMap<RhsSet, Integer>();
		sortedSets = new ArrayList<RhsSet>();
		topLevelSets = new ArrayList<RhsSet>();
		size = 5 * nonterminals + 2 * terminals;
		for (int i = terminals; i < symbols.length; i++) {
			traverse(((Nonterminal) symbols[i]).getDefinition());
		}
		for (LiNamedSet s : namedSets) {
			traverse(s.getSet());
			topLevelSets.add(s.getSet());
			this.namedSets.add(s.getSet());
		}
	}

	int index(RhsSet.Operation op, Symbol s) {
		switch (op) {
			case Any:
				return all((Nonterminal) s);
			case First:
				return first((Nonterminal) s);
			case Last:
				return last((Nonterminal) s);
			case Precede:
				return precede(s);
			case Follow:
				return follow(s);
		}
		throw new IllegalArgumentException("op");
	}

	RhsSet.Operation operation(int index) {
		if (isAll(index)) return Operation.Any;
		if (isFirst(index)) return Operation.First;
		if (isLast(index)) return Operation.Last;
		if (isFollow(index)) return Operation.Follow;
		if (isPrecede(index)) return Operation.Precede;
		throw new IllegalArgumentException("index");
	}

	/**
	 * [0..nonterminals]
	 */
	int first(Nonterminal symbol) {
		return symbol.getIndex() - terminals;
	}

	boolean isFirst(int index) {
		return index < nonterminals;
	}

	/**
	 * [nonterminals..2*nonterminals]
	 */
	int last(Nonterminal symbol) {
		return nonterminals + symbol.getIndex() - terminals;
	}

	boolean isLast(int index) {
		return index >= nonterminals && index < 2 * nonterminals;
	}

	/**
	 * [2*nonterminals..3*nonterminals]
	 */
	int all(Nonterminal symbol) {
		return 2 * nonterminals + symbol.getIndex() - terminals;
	}

	boolean isAll(int index) {
		return index >= 2 * nonterminals && index < 3 * nonterminals;
	}

	/**
	 * [3*nonterminals..4*nonterminals+terminals]
	 */
	int precede(Symbol symbol) {
		return 3 * nonterminals + symbol.getIndex();
	}

	boolean isPrecede(int index) {
		return index >= 3 * nonterminals && index < 4 * nonterminals + terminals;
	}

	/**
	 * [4*nonterminals+terminals..5*nonterminals+2*terminals]
	 */
	int follow(Symbol symbol) {
		return 4 * nonterminals + terminals + symbol.getIndex();
	}

	boolean isFollow(int index) {
		return index >= 4 * nonterminals + terminals && index < 5 * nonterminals + 2 * terminals;
	}

	/**
	 * Only for first, last and all.
	 */
	Nonterminal nonterminal(int index) {
		assert index >= 0 && index < 3 * nonterminals;
		return (Nonterminal) symbols[terminals + (index % nonterminals)];
	}

	/**
	 * Only for follow and precede.
	 */
	Symbol symbol(int index) {
		assert index >= 3 * nonterminals && index < 5 * nonterminals + 2 * terminals;
		return symbols[(index - 3 * nonterminals) % (terminals + nonterminals)];
	}

	Terminal terminal(int index) {
		assert index >= 0 && index < terminals;
		return (Terminal) symbols[index];
	}

	int terminals() {
		return terminals;
	}

	int set(RhsSet set) {
		Integer i = sets.get(set);
		assert i != null;
		return i;
	}

	// Returns indexed sets in topological order.
	Collection<RhsSet> sortedSets() {
		return sortedSets;
	}

	Collection<RhsSet> topLevelSets() {
		return topLevelSets;
	}

	boolean isRhs(RhsSet topLevelSet) {
		return !namedSets.contains(topLevelSet);
	}

	int size() {
		return size;
	}

	private void traverse(RhsSet set) {
		assert !sets.containsKey(set);
		sets.put(set, size++);
		RhsSet[] children = set.getSets();
		if (children != null) {
			for (RhsSet child : children) traverse(child);
		}
		sortedSets.add(set);
	}

	private void traverse(RhsPart p) {
		if (p == null) return;

		switch (p.getKind()) {
			case Assignment:
				traverse(((RhsAssignment) p).getPart());
				break;
			case Cast:
				traverse(((RhsCast) p).getPart());
				break;
			case Ignored:
				traverse(((RhsIgnored) p).getInner());
				break;
			case List:
				RhsList list = (RhsList) p;
				if (list.getCustomInitialElement() != null) traverse(list.getCustomInitialElement());
				if (list.getSeparator() != null) traverse(list.getSeparator());
				traverse(list.getElement());
				break;
			case Optional:
				traverse(((RhsOptional) p).getPart());
				break;
			case Unordered:
				for (RhsPart inner : ((RhsUnordered) p).getParts()) {
					traverse(inner);
				}
				break;
			case Choice:
				for (RhsPart inner : ((RhsChoice) p).getParts()) {
					traverse(inner);
				}
				break;
			case Sequence:
				for (RhsPart inner : ((RhsSequence) p).getParts()) {
					traverse(inner);
				}
				break;
			case Set:
				traverse((RhsSet) p);
				topLevelSets.add((RhsSet) p);
				break;
			case Symbol:
				break;
			case Conditional:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}
}
