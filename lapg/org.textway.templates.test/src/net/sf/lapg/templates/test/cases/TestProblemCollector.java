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
package net.sf.lapg.templates.test.cases;

import junit.framework.Assert;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.IProblemCollector;

import java.util.ArrayList;

public class TestProblemCollector implements IProblemCollector {
	public ArrayList<String> nextErrors = new ArrayList<String>();

	public void addErrors(String... errors) {
		for (String s : errors) {
			nextErrors.add(s);
		}
	}

	public void assertEmptyErrors() {
		if (nextErrors.size() > 0) {
			Assert.fail("error is not reported: " + nextErrors.get(0));
		}
	}

	public void fireError(ILocatedEntity referer, String error) {
		if (nextErrors.size() > 0) {
			String next = nextErrors.remove(0);
			Assert.assertEquals(next, error);
		} else {
			Assert.fail(error);
		}
	}
}
