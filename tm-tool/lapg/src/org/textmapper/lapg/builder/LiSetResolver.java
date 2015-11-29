/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
import org.textmapper.lapg.api.Problem;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsSet.Operation;
import org.textmapper.lapg.util.ArrayIterable;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;
import java.util.stream.Collectors;

class LiSetResolver {
	private static final Descriptor SENTINEL = new Descriptor(-1, SetsClosure.EMPTY_ARRAY);

	private static class Descriptor {
		private int set;                // positive
		private int[] dependencies;        // contains indices in "sets" field

		private Descriptor(int set, int[] dependencies) {
			this.set = set;
			this.dependencies = dependencies;
		}
	}

	private LiSetIndex index;
	private SetsClosure closure;
	private Descriptor[] sets;
	private LiNamedSet[] namedSets;

	private SetBuilder terminalsSet;
	private SetBuilder dependenciesSet;
	private Map<Symbol, Set<LiNonterminal>> usages;

	public LiSetResolver(LiSymbol[] symbols, int terminals, LiNamedSet[] namedSets) {
		index = new LiSetIndex(symbols, terminals, namedSets);
		closure = new SetsClosure();
		sets = new Descriptor[index.size()];
		terminalsSet = new SetBuilder(terminals);
		dependenciesSet = new SetBuilder(index.size());
		this.namedSets = namedSets;
	}

	private void scheduleDependencies(Descriptor d, Queue<Integer> queue,
									  List<Descriptor> postProcess) {
		for (int dep : d.dependencies) {
			if (sets[dep] == null) {
				sets[dep] = SENTINEL;
				queue.add(dep);
			}
		}
		postProcess.add(d);
	}

	public void resolve(ExpansionContext expansionContext, List<Problem> problems) {
		Queue<Integer> queue = new LinkedList<>();
		List<Descriptor> postProcess = new ArrayList<>();

		for (RhsSet set : index.sortedSets()) {
			int i = index.set(set);
			assert sets[i] == null;
			sets[i] = extractSet(set);
			scheduleDependencies(sets[i], queue, postProcess);
		}

		Integer item;
		while ((item = queue.poll()) != null) {
			int i = item;
			assert sets[i] == SENTINEL;
			Operation op = index.operation(i);
			switch (op) {
				case Any:
				case First:
				case Last: {
					RhsPart part = index.nonterminal(i).getDefinition();
					collectTerminals(part, op);
					sets[i] = new Descriptor(closure.addSet(terminalsSet.create(), part),
							dependenciesSet.create());
					scheduleDependencies(sets[i], queue, postProcess);
					break;
				}
				case Follow:
				case Precede: {
					Symbol symbol = index.symbol(i);
					boolean reverse = (op == Operation.Precede);
					for (LiNonterminal left : getUsages(symbol)) {
						if (collectAdjacent(symbol, left.getDefinition(), reverse, false)) {
							dependenciesSet.add(index.index(op, left));
						}
					}
					sets[i] = new Descriptor(closure.addSet(terminalsSet.create(), symbol),
							dependenciesSet.create());
					scheduleDependencies(sets[i], queue, postProcess);
					break;
				}
			}
		}

		for (Descriptor d : postProcess) {
			if (d.dependencies.length == 0) continue;

			int[] converted = new int[d.dependencies.length];
			for (int i = 0; i < converted.length; i++) {
				int dep = d.dependencies[i];
				assert sets[dep] != null;
				converted[i] = sets[dep].set;
			}
			closure.addDependencies(d.set, converted);
		}

		if (!closure.compute()) {
			Set<RhsSet> problemSets = new HashSet<>();
			List<RhsPart> errors = new ArrayList<>();
			for (Object errorNode : closure.getErrorNodes()) {
				if (errorNode instanceof RhsSet) {
					problemSets.add((RhsSet) errorNode);
				} else if (errorNode instanceof RhsPart) {
					errors.add((RhsPart) errorNode);
				}
			}
			for (RhsSet set : index.topLevelSets()) {
				traverseProblemSets(set, problemSets, errors);
			}
			problems.addAll(errors.stream()
					.map(error -> new LiProblem(error,
							"Cannot resolve set, since it recursively depends on itself."))
					.collect(Collectors.toList()));
			return;
		}

		for (RhsSet set : index.topLevelSets()) {
			Terminal[] result = getResolvedElements(set);
			if (index.isRhs(set)) {
				if (result.length == 0) {
					problems.add(new LiProblem(set, "Set is empty."));
					continue;
				}
				expansionContext.addSet(set, result);
			}
		}
		for (LiNamedSet namedSet : namedSets) {
			Terminal[] resolvedElements = getResolvedElements(namedSet.getSet());
			int[] res = new int[resolvedElements.length];
			for (int i = 0; i < resolvedElements.length; i++) {
				res[i] = resolvedElements[i].getIndex();
			}
			namedSet.setElements(res);
		}
	}

