package org.textway.lapg.test.cases;

import java.util.Arrays;

import junit.framework.Assert;
import org.textway.lapg.lex.LexConstants;
import org.textway.lapg.lex.RegexpParseException;
import org.textway.lapg.lex.RegexpParser;
import org.textway.lapg.lex.RegexpParserNew;

public class RegexpParseTest extends LapgTestCase {
	private static char[] HEX = new char[] { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

	private String toHex4(int i) {
		return "" + HEX[i>>12&0xf] + HEX[i>>8&0xf] +HEX[i>>4&0xf]+HEX[i&0xf];
	}

	public void testRegexpParserTime() {
		Assert.assertEquals("0000", toHex4(0));
		Assert.assertEquals("abcf", toHex4(0xabcf));
		Assert.assertEquals("75e1", toHex4(0x75e1));

		RegexpParser rp = new RegexpParser();
		try{
			for(int i = 20; i < 2020; i++) {
				rp.compile(i, "n"+i, "[\\u"+toHex4(i*10)+"-\\u"+toHex4(i*10+6)+"]");
			}
			for(int i = 20; i < 2020; i++) {
				rp.compile(i, "nb"+i, "[\\u"+toHex4(i*10+3)+"-\\u"+toHex4(i*10+8)+"]");
			}
		} catch(RegexpParseException ex) {
			Assert.fail("parse failed: " + ex.getMessage());
		}
		rp.buildSets();

		Assert.assertEquals(6002, rp.getSymbolCount());
	}

	public void testNewParser() {
		RegexpParserNew rp = new RegexpParserNew();
		try {
			rp.compile(0, "a", "Eea{1,2}");
			Assert.fail("no exception");
		} catch (RegexpParseException e) {
			Assert.assertEquals("unsupported quantifier: a{1,2}", e.getMessage());
		}

		try {
			rp.compile(1, "b", "Eea{bbi}");
			Assert.fail("no exception");
		} catch (RegexpParseException e) {
			Assert.assertEquals("cannot expand {bbi}", e.getMessage());
		}
	}

	public void testRegexpParserInvertedSet() {
		RegexpParser rp = new RegexpParser();
		try {
			rp.compile(0, "string", "'[^'\n]+'");
			rp.compile(1, "percent", "%");
		} catch(RegexpParseException ex) {
			Assert.fail("parse failed: " + ex.getMessage());
		}
		rp.buildSets();
		Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));

		int[] character2sym = rp.getCharacterMap();
		Assert.assertEquals(character2sym['%'], 3);
		Assert.assertEquals(character2sym['\''], 2);

