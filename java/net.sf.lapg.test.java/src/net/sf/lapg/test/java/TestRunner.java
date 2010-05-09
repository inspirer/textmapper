package net.sf.lapg.test.java;

import net.sf.lapg.test.java.cases.JavaGen;
import junit.framework.Test;
import junit.framework.TestSuite;


public class TestRunner {
	public static Test suite() {
		TestSuite ts = new TestSuite("lapg generation tests");
		ts.addTestSuite(JavaGen.class);
		return ts;
	}

}
