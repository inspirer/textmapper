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
package org.textway.lapg.test.cases;

import org.junit.Test;
import org.textway.lapg.api.regex.RegexPart;
import org.textway.lapg.lex.LexConstants;
import org.textway.lapg.lex.RegexMatcher;
import org.textway.lapg.lex.RegexpCompiler;
import org.textway.lapg.lex.RegexpParseException;
import org.textway.lapg.regex.RegexUtil;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class RegexpParseTest {
	private static char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private String toHex4(int i) {
		return "" + HEX[i >> 12 & 0xf] + HEX[i >> 8 & 0xf] + HEX[i >> 4 & 0xf] + HEX[i & 0xf];
	}

	@Test
	public void testRegexpParserTime() {
		assertEquals("0000", toHex4(0));
		assertEquals("abcf", toHex4(0xabcf));
		assertEquals("75e1", toHex4(0x75e1));

		RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
		try {
			for (int i = 20; i < 2020; i++) {
				rp.compile(i, parseRegexp("[\\u" + toHex4(i * 10) + "-\\u" + toHex4(i * 10 + 6) + "]"));
			}
			for (int i = 20; i < 2020; i++) {
				rp.compile(i, parseRegexp("[\\u" + toHex4(i * 10 + 3) + "-\\u" + toHex4(i * 10 + 8) + "]"));
			}
		} catch (RegexpParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}

		assertEquals(6002, rp.getInputSymbols().getSymbolCount());
	}

	@Test
	public void testNewParser() {
		RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
		try {
			rp.compile(0, parseRegexp("Eea{1,2}"));
			fail("no exception");
		} catch (RegexpParseException e) {
			assertEquals("unsupported quantifier: a{1,2}", e.getMessage());
		}

		try {
			rp.compile(1, parseRegexp("Eea{bbi}"));
			fail("no exception");
		} catch (RegexpParseException e) {
			assertEquals("cannot expand {bbi}, not found", e.getMessage());
		}
	}

	@Test
	public void testRegexpParserInvertedSet() {
		RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
		try {
			rp.compile(0, parseRegexp("'[^'\n]+'"));
			rp.compile(1, parseRegexp("%"));
		} catch (RegexpParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}
		assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));

		int[] character2sym = rp.getInputSymbols().getCharacterMap();
		assertEquals(character2sym['%'], 3);
		assertEquals(character2sym['\''], 2);

		int[][] res = rp.getInputSymbols().getSetToSymbolsMap();
		assertEquals(1, res.length);
		assertEquals("[1, 3]", Arrays.toString(res[0]));
	}

	@Test
	public void testRegexpParser() {
		RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
		try {
			rp.compile(0, parseRegexp("[a-zA-Z_][a-zA-Z0-9_]*"));
			assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));

			rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
			rp.compile(0, parseRegexp("[a-zA-Z_][a-zA-Z0-9_]*"));
			rp.compile(1, parseRegexp("do"));
			assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 4, 1, 4, 4, 4, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));

			rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
			rp.compile(0, parseRegexp("[a-w][p-z]"));
			rp.compile(1, parseRegexp("[b-c][y-z]"));
			assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 3, 6, 6, 1, 1, 1, 1, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));
			int[][] p = rp.getInputSymbols().getSetToSymbolsMap();
			assertEquals("[2, 4, 5]", Arrays.toString(p[0]));
			assertEquals("[3, 4, 6]", Arrays.toString(p[1]));
			assertEquals("[5]", Arrays.toString(p[2]));
			assertEquals("[6]", Arrays.toString(p[3]));
		} catch (RegexpParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}
	}

	@Test
	public void testUnicode() {
		RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
		try {
			rp.compile(0, parseRegexp("[\\u5151-\\u5252][\\u1000-\\u2000]"));
		} catch (RegexpParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}
		int[] expected = new int[0x8000];
		Arrays.fill(expected, 1);
		Arrays.fill(expected, 0x5151, 0x5252 + 1, 2);
		Arrays.fill(expected, 0x1000, 0x2000 + 1, 3);
		expected[0] = 0;
		assertEquals(Arrays.toString(expected), Arrays.toString(rp.getInputSymbols().getCharacterMap()));
	}

	@Test
	public void testHexConverter() {
		assertEquals(0, RegexUtil.unescapeHex("0"));
		assertEquals(10, RegexUtil.unescapeHex("a"));
		assertEquals(11, RegexUtil.unescapeHex("b"));
		assertEquals(12, RegexUtil.unescapeHex("C"));
		assertEquals(16, RegexUtil.unescapeHex("10"));
		assertEquals(39664, RegexUtil.unescapeHex("9aF0"));
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

	@Test
	public void testParserParen() {
		try {
			RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
			int[] result = rp.compile(0, parseRegexp("()"));
			assertEquals(Arrays.toString(new int[]{LexConstants.LBR + 1, LexConstants.RBR, -1}), Arrays.toString(result));

			rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
			result = rp.compile(0, parseRegexp("(a|)"));
			assertEquals(Arrays.toString(new int[]
					{LexConstants.LBR + 3, LexConstants.SYM | rp.getInputSymbols().getCharacterMap()['a'],
							LexConstants.OR, LexConstants.RBR, -1}), Arrays.toString(result));

			rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
			result = rp.compile(0, parseRegexp("(.)"));
			assertEquals(Arrays.toString(new int[]
					{LexConstants.LBR + 2, LexConstants.ANY, LexConstants.RBR, -1}), Arrays.toString(result));

			rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
			result = rp.compile(0, parseRegexp("(abc|)"));
			assertEquals(Arrays.toString(new int[]
					{LexConstants.LBR + 5, LexConstants.SYM | rp.getInputSymbols().getCharacterMap()['a'],
							LexConstants.SYM | rp.getInputSymbols().getCharacterMap()['b'],
							LexConstants.SYM | rp.getInputSymbols().getCharacterMap()['c'],
							LexConstants.OR, LexConstants.RBR, -1}), Arrays.toString(result));

		} catch (RegexpParseException e) {
			fail(e.toString());
		}

	}

	@Test
	public void testParserExc() {
		RegexpCompiler rp = new RegexpCompiler(Collections.<String, RegexPart>emptyMap());
		try {
			rp.compile(0, parseRegexp("[\\x5151-\\x5252][\\x1000-\\x2000"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(29, ex.getErrorOffset());
		}

		try {
			rp.compile(1, parseRegexp("[\\x5151-\\x5252][\\x1000-\\x2000]]"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp has syntax error near `]'", ex.getMessage());
			assertEquals(30, ex.getErrorOffset());
		}

		try {
			rp.compile(2, parseRegexp("[\\x5151]]-\\x5252]][\\x1000-\\x2000"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp has syntax error near `]-\\x5252]][\\x1000-\\x2000'", ex.getMessage());
			assertEquals(8, ex.getErrorOffset());
		}

		try {
			rp.compile(3, parseRegexp(""));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp is empty", ex.getMessage());
			assertEquals(0, ex.getErrorOffset());
		}

		try {
			rp.compile(4, parseRegexp("(abc"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp is incomplete", ex.getMessage());
			assertEquals(4, ex.getErrorOffset());
		}

		try {
			rp.compile(5, parseRegexp("(abc))xyz"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp has syntax error near `)xyz'", ex.getMessage());
			assertEquals(5, ex.getErrorOffset());
		}

		try {
			rp.compile(6, parseRegexp("((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((abc))"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp is incomplete", ex.getMessage());
			assertEquals(135, ex.getErrorOffset());
		}

		try {
			rp.compile(7, parseRegexp("aaa\\"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		}

		try {
			rp.compile(8, parseRegexp("aaa\\u4a5!zzz"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("regexp has syntax error near `\\u4a5!zzz'", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		}

		try {
			rp.compile(9, parseRegexp("aaa\\u4a"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		}

		try {
			rp.compile(9, parseRegexp("aaa\\00"));
			fail("no exception");
		} catch (RegexpParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		} catch (Exception ex) {
			fail("wrong exception");
		}
	}

	private static RegexPart parseRegexp(String s) throws RegexpParseException {
		return RegexMatcher.parse("", s);
	}
}
