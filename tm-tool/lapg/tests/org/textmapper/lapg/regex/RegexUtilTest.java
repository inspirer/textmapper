/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.lapg.regex;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegexUtilTest {

	@Test
	public void testUnescapeHex() {
		assertEquals(0, RegexUtil.unescapeHex("0"));
		assertEquals(10, RegexUtil.unescapeHex("a"));
		assertEquals(11, RegexUtil.unescapeHex("b"));
		assertEquals(12, RegexUtil.unescapeHex("C"));
		assertEquals(16, RegexUtil.unescapeHex("10"));
		assertEquals(39664, RegexUtil.unescapeHex("9aF0"));
		assertEquals(0xabcdef, RegexUtil.unescapeHex("abcdef"));
		assertEquals(0xabcdef12, RegexUtil.unescapeHex("ABCDEF12"));
		assertEquals(0xffffffff, RegexUtil.unescapeHex("ffffffff"));
		try {
			RegexUtil.unescapeHex("");
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof IllegalArgumentException);
		}
		try {
			RegexUtil.unescapeHex("123456789");
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof IllegalArgumentException);
		}
		try {
			RegexUtil.unescapeHex("9aF0!");
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof NumberFormatException);
		}
		try {
			RegexUtil.unescapeHex("g");
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof NumberFormatException);
		}
		try {
			RegexUtil.unescapeHex("G");
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof NumberFormatException);
		}
	}

	private String escape(int ch, boolean inSet) {
		StringBuilder sb = new StringBuilder();
		RegexUtil.escape(sb, ch, inSet);
		return sb.toString();
	}

	@Test
	public void testEscape() throws Exception {
		assertEquals("\\xfe", escape(0xfe, false));
		assertEquals("\\u0123", escape(0x123, false));
		assertEquals("\\u123a", escape(0x123a, false));
		assertEquals("\\U000abcde", escape(0xabcde, false));
		assertEquals("\\U0010ffff", escape(Character.MAX_CODE_POINT, false));
		try {
			RegexUtil.escape(new StringBuilder(), Character.MAX_CODE_POINT + 1, false);
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof IllegalArgumentException);
		}
		try {
			RegexUtil.escape(new StringBuilder(), -1, false);
			fail("no exception");
		} catch (Throwable th) {
			assertTrue(th instanceof IllegalArgumentException);
		}
	}
}
