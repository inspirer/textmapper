/**
 * Copyright 2002-2010 Evgeny Gryaznov
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

/**
 * Gryaznov Evgeny, 5/7/11
 */
public class MatcherTest extends TestCase {

	public void testIdentifier() throws RegexpParseException {
		RegexMatcher matcher = new RegexMatcher("[a-zA-Z_][a-zA-Z0-9_]+");
		Assert.assertTrue(matcher.matches("aaaa"));
		Assert.assertTrue(matcher.matches("aa0aa"));
		Assert.assertFalse(matcher.matches("aa0aa "));
		Assert.assertFalse(matcher.matches("0aa0aa"));
	}

	public void testRegex() throws RegexpParseException {
		RegexMatcher matcher = new RegexMatcher("\\/([^\\/\\\\\\n]|\\\\.)*\\/");
		Assert.assertTrue(matcher.matches("/aaa/"));
		Assert.assertTrue(matcher.matches("/tt\\\\t+/"));
		Assert.assertFalse(matcher.matches(" /"));
		Assert.assertTrue(matcher.matches("//"));
		Assert.assertFalse(matcher.matches("// "));
	}
}
