package net.sf.lapg.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.lapg.test.cases.CharacterSetTest;
import net.sf.lapg.test.cases.ConsoleArgsTest;
import net.sf.lapg.test.cases.InputTest;
import net.sf.lapg.test.cases.JavaPostProcessorTest;
import net.sf.lapg.test.cases.JavaTablesCompression;
import net.sf.lapg.test.cases.LexerGeneratorTest;


public class TestRunner {
    public static Test suite() {
        TestSuite ts = new TestSuite("lapg tests");
        ts.addTestSuite(JavaPostProcessorTest.class);
        ts.addTestSuite(CharacterSetTest.class);
        ts.addTestSuite(LexerGeneratorTest.class);
        ts.addTestSuite(InputTest.class);
        ts.addTestSuite(JavaTablesCompression.class);
        ts.addTestSuite(ConsoleArgsTest.class);
        return ts;
      }

}
