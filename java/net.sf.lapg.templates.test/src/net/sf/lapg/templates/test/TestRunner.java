package net.sf.lapg.templates.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.lapg.templates.test.cases.LoopTest;

public class TestRunner {
    public static Test suite() {
        TestSuite ts = new TestSuite("templates tests");
        ts.addTestSuite(LoopTest.class);
        return ts;
      }

}
