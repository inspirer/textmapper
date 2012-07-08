/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.lapg.test.unicode;

import org.junit.Test;
import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.common.CharacterSetImpl;
import org.textmapper.lapg.common.CharacterSetImpl.Builder;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.lapg.test.gen.LapgTemplatesTestHelper;
import org.textmapper.lapg.test.unicode.data.UnicodeParser;
import org.textmapper.lapg.test.unicode.data.UnicodeParser.UnicodeBuilder;
import org.textmapper.lapg.unicode.UnicodeData;
import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.types.TypesRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 11/16/11
 */
public class UnicodeTest {

	private static final boolean TEST_DATA_FILES = false;

	@Test
	public void testFilesUpToDate() throws IOException {
		if (!TEST_DATA_FILES) return;
		testContent("data/UnicodeData.txt", "http://www.unicode.org/Public/UNIDATA/UnicodeData.txt");
	}

	private void testContent(String resource, String httplocation) throws IOException {
		String local = getContent(UnicodeTest.class.getResource(resource));
		String remote = getContent(new URL(httplocation));
		assertEquals(remote, local);
	}

	private static String getContent(URL location) throws IOException {
		InputStream inputStream = location.openStream();
		try {
			return FileUtil.getFileContents(inputStream, "utf-8");
		} finally {
			inputStream.close();
		}
	}

	@Test
	public void testUnicodeData() throws IOException {

		final Map<String, Builder> allCharset = new HashMap<String, Builder>();
		for (String category : UnicodeParser.CATEGORIES) {
			allCharset.put(category, new Builder());
		}

		new UnicodeParser().parseData(UnicodeTest.class.getResource("data/UnicodeData.txt"), new UnicodeBuilder() {
			private int prevsym = -1;

			private void yield(int c, String category) {
				allCharset.get(category).addSymbol(c);
			}

			@Override
			public void character(int c, String name, String category, int upper, int lower, int title) {
				assertTrue(c > prevsym);
				prevsym++;
				while (prevsym < c) {
					yield(prevsym++, "Cn");
				}
				yield(c, category);

			}

			@Override
			public void range(int start, int end, String rangeName, String category) {
				assertTrue(start > prevsym);
				prevsym++;
				while (prevsym < start) {
					yield(prevsym++, "Cn");
				}
				while (prevsym < end) {
					yield(prevsym++, category);
				}
				yield(end, category);
			}

			@Override
			public void done() {
			}
		});

		final List<NamedSet> result = new ArrayList<NamedSet>();
		String[] categories = UnicodeParser.CATEGORIES.toArray(new String[UnicodeParser.CATEGORIES.size()]);
		Arrays.sort(categories);
		for (String category : categories) {
			CharacterSet set = allCharset.get(category).create();
			CharacterSet actual = UnicodeData.getInstance().getCharacterSet(category);
			assertFalse(actual.isInverted());
			assertTrue(set.equals(actual));
			if (!"Cs".equals(category)) {
				// only Cs category contains surrogates
				assertTrue(new Builder().intersect(set, new CharacterSetImpl(0xd800, 0xdfff)).isEmpty());
			}
			result.add(new NamedSet(category, set));
		}

		new LapgTemplatesTestHelper() {
			@Override
			protected EvaluationContext createEvaluationContext(TypesRegistry types, Map<String, Object> genOptions) {
				HashMap<String, Object> res = new HashMap<String, Object>();
				res.put("version", "6.1.0");
				res.put("properties", result);
				EvaluationContext evaluationContext = new EvaluationContext(res);
				evaluationContext.setVariable("util", new UnicodeTemplateUtil());
				return evaluationContext;
			}
		}.gentest(
				"unicode.tables", "tests/org/textmapper/lapg/test/unicode/templates",
				"../lapg-core/src/org/textmapper/lapg/unicode",
				new String[]{"UnicodeDataTables.java"});
	}

	public static class NamedSet {

		private final String name;
		private final CharacterSet set;

		public NamedSet(String name, CharacterSet set) {
			this.name = name;
			this.set = set;
		}

		public String getName() {
			return name;
		}

		public CharacterSet getSet() {
			return set;
		}
	}
}
