/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.test.TestStatus;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class TMPhraseTest {

	private static SourceElement SOURCE_ELEMENT = new SourceElement() {};

	@Test
	public void merge() throws Exception {
		TMPhrase p1 = phrase(field("a"), field("b"));
		TMPhrase p2 = phrase(field("a"));

		TMPhrase result = TMPhrase.merge(
				Arrays.asList(p1, p2), SOURCE_ELEMENT, new TestStatus());

		assertEquals("a b?", result.toString());

		result = TMPhrase.merge(
				Arrays.asList(p1, TMPhrase.empty(p1)), SOURCE_ELEMENT, new TestStatus());

		assertEquals("a? b?", result.toString());
	}

	@Test
	public void illegalMerge() throws Exception {
		TMPhrase p1 = phrase(
				field("a").withName("q"), field("b").withName("p"));
		TMPhrase p2 = phrase(field("b").withName("p"),
				field("c"), field("a").withName("q"));

		TMPhrase.merge(
				Arrays.asList(p1, p2), SOURCE_ELEMENT,
				new TestStatus(null,
						"named elements must occur in the same order in all productions\n"));
	}

	@Test
	public void concat() throws Exception {
		TMPhrase p1 = phrase(field("b"));
		TMPhrase p2 = phrase(field("a"));

		TMPhrase result = TMPhrase.concat(
				Arrays.asList(p1, p2), SOURCE_ELEMENT, new TestStatus());

		assertEquals("b a", result.toString());
	}

	@Test
	public void namedAndUnnamed() throws Exception {
		TMPhrase p1 = phrase(field("a"));
		TMPhrase p2 = phrase(field("a").withName("q"));

		TMPhrase.merge(
				Arrays.asList(p1, p2), SOURCE_ELEMENT,
				new TestStatus(null, "`a` occurs in both named and unnamed fields\n"));

		TMPhrase.concat(
				Arrays.asList(p1, p2), SOURCE_ELEMENT,
				new TestStatus(null, "`a` occurs in both named and unnamed fields\n"));
	}

	@Test
	public void unnamedConflict() throws Exception {
		TMPhrase p1 = phrase(field("a"));
		TMPhrase p2 = phrase(field("l", "a", "b"));

		TMPhrase.merge(
				Arrays.asList(p1, p2), SOURCE_ELEMENT,
				new TestStatus(null, "two unnamed fields share the same type `a`\n"));

		TMPhrase.concat(
				Arrays.asList(p1, p2), SOURCE_ELEMENT,
				new TestStatus(null, "two unnamed fields share the same type `a`\n"));
	}

	private static TMPhrase phrase(TMField...fields) {
		return new TMPhrase(Arrays.asList(fields), SOURCE_ELEMENT);
	}

	private static TMField field(String name, String ...types) {
		if (types.length == 0) {
			return new TMField(name);
		}
	    TMField[] fields = new TMField[types.length];
		for (int i = 0; i < types.length; i++) {
			fields[i] = new TMField(types[i]);
		}
		return TMField.merge(name, fields).withName(name);
	}
}
