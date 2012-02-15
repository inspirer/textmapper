/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
import org.textway.lapg.regex.CharacterSetImpl.Builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 11/16/11
 */
public class UnicodeTest {

	private static Map<String, Byte> categories = initCategories();

	private static Map<String, Byte> initCategories() {
		Map<String, Byte> res = new HashMap<String, Byte>();
		res.put("Lu", Character.UPPERCASE_LETTER);
		res.put("Ll", Character.LOWERCASE_LETTER);
		res.put("Lt", Character.TITLECASE_LETTER);
		res.put("Lm", Character.MODIFIER_LETTER);
		res.put("Lo", Character.OTHER_LETTER);
		res.put("Mn", Character.NON_SPACING_MARK);
		res.put("Mc", Character.COMBINING_SPACING_MARK);
		res.put("Me", Character.ENCLOSING_MARK);
		res.put("Nd", Character.DECIMAL_DIGIT_NUMBER);
		res.put("Nl", Character.LETTER_NUMBER);
		res.put("No", Character.OTHER_NUMBER);
		res.put("Pc", Character.CONNECTOR_PUNCTUATION);
		res.put("Pd", Character.DASH_PUNCTUATION);
		res.put("Ps", Character.START_PUNCTUATION);
		res.put("Pe", Character.END_PUNCTUATION);
		res.put("Pi", Character.INITIAL_QUOTE_PUNCTUATION);
		res.put("Pf", Character.FINAL_QUOTE_PUNCTUATION);
		res.put("Po", Character.OTHER_PUNCTUATION);
		res.put("Sm", Character.MATH_SYMBOL);
		res.put("Sc", Character.CURRENCY_SYMBOL);
		res.put("Sk", Character.MODIFIER_SYMBOL);
		res.put("So", Character.OTHER_SYMBOL);
		res.put("Zs", Character.SPACE_SEPARATOR);
		res.put("Zl", Character.LINE_SEPARATOR);
		res.put("Zp", Character.PARAGRAPH_SEPARATOR);
		res.put("Cc", Character.CONTROL);
		res.put("Cf", Character.FORMAT);
		res.put("Cs", Character.SURROGATE);
		res.put("Co", Character.PRIVATE_USE);
		res.put("Cn", Character.UNASSIGNED);
		return res;
	}

	@Test
	public void testUnicodeData() throws IOException {
		URL unicodeData =
				UnicodeTest.class.getResource("data/UnicodeData.txt");
		//new URL("http://www.unicode.org/Public/UNIDATA/UnicodeData.txt");
		URLConnection yc = unicodeData.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;

		Map<String, Builder> allCharset = new HashMap<String, Builder>();
		for (String category : categories.keySet()) {
			allCharset.put(category, new Builder());
		}

		int prevsym = -1;
		while ((inputLine = in.readLine()) != null) {
			String[] row = inputLine.split(";");
			String categoryName = row[2];
			assertTrue("wrong category name: " + inputLine, categoryName != null && categories.containsKey(categoryName));
			String character = row[0];
			assertTrue("wrong character: " + inputLine, character != null && character.length() >= 4 && character.length() <= 6);
			int c;
			try {
				c = Integer.parseInt(character, 16);
			} catch (NumberFormatException ex) {
				fail(ex.getMessage());
				return;
			}
			assertTrue(c > prevsym);
			prevsym++;
			while (prevsym < c) {
				if (prevsym < 0x200) {
					// at least first 0x200 chars should be ok
					assertEquals("Character " + Integer.toHexString(prevsym), Character.getType(prevsym), Character.UNASSIGNED);
				}
				allCharset.get("Cn").addSymbol(prevsym++);
			}
			if (c <= 0x200 && Character.getType(c) != categories.get(categoryName)) {
				fail("Character " + character + " java: " + Character.getType(c) + ", unicode: " + categories.get(categoryName));
			}
			allCharset.get(categoryName).addSymbol(c);
			prevsym = c;
		}
		in.close();

		for (String category : categories.keySet()) {
			CharacterSet set = allCharset.get(category).create();
//			StringBuilder sb = new StringBuilder();
//			System.out.print(category + ": ");
//			boolean first = true;
//			for (int[] v : set) {
//				if (first) {
//					first = false;
//				} else {
//					System.out.print(", ");
//				}
//				System.out.print(v[0] + ", " + v[1]);
//			}
//			System.out.println();
		}
	}

}
