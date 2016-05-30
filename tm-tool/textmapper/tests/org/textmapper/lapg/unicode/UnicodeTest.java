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
package org.textmapper.lapg.unicode;

import org.junit.Test;
import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.common.CharacterSetImpl;
import org.textmapper.lapg.common.CharacterSetImpl.Builder;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.gen.LapgTemplatesTestHelper;
import org.textmapper.lapg.unicode.data.AliasesParser;
import org.textmapper.lapg.unicode.data.AliasesParser.AliasBuilder;
import org.textmapper.lapg.unicode.data.NamedRangesParser;
import org.textmapper.lapg.unicode.data.NamedRangesParser.NamedRangesBuilder;
import org.textmapper.lapg.unicode.data.UnicodeDataParser;
import org.textmapper.lapg.unicode.data.UnicodeDataParser.UnicodeDataBuilder;
import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.types.TypesRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
		testContent("data/PropertyAliases.txt", "http://www.unicode.org/Public/UNIDATA/PropertyAliases.txt");
		testContent("data/PropertyValueAliases.txt", "http://www.unicode.org/Public/UNIDATA/PropertyValueAliases.txt");
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
		try (InputStream inputStream = location.openStream()) {
			return FileUtil.getFileContents(inputStream, "utf-8");
		}
	}

	@Test
	public void testUnicodeData() throws IOException {
		// set properties
		final NamedSetCollection propertiesCollection = new NamedSetCollection();
		addCategoriesList(propertiesCollection);
		addBlocks(propertiesCollection);
		addProperties(propertiesCollection, "DerivedCoreProperties.txt", null);
		addProperties(propertiesCollection, "PropList.txt", null);
		addProperties(propertiesCollection, "Scripts.txt", "unknown");
		addCustomProperties(propertiesCollection);

		Set<String> availableProperties = new HashSet<>(propertiesCollection.collection.keySet());

		// composite properties
		final List<CompositeProperty> compositeProperties = new ArrayList<>();
		addCompositeProperties(compositeProperties);

		// test composite properties
		for (CompositeProperty composite : compositeProperties) {
			for (String content : composite.getContent()) {
				assertTrue(availableProperties.contains(content));
			}
		}
		availableProperties.addAll(compositeProperties.stream()
				.map(CompositeProperty::getPropertyName).collect(Collectors.toList()));

		// aliases
		final AliasCollection aliasesCollection = new AliasCollection();
		readAliases(aliasesCollection, "PropertyAliases.txt", false, availableProperties);
		readAliases(aliasesCollection, "PropertyValueAliases.txt", true, availableProperties);

		final Collection<NamedSet> properties = propertiesCollection.asList();
		final Collection<Alias> aliases = aliasesCollection.asList();
		Collections.sort(compositeProperties);

		// generation test
		new LapgTemplatesTestHelper() {
			@Override
			protected EvaluationContext createEvaluationContext(TypesRegistry types, Map<String, Object> genOptions) {
				HashMap<String, Object> res = new HashMap<>();
				res.put("version", "7.0.0");
				res.put("properties", properties);
				res.put("aliases", aliases);
				res.put("composites", compositeProperties);
				EvaluationContext evaluationContext = new EvaluationContext(res);
				evaluationContext.setVariable("util", new UnicodeTemplateUtil());
				return evaluationContext;
			}
		}.gentest(
				"unicode.tables", "tests/org/textmapper/lapg/unicode/templates",
				"../lapg/src/org/textmapper/lapg/unicode",
				new String[]{"UnicodeDataTables.java"});

		// test current data
		for (NamedSet namedSet : properties) {
			CharacterSet actual = UnicodeData.getInstance().getCharacterSet(namedSet.getPropertyName());
			assertNotNull("`" + namedSet.getPropertyName() + "` doesn't exist", actual);
			assertFalse(actual.isInverted());
			assertTrue("persistence for " + namedSet.getPropertyName() + " is broken", namedSet.getSet().equals(actual));
		}
	}

	private void addCustomProperties(NamedSetCollection propertiesCollection) {
		// http://www.unicode.org/reports/tr18/,  annex C
		Builder b = new Builder();

		// blank ::= \p{Whitespace} -- [\N{LF} \N{VT} \N{FF} \N{CR} \N{NEL} \p{gc=Line_Separator} \p{gc=Paragraph_Separator}]
		CharacterSet blank = propertiesCollection.get("whitespace").getSet();
		blank = b.subtract(blank, new CharacterSetImpl(0xa, 0xf, 0x85, 0x85)); // lf, vt, ff, cr, nel
		blank = b.subtract(blank, propertiesCollection.get("zl").getSet()); // Line_Separator
		blank = b.subtract(blank, propertiesCollection.get("zp").getSet()); // Paragraph_Separator
		propertiesCollection.add(new NamedSet("blank", blank));

		// graph ::= [^ \p{space} \p{gc=Control} \p{gc= Surrogate} \p{gc=Unassigned}]
		CharacterSet graph = new CharacterSetImpl(0, 0x10ffff);
		graph = b.subtract(graph, propertiesCollection.get("whitespace").getSet());
		graph = b.subtract(graph, propertiesCollection.get("cc").getSet()); // Control
		graph = b.subtract(graph, propertiesCollection.get("cs").getSet()); // Surrogate
		graph = b.subtract(graph, propertiesCollection.get("cn").getSet()); // Unassigned
		propertiesCollection.add(new NamedSet("graph", graph));

		// print ::= \p{graph} \p{blank} -- \p{cntrl}
		b.clear();
		b.addSet(propertiesCollection.get("graph").getSet());
		b.addSet(propertiesCollection.get("blank").getSet());
		CharacterSet print = b.create();
		print = b.subtract(print, propertiesCollection.get("cc").getSet());
		propertiesCollection.add(new NamedSet("print", print));

		// Any matches all code points
		propertiesCollection.add(new NamedSet("any", new CharacterSetImpl(0, 0x10ffff)));

		// ASCII is equivalent to [\u0000-\u007F]
		propertiesCollection.add(new NamedSet("ascii", new CharacterSetImpl(0, 0x7f)));

		// Assigned ::= not Cn
		CharacterSet assigned = propertiesCollection.get("any").getSet();
		assigned = b.subtract(assigned, propertiesCollection.get("cn").getSet());
		propertiesCollection.add(new NamedSet("assigned", assigned));
	}

	private void addCompositeProperties(Collection<CompositeProperty> result) {
		// hardcoded
		result.add(new CompositeProperty("c", Arrays.asList("cc", "cf", "cn", "co", "cs")));
		result.add(new CompositeProperty("l", Arrays.asList("ll", "lm", "lo", "lt", "lu")));
		result.add(new CompositeProperty("lc", Arrays.asList("ll", "lt", "lu")));
		result.add(new CompositeProperty("m", Arrays.asList("mc", "me", "mn")));
		result.add(new CompositeProperty("n", Arrays.asList("nd", "nl", "no")));
		result.add(new CompositeProperty("p", Arrays.asList("pc", "pd", "pe", "pf", "pi", "po", "ps")));
		result.add(new CompositeProperty("s", Arrays.asList("sc", "sk", "sm", "so")));
		result.add(new CompositeProperty("z", Arrays.asList("zl", "zp", "zs")));

		// http://www.unicode.org/reports/tr18/,  annex C
		result.add(new CompositeProperty("xdigit", Arrays.asList("nd", "hexdigit")));
		result.add(new CompositeProperty("alnum", Arrays.asList("alphabetic", "nd")));
	}

	private void addProperties(final NamedSetCollection result, String filename, final String defaultName) throws IOException {
		// add derived core properties
		URL resource = UnicodeTest.class.getResource("data/" + filename);
		assertNotNull("cannot open " + filename, resource);
		new NamedRangesParser(false, false).parseData(resource, new NamedRangesBuilder() {
			Map<String, Builder> psets = new HashMap<>();

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
				if (defaultName != null) {
					CharacterSet rest = new CharacterSetImpl(0, 0x10ffff);
					Builder b = new Builder();
					for (Builder v : psets.values()) {
						rest = b.subtract(rest, v.create());
					}
					if (!rest.isEmpty()) {
						result.add(new NamedSet(UnicodeData.toCanonicalName(defaultName), rest));
					}
				}
			}
		});
	}

	private void readAliases(final AliasCollection result, String filename, final boolean isValueAliases, final Set<String> availableProperties) throws IOException {
		URL resource = UnicodeTest.class.getResource("data/" + filename);
		assertNotNull("cannot open " + filename, resource);
		new AliasesParser(isValueAliases).parseData(resource, new AliasBuilder() {
			@Override
			public void alias(Collection<String> aliases, String longName, String propertyName) {
				assertTrue(isValueAliases ? propertyName != null : propertyName == null);
				String canonicalName = UnicodeData.toCanonicalName(longName);
				if (!isValueAliases) {
					if (!availableProperties.contains(canonicalName)) return;
					for (String alias : aliases) {
						result.add(new Alias(UnicodeData.toCanonicalName(alias), canonicalName));
					}
				} else if (propertyName.equals("gc")) {
					String gcName = aliases.iterator().next();
					aliases.remove(gcName);
					canonicalName = UnicodeData.toCanonicalName(gcName);
					assertTrue(canonicalName, canonicalName.length() == 1 && availableProperties.contains(canonicalName) || UnicodeDataParser.GENERAL_CATEGORIES.contains(canonicalName) || canonicalName.equals("lc"));
					aliases.add(longName);
					if (!availableProperties.contains(canonicalName)) return;
					for (String alias : aliases) {
						result.add(new Alias(UnicodeData.toCanonicalName(alias), canonicalName));
					}
				} else if (propertyName.equals("sc")) {
					if (!availableProperties.contains(canonicalName)) return;
					for (String alias : aliases) {
						result.add(new Alias(UnicodeData.toCanonicalName(alias), canonicalName));
					}
				}
			}

			@Override
			public void done() {
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
		final Map<String, Builder> allCharset = new HashMap<>();
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
					yield(prevsym++, "cn");
				}
				yield(c, category);

			}

			@Override
			public void range(int start, int end, String rangeName, String category) {
				assertTrue(start > prevsym);
				prevsym++;
				while (prevsym < start) {
					yield(prevsym++, "cn");
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
					yield(prevsym++, "cn");
				}
			}
		});

		String[] categories = UnicodeDataParser.GENERAL_CATEGORIES.toArray(new String[UnicodeDataParser.GENERAL_CATEGORIES.size()]);
		for (String category : categories) {
			CharacterSet set = allCharset.get(category).create();
			if (!"cs".equals(category)) {
				// only Cs category contains surrogates
				assertTrue(new Builder().intersect(set, new CharacterSetImpl(0xd800, 0xdfff)).isEmpty());
			}
			result.add(new NamedSet(UnicodeData.toCanonicalName(category), set));
		}
	}

	private static class NamedSetCollection {
		private final Map<String, NamedSet> collection = new HashMap<>();

		public void add(NamedSet s) {
			if (collection.containsKey(s.getPropertyName())) {
				throw new IllegalStateException("duplicate NamedSet: " + s.getPropertyName());
			}
			collection.put(s.getPropertyName(), s);
		}

		public NamedSet get(String name) {
			return collection.get(name);
		}

		public Collection<NamedSet> asList() {
			List<NamedSet> list = new ArrayList<>(collection.values());
			Collections.sort(list);
			return list;
		}
	}

	public static class AliasCollection {
		private final Map<String, Alias> collection = new HashMap<>();

		public void add(Alias s) {
			if (collection.containsKey(s.getAlias())) {
				throw new IllegalStateException("duplicate Alias: " + s.getAlias());
			}
			collection.put(s.getAlias(), s);
		}

		public Collection<Alias> asList() {
			List<Alias> list = new ArrayList<>(collection.values());
			Collections.sort(list);
			return list;
		}
	}

	public static class Alias implements Comparable {
		private String alias;
		private String target;

		public Alias(String alias, String target) {
			this.alias = alias;
			this.target = target;
		}

		public String getAlias() {
			return alias;
		}

		public String getTarget() {
			return target;
		}

		@Override
		public int compareTo(Object o) {
			return alias.compareTo(((Alias) o).getAlias());
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

	public static class CompositeProperty implements Comparable {
		private final String propertyName;
		private final List<String> content;

		public CompositeProperty(String propertyName, Collection<String> content) {
			this.propertyName = propertyName;
			this.content = new ArrayList<>(content);
			Collections.sort(this.content);
		}

		public String getPropertyName() {
			return propertyName;
		}

		public Collection<String> getContent() {
			return content;
		}

		@Override
		public int compareTo(Object o) {
			return propertyName.compareTo(((CompositeProperty) o).getPropertyName());
		}
	}
}
