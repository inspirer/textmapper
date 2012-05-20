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
package org.textway.lapg.test.gen;

import org.junit.Test;
import org.textway.lapg.gen.TemplateStaticMethods;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Gryaznov Evgeny, 5/19/11
 */
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
		assertEquals("\t\t\tAA\n\n\t\t\tBB\n", new TemplateStaticMethods().shiftRight("\t\tAA\n\n\t\tBB\n", 1));
	}

	@Test
	public void testShiftRightForEmpty() {
		assertEquals("", new TemplateStaticMethods().shiftRight("", 1));
	}

	@Test
	public void testPackValueCount() {
		List<List<String>> res = TemplateStaticMethods.packValueCount(new int[]{}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\"")), res);

		res = TemplateStaticMethods.packValueCount(new int[]{1}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\1\"")), res);

		res = TemplateStaticMethods.packValueCount(new int[]{1, 1, 1, 2, 2, 2}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\\3\\1\\3\\2\"")), res);

		res = TemplateStaticMethods.packValueCount(new int[]{92, 91, 90}, true);
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\134\\1\\133\\1\\132\"")), res);
	}

	@Test
	public void testPackValueCountNegative() {
		List<List<String>> res = TemplateStaticMethods.packValueCount(new int[]{}, false);
		assertEquals(Arrays.asList(Arrays.asList("\"\"")), res);

		res = TemplateStaticMethods.packValueCount(new int[]{-1, -1, -1}, false);
		assertEquals(Arrays.asList(Arrays.asList("\"\\3\\uffff\"")), res);

		res = TemplateStaticMethods.packValueCount(new int[]{Short.MIN_VALUE, Short.MAX_VALUE}, false);
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\u8000\\1\\u7fff\"")), res);
	}

	@Test
	public void testUnpackRoutineUpToDate() {
		LapgTemplatesTestHelper.generationTest("java_pack.testClass", "org.textway.lapg/tests/org/textway/lapg/test/gen", new String[]{
				"JavaTemplateRoutines.java"});
	}

	private static char[] unpack_char2no(int size, String... st) {
		return JavaTemplateRoutines.test_unpack_vc_char(size, st);
	}

	@Test
	public void testUnpackAsValAndCount() {
		char[] res = unpack_char2no(0, "");
		assertArrayEquals(new char[]{}, res);

		res = unpack_char2no(1, "\1\1");
		assertArrayEquals(new char[]{1}, res);

		// splitted strings
		res = unpack_char2no(1, "\1", "\1");
		assertArrayEquals(new char[]{1}, res);

		res = unpack_char2no(6, "\3\1\3\2");
		assertArrayEquals(new char[]{1, 1, 1, 2, 2, 2}, res);

		// splitted strings
		res = unpack_char2no(6, "\3", "\1\3", "\2");
		assertArrayEquals(new char[]{1, 1, 1, 2, 2, 2}, res);

		res = unpack_char2no(3, "\1\134\1\133\1\132");
		assertArrayEquals(new char[]{92, 91, 90}, res);
	}
}
