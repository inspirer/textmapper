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

import static org.junit.Assert.assertEquals;

public class FormatUtilTest {
	@Test
	public void testToCamelCase() throws Exception {
		assertEquals("aaaBbbCcc", FormatUtil.toCamelCase("aaa_bbb_ccc", false));
		assertEquals("AaaBbbCcc", FormatUtil.toCamelCase("aaa_bbb_ccc_", true));
		assertEquals("A", FormatUtil.toCamelCase("a", true));
		assertEquals("A", FormatUtil.toCamelCase("_A_", true));
	}

	@Test
	public void testToUpperWithUnderscores() throws Exception {
		assertEquals("AAA_BBB_CCC", FormatUtil.toUpperWithUnderscores("aaa_bbb_ccc_"));
		assertEquals("AAA_BBB_CCC", FormatUtil.toUpperWithUnderscores("AaaBbbCcc_"));
		assertEquals("AAA_BBB_CCC", FormatUtil.toUpperWithUnderscores("_Aaa_bbbCcc_"));
	}

	@Test
	public void testEscape() throws Exception {
		assertEquals("\\\'", FormatUtil.escape("'"));
		assertEquals("\\\"", FormatUtil.escape("\""));
		assertEquals("\\\\", FormatUtil.escape("\\"));
		assertEquals("\\uffff", FormatUtil.escape("\uFFFF"));
		assertEquals("\\u0100", FormatUtil.escape("\u0100"));
		assertEquals("\\xff", FormatUtil.escape("\u00FF"));
		assertEquals("\\x08", FormatUtil.escape("\u0008"));
		assertEquals("\\t", FormatUtil.escape("\u0009"));
		assertEquals("\\x00", FormatUtil.escape("\u0000"));
		assertEquals("\\f\\n\\r\\t", FormatUtil.escape("\f\n\r\t"));
		assertEquals(" abc", FormatUtil.escape(" abc"));
	}
}