	private Iterable<LiNonterminal> getUsages(Symbol s) {
		if (usages == null) {
			usages = new HashMap<>();
			for (LiSymbol left : index.getSymbols()) {
				if (!(left instanceof Nonterminal)) continue;
				for (RhsSymbol ref : RhsUtil.getRhsSymbols(((Nonterminal) left).getDefinition())) {
					Symbol target = ref.getTarget();
					assert target != null;
					Set<LiNonterminal> nonterminals = usages.get(target);
					if (nonterminals == null) {
						nonterminals = new LinkedHashSet<>();
						usages.put(target, nonterminals);
					}
					nonterminals.add((LiNonterminal) left);
				}
			}
		}

		Set<LiNonterminal> result = usages.get(s);
		if (result == null) result = Collections.emptySet();
		return result;
	}

	private Terminal[] getResolvedElements(RhsSet set) {
		int i = index.set(set);
		assert sets[i] != null;
		int[] resultSet = closure.getSet(sets[i].set);
		boolean complement = closure.isComplement(sets[i].set);
		Terminal[] result;
		if (complement) {
			result = new Terminal[index.terminals() - resultSet.length];
			int k = 0;
			for (int e = 0; e < index.terminals(); e++) {
				if (k < resultSet.length && e == resultSet[k]) {
					k++;
					continue;
				}
				result[e - k] = index.terminal(e);
			}
		} else {
			result = new Terminal[resultSet.length];
			for (int e = 0; e < resultSet.length; e++) {
				result[e] = index.terminal(resultSet[e]);
			}
		}
		return result;
	}

