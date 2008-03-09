package net.sf.lapg.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.lapg.test.cases.InputTest;
import net.sf.lapg.test.cases.JavaTablesCompression;


public class TestRunner {
    public static Test suite() {
        TestSuite ts = new TestSuite("lapg tests");
        ts.addTestSuite(InputTest.class);
        ts.addTestSuite(JavaTablesCompression.class);
        return ts;
      }

}
