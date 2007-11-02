package net.sf.lapg.templates.test.cases;

import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.model.xml.XmlModel;
import net.sf.lapg.templates.model.xml.XmlNode;
import net.sf.lapg.templates.test.TemplateTestCase;

public class XmlTest extends TemplateTestCase{

	public void testForEach() {
		XmlNode n = XmlModel.load(" <r><user name='jone'/>go<user name='go'/></r> ");


		IEvaluationEnvironment env = new TestEnvironment(getClass().getClassLoader(), "net/sf/lapg/templates/test/ltp");

		// test 1
//		String q = env.executeTemplate("xml.show1", new HashMap<Object,Object>(), null);
//		Assert.assertEquals("???", q);
	}
}
