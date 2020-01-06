/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

import org.junit.Test;

import static org.junit.Assert.*;

public class IntArrayBuilderTest {

	@Test
	public void testEmpty() throws Exception {
		IntArrayBuilder b = new IntArrayBuilder(false);
		assertArrayEquals(new int[]{}, b.create(false));
		assertArrayEquals(new int[]{}, b.create(true));
	}

	@Test
	public void testDedupe() throws Exception {
		IntArrayBuilder b = new IntArrayBuilder(true);
		b.add(3);
		b.add(2);
		b.add(1);
		b.add(2);

		assertArrayEquals(new int[]{3,2,1}, b.create(false));
		assertArrayEquals(new int[]{}, b.create(false));
		assertArrayEquals(new int[]{}, b.create(true));

		b.add(3);
		b.add(2);
		b.add(1);
		b.add(2);

		assertArrayEquals(new int[]{1,2,3}, b.create(true));
	}

	@Test
	public void testWithoutDedupe() throws Exception {
		IntArrayBuilder b = new IntArrayBuilder(false);
		b.add(3);
		b.add(2);
		b.add(1);
		b.add(2);

		assertArrayEquals(new int[]{3,2,1,2}, b.create(false));
		assertArrayEquals(new int[]{}, b.create(false));
		assertArrayEquals(new int[]{}, b.create(true));

		b.add(3);
		b.add(2);
		b.add(1);
		b.add(2);

		assertArrayEquals(new int[]{1,2,2,3}, b.create(true));
	}

	@Test
	public void test32k() throws Exception {
		IntArrayBuilder b = new IntArrayBuilder(true);
		int[] expected = new int[32768];

		for (int i = 0; i < expected.length; i++) {
			expected[i] = expected.length - i;
			b.add(expected.length - i);
		}

		assertArrayEquals(expected, b.create(false));
	}
}
