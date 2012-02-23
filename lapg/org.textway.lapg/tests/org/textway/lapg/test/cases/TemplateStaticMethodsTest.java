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

import org.junit.Test;
import org.textway.lapg.gen.TemplateStaticMethods;

import java.util.Arrays;
import java.util.List;

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
	public void testPackAsValAndCount() {
		List<List<String>> res = TemplateStaticMethods.packAsValAndCount(new int[]{});
		assertEquals(Arrays.asList(Arrays.asList("\"\"")), res);

		res = TemplateStaticMethods.packAsValAndCount(new int[]{1});
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\1\"")), res);

		res = TemplateStaticMethods.packAsValAndCount(new int[]{1,1,1,2,2,2});
		assertEquals(Arrays.asList(Arrays.asList("\"\\3\\1\\3\\2\"")), res);

		res = TemplateStaticMethods.packAsValAndCount(new int[]{92,91,90});
		assertEquals(Arrays.asList(Arrays.asList("\"\\1\\134\\1\\133\\1\\132\"")), res);
	}
	
}
