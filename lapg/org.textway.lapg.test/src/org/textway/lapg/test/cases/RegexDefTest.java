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
import org.textway.lapg.regex.*;
import org.textway.lapg.regex.RegexDefTree.TextSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexDefTest extends TestCase {

	public void testParens() {
		checkRegex("[a-z]");
		checkRegex("(A|)");
		checkRegex("[^A-Z]");
		checkRegex("([^A-Z]+)A");
		checkRegex("([^A-Z]+|B)A");
		checkRegex("(([^A-Z])+|B)A");
		checkRegex("((([^A-Z])+)|B)A");
		checkRegex("(((([^A-Z])+)|B)A)");
	}

	public void testCharClasses() {
		checkRegex("");
		checkRegex("\\xf40");
		checkErrors("\\u200", "Unexpected end of input reached");
		checkErrors("\\x2x0", "invalid lexem at line 1: `\\x2x`, skipped");
		checkErrors("\\x2x", "invalid lexem at line 1: `\\x2x`, skipped");
		checkErrors("\\u200xx", "invalid lexem at line 1: `\\u200x`, skipped");
		checkRegex("\\uf40b");
	}

	public void testIPv6() {
		checkRegex("\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*");
	}

	public void testSet() {
		checkRegex("[a-z-]", "[a-z\\-]");
		checkRegex("[-a-z]", "[\\-a-z]");
		checkRegex("[a-{]", "[a\\-\\{]");

		checkErrors("[\\.-z]", "invalid range in character class (before dash): `\\.', escape `-'");
	}

	public void testQuantifiers() {
		checkRegex("{aaa}");
		checkErrors("{aaa }", "an expansion identifier is expected instead of `aaa '");
		checkErrors("a{aaa }", "quantifier range or expansion identifier is expected instead of `aaa '");
		checkRegex("a{9}");
		checkRegex("a{9,}");
		checkRegex("a{9,10}");
	}

	public void testVisitor1() {
		checkVisitor("(a|[a-z]+){name}a{9,10}\\\\.",
			"before: (a|[a-z]+){name}a{9,10}\\\\.",
				"before: (a|[a-z]+)",
					"before: a|[a-z]+",
						"a",
					"between: a|[a-z]+",
						"before: [a-z]+",
							"[a-z]",
						"after: [a-z]+",
					"after: a|[a-z]+",
				"after: (a|[a-z]+)",
				"{name}",
				"before: a{9,10}",
					"a",
				"after: a{9,10}",
				"\\\\",
				".",
			 "after: (a|[a-z]+){name}a{9,10}\\\\.");
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

	private void checkVisitor(String regex, String ...expectedElements) {
		RegexDefTree<RegexPart> result = RegexDefTree.parse(new TextSource("input", regex.toCharArray(), 1));
		if(result.hasErrors()) {
			Assert.fail(result.getErrors().get(0).getMessage());
		}
		RegexPart root = result.getRoot();
		final List<String> actual = new ArrayList<String>();
		root.accept(new RegexVisitor() {
			@Override
			public void visit(RegexAny c) {
				actual.add(c.toString());
			}

			@Override
			public void visit(RegexChar c) {
				actual.add(c.toString());
			}

			@Override
			public void visit(RegexExpand c) {
				actual.add(c.toString());
			}

			@Override
			public void visitBefore(RegexList c) {
				actual.add("before: " + c.toString());
			}

			@Override
			public void visitAfter(RegexList c) {
				actual.add("after: " + c.toString());
			}

			@Override
			public void visitBefore(RegexOr c) {
				actual.add("before: " + c.toString());
			}

			@Override
			public void visitBetween(RegexOr c) {
				actual.add("between: " + c.toString());
			}

			@Override
			public void visitAfter(RegexOr c) {
				actual.add("after: " + c.toString());
			}

			@Override
			public void visitBefore(RegexQuantifier c) {
				actual.add("before: " + c.toString());
			}

			@Override
			public void visitAfter(RegexQuantifier c) {
				actual.add("after: " + c.toString());
			}

			@Override
			public boolean visit(RegexSet c) {
				actual.add(c.toString());
				return false;
			}

			@Override
			public void visit(RegexRange c) {
				Assert.fail();
			}
		});
		for(int i = 0; i < Math.max(expectedElements.length, actual.size()); i++) {
			String expected = i < expectedElements.length ? expectedElements[i] : null;
			String act = i < actual.size() ? actual.get(i) : null;
			Assert.assertEquals(expected, act);
		}
	}
}


