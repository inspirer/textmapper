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
import org.textmapper.lapg.test.unicode.data.NamedRangesParser;
import org.textmapper.lapg.test.unicode.data.NamedRangesParser.NamedRangesBuilder;
import org.textmapper.lapg.test.unicode.data.UnicodeDataParser;
import org.textmapper.lapg.test.unicode.data.UnicodeDataParser.UnicodeDataBuilder;
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

	private static final boolean TEST_DATA_FILES = "true".equals(System.getProperty("TEST_DATA_FILES"));

	@Test
	public void testFilesUpToDate() throws IOException {
		if (!TEST_DATA_FILES) return;
		testContent("data/UnicodeData.txt", "http://www.unicode.org/Public/UNIDATA/UnicodeData.txt");
		testContent("data/Blocks.txt", "http://www.unicode.org/Public/UNIDATA/Blocks.txt");
		testContent("data/Scripts.txt", "http://www.unicode.org/Public/UNIDATA/Scripts.txt");
		testContent("data/PropList.txt", "http://www.unicode.org/Public/UNIDATA/PropList.txt");
		testContent("data/DerivedCoreProperties.txt", "http://www.unicode.org/Public/UNIDATA/DerivedCoreProperties.txt");
	}

	@Test
	public void testCanonicalRepresentation() throws IOException {
		assertEquals("a", UnicodeData.toCanonicalName("A"));
		assertEquals("a", UnicodeData.toCanonicalName("isA"));
		assertEquals("a", UnicodeData.toCanonicalName(" is A "));
		assertEquals("aaaaaa", UnicodeData.toCanonicalName("aaA-Aaa"));
		assertEquals("a", UnicodeData.toCanonicalName("_a"));
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
		final NamedSetCollection result = new NamedSetCollection();

		addCategoriesList(result);
		addBlocks(result);
		addProperties(result, "DerivedCoreProperties.txt");
		addProperties(result, "PropList.txt");
		addProperties(result, "Scripts.txt");

		final Collection<NamedSet> properties = result.asList();

		// generation test
		new LapgTemplatesTestHelper() {
			@Override
			protected EvaluationContext createEvaluationContext(TypesRegistry types, Map<String, Object> genOptions) {
				HashMap<String, Object> res = new HashMap<String, Object>();
				res.put("version", "6.1.0");
				res.put("properties", properties);
				EvaluationContext evaluationContext = new EvaluationContext(res);
				evaluationContext.setVariable("util", new UnicodeTemplateUtil());
				return evaluationContext;
			}
		}.gentest(
				"unicode.tables", "tests/org/textmapper/lapg/test/unicode/templates",
				"../lapg-core/src/org/textmapper/lapg/unicode",
				new String[]{"UnicodeDataTables.java"});

		// test current data
		for (NamedSet namedSet : properties) {
			CharacterSet actual = UnicodeData.getInstance().getCharacterSet(namedSet.getPropertyName());
			assertNotNull("`" + namedSet.getPropertyName() + "` doesn't exist", actual);
			assertFalse(actual.isInverted());
			assertTrue("persistence for " + namedSet.getPropertyName() + " is broken", namedSet.getSet().equals(actual));
		}
	}

	private void addProperties(final NamedSetCollection result, String filename) throws IOException {
		// add derived core properties
		URL resource = UnicodeTest.class.getResource("data/" + filename);
		assertNotNull("cannot open " + filename, resource);
		new NamedRangesParser(false, false).parseData(resource, new NamedRangesBuilder() {
			Map<String, Builder> psets = new HashMap<String, Builder>();

			@Override
			public void block(int start, int end, String name) {
				Builder s = psets.get(name);
				if (s == null) {
					s = new Builder();
					psets.put(name, s);
				}
				assertTrue("wrong range: " + Integer.toHexString(start) + ".." + Integer.toHexString(end),
						start <= end && start >= 0 && end <= 0x10ffff);
				s.addRange(start, end);
			}

			@Override
			public void done() {
				for (String s : psets.keySet()) {
					result.add(new NamedSet(UnicodeData.toCanonicalName(s), psets.get(s).create()));
				}
			}
		});
	}

	private void addBlocks(final NamedSetCollection result) throws IOException {
		// add Blocks
		URL resource = UnicodeTest.class.getResource("data/Blocks.txt");
		assertNotNull("cannot open Blocks.txt", resource);
		new NamedRangesParser(true, true).parseData(resource, new NamedRangesBuilder() {
			@Override
			public void block(int start, int end, String name) {
				result.add(new NamedSet(UnicodeData.toCanonicalName("block:" + name), new CharacterSetImpl(start, end)));
			}

			@Override
			public void done() {
			}
		});
	}

	private void addCategoriesList(NamedSetCollection result) throws IOException {
		final Map<String, Builder> allCharset = new HashMap<String, Builder>();
		for (String category : UnicodeDataParser.GENERAL_CATEGORIES) {
			allCharset.put(category, new Builder());
		}

		URL resource = UnicodeTest.class.getResource("data/UnicodeData.txt");
		assertNotNull("cannot open UnicodeData.txt", resource);
		new UnicodeDataParser().parseData(resource, new UnicodeDataBuilder() {
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
				prevsym++;
				while (prevsym <= 0x10FFFF) {
					yield(prevsym++, "Cn");
				}
			}
		});

		String[] categories = UnicodeDataParser.GENERAL_CATEGORIES.toArray(new String[UnicodeDataParser.GENERAL_CATEGORIES.size()]);
		for (String category : categories) {
			CharacterSet set = allCharset.get(category).create();
			if (!"Cs".equals(category)) {
				// only Cs category contains surrogates
				assertTrue(new Builder().intersect(set, new CharacterSetImpl(0xd800, 0xdfff)).isEmpty());
			}
			result.add(new NamedSet(UnicodeData.toCanonicalName(category), set));
		}
	}

	private static class NamedSetCollection {
		private final Map<String, NamedSet> collection = new HashMap<String, NamedSet>();

		public void add(NamedSet s) {
			if (collection.containsKey(s.getPropertyName())) {
				throw new IllegalStateException("duplicate NamedSet: " + s.getPropertyName());
			}
			collection.put(s.getPropertyName(), s);
		}

		public Collection<NamedSet> asList() {
			List<NamedSet> list = new ArrayList<NamedSet>(collection.values());
			Collections.sort(list);
			return list;
		}
	}

	public static class NamedSet implements Comparable {

		private final String propertyName;
		private final CharacterSet set;

		public NamedSet(String propertyName, CharacterSet set) {
			this.propertyName = propertyName;
			this.set = set;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public CharacterSet getSet() {
			return set;
		}

		@Override
		public int compareTo(Object o) {
			return propertyName.compareTo(((NamedSet) o).getPropertyName());
		}
	}
}
