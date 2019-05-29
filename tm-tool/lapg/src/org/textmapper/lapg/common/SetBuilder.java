/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
package org.textmapper.lapg.common;

import java.util.Arrays;

public class SetBuilder {
	private int[] set;
	private int size;
	private int first;

	public SetBuilder(int elements) {
		set = new int[elements];
		Arrays.fill(set, -1);
		size = 0;
		first = -1;

	}

	public void add(int i) {
		assert i >= 0 && i < set.length;
		if (set[i] != -1) return;

		set[i] = (first == -1) ? -2 : first;
		first = i;
		size++;
	}


	public int[] create() {
		int[] result = new int[size];
		if (size == 0) return result;
		int index = 0;

		if (size * 20 /* should be lg(size) */ < set.length) {
			while (first >= 0) {
				result[index++] = first;
				int next = set[first];
				set[first] = -1;
				first = next;
			}
			Arrays.sort(result);
		} else {
			for (int i = 0; i < set.length; i++) {
				if (set[i] != -1) {
					result[index++] = i;
					set[i] = -1;
				}
			}
		}
		assert index == result.length;
		first = -1;
		size = 0;
		return result;
	}
}
