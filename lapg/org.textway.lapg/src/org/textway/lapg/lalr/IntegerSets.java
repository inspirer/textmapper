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
package org.textway.lapg.lalr;

import java.util.Arrays;

/**
 * Gryaznov Evgeny, 8/17/11
 * <p/>
 * Effectively stores and merges integer sets (represented as sorted arrays)
 */
public class IntegerSets {

	public int[][] sets = new int[512][];

	private HashEntry[] htable = new HashEntry[997];
	private int count;

	public boolean contains(int i, int element) {
		int[] set = sets[i];
		if (set.length <= 4) {
			for (int e : set) {
				if (e == element) {
					return true;
				}
			}
			return false;
		} else {
			return Arrays.binarySearch(set, element) >= 0;
		}
	}

	public int storeSet(int[] set) {
		int hash = hashCode(set);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && Arrays.equals(sets[bucket.index], set)) {
				return bucket.index;
			}
		}
		return push(hash, set);
	}

	public int intersectSet(int i1, int i2) {
		if (i1 == -1 || i2 == -1) return -1;
		int hash = hashCodeIntersect(sets[i1], sets[i2]);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && equalsIntersect(sets[bucket.index], sets[i1], sets[i2])) {
				return bucket.index;
			}
		}
		int[] set = intersect(sets[i1], sets[i2]);
		if (set == null) return -1;
		return push(hash, set);
	}

	public int mergeSets(int i1, int i2) {
		if (i1 == -1) return i2;
		if (i2 == -1) return i1;
		int hash = hashCode(sets[i1], sets[i2]);
		for (HashEntry bucket = htable[toHashIndex(hash)]; bucket != null; bucket = bucket.next) {
			if (bucket.hash == hash && equals(sets[bucket.index], sets[i1], sets[i2])) {
				return bucket.index;
			}
		}
		int[] set = merge(sets[i1], sets[i2]);
		return push(hash, set);
	}

	public boolean isStrictSubset(int subset, int set) {
		if (set == -1 || set == subset) return false;
		if (subset == -1) return true;
		return isSubset(subset, set);
	}

	public boolean isSubset(int subset, int set) {
		if (set == subset || subset == -1) return true;
		if (set == -1) return false;
		int[] set_arr = sets[set];
		int set_index = 0;
		for (int el : sets[subset]) {
			while (set_index < set_arr.length && el > set_arr[set_index]) set_index++;
			if (set_index >= set_arr.length || el != set_arr[set_index]) return false;
		}

		return true;
	}

	private int push(int hash, int[] set) {
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

	private static int hashCode(int a[], int b[]) {
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

	private static int hashCodeIntersect(int a[], int b[]) {
		int result = 1;
		for (int ai = 0, bi = 0; ai < a.length && bi < b.length; ) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi])
					ai++;
				else bi++;
				continue;
			}
			int element = a[ai++];
			bi++;

			result = 31 * result + element;
		}

		return result;
	}

	private static int[] merge(int a[], int b[]) {
		int len = a.length + b.length;
		for (int ai = 0, bi = 0; ai < a.length || bi < b.length; ) {
			if (ai < a.length && bi < b.length && a[ai] == b[bi]) {
				bi++;
				len--;
			}
			if (ai < a.length && (bi >= b.length || a[ai] <= b[bi]))
				ai++;
			else bi++;
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

	private static int[] intersect(int a[], int b[]) {
		int len = 0;
		for (int ai = 0, bi = 0; ai < a.length && bi < b.length; ) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi])
					ai++;
				else bi++;
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
				if (a[ai] < b[bi])
					ai++;
				else bi++;
				continue;
			}
			int element = a[ai++];
			bi++;
			result[i++] = element;
		}

		assert i == len;
		return result;
	}

	private static boolean equals(int[] expected, int[] a, int[] b) {
		int length = expected.length;

		int i = 0, ai = 0, bi = 0;
		while (i < length && (ai < a.length || bi < b.length)) {
			if (ai < a.length && bi < b.length && a[ai] == b[bi]) bi++;
			int element = ai < a.length && (bi >= b.length || a[ai] <= b[bi])
					? a[ai++]
					: b[bi++];
			if (expected[i++] != element)
				return false;
		}
		return i == length && ai == a.length && bi == b.length;

	}

	private static boolean equalsIntersect(int[] expected, int[] a, int[] b) {
		int length = expected.length;

		int i = 0, ai = 0, bi = 0;
		while (i < length && ai < a.length && bi < b.length) {
			if (a[ai] != b[bi]) {
				if (a[ai] < b[bi])
					ai++;
				else bi++;
				continue;
			}
			int element = a[ai++];
			bi++;
			if (expected[i++] != element)
				return false;
		}
		return i == length && (ai == a.length || bi == b.length);
	}
}
