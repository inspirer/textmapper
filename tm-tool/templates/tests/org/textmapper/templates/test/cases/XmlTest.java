/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
package org.textmapper.templates.test.cases;

import org.junit.Test;
import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.types.ITypesRegistry;
import org.textmapper.templates.bundle.DefaultTemplateLoader;
import org.textmapper.templates.bundle.IBundleLoader;
import org.textmapper.templates.bundle.TemplatesRegistry;
import org.textmapper.templates.eval.TemplatesFacade;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.xml.XmlIxFactory;
import org.textmapper.xml.XmlModel;
import org.textmapper.xml.XmlNode;

import static org.junit.Assert.assertEquals;

public class XmlTest {

	@Test
	public void testSelector() {
		XmlNode n = XmlModel.load(" <r><user name='jone'/>go<user name='go'/></r> ");

		TestProblemCollector collector = new TestProblemCollector();
		TemplatesFacade env = new TemplatesFacade(new XmlIxFactory(), createRegistry(collector), collector);

		// test 1
		String q = env.executeTemplate("xmltest.xmldo", new EvaluationContext(n), null, null);
		assertEquals("jone\ngo\n", q);
		collector.assertEmptyErrors();

		q = env.executeTemplate("xmltest.selectit", new EvaluationContext(n), null, null);
		assertEquals("name=\"jone\"\n", q);
		collector.assertEmptyErrors();
	}

	private TemplatesRegistry createRegistry(TestProblemCollector collector) {
		ResourceRegistry resources = new ResourceRegistry(new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/templates/test/ltp", "utf8"));
		ITypesRegistry types = new TypesRegistry(resources, collector);
		return new TemplatesRegistry(collector, types, (IBundleLoader) new DefaultTemplateLoader(resources));
	}
}
