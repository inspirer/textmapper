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
		TMRangePhrase p1 = phrase(field("a"), field("b"));
		TMRangePhrase p2 = phrase(field("a"));

		TMRangePhrase result = TMRangePhrase.merge(
				Arrays.asList(p1, p2), null, new TestStatus());

		assertEquals("a b?", result.toString());

		result = TMRangePhrase.merge(
				Arrays.asList(p1, TMRangePhrase.empty()), null, new TestStatus());

		assertEquals("a? b?", result.toString());
	}

	@Test
	public void illegalMerge() throws Exception {
		TMRangePhrase p1 = phrase(
				field("a").withExplicitName("q", false), field("b").withExplicitName("p", false));
		TMRangePhrase p2 = phrase(field("b").withExplicitName("p", false),
				field("c"), field("a").withExplicitName("q", false));

		TMRangePhrase.merge(
				Arrays.asList(p1, p2), null,
				new TestStatus(null,
						"named elements must occur in the same order in all productions\n"));
	}

	@Test
	public void concat() throws Exception {
		TMRangePhrase p1 = phrase(field("b"));
		TMRangePhrase p2 = phrase(field("a"));

		TMRangePhrase result = TMRangePhrase.concat(
				Arrays.asList(p1, p2), null, new TestStatus());

		assertEquals("b a", result.toString());
	}

	@Test
	public void identicalTypes() throws Exception {
		TMRangePhrase p1 = phrase(field("a"), field("a"));

		TMRangePhrase result = TMRangePhrase.merge(
				Arrays.asList(p1), null,
				new TestStatus(null, "two fields with the same signature `a`\n"));

		assertEquals("a", result.toString());
	}

	@Test
	public void namedAndUnnamed() throws Exception {
		TMRangePhrase p1 = phrase(field("a"));
		TMRangePhrase p2 = phrase(field("a").withExplicitName("q", false));

		TMRangePhrase.merge(
				Arrays.asList(p1, p2), null,
				new TestStatus(null, "`a` occurs in both named and unnamed fields\n"));

		TMRangePhrase.concat(
				Arrays.asList(p1, p2), null,
				new TestStatus(null, "`a` occurs in both named and unnamed fields\n"));
	}

	@Test
	public void unnamedConflict() throws Exception {
		TMRangePhrase p1 = phrase(field("a"));
		TMRangePhrase p2 = phrase(field("l", "a", "b"));

		TMRangePhrase.merge(
				Arrays.asList(p1, p2), null,
				new TestStatus(null, "two unnamed fields share the same type `a`\n"));

		TMRangePhrase.concat(
				Arrays.asList(p1, p2), null,
				new TestStatus(null, "two unnamed fields share the same type `a`\n"));
	}

	private static TMRangePhrase phrase(TMRangeField ...fields) {
		return new TMRangePhrase(fields);
	}

	private static TMRangeField field(String name, String ...types) {
		if (types.length == 0) {
			return new TMRangeField(name);
		}
	    TMRangeField[] fields = new TMRangeField[types.length];
		for (int i = 0; i < types.length; i++) {
			fields[i] = new TMRangeField(types[i]);
		}
		return TMRangeField.merge(fields).withName(name);
	}
}