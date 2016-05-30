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
package org.textmapper.lapg.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class IntArrayBuilder {
	private Set<Integer> seen;
	private int[] value = new int[16];
	private int size;

	IntArrayBuilder(boolean dedupe) {
		seen = dedupe ? new HashSet<>() : null;
		size = 0;
	}

	void add(int i) {
		if (seen != null && !seen.add(i)) return;

		if (size == value.length) {
			int[] newarr = new int[value.length * 2];
			System.arraycopy(value, 0, newarr, 0, size);
			value = newarr;
		}
		value[size++] = i;
	}

	int[] create(boolean sorted) {
		int[] result = Arrays.copyOf(value, size);
		size = 0;
		if (seen != null) seen.clear();
		if (sorted) Arrays.sort(result);
		return result;
	}
}
