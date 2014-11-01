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
package org.textmapper.lapg.common;

import org.junit.Test;
import org.textmapper.lapg.api.regex.CharacterSet;

import java.util.Iterator;

import static org.junit.Assert.*;

public class CharacterSetTest {

	@Test
	public void testCreation() {

		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();

		b.addRange(1, 10);
		b.addRange(15, 30);
		b.addRange(12, 12);
		CharacterSet characterSet = b.create();
		assertEquals("[1-10,12,15-30]", characterSet.toString());
		assertTrue(characterSet.contains(8));
		assertTrue(characterSet.contains(10));
		assertFalse(characterSet.contains(11));
		assertTrue(characterSet.contains(12));
		assertFalse(characterSet.contains(13));
		assertFalse(characterSet.contains(14));
		assertTrue(characterSet.contains(15));
		assertTrue(characterSet.contains(29));
		assertTrue(characterSet.contains(30));
		assertFalse(characterSet.contains(33));

		b.clear();
		b.addSymbol(10);
		b.addSymbol(30);
		b.addSymbol(20);
		assertEquals("[10,20,30]", b.create().toString());

		b.clear();
		b.addSymbol(25);
		characterSet = b.create(true);
		assertTrue(characterSet.contains(1));
		assertFalse(characterSet.contains(25));

		b.clear();
		assertEquals("[]", b.create().toString());

		b.clear();
		b.addSymbol(1);
		assertEquals("[1]", b.create().toString());

		b.clear();
		b.addSymbol(0);
		assertEquals("[0]", b.create().toString());

		b.clear();
		b.addRange(0, 10);
		characterSet = b.create();
		assertEquals("[0-10]", characterSet.toString());
		assertTrue(characterSet.contains(0));
		assertTrue(characterSet.contains(1));
	}

	@Test
	public void testSubtract1() {
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();

		b.clear();
		b.addSymbol(0);
		b.addRange('a', 'z');
		b.addSymbol('_');
		CharacterSet set = b.create();

		b.clear();
		b.addSymbol('i');
		b.addSymbol('e');
		b.addSymbol('c');
		CharacterSet set2 = b.create();
		assertEquals("[0,95,97-98,100,102-104,106-122]", b.subtract(set, set2).toString());
	}

	@Test
	public void testIterator() {
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();

		b.clear();
		b.addRange('a', 'l');
		b.addSymbol('_');
		CharacterSet set = b.create();

		b.clear();
		b.addSymbol('i');
		b.addSymbol('e');
		b.addSymbol('c');
		CharacterSet set2 = b.create();

		set = b.subtract(set, set2);
		Iterator<int[]> it = set.iterator();

		assertEquals(true, it.hasNext());
		int[] next = it.next();
		assertEquals('_', next[0]);
		assertEquals('_', next[1]);
		next = it.next();
		assertEquals('a', next[0]);
		assertEquals('b', next[1]);
		next = it.next();
		assertEquals('d', next[0]);
		assertEquals('d', next[1]);
		next = it.next();
		assertEquals('f', next[0]);
		assertEquals('h', next[1]);
		assertEquals(true, it.hasNext());
		next = it.next();
		assertEquals('j', next[0]);
		assertEquals('l', next[1]);
		assertEquals(false, it.hasNext());
		assertNull(it.next());
		assertNull(it.next());

		it = set.iterator();
		it.next();
		boolean exc = false;
		try {
			it.remove();
		} catch (UnsupportedOperationException ex) {
			exc = true;
		}
		assertTrue("no exception", exc);
	}

	@Test
	public void testSubtract2() {
		subtract(new int[]{95, 95, 96, 96, 97, 97}, new int[]{96, 96}, "[95,97]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 80, 99}, "[100-200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 80, 100}, "[101-200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 80, 120}, "[121-200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 140}, "[100-119,141-200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 140, 200, 220}, "[100-119,141-199]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 140, 205, 220}, "[100-119,141-200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 198}, "[100-119,199-200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 199}, "[100-119,200]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 200}, "[100-119]");
		subtract(new int[]{100, 200}, new int[]{1, 2, 3, 4, 120, 240}, "[100-119]");
		subtract(new int[]{100, 200}, new int[]{80, 120}, "[121-200]");
		subtract(new int[]{100, 200}, new int[]{120, 140}, "[100-119,141-200]");
		subtract(new int[]{100, 200}, new int[]{120, 140, 200, 220}, "[100-119,141-199]");
		subtract(new int[]{100, 200}, new int[]{120, 140, 205, 220}, "[100-119,141-200]");
		subtract(new int[]{100, 200}, new int[]{120, 198}, "[100-119,199-200]");
		subtract(new int[]{100, 200}, new int[]{120, 199}, "[100-119,200]");
		subtract(new int[]{100, 200}, new int[]{120, 200}, "[100-119]");
		subtract(new int[]{100, 200}, new int[]{120, 240}, "[100-119]");

		subtract(new int[]{100, 200}, new int[]{1, 1, 50, 50, 150, 150}, "[100-149,151-200]");
		subtract(new int[]{100, 200}, new int[]{1, 1, 50, 50, 150, 150, 250, 250}, "[100-149,151-200]");
		subtract(new int[]{100, 200}, new int[]{1, 1, 50, 50, 150, 151, 250, 250}, "[100-149,152-200]");

		subtract(new int[]{7, 7, 14, 14, 21, 21, 55, 55}, new int[]{1, 100}, "[]");
		subtract(new int[]{0, 0, 55, 55}, new int[]{1, 100}, "[0]");
		subtract(new int[]{0, 0, 55, 55}, new int[]{60, 100}, "[0,55]");
	}