	private Descriptor extractSet(RhsSet set) {
		switch (set.getOperation()) {
			case Any:
			case First:
			case Last: {
				Symbol target = set.getSymbol();
				if (target.isTerm()) {
					terminalsSet.add(target.getIndex());
				} else {
					dependenciesSet.add(index.index(set.getOperation(), target));
				}
				return new Descriptor(closure.addSet(terminalsSet.create(), set),
						dependenciesSet.create());
			}
			case Precede:
			case Follow:
				dependenciesSet.add(index.index(set.getOperation(), set.getSymbol()));
				return new Descriptor(closure.addSet(terminalsSet.create(), set),
						dependenciesSet.create());
			case Complement: {
				assert set.getSets().length == 1;
				int targetIndex = index.set(set.getSets()[0]);
				assert sets[targetIndex] != null;
				return new Descriptor(closure.complement(sets[targetIndex].set, set),
						SetsClosure.EMPTY_ARRAY);
			}
			case Union:
				for (RhsSet child : set.getSets()) {
					int targetIndex = index.set(child);
					assert sets[targetIndex] != null;
					dependenciesSet.add(targetIndex);
				}
				return new Descriptor(closure.addSet(SetsClosure.EMPTY_ARRAY, set),
						dependenciesSet.create());
			case Intersection: {
				RhsSet[] children = set.getSets();
				int[] childSets = new int[children.length];
				for (int i = 0; i < children.length; i++) {
					int targetIndex = index.set(children[i]);
					assert sets[targetIndex] != null;
					childSets[i] = sets[targetIndex].set;
				}
				return new Descriptor(closure.addIntersection(childSets, set),
						SetsClosure.EMPTY_ARRAY);
			}
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * Only first, last and all are supported.
	 */
	private void collectTerminals(RhsPart p, Operation op) {
		if (p == null) throw new IllegalStateException();

		switch (p.getKind()) {
			case Assignment:
				collectTerminals(((RhsAssignment) p).getPart(), op);
				break;
			case Cast:
				collectTerminals(((RhsCast) p).getPart(), op);
				break;
			case Ignored:
				collectTerminals(((RhsIgnored) p).getInner(), op);
				break;
			case List:
				RhsList list = (RhsList) p;
				for (RhsSequence s : list.asRules()) {
					collectTerminals(s, op);
				}
				break;
			case Optional:
				collectTerminals(((RhsOptional) p).getPart(), op);
				break;
			case Unordered:
				for (RhsPart inner : ((RhsUnordered) p).getParts()) {
					collectTerminals(inner, op);
				}
				break;
			case Choice:
				for (RhsPart inner : ((RhsChoice) p).getParts()) {
					collectTerminals(inner, op);
				}
				break;
			case Sequence: {
				RhsPart[] parts = ((RhsSequence) p).getParts();
				for (RhsPart inner : new ArrayIterable<>(parts, op == Operation.Last)) {
					collectTerminals(inner, op);

					if (op != Operation.Any && !RhsUtil.isNullable(inner, null)) break;
				}
				break;
			}
			case Set:
				dependenciesSet.add(index.set((RhsSet) p));
				break;
			case Symbol:
				Symbol target = ((RhsSymbol) p).getTarget();
				if (target.isTerm()) {
					terminalsSet.add(target.getIndex());
				} else {
					dependenciesSet.add(index.index(op, target));
				}
				break;
			case Conditional:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * Only precede and follow are supported.
	 */
	private boolean collectAdjacent(Symbol symbol, RhsPart p, boolean reverse, boolean isAfter) {
		if (p == null) throw new IllegalStateException();

		switch (p.getKind()) {
			case Assignment:
				return collectAdjacent(symbol, ((RhsAssignment) p).getPart(), reverse, isAfter);
			case Cast:
				return collectAdjacent(symbol, ((RhsCast) p).getPart(), reverse, isAfter);
			case Ignored:
				return collectAdjacent(symbol, ((RhsIgnored) p).getInner(), reverse, isAfter);
			case Optional:
				return collectAdjacent(symbol, ((RhsOptional) p).getPart(), reverse, isAfter)
						|| isAfter;
			case List: {
				RhsList list = (RhsList) p;
				boolean result = false;
				for (RhsSequence s : list.asRules()) {
					result |= collectAdjacent(symbol, s, reverse, isAfter);
				}
				return result;
			}
			case Choice: {
				boolean result = false;
				for (RhsPart inner : ((RhsChoice) p).getParts()) {
					result |= collectAdjacent(symbol, inner, reverse, isAfter);
				}
				return result;
			}
			case Unordered: {
//				boolean result = false;
//				for (RhsPart inner : ((RhsUnordered) p).getParts()) {
//					result |= collectAdjacent(symbol, inner, reverse, isAfter);
//				}
//				if (result && !isAfter) {
//					for (RhsPart inner : ((RhsUnordered) p).getParts()) {
//						collectAdjacent(symbol, inner, reverse, true);
//					}
//				}
//				return result;
				// TODO!!!
				throw new UnsupportedOperationException();
			}
			case Sequence: {
				RhsPart[] parts = ((RhsSequence) p).getParts();
				for (RhsPart inner : new ArrayIterable<>(parts, reverse)) {
					isAfter = collectAdjacent(symbol, inner, reverse, isAfter);
				}
				return isAfter;
			}
			case Set:
				if (isAfter) {
					RhsSet set = (RhsSet) p;
					dependenciesSet.add(index.set(set));
				}
				if (symbol.isTerm()) {
					// TODO!!!
					throw new UnsupportedOperationException();
				}
				return false;
			case Symbol:
				Symbol target = ((RhsSymbol) p).getTarget();
				if (isAfter) {
					if (target.isTerm()) {
						terminalsSet.add(target.getIndex());
					} else {
						dependenciesSet.add(
								index.index(reverse ? Operation.Last : Operation.First, target));
					}
				}
				return target == symbol;
			case Conditional:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * Copies to errors only topmost problem sets.
	 */
	private static void traverseProblemSets(RhsSet set, Set<RhsSet> problemSets,
											List<RhsPart> errors) {
		if (problemSets.contains(set)) {
			errors.add(set);
			return;
		}

		RhsSet[] children = set.getSets();
		if (children == null) return;

		for (RhsSet child : children) {
			traverseProblemSets(child, problemSets, errors);
		}
	}
}
