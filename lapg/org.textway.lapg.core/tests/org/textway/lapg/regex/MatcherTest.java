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
package org.textway.lapg.regex;

import org.junit.Test;
import org.textway.lapg.api.regex.RegexContext;
import org.textway.lapg.api.regex.RegexParseException;
import org.textway.lapg.api.regex.RegexPart;

import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Gryaznov Evgeny, 5/7/11
 */
public class MatcherTest {

	@Test
	public void testSimple() throws RegexParseException {
		checkMatch("axy", "ayy", false);
		checkMatch("axy", "axy", true);
		checkMatch("abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz", true);
		checkMatch("\\u1234", "\u1234", true);
		checkMatch("(b)", "b", true);

		// set
		checkMatch("[@]", "@", true);
		checkMatch("[^@]", "@", false);
		checkMatch("[^@]", "\u1234", true);

		// or
		checkMatch("a|ax", "ax", true);
		checkMatch("a|ax", "a", true);
		checkMatch("a|ax", "ay", false);
	}

	@Test
	public void testSpecialChars() throws RegexParseException {
		checkMatch("\\a", "\\a", false);
		checkMatch("\\a", "\007", true);
		checkMatch("\\b", "\b", true);
		checkMatch("\\b", "\\b", false);
		checkMatch("\\f", "\f", true);
		checkMatch("\\f", "\\f", false);
		checkMatch("\\f", "f", false);
		checkMatch("\\n", "\n", true);
		checkMatch("\\n", "\\", false);
		checkMatch("\\n", "\\n", false);
		checkMatch("\\n", "n", false);
		checkMatch("\\r", "\r", true);
		checkMatch("\\t", "\t", true);
		checkMatch("\\v", "\u000b", true);
	}

	@Test
	public void testQuantifiers() {
		checkMatch("lapg(T*)", "lapgTTTT", true);
		checkMatch("lapg(T*)", "prefixlapgTTTTTTTTT", false);
		checkMatch("lapg(T*)", "lapgTpostfix", false);
	}

	@Test
	public void testIdentifier() throws RegexParseException {
		RegexPart parsedRegex = RegexFacade.parse("id", "[a-zA-Z_][a-zA-Z0-9_]+");
		RegexMatcherImpl matcher = new RegexMatcherImpl(parsedRegex, createEmptyContext());
		checkMatch(matcher, "aaaa", true);
		checkMatch(matcher, "aa0aa", true);
		checkMatch(matcher, "aa0aa ", false);
		checkMatch(matcher, "0aa0aa", false);
	}

	@Test
	public void testRegex() throws RegexParseException {
		RegexPart parsedRegex = RegexFacade.parse("re", "\\/([^\\/\\\\\\n]|\\\\.)*\\/");
		RegexMatcherImpl matcher = new RegexMatcherImpl(parsedRegex, createEmptyContext());
		checkMatch(matcher, "/aaa/", true);
		checkMatch(matcher, "/tt\\\\t+/", true);
		checkMatch(matcher, "//", true);
		checkMatch(matcher, " /", false);
		checkMatch(matcher, "// ", false);
		checkPatternMatch(matcher.toString(), "/tt\\/", false);
		checkMatch(matcher, "/tt\\/", false);
	}

	@Test
	public void testUnicode() {
		for (int cp = 0; cp < 0x333; cp++) {
			String s = "L" + new String(Character.toChars(cp)) + "R";
			checkMatch("L" + String.format("\\u%04x", cp) + "R", s, true);
			checkMatch("L[" + String.format("\\u%04x", cp) + "]R", s, true);
			if (cp < 0xff) {
				checkMatch("L" + String.format("\\x%02x", cp) + "R", s, true);
				checkMatch("L[" + String.format("\\x%02x", cp) + "]R", s, true);
			}
		}
	}

	private static void checkPatternMatch(String regex, String sample, boolean expected) {
		boolean matches = Pattern.matches(regex, sample);
		assertEquals(expected, matches);
	}

	private static void checkMatch(String regex, String sample, boolean expected) {
		try {
			RegexPart parsedRegex = RegexFacade.parse("unknown", regex);
			RegexMatcherImpl matcher = new RegexMatcherImpl(parsedRegex, createEmptyContext());
			assertEquals("regex: `" + regex + "` vs sample: `" + sample + "`", expected, matcher.matches(sample));
		} catch (RegexParseException ex) {
			fail(ex.getMessage());
		}
	}

	private static void checkMatch(RegexMatcherImpl matcher, String sample, boolean expected) {
		assertEquals("regex: `" + matcher.toString() + "` vs sample: `" + sample + "`", expected, matcher.matches(sample));
	}

	private static RegexContext createEmptyContext() {
		return RegexFacade.createContext(Collections.<String, RegexPart>emptyMap());
	}
}
