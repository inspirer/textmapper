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

import org.textmapper.lapg.common.JavaArrayEncoder;

import java.util.List;

/**
 * Gryaznov Evgeny, 7/6/12
 */
public class UnicodeTemplateUtil {

	public static List<String> packCodePoint(int[] arr) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		if (arr.length > 0xffff) {
			throw new IllegalArgumentException("array is too big");
		}
		enc.appendChar(arr.length);
		for (int i : arr) {
			if (i >= 0 && i <= 0xffff) {
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
