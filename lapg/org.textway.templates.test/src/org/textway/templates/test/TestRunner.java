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
package org.textway.templates.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.textway.templates.test.cases.ApiTests;
import org.textway.templates.test.cases.TemplateConstructionsTest;
import org.textway.templates.test.cases.TypesTest;
import org.textway.templates.test.cases.XmlTest;

public class TestRunner {
	public static Test suite() {
		TestSuite ts = new TestSuite("templates tests");
		ts.addTestSuite(TypesTest.class);
		ts.addTestSuite(ApiTests.class);
		ts.addTestSuite(TemplateConstructionsTest.class);
		ts.addTestSuite(XmlTest.class);
		return ts;
	}
}
