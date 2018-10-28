/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.lapg.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArrayIterableTest {

	@Test
	public void testForward() throws Exception {
		Integer[] array = new Integer[]{1, 5, 3, 8};
		int index = 0;
		for (int i : new ArrayIterable<>(array, false)) {
			assertEquals((int) array[index], i);
			index++;
		}
		assertEquals(array.length, index);
	}

	@Test
	public void testBackward() throws Exception {
		Integer[] array = new Integer[]{1, 5, 3, 8};
		int index = array.length - 1;
		for (int i : new ArrayIterable<>(array, true)) {
			assertEquals((int) array[index], i);
			index--;
		}
		assertEquals(-1, index);
	}
}
