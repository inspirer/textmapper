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

import org.textmapper.lapg.api.Problem;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsSet.Operation;
import org.textmapper.lapg.util.RhsUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class LiSetResolver {

	private static final int[] EMPTY_ARRAY = new int[0];
	private static final Descriptor SENTINEL = new Descriptor(-1, EMPTY_ARRAY);

	private static class Descriptor {
		private int set;      			// positive
		private int[] dependencies;		// contains indices in "sets" field

		private Descriptor(int set, int[] dependencies) {
			this.set = set;
			this.dependencies = dependencies;
		}
	}

	private LiSetIndex index;
	private SetsClosure closure;
	private Descriptor[] sets;

	private SetBuilder terminalsSet;
	private SetBuilder dependenciesSet;

	public LiSetResolver(LiSymbol[] symbols, int terminals) {
		index = new LiSetIndex(symbols, terminals);
		closure = new SetsClosure();
		sets = new Descriptor[index.size()];
		terminalsSet = new SetBuilder(terminals);
		dependenciesSet = new SetBuilder(index.size());
	}

	private void scheduleDependencies(Descriptor d, Queue<Integer> queue, List<Descriptor> postProcess) {
		for (int dep : d.dependencies) {
			if (sets[dep] == null) {
				sets[dep] = SENTINEL;
				queue.add(dep);
			}
		}
		postProcess.add(d);
	}

	public void resolve(ExpansionContext expansionContext, List<Problem> problems) {
		Queue<Integer> queue = new LinkedList<Integer>();
		List<Descriptor> postProcess = new ArrayList<Descriptor>();

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
			if (index.isFirst(i) || index.isAll(i)) {
				RhsPart part = index.nonterminal(i).getDefinition();
				collectFirstOrAll(part, index.isFirst(i));
				sets[i] = new Descriptor(closure.addSet(terminalsSet.create(), part), dependenciesSet.create());
				scheduleDependencies(sets[i], queue, postProcess);
			} else if (index.isFollow(i)) {
				// TODO handle follow
				problems.add(new LiProblem(index.symbol(i),
						"Follow sets are not supported yet."));
			} else {
				throw new IllegalStateException();
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
			for (Object errorNode : closure.getErrorNodes()) {
				problems.add(new LiProblem((RhsPart) errorNode,
						"Cannot resolve set, since it recursively depends on itself."));
			}
			return;
		}

		for (RhsSet set : index.topLevelSets()) {
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
			if (result.length == 0) {
				problems.add(new LiProblem(set, "Set is empty."));
				continue;
			}
			expansionContext.addSet(set, result);
		}
	}

	private Descriptor extractSet(RhsSet set) {
		switch (set.getOperation()) {
			case Any:
			case First: {
				Symbol target = set.getSymbol();
				if (target.isTerm()) {
					terminalsSet.add(target.getIndex());
				} else if (set.getOperation() == Operation.Any) {
					dependenciesSet.add(index.all(target));
				} else {
					dependenciesSet.add(index.first(target));
				}
				return new Descriptor(closure.addSet(terminalsSet.create(), set), dependenciesSet.create());
			}
			case Follow:
				dependenciesSet.add(index.follow(set.getSymbol()));
				return new Descriptor(closure.addSet(terminalsSet.create(), set), dependenciesSet.create());
			case Complement: {
				assert set.getSets().length == 1;
				int targetIndex = index.set(set.getSets()[0]);
				assert sets[targetIndex] != null;
				return new Descriptor(closure.complement(sets[targetIndex].set, set), EMPTY_ARRAY);
			}
			case Union:
				for (RhsSet child : set.getSets()) {
					int targetIndex = index.set(child);
					assert sets[targetIndex] != null;
					dependenciesSet.add(targetIndex);
				}
				return new Descriptor(closure.addSet(EMPTY_ARRAY, set), dependenciesSet.create());
			case Intersection: {
				RhsSet[] children = set.getSets();
				int[] childSets = new int[children.length];
				for (int i = 0; i < children.length; i++) {
					int targetIndex = index.set(children[i]);
					assert sets[targetIndex] != null;
					childSets[i] = sets[targetIndex].set;
				}
				return new Descriptor(closure.addIntersection(childSets, set), EMPTY_ARRAY);
			}
			default:
				throw new IllegalStateException();
		}
	}

	private void collectFirstOrAll(RhsPart p, boolean onlyFirst) {
		if (p == null) throw new IllegalStateException();

		switch (p.getKind()) {
			case Assignment:
				collectFirstOrAll(((RhsAssignment) p).getPart(), onlyFirst);
				break;
			case Cast:
				collectFirstOrAll(((RhsCast) p).getPart(), onlyFirst);
				break;
			case Ignored:
				collectFirstOrAll(((RhsIgnored) p).getInner(), onlyFirst);
				break;
			case List:
				RhsList list = (RhsList) p;
				RhsSequence initialElement = list.getCustomInitialElement();
				if (initialElement != null && !list.isRightRecursive()) {
					collectFirstOrAll(initialElement, onlyFirst);
					if (onlyFirst && !RhsUtil.isNullable(initialElement, null)) break;
					collectFirstOrAll(list.getSeparator(), onlyFirst);
					if (onlyFirst && !RhsUtil.isNullable(list.getSeparator(), null)) break;
				}
				collectFirstOrAll(list.getElement(), onlyFirst);
				if (onlyFirst && !RhsUtil.isNullable(list.getElement(), null)) break;
				collectFirstOrAll(list.getSeparator(), onlyFirst);
				if (initialElement != null && list.isRightRecursive()) {
					if (onlyFirst && !RhsUtil.isNullable(list.getSeparator(), null)) break;
					collectFirstOrAll(initialElement, onlyFirst);
				}
				break;
			case Optional:
				collectFirstOrAll(((RhsOptional) p).getPart(), onlyFirst);
				break;
			case Unordered:
				for (RhsPart inner : ((RhsUnordered) p).getParts()) {
					collectFirstOrAll(inner, onlyFirst);
				}
				break;
			case Choice:
				for (RhsPart inner : ((RhsChoice) p).getParts()) {
					collectFirstOrAll(inner, onlyFirst);
				}
				break;
			case Sequence:
				for (RhsPart inner : ((RhsSequence) p).getParts()) {
					collectFirstOrAll(inner, onlyFirst);

					if (onlyFirst && !RhsUtil.isNullable(inner, null)) break;
				}
				break;
			case Set:
				dependenciesSet.add(index.set((RhsSet) p));
				break;
			case Symbol:
				Symbol target = ((RhsSymbol) p).getTarget();
				if (target.isTerm()) {
					terminalsSet.add(target.getIndex());
				} else if (onlyFirst) {
					dependenciesSet.add(index.first(target));
				} else {
					dependenciesSet.add(index.all(target));
				}
				break;
			default:
				throw new IllegalStateException();
		}
	}
}