	@Test
	public void testRealloc() {
		StringBuilder res = new StringBuilder();
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();
		res.append("[");

		for (int i = 0; i < 6000; i++) {
			if (i > 0) {
				res.append(",");
			}
			res.append(i * 4);
			b.addSymbol(i * 4);
		}
		res.append("]");
		assertEquals(res.toString(), b.create().toString());
	}

	@Test
	public void testReverseRealloc() {
		StringBuilder res = new StringBuilder();
		res.append("[");
		for (int i = 0; i < 6000; i++) {
			if (i > 0) {
				res.append(",");
			}
			res.append(i * 4);
		}
		res.append("]");

		// set in reverse order
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();
		b.addSymbol(0);
		for (int i = 6000 - 1; i > 0; i--) {
			b.addSymbol(i * 4);
		}
		assertEquals(res.toString(), b.create().toString());
	}

	private static final int TESTLEN = 9;

	@Test
	public void testSubtractGeneric() {
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();
		CharacterSet s1, s2, s3;
		int[] array1 = new int[TESTLEN];
		int[] array2 = new int[TESTLEN];
		int[] array3 = new int[TESTLEN];

		fillArray(array1, 255);
		assertEquals("[0-7]", fromArray(array1, b).toString());
		fillArray(array1, 55);
		assertEquals("[0-2,4-5]", fromArray(array1, b).toString());
		fillArray(array1, 3);
		assertEquals("[0-1]", fromArray(array1, b).toString());
		fillArray(array1, 1);
		assertEquals("[0]", fromArray(array1, b).toString());
		fillArray(array1, 0);
		assertEquals("[]", fromArray(array1, b).toString());

		for (int i = 0; i < (1 << TESTLEN); i++) {
			fillArray(array1, i);
			s1 = fromArray(array1, b);
			for (int e = 0; e < (1 << TESTLEN); e++) {
				fillArray(array2, e);
				s2 = fromArray(array2, b);
				for (int q = 0; q < TESTLEN; q++) {
					array3[q] = array2[q] == 1 ? 0 : array1[q];
				}
				s3 = fromArray(array3, b);
				assertEquals(// turn on for debug: s1.toString() + " - " + s2.toString(),
						s3.toString(), b.subtract(s1, s2).toString());

			}
		}
	}

	@Test
	public void testIntersectGeneric() {
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();
		CharacterSet s1, s2, s3;
		int[] array1 = new int[TESTLEN];
		int[] array2 = new int[TESTLEN];
		int[] array3 = new int[TESTLEN];

		for (int i = 0; i < (1 << TESTLEN); i++) {
			fillArray(array1, i);
			s1 = fromArray(array1, b);
			for (int e = 0; e < (1 << TESTLEN); e++) {
				fillArray(array2, e);
				s2 = fromArray(array2, b);
				for (int q = 0; q < TESTLEN; q++) {
					array3[q] = array2[q] == 1 && array1[q] == 1 ? 1 : 0;
				}
				s3 = fromArray(array3, b);
				assertEquals(// turn on for debug: s1.toString() + " - " + s2.toString(),
						s3.toString(), b.intersect(s1, s2).toString());

			}
		}
	}

	private static final int ARTESTLEN = 12;

	@Test
	public void testAddRangeGeneric() {
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();
		CharacterSet s2, s3;
		int[] array1 = new int[ARTESTLEN];
		int[] array2 = new int[ARTESTLEN];

		for (int i = 0; i < (1 << ARTESTLEN); i++) {
			fillArray(array1, i);

			for (int start = 0; start < ARTESTLEN; start++) {
				for (int end = start; end < ARTESTLEN; end++) {
					for (int e = 0; e < ARTESTLEN; e++) {
						array2[e] = (e >= start && e <= end) ? 1 : array1[e];
					}
					b.clear();

					int q = 0;
					while (q < array1.length) {
						if (array1[q] == 1) {
							int st = q;
							while (q + 1 < array1.length && array1[q + 1] == 1) {
								q++;
							}
							b.addRange(st, q);
						}
						q++;
					}

					b.addRange(start, end);
					s2 = b.create();

					s3 = fromArray(array2, b);
					assertEquals(i + ": " + start + "-" + end,
							s3.toString(), s2.toString());
				}
			}
		}
	}

	private static void subtract(int[] a1, int[] a2, String result) {
		CharacterSetImpl.Builder b = new CharacterSetImpl.Builder();
		assertEquals(result, b.subtract(new CharacterSetImpl(a1, a1.length), new CharacterSetImpl(a2, a2.length)).toString());
	}

	private static CharacterSet fromArray(int[] arr, CharacterSetImpl.Builder b) {
		b.clear();
		int i = 0;
		while (i < arr.length) {
			if (arr[i] == 1) {
				int start = i;
				while (i + 1 < arr.length && arr[i + 1] == 1) {
					i++;
				}
				b.addRange(start, i);
			}
			i++;
		}

		return b.create();
	}

	private static void fillArray(int[] arr, int number) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = number & 1;
			number >>= 1;
		}
	}
}
