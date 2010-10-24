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
package org.textway.templates.test.cases;

import junit.framework.Assert;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.impl.ClassTemplateLoader;
import org.textway.templates.api.impl.TemplatesFacade;
import org.textway.templates.api.impl.TemplatesRegistry;
import org.textway.templates.model.xml.XmlModel;
import org.textway.templates.model.xml.XmlNavigationFactory;
import org.textway.templates.model.xml.XmlNode;
import org.textway.templates.test.TemplateTestCase;

public class XmlTest extends TemplateTestCase{

	public void testSelector() {
		XmlNode n = XmlModel.load(" <r><user name='jone'/>go<user name='go'/></r> ");

		TestProblemCollector collector = new TestProblemCollector();
		TemplatesFacade env = new TemplatesFacade(new XmlNavigationFactory(), new TemplatesRegistry(collector, new ClassTemplateLoader(getClass().getClassLoader(), "net/sf/lapg/templates/test/ltp", "utf8")), collector);

		// test 1
		String q = env.executeTemplate("xmltest.xmldo", new EvaluationContext(n), null, null);
		Assert.assertEquals("jone\ngo\n", q);
		collector.assertEmptyErrors();

		q = env.executeTemplate("xmltest.selectit", new EvaluationContext(n), null, null);
		Assert.assertEquals("name=\"jone\"\n", q);
		collector.assertEmptyErrors();
	}
}
