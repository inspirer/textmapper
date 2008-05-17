package net.sf.lapg.templates.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.lapg.templates.test.cases.TemplateConstructionsTest;
import net.sf.lapg.templates.test.cases.XmlTest;

public class TestRunner {
    public static Test suite() {
        TestSuite ts = new TestSuite("templates tests");
        ts.addTestSuite(TemplateConstructionsTest.class);
        ts.addTestSuite(XmlTest.class);
        return ts;
      }

}
