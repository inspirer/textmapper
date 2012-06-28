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
package org.textway.lapg.test.cases;

import org.junit.Test;
import org.textway.lapg.api.regex.CharacterSet;
import org.textway.lapg.common.CharacterSetImpl.Builder;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.test.cases.data.UnicodeParser;
import org.textway.lapg.test.cases.data.UnicodeParser.UnicodeBuilder;
import org.textway.lapg.unicode.UnicodeData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 11/16/11
 */
public class UnicodeTest {

	private static final boolean TEST_DATA_FILES = false;

	private static final Map<Byte, String> javaRep = initRepresentation();

	private static Map<Byte, String> initRepresentation() {
		HashMap<Byte, String> res = new HashMap<Byte, String>();
		res.put(Character.UPPERCASE_LETTER, "UPPERCASE_LETTER");
		res.put(Character.LOWERCASE_LETTER, "LOWERCASE_LETTER");
		res.put(Character.TITLECASE_LETTER, "TITLECASE_LETTER");
		res.put(Character.MODIFIER_LETTER, "MODIFIER_LETTER");
		res.put(Character.OTHER_LETTER, "OTHER_LETTER");
		res.put(Character.NON_SPACING_MARK, "NON_SPACING_MARK");
		res.put(Character.COMBINING_SPACING_MARK, "COMBINING_SPACING_MARK");
		res.put(Character.ENCLOSING_MARK, "ENCLOSING_MARK");
		res.put(Character.DECIMAL_DIGIT_NUMBER, "DECIMAL_DIGIT_NUMBER");
		res.put(Character.LETTER_NUMBER, "LETTER_NUMBER");
		res.put(Character.OTHER_NUMBER, "OTHER_NUMBER");
		res.put(Character.CONNECTOR_PUNCTUATION, "CONNECTOR_PUNCTUATION");
		res.put(Character.DASH_PUNCTUATION, "DASH_PUNCTUATION");
		res.put(Character.START_PUNCTUATION, "START_PUNCTUATION");
		res.put(Character.END_PUNCTUATION, "END_PUNCTUATION");
		res.put(Character.INITIAL_QUOTE_PUNCTUATION, "INITIAL_QUOTE_PUNCTUATION");
		res.put(Character.FINAL_QUOTE_PUNCTUATION, "FINAL_QUOTE_PUNCTUATION");
		res.put(Character.OTHER_PUNCTUATION, "OTHER_PUNCTUATION");
		res.put(Character.MATH_SYMBOL, "MATH_SYMBOL");
		res.put(Character.CURRENCY_SYMBOL, "CURRENCY_SYMBOL");
		res.put(Character.MODIFIER_SYMBOL, "MODIFIER_SYMBOL");
		res.put(Character.OTHER_SYMBOL, "OTHER_SYMBOL");
		res.put(Character.SPACE_SEPARATOR, "SPACE_SEPARATOR");
		res.put(Character.LINE_SEPARATOR, "LINE_SEPARATOR");
		res.put(Character.PARAGRAPH_SEPARATOR, "PARAGRAPH_SEPARATOR");
		res.put(Character.CONTROL, "CONTROL");
		res.put(Character.FORMAT, "FORMAT");
		res.put(Character.SURROGATE, "SURROGATE");
		res.put(Character.PRIVATE_USE, "PRIVATE_USE");
		res.put(Character.UNASSIGNED, "UNASSIGNED");
		return res;
	}

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

		final Map<Byte, Builder> allCharset = new HashMap<Byte, Builder>();
		for (byte category : UnicodeData.categories.values()) {
			allCharset.put(category, new Builder());
		}

		new UnicodeParser().parseData(UnicodeTest.class.getResource("data/UnicodeData.txt"), new UnicodeBuilder() {
			private int prevsym = -1;

			private void yield(int c, int category) {
				if (c <= 0x200 && Character.getType(c) != category &&
						/* exceptions: */ c != 0xa7 && c != 0xaa && c != 0xb6 && c != 0xba) {
					// at least first 0x200 chars should be ok
					fail("Character " + Integer.toHexString(c) + " java: " + Character.getType(c) + ", unicode: " + category);
				}
				allCharset.get((byte) category).addSymbol(c);
			}

			@Override
			public void character(int c, String name, int category, int upper, int lower, int title) {
				assertTrue(c > prevsym);
				prevsym++;
				while (prevsym < c) {
					yield(prevsym++, Character.UNASSIGNED);
				}
				yield(c, category);

			}

			@Override
			public void range(int start, int end, String rangeName, int category) {
				assertTrue(start > prevsym);
				prevsym++;
				while (prevsym < start) {
					yield(prevsym++, Character.UNASSIGNED);
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

		String[] categoryName = new String[32];
		for (Entry<String, Byte> entry : UnicodeData.categories.entrySet()) {
			categoryName[entry.getValue()] = entry.getKey();
		}

		for (byte category : UnicodeData.categories.values()) {
			CharacterSet set = allCharset.get(category).create();
			CharacterSet actual = UnicodeData.getCategory(category);
			assertFalse(actual.isInverted());
			if (set.equals(actual)) continue;

			StringBuilder sb = new StringBuilder();
			sb.append("case Character.").append(javaRep.get(category)).append(": // ");
			sb.append(categoryName[category]);
			sb.append("\n\treturn new CharacterSetImpl(");
			int size = 0;
			for (int[] v : set) {
				size += 2;
			}
			if (size > 10) sb.append("\n\t\t");
			boolean first = true;
			int nextWrap = sb.length() + 90;
			for (int[] v : set) {
				if (first) {
					first = false;
				} else {
					if (sb.length() >= nextWrap) {
						sb.append(",\n\t\t");
						nextWrap = sb.length() + 90;
					} else {
						sb.append(", ");
					}
				}
				sb.append("0x" + Long.toHexString(v[0]));
				if (sb.length() >= nextWrap) {
					sb.append(",\n\t\t");
					nextWrap = sb.length() + 90;
				} else {
					sb.append(", ");
				}
				sb.append("0x" + Long.toHexString(v[1]));
			}
			sb.append(");");
			System.out.println(sb.toString());
			//fail();
		}
	}

}
