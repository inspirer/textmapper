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

import org.junit.Test;
import org.textmapper.lapg.common.SetBuilder;

import static org.junit.Assert.*;

public class SetBuilderTest {
	@Test
	public void testSimple() throws Exception {
		SetBuilder b = new SetBuilder(7);
		b.add(1);
		b.add(4);
		b.add(3);

		assertArrayEquals(new int[]{1,3,4}, b.create());
		assertArrayEquals(new int[]{}, b.create());

		b.add(6);

		assertArrayEquals(new int[]{6}, b.create());
	}

	@Test
	public void testFull() throws Exception {
		SetBuilder b = new SetBuilder(100);
		for (int i = 99; i >= 0; i--) {
			b.add(i);
		}

		int[] expected = new int[100];
		for (int i = 0; i < 100; i++) expected[i] = i;
		assertArrayEquals(expected, b.create());
	}
}
