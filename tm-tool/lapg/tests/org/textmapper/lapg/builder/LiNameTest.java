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
package org.textmapper.lapg.builder;

import org.junit.Test;
import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.NameParseException;

import static org.junit.Assert.*;

public class LiNameTest {

	@Test
	public void test() {
		testAbcDef("AbcDef");
		testAbcDef("abcDef");
		testAbcDef("ABCDef");
		testAbcDef("AbcDEF");
		testAbcDef("abc_def");
		testAbcDef("ABC_DEF");
		testAbcDef("abc-def");

		testBreakage("");

		testBreakage("-");
		testBreakage("-abc");
		testBreakage("abc-");
		testBreakage("abc--def");

		testBreakage("abc__def");
		testBreakage("_abc-def");
		testBreakage("__");

		Name n = LiName.create("__abc_def");
		assertEquals("__AbcDef", n.camelCase(true));
		assertEquals("__abcDef", n.camelCase(false));
		assertEquals("__ABC_DEF", n.snakeCase(true));
		assertEquals("__abc_def", n.snakeCase(false));

		n = LiName.create("__abcDef");
		assertEquals("__AbcDef", n.camelCase(true));
		assertEquals("__abcDef", n.camelCase(false));
		assertEquals("__ABC_DEF", n.snakeCase(true));
		assertEquals("__abc_def", n.snakeCase(false));

		n = LiName.create("abc_def_");
		assertEquals("AbcDef_", n.camelCase(true));
		assertEquals("abcDef_", n.camelCase(false));
		assertEquals("ABC_DEF_", n.snakeCase(true));
		assertEquals("abc_def_", n.snakeCase(false));

		n = LiName.create("AbcDef_");
		assertEquals("AbcDef_", n.camelCase(true));
		assertEquals("abcDef_", n.camelCase(false));
		assertEquals("ABC_DEF_", n.snakeCase(true));
		assertEquals("abc_def_", n.snakeCase(false));

		n = LiName.create("abc");
		assertEquals("Abc", n.camelCase(true));
		assertEquals("abc", n.camelCase(false));
		assertEquals("ABC", n.snakeCase(true));
		assertEquals("abc", n.snakeCase(false));

		n = LiName.create("ABC");
		assertEquals("Abc", n.camelCase(true));
		assertEquals("abc", n.camelCase(false));
		assertEquals("ABC", n.snakeCase(true));
		assertEquals("abc", n.snakeCase(false));
		assertTrue(n.isReference("ABC"));
		assertFalse(n.isReference("Abc"));
		assertArrayEquals(new String[]{"abc", "ABC"}, n.uniqueIds());

		n = LiName.create("qwe-xyz-def");
		assertEquals("QweXyzDef", n.camelCase(true));
		assertEquals("qweXyzDef", n.camelCase(false));
		assertEquals("QWE_XYZ_DEF", n.snakeCase(true));
		assertEquals("qwe_xyz_def", n.snakeCase(false));
		assertArrayEquals(new String[]{"qwexyzdef", "qwe-xyz-def"}, n.uniqueIds());
	}

	private void testAbcDef(String identifier) {
		Name n = LiName.create(identifier);
		assertEquals("AbcDef", n.camelCase(true));
		assertEquals("AbcDef", n.camelCase(true)); // caching
		assertEquals("abcDef", n.camelCase(false));
		assertEquals("abcDef", n.camelCase(false)); // caching
		assertEquals("ABC_DEF", n.snakeCase(true));
		assertEquals("ABC_DEF", n.snakeCase(true)); // caching
		assertEquals("abc_def", n.snakeCase(false));
		assertEquals("abc_def", n.snakeCase(false)); // caching

		assertTrue(n.isReference(identifier));
		assertArrayEquals(new String[]{"abcdef", identifier}, n.uniqueIds());
	}

	private void testBreakage(String identifier) {
		try {
			LiName.create(identifier);
			fail("no exception for " + identifier);
		} catch (NameParseException ex) {
			/* ignore */
		}
	}
}
