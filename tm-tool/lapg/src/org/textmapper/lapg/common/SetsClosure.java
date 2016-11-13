/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes a transitive closure of a set of sets based on the given relations.
 */
public class SetsClosure {
	public static final int[] EMPTY_ARRAY = new int[0];

	private final IntegerSets sets = new IntegerSets();
	private final List<SetNode> nodes = new ArrayList<>();
	private int[][] graph;
	private int[] nodeSet;
	private BitSet isIntersection;

	public SetsClosure() {
	}

	/**
	 * Returns a new 'node' for the given set, which can be used to add dependencies
	 * and, after processing, get the result by using getSet() and isComplement().
	 */
	public int addSet(int[] set, Object origin) {
		int i = sets.add(set);
		nodes.add(new SetNode(i, origin));
		return nodes.size() - 1;
	}

	public int addIntersection(int[] nodes, Object origin) {
		SetNode result = new SetNode(SetNode.INTERSECTION, origin);
		this.nodes.add(result);
		result.setEdges(nodes);
		return this.nodes.size() - 1;
	}

	public void addDependencies(int node, int... source) {
		assert node >= 0 && node < nodes.size();
		nodes.get(node).setEdges(source);
	}

	public int complement(int node, Object origin) {
		SetNode result = new SetNode(IntegerSets.EMPTY_SET, origin);
		this.nodes.add(result);
		result.setEdges(-1 - node);
		return nodes.size() - 1;
	}

	public boolean compute() {
		int size = nodes.size();
		graph = new int[size][];
		nodeSet = new int[size];
		isIntersection = new BitSet(size);
		for (int i = 0; i < size; i++) {
			graph[i] = nodes.get(i).edges;
			if (graph[i] == null) graph[i] = EMPTY_ARRAY;
			nodeSet[i] = nodes.get(i).index;
			if (nodeSet[i] == SetNode.INTERSECTION) {
				nodeSet[i] = IntegerSets.EMPTY_SET;
				isIntersection.set(i, true);
			}
			assert nodeSet[i] >= 0;
		}
		return new TransitiveClosure().run();
	}

	public boolean isComplement(int node) {
		assert node >= 0 && node < nodes.size();
		return nodeSet[node] < 0;
	}

	public int[] getSet(int node) {
		assert node >= 0 && node < nodes.size();
		int set = nodeSet[node];
		return sets.sets[set < 0 ? sets.complement(set) : set];
	}

	/**
	 * Extracts the results of 'node' into 'set'. Values are expected to
	 * be in the range of [0, max).
	 */
	public void exportIntoBitset(int node, int max, BitSet set) {
		if (max < 1 || max > set.size()) throw new IllegalArgumentException();

		int[] resultSet = getSet(node);
		boolean complement = isComplement(node);

		set.set(0, max, complement);
		for (int value : resultSet) {
			if (value >= max) {
				throw new IndexOutOfBoundsException("bitset is too small for " + value);
			}
			set.set(value, !complement);
		}
	}

	public Object[] getErrorNodes() {
		List<Object> list = nodes.stream()
				.filter(node -> node.isError)
				.map(node -> node.origin)
				.collect(Collectors.toList());

		return list.toArray();
	}

	private class TransitiveClosure {
		private int[] stack;
		private int[] index;
		private int[] lowlink;
		private boolean[] onstack;
		private int current = 0;
		private int top = 0;
		private boolean hasErrors = false;

		public TransitiveClosure() {
			index = new int[graph.length];
			Arrays.fill(index, -1);
			lowlink = new int[graph.length];
			Arrays.fill(lowlink, 0);
			onstack = new boolean[graph.length];
			Arrays.fill(onstack, false);
			stack = new int[graph.length];
		}

		private boolean run() {
			if (graph.length < 2) return true;

			for (int i = 0; i < graph.length; i++) {
				if (index[i] == -1) strongConnect(i);
			}

			return !hasErrors;
		}

		private void strongConnect(int v) {
			index[v] = current;
			lowlink[v] = current;
			current++;
			stack[top++] = v;
			onstack[v] = true;
			for (int w : graph[v]) {
				if (w < 0) w = -1 - w;
				if (index[w] == -1) {
					strongConnect(w);
					lowlink[v] = Math.min(lowlink[v], lowlink[w]);
				} else if (onstack[w]) {
					lowlink[v] = Math.min(lowlink[v], index[w]);
				}
			}
			if (lowlink[v] == index[v]) {
				int stackSize = top;
				do {
					top--;
				} while (stack[top] != v);
				closure(stackSize - top);
				for (int i = top; i < stackSize; i++) {
					onstack[stack[i]] = false;
				}
			}
		}

		private void slowClosure(int size) {
			boolean dirty = true;
			while (dirty) {
				dirty = false;
				for (int i = top; i < top + size; i++) {
					int node = stack[i];
					boolean shouldInvalidate = false;
					boolean intersect = isIntersection.get(node);
					int result =
							intersect ? sets.complement(IntegerSets.EMPTY_SET) : nodeSet[node];
					for (int w : graph[node]) {
						int targetNode = w < 0 ? -w - 1 : w;
						int targetSet =
								w < 0 ? sets.complement(nodeSet[targetNode]) : nodeSet[targetNode];
						shouldInvalidate |= onstack[targetNode];
						if (w < 0 && onstack[targetNode]) {
							// Complements cannot be part of a dependency cycle.
							nodes.get(node).markAsError();
							hasErrors = true;
							break;
						}
						result = intersect
								? sets.intersection(result, targetSet)
								: sets.union(result, targetSet);
					}
					if (shouldInvalidate) dirty |= (nodeSet[node] != result);
					nodeSet[node] = result;
				}
				if (hasErrors) break;
			}
		}

		private void closure(int size) {
			for (int i = top; i < top + size; i++) {
				if (isIntersection.get(stack[i])) {
					slowClosure(size);
					return;
				}
			}

			// Simple union (no intersections).
			int result = IntegerSets.EMPTY_SET;
			for (int i = top; i < top + size; i++) {
				int node = stack[i];
				result = sets.union(result, nodeSet[node]);
				for (int w : graph[node]) {
					int targetNode = w < 0 ? -w - 1 : w;
					int targetSet =
							w < 0 ? sets.complement(nodeSet[targetNode]) : nodeSet[targetNode];
					if (w < 0 && onstack[targetNode]) {
						// Complements cannot be part of a dependency cycle.
						nodes.get(node).markAsError();
						hasErrors = true;
						break;
					}
					if (onstack[targetNode]) continue;
					result = sets.union(result, targetSet);
				}
			}

			if (hasErrors) return;

			// Update all nodes.
			for (int i = top; i < top + size; i++) {
				nodeSet[stack[i]] = result;
			}
		}
	}

	private static class SetNode {
		public static final int INTERSECTION = -1;

		public final int index;    // -1 for intersection nodes, index in sets otherwise
		public final Object origin;
		private int[] edges;  // negative edges mean complement of the target set
		private boolean isError;

		private SetNode(int index, Object origin) {
			this.index = index;
			this.origin = origin;
		}

		public void setEdges(int... target) {
			if (edges != null) throw new IllegalStateException();

			edges = Arrays.copyOf(target, target.length);
		}

		public void markAsError() {
			isError = true;
		}
	}
}
