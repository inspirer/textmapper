/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 8/17/11
 */
public class IntegerSetsTest {

	@Test
	public void testCreation() {
		IntegerSets sets = new IntegerSets();
		assertEquals(1, sets.add(1, 2, 3));
		assertEquals(1, sets.add(1, 2, 3));
		assertEquals(2, sets.add(1, 2));
		assertEquals(2, sets.add(1, 2));
		assertEquals(1, sets.union(0, 1));
		assertEquals(3, sets.add(42));
		assertEquals(3, sets.add(42));
		assertEquals(0, sets.intersection(2, 3));
		assertEquals(0, sets.intersection(2, 3));
		assertEquals(0, sets.add());
	}

	@Test
	public void testContains() {
		IntegerSets sets = new IntegerSets();
		int s1 = sets.add(1, 3, 9, 13);
		assertTrue(sets.contains(s1, 1));
		assertFalse(sets.contains(s1, 8));
		assertTrue(sets.contains(s1, 13));
		assertFalse(sets.contains(s1, 14));

		int s2 = sets.complement(s1);
		assertTrue(sets.contains(s2, 0));
		assertFalse(sets.contains(s2, 1));
		assertTrue(sets.contains(s2, 4));
		assertFalse(sets.contains(s2, 13));
	}

	@Test
	public void testResize() {
		IntegerSets sets = new IntegerSets();
		for (int i = 0; i < 2222; i++) {
			assertEquals(i + 1, sets.add(i));
		}
		int lastSet = 1;
		for (int i = 2; i < 222; i++) {
			int merged = sets.union(lastSet, i);
			assertEquals(2221 + i, merged);
			assertEquals(i, sets.sets[merged].length);
			lastSet = merged;
		}
		for (int i = 555; i > 300; i--) {
			int merged = sets.union(lastSet, i);
			assertEquals(2221 + 222 + (555 - i), merged);
			assertEquals((555 - i) + 222, sets.sets[merged].length);
			lastSet = merged;
		}
	}

	@Test
	public void testUnion() {
		IntegerSets sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(2, 4, 6));
		expect(sets.union(1, 2), sets, 1, 2, 3, 4, 5, 6);

		sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(2, 3, 4));
		assertEquals(3, sets.union(1, 2));
		expect(sets.union(1, 2), sets, 1, 2, 3, 4, 5);

		sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(3));
		expect(sets.union(1, 2), sets, 1, 3, 5);

		sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3));
		assertEquals(2, sets.add(5, 8));
		expect(sets.union(1, 2), sets, 1, 3, 5, 8);
		assertEquals(3, sets.union(1, 2));
		assertEquals(3, sets.union(2, 1));
	}

	@Test
	public void testIntersect() {
		IntegerSets sets = new IntegerSets();
		assertEquals(0, sets.intersection(0, 0));
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(2, 4, 6));
		assertEquals(0, sets.intersection(1, 2));
		assertEquals(0, sets.intersection(0, 1));
		assertEquals(0, sets.intersection(2, 0));

		sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(2, 3, 4));
		expect(sets.intersection(1, 2), sets, 3);
		assertEquals(3, sets.add(3));
		assertEquals(3, sets.intersection(1, 2));
		assertEquals(0, sets.intersection(3, 0));

		sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(3));
		assertEquals(2, sets.intersection(1, 2));
		expect(sets.intersection(1, 2), sets, 3);

		sets = new IntegerSets();
		assertEquals(1, sets.add(1, 3, 5));
		assertEquals(2, sets.add(3, 5, 8));
		expect(sets.intersection(1, 2), sets, 3, 5);
		assertEquals(3, sets.intersection(2, 3));
		assertEquals(3, sets.intersection(1, 3));
		assertEquals(3, sets.intersection(1, 2));
		assertEquals(3, sets.intersection(2, 1));
	}

	@Test
	public void testComplement() {
		IntegerSets sets = new IntegerSets();
		int s1 = sets.add(3, 4, 5, 6);
		int s2 = sets.complement(s1);
		assertEquals(-2, s2);
		assertEquals(s1, sets.complement(s2));
	}

	@Test
	public void testUnionComplement() {
		IntegerSets sets = new IntegerSets();
		int s1 = sets.add(1, 3, 9, 13);
		int s2 = sets.add(1, 5, 8, 9, 10, 16);

		expect(sets.intersection(s1, s2), sets, 1, 9);
		expect(sets.union(s1, s2), sets, 1, 3, 5, 8, 9, 10, 13, 16);
		expect(sets.complement(sets.union(s1, sets.complement(s2))), sets, 5, 8, 10, 16);
		expect(sets.complement(sets.union(sets.complement(s2), s1)), sets, 5, 8, 10, 16);
		expect(sets.complement(sets.union(s2, sets.complement(s1))), sets, 3, 13);
		expect(sets.complement(sets.union(sets.complement(s1), s2)), sets, 3, 13);
		expect(sets.complement(sets.union(sets.complement(s1), sets.complement(s2))), sets, 1, 9);
	}

	@Test
	public void testIntersectionComplement() {
		IntegerSets sets = new IntegerSets();
		int s1 = sets.add(1, 3, 9, 13, 14, 16);
		int s2 = sets.add(10, 12, 13, 15, 16);

		expect(sets.intersection(s1, s2), sets, 13, 16);
		expect(sets.intersection(s1, sets.complement(s2)), sets, 1, 3, 9, 14);
		expect(sets.intersection(sets.complement(s2), s1), sets, 1, 3, 9, 14);
		expect(sets.intersection(s2, sets.complement(s1)), sets, 10, 12, 15);
		expect(sets.intersection(sets.complement(s1), s2), sets, 10, 12, 15);
		expect(sets.complement(sets.intersection(sets.complement(s1), sets.complement(s2))), sets, 1, 3, 9, 10, 12,
				13, 14, 15, 16);
	}

	private void expect(int result, IntegerSets sets, int... expected) {
		assertArrayEquals(expected, sets.sets[result]);
	}
}
