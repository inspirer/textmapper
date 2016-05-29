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
package org.textmapper.tool.gen;

import org.junit.Test;
import org.textmapper.tool.gen.TemplateStaticMethods.MapRange;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.textmapper.tool.gen.TemplateStaticMethods.packAsMapRanges;

public class TemplateStaticMethodsTest {

	@Test
	public void testShiftLeft() {
		assertEquals("AA\nBB\n", new TemplateStaticMethods().shiftLeft("\t\tAA\n\t\tBB\n"));
		assertEquals("AA\n\tBB\n", new TemplateStaticMethods().shiftLeft("\t\tAA\n\t\t\tBB\n"));
		assertEquals("\tAA\nBB\n", new TemplateStaticMethods().shiftLeft("\t\t\tAA\n\t\tBB\n"));
		assertEquals(" AA\nBB\n", new TemplateStaticMethods().shiftLeft("\t\t AA\n\t\tBB\n"));
		assertEquals(
				" AA\n" +
						"\n" +
						"BB\n", new TemplateStaticMethods().shiftLeft(
						"\t\t AA\n" +
								"\n" +
								"\t\tBB\n"));
		assertEquals(
				" AA\n" +
						"\n" +
						"BB\n", new TemplateStaticMethods().shiftLeft(
						"\t\t AA\r\n" +
								"\r\n" +
								"\t\tBB\r\n"));
	}

	@Test
	public void testShiftRight() {
		assertEquals("\t\t\tAA\n\n\t\t\tBB\n", new TemplateStaticMethods()
				.shiftRight("\t\tAA\n\n\t\tBB\n", 1));
	}

	@Test
	public void testShiftRightForEmpty() {
		assertEquals("", new TemplateStaticMethods().shiftRight("", 1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testpackCountValue() {
		List<List<String>> res = TemplateStaticMethods.packShortCountValue(new int[]{}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\"")), res);

		res = TemplateStaticMethods.packShortCountValue(new int[]{1}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\1\"")), res);

