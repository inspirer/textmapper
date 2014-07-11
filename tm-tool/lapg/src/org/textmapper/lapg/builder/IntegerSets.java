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

import java.util.Arrays;

/**
 * Gryaznov Evgeny, 8/17/11
 * <p/>
 * Effectively stores and merges integer sets (represented as sorted arrays)
 */
final class IntegerSets {

	public static final int EMPTY_SET = 0;
	public int[][] sets = new int[512][];

	private HashEntry[] htable = new HashEntry[997];
	private int count = 0;

	public IntegerSets() {
		push(0, new int[]{});
	}

	public int add(int... set) {
		if (set.length == 0) return EMPTY_SET;
		int hash = hashCode(set);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && Arrays.equals(sets[bucket.index], set)) {
				return bucket.index;
			}
		}
		return push(hash, set);
	}

	public boolean contains(int i, int element) {
		if (i == EMPTY_SET) return false;
		if (i < 0) return !contains(complement(i), element);

		int[] set = sets[i];
		if (set.length <= 4) {
			for (int e : set) {
				if (e == element) return true;
			}
			return false;
		} else {
			return Arrays.binarySearch(set, element) >= 0;
		}
	}

	public int intersection(int i1, int i2) {
		if (i1 == EMPTY_SET || i2 == EMPTY_SET) return EMPTY_SET;

		if (i1 < 0 && i2 < 0) {
			return complement(union(complement(i1), complement(i2)));
		} else if (i1 < 0) {
			return subtract(i2, complement(i1));
		} else if (i2 < 0) {
			return subtract(i1, complement(i2));
		}

		int hash = hashCodeIntersect(sets[i1], sets[i2]);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && equalsIntersect(sets[bucket.index], sets[i1], sets[i2])) {
				return bucket.index;
			}
		}
		int[] set = intersectArrays(sets[i1], sets[i2]);
		if (set == null) return EMPTY_SET;
		return push(hash, set);
	}

	public int union(int i1, int i2) {
		if (i1 == EMPTY_SET) return i2;
		if (i2 == EMPTY_SET) return i1;

		if (i1 < 0 && i2 < 0) {
			return complement(intersection(complement(i1), complement(i2)));
		} else if (i1 < 0) {
			return complement(subtract(complement(i1), i2));
		} else if (i2 < 0) {
			return complement(subtract(complement(i2), i1));
		}

		int hash = hashCodeMerge(sets[i1], sets[i2]);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && equalsMerge(sets[bucket.index], sets[i1], sets[i2])) {
				return bucket.index;
			}
		}
		int[] set = mergeArrays(sets[i1], sets[i2]);
		return push(hash, set);
	}

	private int subtract(int i1, int i2) {
		if (i1 == EMPTY_SET || i2 == EMPTY_SET) return i1;

		assert i1 >= 0 && i2 >= 0;
		int hash = hashCodeSubtract(sets[i1], sets[i2]);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && equalsSubtract(sets[bucket.index], sets[i1], sets[i2])) {
				return bucket.index;
			}
		}
		int[] set = subtractArrays(sets[i1], sets[i2]);
		return push(hash, set);
	}

	public int complement(int set) {
		return -1 - set;
	}

	private int push(int hash, int[] set) {
		if (set.length == 0 && count > 0) return EMPTY_SET;
		if (count >= sets.length) {
			int[][] nn = new int[sets.length * 2][];
			System.arraycopy(sets, 0, nn, 0, count);
			sets = nn;
		}
		sets[count] = set;
		HashEntry hashEntry = new HashEntry();
		hashEntry.index = count++;
		hashEntry.hash = hash;
		hashEntry.next = htable[toHashIndex(hash)];
		htable[toHashIndex(hash)] = hashEntry;
		return hashEntry.index;
	}

	private int toHashIndex(int hash) {
		return Math.abs(hash) % htable.length;
	}

	private static class HashEntry {
		int index;
		int hash;
		HashEntry next;
	}

	private static int hashCode(int a[]) {
		int result = 1;
		for (int element : a)
			result = 31 * result + element;

		return result;
	}

	private static int hashCodeSubtract(int a[], int b[]) {
		int result = 1;
		for (int ai = 0, bi = 0; ai < a.length; ai++) {
			while (bi < b.length && b[bi] < a[ai]) bi++;
			if (bi < b.length && a[ai] == b[bi]) continue;

			result = 31 * result + a[ai];
		}

		return result;
	}

	private static int[] subtractArrays(int a[], int b[]) {
		int len = 0;
		for (int ai = 0, bi = 0; ai < a.length; ai++) {
			while (bi < b.length && b[bi] < a[ai]) bi++;
			if (bi < b.length && a[ai] == b[bi]) continue;
			len++;
		}
		int[] result = new int[len];
		int i = 0;
		for (int ai = 0, bi = 0; ai < a.length; ai++) {
			while (bi < b.length && b[bi] < a[ai]) bi++;
			if (bi < b.length && a[ai] == b[bi]) continue;

			result[i++] = a[ai];
		}

		assert i == len;
		return result;
	}

	private static boolean equalsSubtract(int[] expected, int[] a, int[] b) {
		int length = expected.length;
		int index = 0;

		for (int ai = 0, bi = 0; ai < a.length; ai++) {
			while (bi < b.length && b[bi] < a[ai]) bi++;
			if (bi < b.length && a[ai] == b[bi]) continue;
			if (index == length || expected[index++] != a[ai]) return false;
		}
		return index == length;

	}

	private static int hashCodeMerge(int a[], int b[]) {
		int result = 1;
		for (int ai = 0, bi = 0; ai < a.length || bi < b.length; ) {
			if (ai < a.length && bi < b.length && a[ai] == b[bi]) bi++;
			int element = ai < a.length && (bi >= b.length || a[ai] <= b[bi])
					? a[ai++]
					: b[bi++];

			result = 31 * result + element;
		}

		return result;
	}

	private static int[] mergeArrays(int a[], int b[]) {
		int len = 0;
		for (int ai = 0, bi = 0; ai < a.length || bi < b.length; ) {
			if (ai < a.length && bi < b.length && a[ai] == b[bi]) bi++;
			if (ai < a.length && (bi >= b.length || a[ai] <= b[bi]))
				ai++;
			else bi++;
			len++;
		}
		int[] result = new int[len];
		int i = 0;
		for (int ai = 0, bi = 0; ai < a.length || bi < b.length; ) {
			if (ai < a.length && bi < b.length && a[ai] == b[bi]) bi++;
			int element = ai < a.length && (bi >= b.length || a[ai] <= b[bi])
					? a[ai++]
					: b[bi++];
			result[i++] = element;
		}

		assert i == len;
		return result;
	}

	private static boolean equalsMerge(int[] expected, int[] a, int[] b) {
		int length = expected.length;
		int index = 0;

		for (int ai = 0, bi = 0; ai < a.length || bi < b.length; ) {
			if (ai < a.length && bi < b.length && a[ai] == b[bi]) bi++;
			int element = ai < a.length && (bi >= b.length || a[ai] <= b[bi])
					? a[ai++]
					: b[bi++];
			if (index == length || expected[index++] != element) return false;
		}
		return index == length;

	}

	private static int hashCodeIntersect(int a[], int b[]) {
		int result = 1;
		for (int ai = 0, bi = 0; ai < a.length && bi < b.length; ) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi]) {
					ai++;
				} else {
					bi++;
				}
				continue;
			}
			int element = a[ai++];
			bi++;

			result = 31 * result + element;
		}

		return result;
	}

	private static boolean equalsIntersect(int[] expected, int[] a, int[] b) {
		int length = expected.length;
		int index = 0;

		for (int ai = 0, bi = 0; ai < a.length && bi < b.length; ) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi]) {
					ai++;
				} else {
					bi++;
				}
				continue;
			}
			int element = a[ai++];
			bi++;

			if (index == length || expected[index++] != element) return false;
		}

		return index == length;
	}

	private static int[] intersectArrays(int a[], int b[]) {
		int len = 0;
		for (int ai = 0, bi = 0; ai < a.length && bi < b.length; ) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi]) {
					ai++;
				} else {
					bi++;
				}
				continue;
			}
			ai++;
			bi++;
			len++;
		}
		if (len == 0) return null;
		int[] result = new int[len];
		int i = 0;
		for (int ai = 0, bi = 0; ai < a.length && bi < b.length; ) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi]) {
					ai++;
				} else {
					bi++;
				}
				continue;
			}
			int element = a[ai++];
			bi++;
			result[i++] = element;
		}
		assert i == len;
		return result;
	}
}
