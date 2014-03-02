/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
package org.textmapper.templates.test.cases;

import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestProblemCollector implements TemplatesStatus {
	public ArrayList<String> nextErrors = new ArrayList<String>();

	public void addErrors(String... errors) {
		for (String s : errors) {
			nextErrors.add(s);
		}
	}

	public void assertEmptyErrors() {
		if (nextErrors.size() > 0) {
			fail("error is not reported: " + nextErrors.get(0));
		}
	}

	public void report(int kind, String message, SourceElement... anchors) {
		if (kind == KIND_ERROR || kind == KIND_FATAL) {
			if (anchors != null && anchors.length >= 1 && anchors[0] != null) {
				String resourceName = anchors[0].getResourceName();
				resourceName = resourceName.replaceAll("\\\\", "/");
				if (resourceName.indexOf('/') != -1) {
					resourceName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
				}
				message = resourceName + "," + anchors[0].getLine() + ": " + message;
			}
			if (nextErrors.size() > 0) {
				String next = nextErrors.remove(0);
				assertEquals(next, message);
			} else {
				fail(message);
			}
		}
	}
}
