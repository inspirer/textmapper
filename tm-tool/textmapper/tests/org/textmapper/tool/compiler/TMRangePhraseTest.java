/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.tool.compiler;

import org.junit.Test;
import org.textmapper.lapg.test.TestStatus;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TMRangePhraseTest {
	@Test
	public void merge() throws Exception {
		TMRangePhrase p1 = new TMRangePhrase(new TMRangeField("a"), new TMRangeField("b"));
		TMRangePhrase p2 = new TMRangePhrase(new TMRangeField("a"));

		TMRangePhrase result = TMRangePhrase.merge(
				"cc", Arrays.asList(p1, p2), null, new TestStatus());

		assertEquals("a b?", result.toString());

		result = TMRangePhrase.merge(
				"cc", Arrays.asList(p1, TMRangePhrase.empty()), null, new TestStatus());

		assertEquals("a? b?", result.toString());
	}

	@Test
	public void identicalTypes() throws Exception {
		TMRangePhrase p1 = new TMRangePhrase(new TMRangeField("a"), new TMRangeField("a"));

		TMRangePhrase result = TMRangePhrase.merge(
				"cc", Arrays.asList(p1), null,
				new TestStatus(null, "two fields with the same signature `a` in cc\n"));

		assertEquals("a", result.toString());
	}
}