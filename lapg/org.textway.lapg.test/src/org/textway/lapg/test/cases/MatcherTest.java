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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.textway.lapg.lex.RegexMatcher;
import org.textway.lapg.lex.RegexpParseException;

import java.util.regex.Pattern;

/**
 * Gryaznov Evgeny, 5/7/11
 */
public class MatcherTest extends TestCase {

	public void testSimple() throws RegexpParseException {
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

	public void testQuantifiers() {
		checkMatch("lapg(T*)", "lapgTTTT", true);
		checkMatch("lapg(T*)", "prefixlapgTTTTTTTTT", false);
		checkMatch("lapg(T*)", "lapgTpostfix", false);
	}

	public void testIdentifier() throws RegexpParseException {
		RegexMatcher matcher = new RegexMatcher("[a-zA-Z_][a-zA-Z0-9_]+");
		checkMatch(matcher, "aaaa", true);
		checkMatch(matcher, "aa0aa", true);
		checkMatch(matcher, "aa0aa ", false);
		checkMatch(matcher, "0aa0aa", false);
	}

	public void testRegex() throws RegexpParseException {
		RegexMatcher matcher = new RegexMatcher("\\/([^\\/\\\\\\n]|\\\\.)*\\/");
		checkMatch(matcher, "/aaa/", true);
		checkMatch(matcher, "/tt\\\\t+/", true);
		checkMatch(matcher, "//", true);
		checkMatch(matcher, " /", false);
		checkMatch(matcher, "// ", false);
		checkPatternMatch(matcher.toString(), "/tt\\/", false);
		checkMatch(matcher, "/tt\\/", false);
	}

	public void testUnicode() {
		for (int cp = 0; cp < 0x333; cp++) {
			String s = "L" + new String(Character.toChars(cp)) + "R";
			checkMatch("L" + String.format("\\u%04x", cp) + "R", s, true);
			checkMatch("L[" + String.format("\\u%04x", cp) + "]R", s, true);
			if(cp < 0xff) {
				checkMatch("L" + String.format("\\x%02x", cp) + "R", s, true);
				checkMatch("L[" + String.format("\\x%02x", cp) + "]R", s, true);
			}
		}
	}

	private static void checkPatternMatch(String regex, String sample, boolean expected) {
		boolean matches = Pattern.matches(regex, sample);
		Assert.assertEquals(expected, matches);
	}

	private static void checkMatch(String regex, String sample, boolean expected) {
		try {
			RegexMatcher matcher = new RegexMatcher(regex);
			Assert.assertEquals("regex: `" + regex + "` vs sample: `" + sample + "`", expected, matcher.matches(sample));
		} catch (RegexpParseException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private static void checkMatch(RegexMatcher matcher, String sample, boolean expected) {
		Assert.assertEquals("regex: `" + matcher.toString() + "` vs sample: `" + sample + "`", expected, matcher.matches(sample));
	}
}