		int[][] res = rp.getSetToSymbolsMap();
		Assert.assertEquals(1, res.length);
		Assert.assertEquals("[1, 3]", Arrays.toString(res[0]));
	}

	public void testRegexpParser() {
		RegexpParser rp = new RegexpParser();
		try {
			rp.compile(0, "string", "[a-zA-Z_][a-zA-Z0-9_]*");
			rp.buildSets();
			Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));

			rp = new RegexpParser();
			rp.compile(0, "string", "[a-zA-Z_][a-zA-Z0-9_]*");
			rp.compile(1, "keyw", "do");
			rp.buildSets();
			Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 4, 1, 4, 4, 4, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));

			rp = new RegexpParser();
			rp.compile(0, "string", "[a-w][p-z]");
			rp.compile(1, "string2", "[b-c][y-z]");
			rp.buildSets();
			Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 3, 6, 6, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));
			int[][] p = rp.getSetToSymbolsMap();
			Assert.assertEquals("[2, 4, 5]", Arrays.toString(p[0]));
			Assert.assertEquals("[3, 4, 6]", Arrays.toString(p[1]));
			Assert.assertEquals("[5]", Arrays.toString(p[2]));
			Assert.assertEquals("[6]", Arrays.toString(p[3]));
		} catch(RegexpParseException ex) {
			Assert.fail("parse failed: " + ex.getMessage());
		}
	}

	public void testUnicode() {
		RegexpParser rp = new RegexpParser();
		try {
			rp.compile(0, "string", "[\\x5151-\\x5252][\\x1000-\\x2000]");
		} catch (RegexpParseException ex) {
			Assert.fail("parse failed: " + ex.getMessage());
		}
		int[] expected = new int[0x8000];
		Arrays.fill(expected, 1);
		Arrays.fill(expected, 0x5151, 0x5252+1, 2);
		Arrays.fill(expected, 0x1000, 0x2000+1, 3);
		expected[0] = 0;
		rp.buildSets();
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(rp.getCharacterMap()));
	}

	public void testHexConverter() {
		Assert.assertEquals(0, RegexpParser.parseHex("0"));
		Assert.assertEquals(10, RegexpParser.parseHex("a"));
		Assert.assertEquals(11, RegexpParser.parseHex("b"));
		Assert.assertEquals(12, RegexpParser.parseHex("C"));
		Assert.assertEquals(16, RegexpParser.parseHex("10"));
		Assert.assertEquals(39664, RegexpParser.parseHex("9aF0"));
		try {
			RegexpParser.parseHex("9aF0!");
			Assert.fail("no exception");
		} catch(Throwable th) {
			Assert.assertTrue(th instanceof NumberFormatException);
		}
		try {
			RegexpParser.parseHex("g");
			Assert.fail("no exception");
		} catch(Throwable th) {
			Assert.assertTrue(th instanceof NumberFormatException);
		}
		try {
			RegexpParser.parseHex("G");
			Assert.fail("no exception");
		} catch(Throwable th) {
			Assert.assertTrue(th instanceof NumberFormatException);
		}
	}

	public void testParserParen() {
		try {
			RegexpParser rp = new RegexpParser();
			int[] result = rp.compile(0, "string", "()");
			Assert.assertEquals(Arrays.toString(new int[]{ LexConstants.LBR + 1, LexConstants.RBR, -1 }), Arrays.toString(result));

			result = rp.compile(0, "string2", "(a|)");
			Assert.assertEquals(Arrays.toString(new int[]
					{ LexConstants.LBR + 3, LexConstants.SYM | rp.getCharacterMap()['a'],
							LexConstants.OR, LexConstants.RBR, -1 }), Arrays.toString(result));

			result = rp.compile(0, "string3", "(.)");
			Assert.assertEquals(Arrays.toString(new int[]
					{ LexConstants.LBR + 2, LexConstants.ANY, LexConstants.RBR, -1 }), Arrays.toString(result));

			result = rp.compile(0, "string4", "(abc|)");
			Assert.assertEquals(Arrays.toString(new int[]
					{ LexConstants.LBR + 5, LexConstants.SYM | rp.getCharacterMap()['a'],
							LexConstants.SYM | rp.getCharacterMap()['b'],
							LexConstants.SYM | rp.getCharacterMap()['c'],
							LexConstants.OR, LexConstants.RBR, -1 }), Arrays.toString(result));

		} catch (RegexpParseException e) {
			Assert.fail(e.toString());
		}

	}

	public void testParserExc() {
		RegexpParser rp = new RegexpParser();
		try {
			rp.compile(0, "string", "[\\x5151-\\x5252][\\x1000-\\x2000");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains unpaired brackets", ex.getMessage());
			Assert.assertEquals(29, ex.getErrorOffset());
		}

		try {
			rp.compile(1, "string1", "[\\x5151]]-\\x5252]][\\x1000-\\x2000");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("unexpected closing brace, escape it to use as character", ex.getMessage());
			Assert.assertEquals(8, ex.getErrorOffset());
		}

		try {
			rp.compile(2, "empty", "");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp is empty", ex.getMessage());
			Assert.assertEquals(0, ex.getErrorOffset());
		}

		try {
			rp.compile(3, "paren", "(abc");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains unpaired parentheses", ex.getMessage());
			Assert.assertEquals(4, ex.getErrorOffset());
		}

		try {
			rp.compile(4, "paren2", "(abc))xyz");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains unpaired parentheses", ex.getMessage());
			Assert.assertEquals(5, ex.getErrorOffset());
		}

		try {
			rp.compile(5, "paren3", "((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((abc))");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains too much parentheses", ex.getMessage());
			Assert.assertEquals(126, ex.getErrorOffset());
		}

		try {
			rp.compile(5, "slash1", "aaa\\");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains \\ at the end of expression", ex.getMessage());
			Assert.assertEquals(4, ex.getErrorOffset());
		}

		try {
			rp.compile(5, "unicode1", "aaa\\x4a5!zzz");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains incomplete unicode symbol", ex.getMessage());
			Assert.assertEquals(5, ex.getErrorOffset());
		}

		try {
			rp.compile(5, "unicode2", "aaa\\x4a");
		} catch (RegexpParseException ex) {
			Assert.assertEquals("regexp contains incomplete unicode symbol", ex.getMessage());
			Assert.assertEquals(7, ex.getErrorOffset());
		}
	}
}
