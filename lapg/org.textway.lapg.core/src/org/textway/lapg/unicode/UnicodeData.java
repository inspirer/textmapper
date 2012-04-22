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
package org.textway.lapg.unicode;

import org.textway.lapg.api.regex.CharacterSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Gryaznov Evgeny, 4/22/12
 */
public class UnicodeData {

	public static final String VERSION = "6.1.0";

	public static final Map<String, Byte> categories;

	static {
		categories = new HashMap<String, Byte>();
		categories.put("Lu", Character.UPPERCASE_LETTER);
		categories.put("Ll", Character.LOWERCASE_LETTER);
		categories.put("Lt", Character.TITLECASE_LETTER);
		categories.put("Lm", Character.MODIFIER_LETTER);
		categories.put("Lo", Character.OTHER_LETTER);
		categories.put("Mn", Character.NON_SPACING_MARK);
		categories.put("Mc", Character.COMBINING_SPACING_MARK);
		categories.put("Me", Character.ENCLOSING_MARK);
		categories.put("Nd", Character.DECIMAL_DIGIT_NUMBER);
		categories.put("Nl", Character.LETTER_NUMBER);
		categories.put("No", Character.OTHER_NUMBER);
		categories.put("Pc", Character.CONNECTOR_PUNCTUATION);
		categories.put("Pd", Character.DASH_PUNCTUATION);
		categories.put("Ps", Character.START_PUNCTUATION);
		categories.put("Pe", Character.END_PUNCTUATION);
		categories.put("Pi", Character.INITIAL_QUOTE_PUNCTUATION);
		categories.put("Pf", Character.FINAL_QUOTE_PUNCTUATION);
		categories.put("Po", Character.OTHER_PUNCTUATION);
		categories.put("Sm", Character.MATH_SYMBOL);
		categories.put("Sc", Character.CURRENCY_SYMBOL);
		categories.put("Sk", Character.MODIFIER_SYMBOL);
		categories.put("So", Character.OTHER_SYMBOL);
		categories.put("Zs", Character.SPACE_SEPARATOR);
		categories.put("Zl", Character.LINE_SEPARATOR);
		categories.put("Zp", Character.PARAGRAPH_SEPARATOR);
		categories.put("Cc", Character.CONTROL);
		categories.put("Cf", Character.FORMAT);
		categories.put("Cs", Character.SURROGATE);
		categories.put("Co", Character.PRIVATE_USE);
		categories.put("Cn", Character.UNASSIGNED);
	}

	public static CharacterSet getCategory(byte categoryId) {
		return UnicodeDataTables.getCategory(categoryId);
	}
}
