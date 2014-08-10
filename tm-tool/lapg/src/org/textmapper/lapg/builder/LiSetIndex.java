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

import java.util.*;

class LiSetIndex {

	private Map<RhsSet, Integer> sets;
	private List<RhsSet> sortedSets;
	private List<RhsSet> topLevelSets;
	private LiSymbol[] symbols;
	private int terminals;
	private int nonterminals;
	private int size;

	LiSetIndex(LiSymbol[] symbols, int terminals) {
		this.symbols = symbols;
		this.terminals = terminals;
		this.nonterminals = symbols.length - terminals;
		sets = new HashMap<RhsSet, Integer>();
		sortedSets = new ArrayList<RhsSet>();
		topLevelSets = new ArrayList<RhsSet>();
		size = nonterminals * 3 + terminals;
		for (int i = terminals; i < symbols.length; i++) {
			traverse(((Nonterminal) symbols[i]).getDefinition());
		}
	}

	int first(Symbol symbol) {
		assert !symbol.isTerm();
		return symbol.getIndex() - terminals;
	}

	boolean isFirst(int index) {
		return index < nonterminals;
	}

	int all(Symbol symbol) {
		assert !symbol.isTerm();
		return nonterminals + symbol.getIndex() - terminals;
	}

	boolean isAll(int index) {
		return index >= nonterminals && index < 2 * nonterminals;
	}

	int follow(Symbol symbol) {
		return 2 * nonterminals + symbol.getIndex();
	}

	boolean isFollow(int index) {
		return index >= 2 * nonterminals && index < 3 * nonterminals + terminals;
	}

	Nonterminal nonterminal(int index) {
		assert index >= 0 && index < 2 * nonterminals;
		return (Nonterminal) symbols[terminals + (index % nonterminals)];
	}

	Symbol symbol(int index) {
		assert index >= 2 * nonterminals && index < 3 * nonterminals + terminals;
		return symbols[index - 2 * nonterminals];
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
			default:
				throw new IllegalStateException();
		}
	}
}
