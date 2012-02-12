/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.textway.lapg.test.cases.*;


public class TestRunner {
	public static Test suite() {
		TestSuite ts = new TestSuite("lapg tests");
		ts.addTestSuite(NlaTest.class);
		ts.addTestSuite(SoftTermsTest.class);
		ts.addTestSuite(GrammarTest.class);
		ts.addTestSuite(TemplateStaticMethodsTest.class);
		ts.addTestSuite(AnnotationsTest.class);
		ts.addTestSuite(JavaPostProcessorTest.class);
		ts.addTestSuite(LexerGeneratorTest.class);
		ts.addTestSuite(InputTest.class);
		ts.addTestSuite(JavaTablesCompression.class);
		ts.addTestSuite(ConsoleArgsTest.class);
		ts.addTestSuite(BootstrapTest.class);

		ts.addTestSuite(CharacterSetTest.class);
		ts.addTestSuite(IntegerSetsTest.class);
		ts.addTestSuite(MatcherTest.class);
		ts.addTestSuite(RegexDefTest.class);
		ts.addTestSuite(RegexpParseTest.class);
		return ts;
	}

}
