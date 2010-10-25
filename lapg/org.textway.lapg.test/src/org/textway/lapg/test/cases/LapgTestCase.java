/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg.test.cases;

import java.io.InputStream;
import org.junit.Assert;

import junit.framework.TestCase;

public abstract class LapgTestCase extends TestCase {

	protected static final String TESTCONTAINER = "org/textway/lapg/test/cases/input";
	protected static final String RESULTCONTAINER = "org/textway/lapg/test/cases/expected";

	protected InputStream openStream(String name, String root) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(root + "/" + name);
		Assert.assertNotNull(is);
		return is;
	}

	protected static String removeSpaces(String input) {
		char[] c = new char[input.length()];
		input.getChars(0, input.length(), c, 0);

		int to = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] != ' ' && c[i] != '\t') {
				c[to++] = c[i];
			}
		}

		return new String(c, 0, to);
	}
}