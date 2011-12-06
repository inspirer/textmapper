/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.test.cases;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.textway.lapg.lex.CharacterSet;

public class CharacterSetTest extends TestCase {

	public void testCreation() {

		CharacterSet.Builder b = new CharacterSet.Builder();

		b.addRange(1,10);
		b.addRange(15,30);
		b.addRange(12,12);
		CharacterSet characterSet = b.create();
		Assert.assertEquals("[1-10,12,15-30]", characterSet.toString());
		Assert.assertTrue(characterSet.contains(8));
		Assert.assertTrue(characterSet.contains(10));
		Assert.assertFalse(characterSet.contains(11));
		Assert.assertTrue(characterSet.contains(12));
		Assert.assertFalse(characterSet.contains(13));
		Assert.assertFalse(characterSet.contains(14));
		Assert.assertTrue(characterSet.contains(15));
		Assert.assertTrue(characterSet.contains(29));
		Assert.assertTrue(characterSet.contains(30));
		Assert.assertFalse(characterSet.contains(33));

		b.clear();
		b.addSymbol(10);
		b.addSymbol(30);
		b.addSymbol(20);
		Assert.assertEquals("[10,20,30]", b.create().toString());

		b.clear();
		b.addSymbol(25);
		characterSet = b.create(true);
		Assert.assertTrue(characterSet.contains(1));
		Assert.assertFalse(characterSet.contains(25));

		b.clear();
		Assert.assertEquals("[]", b.create().toString());

		b.clear();
		b.addSymbol(1);
		Assert.assertEquals("[1]", b.create().toString());
	}

	public void testSubtract1() {
		CharacterSet.Builder b = new CharacterSet.Builder();

		b.clear();
		b.addRange('a', 'z');
		b.addSymbol('_');
		CharacterSet set = b.create();

		b.clear();
		b.addSymbol('i');
		b.addSymbol('e');
		b.addSymbol('c');
		CharacterSet set2 = b.create();
		Assert.assertEquals("[95,97-98,100,102-104,106-122]", b.subtract(set, set2).toString());
	}

	public void testIterator() {
		CharacterSet.Builder b = new CharacterSet.Builder();

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

		Assert.assertEquals(true, it.hasNext());
		int[] next = it.next();
		Assert.assertEquals('_', next[0]);
		Assert.assertEquals('_', next[1]);
		next = it.next();
		Assert.assertEquals('a', next[0]);
		Assert.assertEquals('b', next[1]);
		next = it.next();
		Assert.assertEquals('d', next[0]);
		Assert.assertEquals('d', next[1]);
		next = it.next();
		Assert.assertEquals('f', next[0]);
		Assert.assertEquals('h', next[1]);
		Assert.assertEquals(true, it.hasNext());
		next = it.next();
		Assert.assertEquals('j', next[0]);
		Assert.assertEquals('l', next[1]);
		Assert.assertEquals(false, it.hasNext());
		Assert.assertNull(it.next());
		Assert.assertNull(it.next());

		it = set.iterator();
		it.next();
		boolean exc = false;
		try {
			it.remove();
		} catch(UnsupportedOperationException ex) {
			exc = true;
		}
		Assert.assertTrue("no exception", exc);
	}

