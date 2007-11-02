package net.sf.lapg.templates.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.lapg.templates.test.cases.LoopTest;
import net.sf.lapg.templates.test.cases.XmlTest;

public class TestRunner {
    public static Test suite() {
        TestSuite ts = new TestSuite("templates tests");
        ts.addTestSuite(LoopTest.class);
        ts.addTestSuite(XmlTest.class);
        return ts;
      }

}
