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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 8/17/11
 */
public class IntegerSetsTest {

	@Test
	public void testCreation() {
		IntegerSets sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 2, 3}));
		assertEquals(0, sets.storeSet(new int[]{1, 2, 3}));
		assertEquals(1, sets.storeSet(new int[]{1, 2}));
		assertEquals(1, sets.storeSet(new int[]{1, 2}));
		assertEquals(0, sets.mergeSets(0, 1));
	}

	@Test
	public void testResize() {
		IntegerSets sets = new IntegerSets();
		for (int i = 0; i < 2222; i++) {
			assertEquals(i, sets.storeSet(new int[]{i}));
		}
		int lastSet = 0;
		for (int i = 1; i < 222; i++) {
			int merged = sets.mergeSets(lastSet, i);
			assertEquals(2221 + i, merged);
			assertEquals(i + 1, sets.sets[merged].length);
			lastSet = merged;
		}
		for (int i = 555; i > 300; i--) {
			int merged = sets.mergeSets(lastSet, i);
			assertEquals(2221 + 222 + (555 - i), merged);
			assertEquals((555 - i) + 222 + 1, sets.sets[merged].length);
			lastSet = merged;
		}
	}

	@Test
	public void testMerge() {
		IntegerSets sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{2, 4, 6}));
		assertEquals(2, sets.mergeSets(0, 1));
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6}, sets.sets[2]));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{2, 3, 4}));
		assertEquals(2, sets.mergeSets(0, 1));
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5}, sets.sets[2]));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{3}));
		assertEquals(0, sets.mergeSets(0, 1));
		assertTrue(Arrays.equals(new int[]{1, 3, 5}, sets.sets[0]));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3}));
		assertEquals(1, sets.storeSet(new int[]{5, 8}));
		assertEquals(2, sets.mergeSets(0, 1));
		assertTrue(Arrays.equals(new int[]{1, 3, 5, 8}, sets.sets[2]));
		assertEquals(2, sets.mergeSets(0, 1));
		assertEquals(2, sets.mergeSets(1, 0));
	}

	@Test
	public void testSubset() {
		IntegerSets sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{2, 4, 6}));
		assertEquals(2, sets.mergeSets(0, 1));
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6}, sets.sets[2]));
		assertTrue(sets.isStrictSubset(0, 2));
		assertTrue(sets.isStrictSubset(1, 2));
		assertFalse(sets.isStrictSubset(0, 1));
		assertFalse(sets.isStrictSubset(1, 0));
		assertFalse(sets.isStrictSubset(0, 0));
		assertFalse(sets.isStrictSubset(1, 1));
		assertTrue(sets.isStrictSubset(-1, 1));
		assertFalse(sets.isStrictSubset(-1, -1));
		assertFalse(sets.isStrictSubset(1, -1));

		assertFalse(sets.isSubset(1, -1));
		assertTrue(sets.isSubset(-1, 1));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 2}));
		assertEquals(1, sets.storeSet(new int[]{1}));
		assertTrue(sets.isStrictSubset(1, 0));
		assertFalse(sets.isStrictSubset(0, 1));
		assertFalse(sets.isStrictSubset(0, 0));
		assertFalse(sets.isStrictSubset(1, 1));

		assertTrue(sets.isSubset(1, 1));
		assertTrue(sets.isSubset(0, 0));
		assertTrue(sets.isSubset(1, 0));
		assertFalse(sets.isSubset(0, 1));
	}

	@Test
	public void testIntersect() {
		IntegerSets sets = new IntegerSets();
		assertEquals(-1, sets.intersectSet(-1, -1));
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{2, 4, 6}));
		assertEquals(-1, sets.intersectSet(0, 1));
		assertEquals(-1, sets.intersectSet(-1, 0));
		assertEquals(-1, sets.intersectSet(1, -1));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{2, 3, 4}));
		assertEquals(2, sets.intersectSet(0, 1));
		assertTrue(Arrays.equals(new int[]{3}, sets.sets[2]));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{3}));
		assertEquals(1, sets.intersectSet(0, 1));

		sets = new IntegerSets();
		assertEquals(0, sets.storeSet(new int[]{1, 3, 5}));
		assertEquals(1, sets.storeSet(new int[]{3, 5, 8}));
		assertEquals(2, sets.intersectSet(0, 1));
		assertTrue(Arrays.equals(new int[]{3, 5}, sets.sets[2]));
		assertEquals(2, sets.intersectSet(0, 1));
		assertEquals(2, sets.intersectSet(1, 0));
	}
}