		res = TemplateStaticMethods.packShortCountValue(new int[]{1, 1, 1, 2, 2, 2}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\\3\\1\\3\\2\"")), res);

		res = TemplateStaticMethods.packShortCountValue(new int[]{92, 91, 90}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\134\\1\\133\\1\\132\"")), res);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testpackCountValueNegative() {
		List<List<String>> res = TemplateStaticMethods.packShortCountValue(new int[]{}, false);
		assertEquals(Arrays.asList(Arrays.asList("\"\"")), res);

		res = TemplateStaticMethods.packShortCountValue(new int[]{-1, -1, -1}, false);
		assertEquals(Arrays.asList(Arrays.asList("\"\\3\\uffff\"")), res);

		res = TemplateStaticMethods.packShortCountValue(
				new int[]{Short.MIN_VALUE, Short.MAX_VALUE}, false);
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\u8000\\1\\u7fff\"")), res);
	}

	@Test
	public void testUnpackRoutineUpToDate() {
		LapgTemplatesTestHelper.generationTest("java_pack.testClass",
				"tests/org/textmapper/tool/gen", new String[]{"JavaTemplateRoutines.java"});
	}

	private static char[] unpack_vc_char(int size, String... st) {
		return JavaTemplateRoutines.test_unpack_vc_char(size, st);
	}

	@Test
	public void testUnpackCountValue() {
		char[] res = unpack_vc_char(0, "");
		assertArrayEquals(new char[]{}, res);

		res = unpack_vc_char(1, "\1\1");
		assertArrayEquals(new char[]{1}, res);

		// splitted strings
		res = unpack_vc_char(1, "\1", "\1");
		assertArrayEquals(new char[]{1}, res);

		res = unpack_vc_char(6, "\3\1\3\2");
		assertArrayEquals(new char[]{1, 1, 1, 2, 2, 2}, res);

		// splitted strings
		res = unpack_vc_char(6, "\3", "\1\3", "\2");
		assertArrayEquals(new char[]{1, 1, 1, 2, 2, 2}, res);

		res = unpack_vc_char(3, "\1\134\1\133\1\132");
		assertArrayEquals(new char[]{92, 91, 90}, res);
	}

	private static short[] unpack_vc_short(int size, String... st) {
		return JavaTemplateRoutines.test_unpack_vc_short(size, st);
	}

	@Test
	public void testUnpackCountValueNegative() {
		short[] res = unpack_vc_short(0, "");
		assertArrayEquals(new short[]{}, res);

		res = unpack_vc_short(1, "\1\uffff");
		assertArrayEquals(new short[]{-1}, res);

		// splitted strings
		res = unpack_vc_short(1, "\1", "\ufffe");
		assertArrayEquals(new short[]{-2}, res);

		res = unpack_vc_short(2, "\1\u8000\1\u7fff");
		assertArrayEquals(new short[]{Short.MIN_VALUE, Short.MAX_VALUE}, res);

		// splitted strings
		res = unpack_vc_short(6, "\3", "\1\3", "\2");
		assertArrayEquals(new short[]{1, 1, 1, 2, 2, 2}, res);

		res = unpack_vc_short(3, "\1\134\1\133\1\132");
		assertArrayEquals(new short[]{92, 91, 90}, res);
	}

	@Test
	public void testConvertIntToShort() {
		try {
			TemplateStaticMethods.packShortCountValue(new int[]{-373}, true);
			fail("no exception");
		} catch (IllegalArgumentException ex) {
			assertEquals("cannot convert int[] into char[], contains `-373'", ex.getMessage());
		}

		try {
			TemplateStaticMethods.packShortCountValue(new int[]{65536}, true);
			fail("no exception");
		} catch (IllegalArgumentException ex) {
			assertEquals("cannot convert int[] into char[], contains `65536'", ex.getMessage());
		}

		try {
			TemplateStaticMethods.packShortCountValue(new int[]{32768}, false);
			fail("no exception");
		} catch (IllegalArgumentException ex) {
			assertEquals("cannot convert int[] into short[], contains `32768'", ex.getMessage());
		}
	}

	@Test
	public void testExtractStatements() throws Exception {
		TemplateStaticMethods util = new TemplateStaticMethods();

		assertEquals("", util.extractStatements(""));
		assertEquals("abc", util.extractStatements("abc"));
		assertEquals("d\nabc ", util.extractStatements("abc {{d}}"));
		assertEquals("d\n abc", util.extractStatements("{{d}} abc"));
		assertEquals("d\ne\nabc  : ", util.extractStatements("abc {{d}} : {{e}}"));
		assertEquals("{ }\nabc ", util.extractStatements("abc {{ { } }}"));
		assertEquals("q1\nabc ", util.extractStatements("abc {{ q1 }}{{q1}}"));
	}

	private void testRanges(int[] arr) {
		List<MapRange> res = packAsMapRanges(arr, 0);
		int[] restored = new int[arr.length];
		Arrays.fill(restored, 1);
		int start = 0;
		for (MapRange r : res) {
			assertTrue(start <= r.lo);
			assertTrue(r.lo < r.hi);
			start = r.hi;
			assertTrue(start <= arr.length);
			assertTrue((r.val == null ? 0 : r.val.length) < r.hi - r.lo);
			Arrays.fill(restored, r.lo, r.hi, r.defaultVal);
			if (r.val != null) {
				System.arraycopy(r.val, 0, restored, r.lo, r.val.length);
			}
		}
		assertArrayEquals(Arrays.toString(restored), arr, restored);
	}

	@Test
	public void testPackAsMapRanges() throws Exception {
		testRanges(new int[] {2,3,4,5,6,1});
		testRanges(new int[] {1,2,3,3,3,4,1,1,1,1,1});
		testRanges(new int[] {1,1,1,1,1,1,22,1,1,1,1,1});
		testRanges(new int[] {0});
		testRanges(new int[] {0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,22});
		testRanges(new int[] {0,1,1,1,0,1,1,1,1,1});
		testRanges(new int[] {0,1,1,1,1,1,1,1,1,1,2,3,8,6,3,10,10,10,10,10,10,10,10,0,1,1,1,1,1});
		testRanges(new int[] {0,2,2,2,1,1,1,1,1,1,1,1,1,2,3,8,6,3,10,10,10,10,10,10,10,10,0,1,1,1,1,1});
		testRanges(new int[] {1,1,1,1,2,2,2,2,8,8,8,8,1,1,1,1,1,4,4,4,1,1,1,1,1,1,1,1,1});
	}

	@Test
	public void rangedHashes() throws Exception {
		assertEquals(1, new TemplateStaticMethods().rangedHash("default", 64));
		assertEquals("5c13d641", new TemplateStaticMethods().hashHex("default"));
		assertEquals(0x5c13d641, new TemplateStaticMethods().stringHash("default"));

		assertEquals(37, new TemplateStaticMethods().rangedHash("in", 64));
		assertEquals(0xd25, new TemplateStaticMethods().stringHash("in"));
		assertEquals("d25", new TemplateStaticMethods().hashHex("in"));

		assertEquals(37, new TemplateStaticMethods().rangedHash("import", 64));
		assertEquals("b96173a5", new TemplateStaticMethods().hashHex("import"));
	}
}
