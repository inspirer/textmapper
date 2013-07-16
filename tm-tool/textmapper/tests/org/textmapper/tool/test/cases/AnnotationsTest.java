/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.tool.test.cases;

import org.junit.Test;
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.compiler.TMGrammar;
import org.textmapper.tool.gen.SyntaxUtil;
import org.textmapper.tool.parser.TMTree.TextSource;
import org.textmapper.lapg.test.TestStatus;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings({"deprecation"})
public class AnnotationsTest extends LapgTestCase {

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			@Override
			public void report(int kind, String message, SourceElement... anchors) {
				fail(message);
			}
		});
	}

	@Test
	public void testAllAnnotations() {
		TMGrammar lg = SyntaxUtil.parseSyntax(new TextSource("syntax1annotated", FileUtil.getFileContents(openStream("syntax1annotated", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), new TestStatus(), createDefaultTypesRegistry());
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
		TestStatus notifier = new TestStatus("", "syntax1errannotated,27: notexistingsym cannot be resolved\n"
				+ "syntax1errannotated,33: redeclaration of annotation `name' for non-terminal: tempanno, skipped\n");
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax1errannotated", FileUtil.getFileContents(openStream("syntax1errannotated", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), notifier, createDefaultTypesRegistry());
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
