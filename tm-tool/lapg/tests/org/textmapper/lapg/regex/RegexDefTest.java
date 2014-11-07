/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
import org.textmapper.lapg.api.regex.*;
import org.textmapper.lapg.regex.RegexDefLexer.ErrorReporter;
import org.textmapper.lapg.regex.RegexDefLexer.Span;
import org.textmapper.lapg.regex.RegexDefLexer.Tokens;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexDefTest {

	@Test
	public void testParens() {
		checkRegex("[a-z]");
		checkRegex("[{a}(aa)]");
		checkRegex("{a}{2}");
		checkRegex("(A|)");
		checkRegex("[^A-Z]");
		checkRegex("([^A-Z]+)A");
		checkRegex("([^A-Z]+|B)A");
		checkRegex("(([^A-Z])+|B)A");
		checkRegex("((([^A-Z])+)|B)A");
		checkRegex("(((([^A-Z])+)|B)A)");
	}

	@Test
	public void testSpecialChars() {
		checkRegex("\\a");
		checkRegex("\\b");
		checkRegex("\\f");
		checkRegex("\\n");
		checkRegex("\\r");
		checkRegex("\\t");
		checkRegex("\\v");
	}

	@Test
	public void testCharClasses() {
		checkRegex("");
		checkRegex("\\001", "\\x01");
		checkRegex("\\011", "\\t");
		checkRegex("\\022", "\\x12");
		checkRegex("\\111", "I");
		checkRegex("\\xf40");
		checkErrors("\\u200", "Unexpected end of input reached");
		checkErrors("\\x2x0", "invalid lexeme at line 1: `\\x2x`, skipped");
		checkErrors("\\x2x", "invalid lexeme at line 1: `\\x2x`, skipped");
		checkErrors("\\u200xx", "invalid lexeme at line 1: `\\u200x`, skipped");
		checkRegex("\\uf40b");
	}

	@Test
	public void testIPv6() {
		checkRegex("\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1," +
				"4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(" +
				"([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
				"(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1," +
				"3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
				"(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1," +
				"4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
				"(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1," +
				"5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
				"(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1," +
				"6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
				"(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1," +
				"4}){0," +
				"5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)" +
				"?\\s*");
	}

	@Test
	public void testSet() {
		checkRegex("[a-z-]", "[a-z\\-]");
		checkRegex("[-a-z]", "[\\-a-z]");
		checkRegex("[a\\-{]", "[a\\-{]");

		checkErrors("[a-{]", "invalid range in character class (after dash): `\\{', escape `-'");
		checkErrors("[\\.-z]", "invalid range in character class (before dash): `\\.', escape `-'");
	}

	@Test
	public void testUnicodeSet() {
		RegexPart r = checkRegex("[\\w\\p{Ll}]");
		assertTrue(r instanceof RegexSet);

		CharacterSet set = ((RegexSet) r).getSet();
		assertTrue(set.contains('a'));
		assertTrue(set.contains('_'));
		assertTrue(set.contains('\u0458'));
		assertTrue(!set.contains('\u0408'));
	}

	@Test
	public void testQuantifiers() {
		checkRegex("{aaa}");
		checkErrors("{aaa }", "invalid lexeme at line 1: `{aaa `, skipped");
		checkErrors("a{aaa }", "invalid lexeme at line 1: `{aaa `, skipped");
		checkRegex("a{9}");
		checkRegex("a{9,}");
		checkRegex("a{9,10}");
	}

	@Test
	public void testLexer() throws IOException {
		checkLexer("abc", Tokens._char, Tokens._char, Tokens._char);
		checkLexer("\\w++", Tokens.charclass, Tokens.Plus, Tokens._char);
		checkLexer("(\\011{1,3}{name})",
				Tokens.Lparen, Tokens.escaped, Tokens.quantifier, Tokens.expand, Tokens.Rparen);
		checkLexer("[^()a-z]", Tokens.LsquareXor, Tokens._char, Tokens._char, Tokens._char, Tokens.Minus, Tokens._char,
				Tokens.Rsquare);
		checkLexer("a{+}\\p{abc}{-}\\x12{eoi}", Tokens._char, Tokens.op_union, Tokens.charclass, Tokens.op_minus,
				Tokens.escaped, Tokens.kw_eoi);
	}

	@Test
	public void testConstants() {
		checkConstantRegex("abc", null, "abc");
		checkConstantRegex("(a(b)c)", null, "abc");
		checkConstantRegex("ab(c)", null, "abc");
		checkConstantRegex("(abc)", null, "abc");
		checkConstantRegex("abc()", null, "abc");
		checkConstantRegex("\\t", null, "\t");
		checkConstantRegex("\\u0009", "\\t", "\t");

		assertFalse(checkRegex("a{9,10}").isConstant());
		assertFalse(checkRegex("aa(b|)").isConstant());
		assertFalse(checkRegex("aab?").isConstant());
		assertFalse(checkRegex("aab*").isConstant());
	}

	@Test
	public void testSwitch1() {
		checkParserViaSwitch("(a|[a-z]+){name}+a{9,10}\\\\.",
				"list () [\n" +
						"\tlist (in paren) [\n" +
						"\t\tor [\n" +
						"\t\t\ta\n" +
						"\t\t\tquantifier {1,-1} [\n" +
						"\t\t\t\t[a-z]\n" +
						"\t\t\t]\n" +
						"\t\t]\n" +
						"\t]\n" +
						"\tquantifier {1,-1} [\n" +
						"\t\t{name}\n" +
						"\t]\n" +
						"\tquantifier {9,10} [\n" +
						"\t\ta\n" +
						"\t]\n" +
						"\t\\\\\n" +
						"\t.\n" +
						"]\n");
	}

	@Test
	public void testSwitch2() {
		checkParserViaSwitch("(a|)++",
				"list () [\n" +
						"\tquantifier {1,-1} [\n" +
						"\t\tlist (in paren) [\n" +
						"\t\t\tor [\n" +
						"\t\t\t\ta\n" +
						"\t\t\t\t<empty>\n" +
						"\t\t\t]\n" +
						"\t\t]\n" +
						"\t]\n" +
						"\t\\+\n" +
						"]\n");
	}

	private void checkConstantRegex(String regex, String converted, String value) {
		RegexPart regexPart = checkRegex(regex, converted == null ? regex : converted);
		assertTrue(regexPart.isConstant());
		String val = regexPart.getConstantValue();
		assertEquals(value, val);
	}

	private RegexPart checkRegex(String regex) {
		return checkRegex(regex, regex);
	}

	private void checkLexer(String regex, int... tokens) throws IOException {
		RegexDefLexer lexer = new RegexDefLexer(regex, new ErrorReporter() {
			@Override
			public void error(String message, int offset, int endoffset) {
				fail(message);
			}
		});
		Span next;
		for (int i = 0; i < tokens.length; i++) {
			next = lexer.next();
			assertEquals(tokens[i], next.symbol);
		}
		next = lexer.next();
		assertEquals(Tokens.eoi, next.symbol);
	}

	private RegexPart checkRegex(String regex, String expected) {
		RegexDefTree<RegexAstPart> result = RegexDefTree.parse(new TextSource("input", regex, 1));
		if (result.hasErrors()) {
			fail(result.getErrors().get(0).getMessage());
		}
		RegexPart root = result.getRoot();
		assertNotNull(root);
		assertEquals(expected, root.toString());
		return root;
	}

	private void checkErrors(String regex, String... expectedErrors) {
		RegexDefTree<RegexAstPart> result = RegexDefTree.parse(new TextSource("input", regex, 1));
		assertTrue("no errors :(", result.hasErrors());
		for (int i = 0; i < Math.max(expectedErrors.length, result.getErrors().size()); i++) {
			String expected = i < expectedErrors.length ? expectedErrors[i] : null;
			String actual = i < result.getErrors().size() ? result.getErrors().get(i).getMessage() : null;
			assertEquals(expected, actual);
		}
	}

	private void checkParserViaSwitch(String regex, String expected) {
		RegexDefTree<RegexAstPart> result = RegexDefTree.parse(new TextSource("input", regex, 1));
		if (result.hasErrors()) {
			fail(result.getErrors().get(0).getMessage());
		}
		RegexPart root = result.getRoot();
		String actual = root.accept(new RegexSwitch<String>() {
			@Override
			public String caseAny(RegexAny c) {
				return c.toString();
			}

			@Override
			public String caseChar(RegexChar c) {
				return c.toString();
			}

			@Override
			public String caseExpand(RegexExpand c) {
				return c.toString();
			}

			@Override
			public String caseList(RegexList c) {
				StringBuilder sb = new StringBuilder();
				sb.append("list (").append(c.isParenthesized() ? "in paren" : "").append(") [\n");
				for (RegexPart regexPart : c.getElements()) {
					String s = regexPart.accept(this);
					for (String line : s.split("\n")) {
						sb.append('\t').append(line).append('\n');
					}
				}
				sb.append("]\n");
				return sb.toString();
			}

			@Override
			public String caseOr(RegexOr c) {
				StringBuilder sb = new StringBuilder();
				sb.append("or [\n");
				for (RegexPart regexPart : c.getVariants()) {
					String s = regexPart.accept(this);
					for (String line : s.split("\n")) {
						sb.append('\t').append(line).append('\n');
					}
				}
				sb.append("]\n");
				return sb.toString();
			}

			@Override
			public String caseQuantifier(RegexQuantifier c) {
				StringBuilder sb = new StringBuilder();
				sb.append("quantifier {" + c.getMin() + "," + c.getMax() + "} [\n");
				String s = c.getInner().accept(this);
				for (String line : s.split("\n")) {
					sb.append('\t').append(line).append('\n');
				}
				sb.append("]\n");
				return sb.toString();
			}

			@Override
			public String caseSet(RegexSet c) {
				return c.toString();
			}

			@Override
			public String caseEmpty(RegexEmpty c) {
				return "<empty>";
			}

			@Override
			public String caseRange(RegexRange c) {
				fail();
				return null;
			}
		});
		assertEquals(expected, actual);
	}
}


