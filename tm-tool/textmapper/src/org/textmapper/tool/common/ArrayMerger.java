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
package org.textmapper.tool.common;

import java.util.*;
import java.util.function.IntFunction;

/**
 *  Merges several arrays into one respecting the order of elements in each array.
 */
public class ArrayMerger<T extends Comparable<T>> {

	private final List<T> current = new ArrayList<>();
	private final Map<T, Node> graph = new HashMap<>();

	public ArrayMerger() {
	}

	public void add(T... elements) {
		for (T element : elements) {
			current.add(element);
			if (!graph.containsKey(element)) {
				graph.put(element, new Node(element));
			}
		}
	}

	public void flush() {
		T prev = null;
		for (T element : current) {
			if (prev != null) {
				graph.get(element).after.add(graph.get(prev));
			}
			prev = element;
		}
		current.clear();
	}

	/**
	 * Same as topoSort, but returns null if the resulting order is not unique.
	 */
	public T[] uniqueSort(IntFunction<T[]> generator) {
		if (graph.isEmpty()) return generator.apply(0);

		Node last = null;
		for (Node node : graph.values()) {
			node.computeHeight();
			if (node.height == graph.size()) {
				last = node;
			}
		}
		if (last == null) return null;
		T[] arr = generator.apply(graph.size());
		while (last != null) {
			arr[last.height - 1] = last.element;
			last = last.next();
		}
		return arr;
	}

	/**
	 * Returns null in case of cycles.
	 */
	public T[] topoSort(IntFunction<T[]> generator) {
		if (graph.isEmpty()) return generator.apply(0);

		int maxHeight = 0;
		for (Node node : graph.values()) {
			node.computeHeight();
			if (node.height < 1) {
				return null;
			}
			maxHeight = Math.max(maxHeight, node.height);
		}
		assert maxHeight > 0;
		int[] buckets = new int[maxHeight];
		for (Node node : graph.values()) {
			buckets[node.height-1]++;
		}
		int acc = 0;
		for (int i = 0; i < buckets.length; i++) {
			int start = acc;
			acc += buckets[i];
			buckets[i] = start;
		}
		T[] result = generator.apply(graph.size());
		for (Node node : graph.values()) {
			int index = buckets[node.height - 1]++;
			result[index] = node.element;
		}
		acc = 0;
		for (int bucket : buckets) {
			Arrays.sort(result, acc, bucket);
			acc = bucket;
		}
		return result;
	}

	private class Node {
		T element;
		List<Node> after = new ArrayList<>();
		int height = -1;
		boolean visiting = false;

		public Node(T element) {
			this.element = element;
		}

		Node next() {
			for (Node node : after) {
				if (node.height == height - 1) {
					return node;
				}
			}
			return null;
		}

		void computeHeight() {
			if (visiting) {
				height = -2;
				return;
			}
			if (height != -1) {
				return;
			}
			visiting = true;
			height = 1;
			for (Node node : after) {
				node.computeHeight();
				if (node.height == -2) {
					height = -2;
					break;
				}
				height = Math.max(height, node.height + 1);
			}
			visiting = false;
		}
	}
}
