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
import org.textway.lapg.regex.RegexDefTree;
import org.textway.lapg.regex.RegexDefTree.TextSource;
import org.textway.lapg.regex.RegexPart;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexParserTest extends TestCase {

	public void testParens() {
		checkRegex("[a-z]");
		checkRegex("[^A-Z]");
		checkRegex("([^A-Z]+)A");
		checkRegex("([^A-Z]+|B)A");
		checkRegex("(([^A-Z])+|B)A");
		checkRegex("((([^A-Z])+)|B)A");
		checkRegex("(((([^A-Z])+)|B)A)");
	}

	public void testSet() {
		checkRegex("[a-z-]", "[a-z\\-]");
		checkRegex("[-a-z]", "[\\-a-z]");
		checkRegex("[a-{]", "[a\\-\\{]");

		checkErrors("[\\.-z]", "invalid range in character class (before dash): `\\.', escape `-'");
	}

	private void checkRegex(String regex) {
		checkRegex(regex, regex);
	}

	private void checkRegex(String regex, String expected) {
		RegexDefTree<RegexPart> result = RegexDefTree.parse(new TextSource("input", regex.toCharArray(), 1));
		if(result.hasErrors()) {
			Assert.fail(result.getErrors().get(0).getMessage());
		}
		RegexPart root = result.getRoot();
		Assert.assertNotNull(root);
		Assert.assertEquals(expected, root.toString());
	}

	private void checkErrors(String regex, String ...expectedErrors) {
		RegexDefTree<RegexPart> result = RegexDefTree.parse(new TextSource("input", regex.toCharArray(), 1));
		Assert.assertTrue("no errors :(", result.hasErrors());
		for(int i = 0; i < Math.max(expectedErrors.length, result.getErrors().size()); i++) {
			String expected = i < expectedErrors.length ? expectedErrors[i] : null;
			String actual = i < result.getErrors().size() ? result.getErrors().get(i).getMessage() : null;
			Assert.assertEquals(expected, actual);
		}
	}
}


