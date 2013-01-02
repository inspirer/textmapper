/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.tool.test.unicode;

import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.common.CharacterSetImpl;
import org.textmapper.lapg.common.CharacterSetImpl.Builder;
import org.textmapper.tool.common.JavaArrayEncoder;

import java.util.List;

/**
 * Gryaznov Evgeny, 7/6/12
 */
public class UnicodeTemplateUtil {

	@SuppressWarnings("UnusedDeclaration")
	public static List<String> packCharacterSet(CharacterSet set) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		boolean containsSurrogate = !new Builder().intersect(set, new CharacterSetImpl(0xd800, 0xdfff)).isEmpty();
		int[] arr = set.toArray();
		if (arr.length > 0xffff) {
			throw new IllegalArgumentException("array is too big");
		}
		if (containsSurrogate) {
			enc.appendChar(0);
		}
		enc.appendChar(arr.length);
		for (int i : arr) {
			if (containsSurrogate) {
				enc.appendInt(i);
			} else if (i >= 0 && i <= 0xffff) {
				enc.appendChar(i);
			} else {
				for (char c : Character.toChars(i)) {
					enc.appendChar(c);
				}
			}
		}
		List<List<String>> result = enc.getResult();
		if (result.size() != 1) {
			throw new IllegalArgumentException("array is too big");
		}
		return result.get(0);
	}
}