	public void testSubtract2() {
		subtract(new int[]{ 95,95,96,96,97,97}, new int[] {96,96}, "[95,97]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 80, 99}, "[100-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 80, 100}, "[101-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 80, 120}, "[121-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 140}, "[100-119,141-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 140, 200, 220}, "[100-119,141-199]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 140, 205, 220}, "[100-119,141-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 198}, "[100-119,199-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 199}, "[100-119,200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 200}, "[100-119]");
		subtract(new int[]{ 100, 200 }, new int[] {1,2,3,4, 120, 240}, "[100-119]");
		subtract(new int[]{ 100, 200 }, new int[] {80, 120}, "[121-200]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 140}, "[100-119,141-200]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 140, 200, 220}, "[100-119,141-199]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 140, 205, 220}, "[100-119,141-200]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 198}, "[100-119,199-200]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 199}, "[100-119,200]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 200}, "[100-119]");
		subtract(new int[]{ 100, 200 }, new int[] {120, 240}, "[100-119]");

		subtract(new int[]{ 100, 200 }, new int[] {1,1, 50,50, 150,150}, "[100-149,151-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,1, 50,50, 150,150, 250,250}, "[100-149,151-200]");
		subtract(new int[]{ 100, 200 }, new int[] {1,1, 50,50, 150,151, 250,250}, "[100-149,152-200]");

		subtract(new int[]{ 7,7, 14,14, 21,21, 55,55 }, new int[] {1,100}, "[]");
	}

	public void testRealloc() {
		StringBuilder res = new StringBuilder();
		CharacterSet.Builder b = new CharacterSet.Builder();
		res.append("[");

		for(int i = 0; i < 6000; i++) {
			if(i > 0) {
				res.append(",");
			}
			res.append(i*4);
			b.addSymbol(i*4);
		}
		res.append("]");
		Assert.assertEquals(res.toString(), b.create().toString());
	}

	public void testReverseRealloc() {
		StringBuilder res = new StringBuilder();
		res.append("[");
		for(int i = 0; i < 6000; i++) {
			if(i > 0) {
				res.append(",");
			}
			res.append(i*4);
		}
		res.append("]");

		// set in reverse order
		CharacterSet.Builder b = new CharacterSet.Builder();
		b.addSymbol(0);
		for(int i = 6000-1; i > 0; i--) {
			b.addSymbol(i*4);
		}
		Assert.assertEquals(res.toString(), b.create().toString());
	}

	private static final int TESTLEN = 9;

	public void testSubtractGeneric() {
		CharacterSet.Builder b = new CharacterSet.Builder();
		CharacterSet s1, s2, s3;
		int[] array1 = new int[TESTLEN];
		int[] array2 = new int[TESTLEN];
		int[] array3 = new int[TESTLEN];

		fillArray(array1, 255);
		Assert.assertEquals("[0-7]", fromArray(array1, b).toString());
		fillArray(array1, 55);
		Assert.assertEquals("[0-2,4-5]", fromArray(array1, b).toString());
		fillArray(array1, 3);
		Assert.assertEquals("[0-1]", fromArray(array1, b).toString());
		fillArray(array1, 1);
		Assert.assertEquals("[0]", fromArray(array1, b).toString());
		fillArray(array1, 0);
		Assert.assertEquals("[]", fromArray(array1, b).toString());

		for(int i = 0; i < (1<<TESTLEN); i++) {
			fillArray(array1, i);
			s1 = fromArray(array1,b);
			for(int e = 0; e < (1<<TESTLEN); e++) {
				fillArray(array2, e);
				s2 = fromArray(array2,b);
				for(int q = 0; q < TESTLEN; q++) {
					array3[q] = array2[q] == 1 ? 0 : array1[q];
				}
				s3 = fromArray(array3,b);
				Assert.assertEquals(// turn on for debug: s1.toString() + " - " + s2.toString(),
						s3.toString(), b.subtract(s1, s2).toString());

			}
		}
	}

	public void testIntersectGeneric() {
		CharacterSet.Builder b = new CharacterSet.Builder();
		CharacterSet s1, s2, s3;
		int[] array1 = new int[TESTLEN];
		int[] array2 = new int[TESTLEN];
		int[] array3 = new int[TESTLEN];

		for(int i = 0; i < (1<<TESTLEN); i++) {
			fillArray(array1, i);
			s1 = fromArray(array1,b);
			for(int e = 0; e < (1<<TESTLEN); e++) {
				fillArray(array2, e);
				s2 = fromArray(array2,b);
				for(int q = 0; q < TESTLEN; q++) {
					array3[q] = array2[q] == 1 && array1[q] == 1 ? 1 : 0;
				}
				s3 = fromArray(array3,b);
				Assert.assertEquals(// turn on for debug: s1.toString() + " - " + s2.toString(),
						s3.toString(), b.intersect(s1, s2).toString());

			}
		}
	}

	private static final int ARTESTLEN = 12;

	public void testAddRangeGeneric() {
		CharacterSet.Builder b = new CharacterSet.Builder();
		CharacterSet s2, s3;
		int[] array1 = new int[ARTESTLEN];
		int[] array2 = new int[ARTESTLEN];

		for(int i = 0; i < (1<<ARTESTLEN); i++) {
			fillArray(array1, i);

			for(int start = 0; start < ARTESTLEN; start++) {
				for(int end = start; end < ARTESTLEN; end++) {
					for(int e = 0; e < ARTESTLEN; e++) {
						array2[e] = (e >= start && e <= end) ? 1 : array1[e];
					}
					b.clear();

					int q = 0;
					while(q < array1.length) {
						if(array1[q] == 1) {
							int st = q;
							while(q+1 < array1.length && array1[q+1] == 1) {
								q++;
							}
							b.addRange(st, q);
						}
						q++;
					}

					b.addRange(start, end);
					s2 = b.create();

					s3 = fromArray(array2,b);
					Assert.assertEquals(i+": "+start+"-"+end,
							s3.toString(), s2.toString());
				}
			}
		}
	}

	private static void subtract(int[] a1, int[] a2, String result) {
		CharacterSet.Builder b = new CharacterSet.Builder();
		Assert.assertEquals(result, b.subtract(new CharacterSet(a1, a1.length), new CharacterSet(a2, a2.length)).toString());
	}

	private static CharacterSet fromArray(int[] arr, CharacterSet.Builder b) {
		b.clear();
		int i = 0;
		while(i < arr.length) {
			if(arr[i] == 1) {
				int start = i;
				while(i+1 < arr.length && arr[i+1] == 1) {
					i++;
				}
				b.addRange(start, i);
			}
			i++;
		}

		return b.create();
	}

	private static void fillArray(int[] arr, int number) {
		for(int i = 0; i < arr.length; i++) {
			arr[i] = number & 1;
			number >>= 1;
		}
	}
}
