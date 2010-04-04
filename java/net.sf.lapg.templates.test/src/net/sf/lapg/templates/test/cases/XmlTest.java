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
package net.sf.lapg.templates.test.cases;

import junit.framework.Assert;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.impl.ClassTemplateLoader;
import net.sf.lapg.templates.model.xml.XmlModel;
import net.sf.lapg.templates.model.xml.XmlNavigationFactory;
import net.sf.lapg.templates.model.xml.XmlNode;
import net.sf.lapg.templates.test.TemplateTestCase;

public class XmlTest extends TemplateTestCase{

	public void testSelector() {
		XmlNode n = XmlModel.load(" <r><user name='jone'/>go<user name='go'/></r> ");

		TestTemplatesFacade env = new TestTemplatesFacade(new XmlNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), "net/sf/lapg/templates/test/ltp", "utf8"));

		// test 1
		String q = env.executeTemplate("xmltest.xmldo", new EvaluationContext(n), null, null);
		Assert.assertEquals("jone\ngo\n", q);
		env.assertEmptyErrors();

		q = env.executeTemplate("xmltest.selectit", new EvaluationContext(n), null, null);
		Assert.assertEquals("name=\"jone\"\n", q);
		env.assertEmptyErrors();
	}
}
