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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.gen.SyntaxUtil;
import org.textway.lapg.test.TestStatus;
import org.textway.templates.api.SourceElement;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TypesRegistry;

@SuppressWarnings({"deprecation"})
public class AnnotationsTest extends LapgTestCase {

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			public void report(int kind, String message, SourceElement... anchors) {
				Assert.fail(message);
			}
		});
	}

	public void testAllAnnotations() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1annotated", openStream("syntax1annotated", TESTCONTAINER),
				new TestStatus(), createDefaultTypesRegistry());
		Assert.assertNotNull(g);

		Rule[] listItemRules = rulesForName(g.getRules(), "list_item");
		Assert.assertEquals(2, listItemRules.length);

		Symbol s = listItemRules[0].getLeft();
		Assert.assertEquals(5, s.getAnnotation("weight"));
		Assert.assertEquals("wwo", s.getAnnotation("name"));
		Assert.assertEquals(Boolean.TRUE, s.getAnnotation("noast"));

		Assert.assertEquals("rule1", listItemRules[0].getAnnotation("name"));
		Assert.assertEquals("rule2", listItemRules[1].getAnnotation("name"));

		Object val = listItemRules[0].getRight()[0].getAnnotation("ids");
		Assert.assertTrue(val instanceof List<?>);

		List<?> list = (List<?>) val;
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(4, list.get(0));
		Assert.assertEquals(2, list.get(1));
		Assert.assertEquals(3, list.get(2));

		Rule[] inputRules = rulesForName(g.getRules(), "input");
		Assert.assertEquals(1, inputRules.length);
		Symbol input = inputRules[0].getLeft();

		Assert.assertNotNull(input);
		Object refval = input.getAnnotation("ref");
		Assert.assertNotNull(refval);
		Assert.assertTrue(refval instanceof Symbol);
		Assert.assertTrue(refval == s);
	}

	public void testBadAnnotations() {
		TestStatus notifier = new TestStatus("", "syntax1errannotated,23: notexistingsym cannot be resolved\n"
				+ "syntax1errannotated,29: redeclaration of annotation `name' for non-terminal: tempanno, skipped\n");
		Grammar g = SyntaxUtil.parseSyntax("syntax1errannotated", openStream("syntax1errannotated", TESTCONTAINER),
				notifier, createDefaultTypesRegistry());
		notifier.assertDone();
		Assert.assertNull(g);
	}

	private static Rule[] rulesForName(Rule[] rules, String name) {
		ArrayList<Rule> result = new ArrayList<Rule>();
		for (Rule r : rules) {
			Assert.assertNotNull(r.getLeft());
			Assert.assertNotNull(r.getLeft().getName());
			if (r.getLeft().getName().equals(name)) {
				result.add(r);
			}
		}
		return result.toArray(new Rule[result.size()]);
	}
}
