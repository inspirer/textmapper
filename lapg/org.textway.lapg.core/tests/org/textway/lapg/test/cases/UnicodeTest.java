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
import org.textway.lapg.common.CharacterSetImpl.Builder;
import org.textway.lapg.common.UnicodeData;

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
	public void testUnicodeData() throws IOException {
		URL unicodeData =
				UnicodeTest.class.getResource("data/UnicodeData.txt");
//		new URL("http://www.unicode.org/Public/UNIDATA/UnicodeData.txt");
		URLConnection yc = unicodeData.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;

		Map<String, Builder> allCharset = new HashMap<String, Builder>();
		for (String category : UnicodeData.categories.keySet()) {
			allCharset.put(category, new Builder());
		}

		int prevsym = -1;
		while ((inputLine = in.readLine()) != null) {
			String[] row = inputLine.split(";");
			String categoryName = row[2];
			assertTrue("wrong category name: " + inputLine, categoryName != null && UnicodeData.categories.containsKey(categoryName));
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
			if (c <= 0x200 && Character.getType(c) != UnicodeData.categories.get(categoryName)) {
				fail("Character " + character + " java: " + Character.getType(c) + ", unicode: " + UnicodeData.categories.get(categoryName));
			}
			allCharset.get(categoryName).addSymbol(c);
			prevsym = c;
		}
		in.close();

		for (String category : UnicodeData.categories.keySet()) {
			CharacterSet set = allCharset.get(category).create();
			CharacterSet actual = UnicodeData.getCategory(UnicodeData.categories.get(category));
			assertFalse(actual.isInverted());
			if(set.equals(actual)) continue;
			
			System.out.print("case Character." + javaRep.get(UnicodeData.categories.get(category)) + ": // " + category + "\n\treturn new CharacterSetImpl(");
			boolean first = true;
			int len = 0;
			for (int[] v : set) {
				if (first) {
					first = false;
				} else {
					System.out.print(", ");
				}
				System.out.print("0x"+Long.toHexString(v[0]) + ", " + "0x"+Long.toHexString(v[1]));
				len += 2;
			}
			System.out.println(");");
			fail();
		}
	}

}
