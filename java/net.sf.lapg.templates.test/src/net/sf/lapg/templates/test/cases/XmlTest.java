package net.sf.lapg.templates.test.cases;

import junit.framework.Assert;
import net.sf.lapg.templates.model.xml.XmlModel;
import net.sf.lapg.templates.model.xml.XmlNode;
import net.sf.lapg.templates.test.TemplateTestCase;

public class XmlTest extends TemplateTestCase{

	public void testSelector() {
		XmlNode n = XmlModel.load(" <r><user name='jone'/>go<user name='go'/></r> ");
		TestEnvironment env = new TestEnvironment(getClass().getClassLoader(), "net/sf/lapg/templates/test/ltp");

		// test 1
		String q = env.executeTemplate("loop.xmldo", n, null);
		Assert.assertEquals("jone\r\ngo\r\n", q);
		env.assertEmptyErrors();

		q = env.executeTemplate("loop.selectit", n, null);
		Assert.assertEquals("jone\r\n", q);
		env.assertEmptyErrors();
	}
}
