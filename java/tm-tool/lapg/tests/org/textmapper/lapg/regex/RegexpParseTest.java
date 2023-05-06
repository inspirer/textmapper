/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.lex.RegexInstruction;
import org.textmapper.lapg.lex.RegexInstructionKind;
import org.textmapper.lapg.lex.RegexpCompiler;

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

		RegexpCompiler rp = createTestCompiler();
		try {
			for (int i = 20; i < 2020; i++) {
				rp.compile(i, parseRegexp("[\\u" + toHex4(i * 10) + "-\\u" + toHex4(i * 10 + 6) + "]"));
			}
			for (int i = 20; i < 2020; i++) {
				rp.compile(i, parseRegexp("[\\u" + toHex4(i * 10 + 3) + "-\\u" + toHex4(i * 10 + 8) + "]"));
			}
		} catch (RegexParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}

		assertEquals(6002, rp.getInputSymbols().getSymbolCount());
	}

	@Test
	public void testNewParser() {
		RegexpCompiler rp = createTestCompiler();
		try {
			rp.compile(0, parseRegexp("Eea{3,2}"));
			fail("no exception");
		} catch (RegexParseException e) {
			assertEquals("wrong quantifier: a{3,2}", e.getMessage());
		}

		try {
			rp.compile(1, parseRegexp("Eea{bbi}"));
			fail("no exception");
		} catch (RegexParseException e) {
			assertEquals("cannot expand {bbi}, not found", e.getMessage());
		}
	}

	@Test
	public void testRegexpParserInvertedSet() {
		RegexpCompiler rp = createTestCompiler();
		try {
			rp.compile(0, parseRegexp("'[^'\n]+'"));
			rp.compile(1, parseRegexp("%"));
		} catch (RegexParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}
		assertEquals("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 4, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));

		int[] character2sym = rp.getInputSymbols().getCharacterMap();
		assertEquals(3, character2sym['%']);
		assertEquals(4, character2sym['\'']);

		int[][] res = rp.getInputSymbols().getSetToSymbolsMap();
		assertEquals(1, res.length);
		assertEquals("[1, 3]", Arrays.toString(res[0]));
	}

	@Test
	public void testRegexpParser() {
		RegexpCompiler rp = createTestCompiler();
		try {
			rp.compile(0, parseRegexp("[a-zA-Z_][a-zA-Z0-9_]*"));
			assertEquals("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));

			rp = createTestCompiler();
			rp.compile(0, parseRegexp("[a-zA-Z_][a-zA-Z0-9_]*"));
			rp.compile(1, parseRegexp("do"));
			assertEquals("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 3, 1, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 5, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));

			rp = createTestCompiler();
			rp.compile(0, parseRegexp("[a-w][p-z]"));
			rp.compile(1, parseRegexp("[b-c][y-z]"));
			assertEquals("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 5, 6, 6, 1]", Arrays.toString(rp.getInputSymbols().getCharacterMap()));
			int[][] p = rp.getInputSymbols().getSetToSymbolsMap();
			assertEquals("[2, 3, 4]", Arrays.toString(p[0]));
			assertEquals("[4, 5, 6]", Arrays.toString(p[1]));
			assertEquals("[3]", Arrays.toString(p[2]));
			assertEquals("[6]", Arrays.toString(p[3]));
		} catch (RegexParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}
	}

	@Test
	public void testUnicode() {
		RegexpCompiler rp = createTestCompiler();
		try {
			rp.compile(0, parseRegexp("[\\u5151-\\u5252][\\u1000-\\u2000]"));
		} catch (RegexParseException ex) {
			fail("parse failed: " + ex.getMessage());
		}
		int[] expected = new int[0x5254];
		Arrays.fill(expected, 1);
		Arrays.fill(expected, 0x5151, 0x5252 + 1, 3);
		Arrays.fill(expected, 0x1000, 0x2000 + 1, 2);
		assertEquals(Arrays.toString(expected), Arrays.toString(rp.getInputSymbols().getCharacterMap()));
	}

	@Test
	public void testParserParen() {
		try {
			RegexpCompiler rp = createTestCompiler();
			RegexInstruction[] result = rp.compile(0, parseRegexp("()"));
			assertArrayEquals(new RegexInstruction[]{
					new RegexInstruction(RegexInstructionKind.Done, 0, null /*origin*/),
			}, result);

			rp = createTestCompiler();
			result = rp.compile(0, parseRegexp("(a|)"));
			assertArrayEquals(new RegexInstruction[]{
					new RegexInstruction(RegexInstructionKind.LeftParen, 3, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Symbol, 'a', null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Or, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.RightParen, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Done, 0, null /*origin*/),
			}, result);

			rp = createTestCompiler();
			result = rp.compile(0, parseRegexp("(.)"));
			assertArrayEquals(new RegexInstruction[]{
					new RegexInstruction(RegexInstructionKind.Any, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Done, 0, null /*origin*/),
			}, result);

			rp = createTestCompiler();
			result = rp.compile(0, parseRegexp("(.)+"));
			assertArrayEquals(new RegexInstruction[]{
					new RegexInstruction(RegexInstructionKind.LeftParen, 2, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Any, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.RightParen, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.OneOrMore, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Done, 0, null /*origin*/),
			}, result);

			rp = createTestCompiler();
			result = rp.compile(0, parseRegexp(".+"));
			assertArrayEquals(new RegexInstruction[]{
					new RegexInstruction(RegexInstructionKind.LeftParen, 2, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Any, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.RightParen, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.OneOrMore, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Done, 0, null /*origin*/),
			}, result);

			rp = createTestCompiler();
			result = rp.compile(0, parseRegexp("(abc|)"));

			assertArrayEquals(new RegexInstruction[]{
					new RegexInstruction(RegexInstructionKind.LeftParen, 5, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Symbol, 'a', null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Symbol, 'b', null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Symbol, 'c', null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Or, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.RightParen, 0, null /*origin*/),
					new RegexInstruction(RegexInstructionKind.Done, 0, null /*origin*/),
			}, result);

		} catch (RegexParseException e) {
			fail(e.toString());
		}

	}

	@Test
	public void testParserExc() {
		RegexpCompiler rp = createTestCompiler();
		try {
			rp.compile(0, parseRegexp("[\\x5151-\\x5252][\\x1000-\\x2000"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(29, ex.getErrorOffset());
		}

		try {
			rp.compile(1, parseRegexp("[\\x5151-\\x5252][\\x1000-\\x2000]]"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp has syntax error near `]'", ex.getMessage());
			assertEquals(30, ex.getErrorOffset());
		}

		try {
			rp.compile(2, parseRegexp("[\\x5151]]-\\x5252]][\\x1000-\\x2000"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp has syntax error near `]-\\x5252]][\\x1000-\\x2000'", ex.getMessage());
			assertEquals(8, ex.getErrorOffset());
		}

		try {
			rp.compile(3, parseRegexp(""));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp is empty", ex.getMessage());
			assertEquals(0, ex.getErrorOffset());
		}

		try {
			rp.compile(4, parseRegexp("(abc"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp is incomplete", ex.getMessage());
			assertEquals(4, ex.getErrorOffset());
		}

		try {
			rp.compile(5, parseRegexp("(abc))xyz"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp has syntax error near `)xyz'", ex.getMessage());
			assertEquals(5, ex.getErrorOffset());
		}

		try {
			rp.compile(6, parseRegexp("((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((abc))"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp is incomplete", ex.getMessage());
			assertEquals(135, ex.getErrorOffset());
		}

		try {
			rp.compile(7, parseRegexp("aaa\\"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		}

		try {
			rp.compile(8, parseRegexp("aaa\\u4a5!zzz"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("regexp has syntax error near `\\u4a5!zzz'", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		}

		try {
			rp.compile(9, parseRegexp("aaa\\u4a"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		}

		try {
			rp.compile(9, parseRegexp("aaa\\00"));
			fail("no exception");
		} catch (RegexParseException ex) {
			assertEquals("unfinished regexp", ex.getMessage());
			assertEquals(3, ex.getErrorOffset());
		} catch (Exception ex) {
			fail("wrong exception");
		}
	}

	private static RegexPart parseRegexp(String s) throws RegexParseException {
		return RegexFacade.parse("", s);
	}

	private static RegexpCompiler createTestCompiler() {
		return new RegexpCompiler(RegexFacade.createContext(Collections.<String, RegexPart>emptyMap()));
	}
}
