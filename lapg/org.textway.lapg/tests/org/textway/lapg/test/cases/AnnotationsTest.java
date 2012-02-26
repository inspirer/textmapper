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
import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.gen.SyntaxUtil;
import org.textway.lapg.parser.LapgGrammar;
import org.textway.lapg.test.TestStatus;
import org.textway.templates.api.SourceElement;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TypesRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings({"deprecation"})
public class AnnotationsTest extends LapgTestCase {

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			@Override
			public void report(int kind, String message, SourceElement... anchors) {
				fail(message);
			}
		});
	}

	@Test
	public void testAllAnnotations() {
		LapgGrammar lg = SyntaxUtil.parseSyntax("syntax1annotated", openStream("syntax1annotated", TESTCONTAINER),
				new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(lg);

		Grammar g = lg.getGrammar();
		assertNotNull(g);

		Rule[] listItemRules = rulesForName(g.getRules(), "list_item");
		assertEquals(2, listItemRules.length);

		Symbol s = listItemRules[0].getLeft();
		assertEquals(5, lg.getAnnotation(s, "weight"));
		assertEquals("wwo", lg.getAnnotation(s, "name"));
		assertEquals(Boolean.TRUE, lg.getAnnotation(s, "noast"));

		assertEquals("rule1", lg.getAnnotation(listItemRules[0], "name"));
		assertEquals("rule2", lg.getAnnotation(listItemRules[1], "name"));

		Object val = lg.getAnnotation(listItemRules[0].getRight()[0], "ids");
		assertTrue(val instanceof List<?>);

		List<?> list = (List<?>) val;
		assertEquals(3, list.size());
		assertEquals(4, list.get(0));
		assertEquals(2, list.get(1));
		assertEquals(3, list.get(2));

		Rule[] inputRules = rulesForName(g.getRules(), "input");
		assertEquals(1, inputRules.length);
		Symbol input = inputRules[0].getLeft();

		assertNotNull(input);
		Object refval = lg.getAnnotation(input, "ref");
		assertNotNull(refval);
		assertTrue(refval instanceof Symbol);
		assertTrue(refval == s);
	}

	@Test
	public void testBadAnnotations() {
		TestStatus notifier = new TestStatus("", "syntax1errannotated,23: notexistingsym cannot be resolved\n"
				+ "syntax1errannotated,29: redeclaration of annotation `name' for non-terminal: tempanno, skipped\n");
		LapgGrammar g = SyntaxUtil.parseSyntax("syntax1errannotated", openStream("syntax1errannotated", TESTCONTAINER),
				notifier, createDefaultTypesRegistry());
		notifier.assertDone();
		assertNull(g);
	}

	private static Rule[] rulesForName(Rule[] rules, String name) {
		ArrayList<Rule> result = new ArrayList<Rule>();
		for (Rule r : rules) {
			assertNotNull(r.getLeft());
			assertNotNull(r.getLeft().getName());
			if (r.getLeft().getName().equals(name)) {
				result.add(r);
			}
		}
		return result.toArray(new Rule[result.size()]);
	}
}
