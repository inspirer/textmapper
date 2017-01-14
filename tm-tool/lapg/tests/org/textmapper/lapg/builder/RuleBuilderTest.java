/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * evgeny, 8/9/12
 */
public class RuleBuilderTest {

	@Test
	public void testPermute() throws Exception {
		int[] numbers = new int[]{0, 1, 2, 3, 4};
		StringBuilder sb = new StringBuilder();
		do {
			sb.append(Arrays.toString(numbers));
			sb.append("\n");
		} while (LiRhsUnordered.permute(numbers));

		assertEquals("[0, 1, 2, 3, 4]\n" +
				"[0, 1, 2, 4, 3]\n" +
				"[0, 1, 3, 2, 4]\n" +
				"[0, 1, 3, 4, 2]\n" +
				"[0, 1, 4, 2, 3]\n" +
				"[0, 1, 4, 3, 2]\n" +
				"[0, 2, 1, 3, 4]\n" +
				"[0, 2, 1, 4, 3]\n" +
				"[0, 2, 3, 1, 4]\n" +
				"[0, 2, 3, 4, 1]\n" +
				"[0, 2, 4, 1, 3]\n" +
				"[0, 2, 4, 3, 1]\n" +
				"[0, 3, 1, 2, 4]\n" +
				"[0, 3, 1, 4, 2]\n" +
				"[0, 3, 2, 1, 4]\n" +
				"[0, 3, 2, 4, 1]\n" +
				"[0, 3, 4, 1, 2]\n" +
				"[0, 3, 4, 2, 1]\n" +
				"[0, 4, 1, 2, 3]\n" +
				"[0, 4, 1, 3, 2]\n" +
				"[0, 4, 2, 1, 3]\n" +
				"[0, 4, 2, 3, 1]\n" +
				"[0, 4, 3, 1, 2]\n" +
				"[0, 4, 3, 2, 1]\n" +
				"[1, 0, 2, 3, 4]\n" +
				"[1, 0, 2, 4, 3]\n" +
				"[1, 0, 3, 2, 4]\n" +
				"[1, 0, 3, 4, 2]\n" +
				"[1, 0, 4, 2, 3]\n" +
				"[1, 0, 4, 3, 2]\n" +
				"[1, 2, 0, 3, 4]\n" +
				"[1, 2, 0, 4, 3]\n" +
				"[1, 2, 3, 0, 4]\n" +
				"[1, 2, 3, 4, 0]\n" +
				"[1, 2, 4, 0, 3]\n" +
				"[1, 2, 4, 3, 0]\n" +
				"[1, 3, 0, 2, 4]\n" +
				"[1, 3, 0, 4, 2]\n" +
				"[1, 3, 2, 0, 4]\n" +
				"[1, 3, 2, 4, 0]\n" +
				"[1, 3, 4, 0, 2]\n" +
				"[1, 3, 4, 2, 0]\n" +
				"[1, 4, 0, 2, 3]\n" +
				"[1, 4, 0, 3, 2]\n" +
				"[1, 4, 2, 0, 3]\n" +
				"[1, 4, 2, 3, 0]\n" +
				"[1, 4, 3, 0, 2]\n" +
				"[1, 4, 3, 2, 0]\n" +
				"[2, 0, 1, 3, 4]\n" +
				"[2, 0, 1, 4, 3]\n" +
				"[2, 0, 3, 1, 4]\n" +
				"[2, 0, 3, 4, 1]\n" +
				"[2, 0, 4, 1, 3]\n" +
				"[2, 0, 4, 3, 1]\n" +
				"[2, 1, 0, 3, 4]\n" +
				"[2, 1, 0, 4, 3]\n" +
				"[2, 1, 3, 0, 4]\n" +
				"[2, 1, 3, 4, 0]\n" +
				"[2, 1, 4, 0, 3]\n" +
				"[2, 1, 4, 3, 0]\n" +
				"[2, 3, 0, 1, 4]\n" +
				"[2, 3, 0, 4, 1]\n" +
				"[2, 3, 1, 0, 4]\n" +
				"[2, 3, 1, 4, 0]\n" +
				"[2, 3, 4, 0, 1]\n" +
				"[2, 3, 4, 1, 0]\n" +
				"[2, 4, 0, 1, 3]\n" +
				"[2, 4, 0, 3, 1]\n" +
				"[2, 4, 1, 0, 3]\n" +
				"[2, 4, 1, 3, 0]\n" +
				"[2, 4, 3, 0, 1]\n" +
				"[2, 4, 3, 1, 0]\n" +
				"[3, 0, 1, 2, 4]\n" +
				"[3, 0, 1, 4, 2]\n" +
				"[3, 0, 2, 1, 4]\n" +
				"[3, 0, 2, 4, 1]\n" +
				"[3, 0, 4, 1, 2]\n" +
				"[3, 0, 4, 2, 1]\n" +
				"[3, 1, 0, 2, 4]\n" +
				"[3, 1, 0, 4, 2]\n" +
				"[3, 1, 2, 0, 4]\n" +
				"[3, 1, 2, 4, 0]\n" +
				"[3, 1, 4, 0, 2]\n" +
				"[3, 1, 4, 2, 0]\n" +
				"[3, 2, 0, 1, 4]\n" +
				"[3, 2, 0, 4, 1]\n" +
				"[3, 2, 1, 0, 4]\n" +
				"[3, 2, 1, 4, 0]\n" +
				"[3, 2, 4, 0, 1]\n" +
				"[3, 2, 4, 1, 0]\n" +
				"[3, 4, 0, 1, 2]\n" +
				"[3, 4, 0, 2, 1]\n" +
				"[3, 4, 1, 0, 2]\n" +
				"[3, 4, 1, 2, 0]\n" +
				"[3, 4, 2, 0, 1]\n" +
				"[3, 4, 2, 1, 0]\n" +
				"[4, 0, 1, 2, 3]\n" +
				"[4, 0, 1, 3, 2]\n" +
				"[4, 0, 2, 1, 3]\n" +
				"[4, 0, 2, 3, 1]\n" +
				"[4, 0, 3, 1, 2]\n" +
				"[4, 0, 3, 2, 1]\n" +
				"[4, 1, 0, 2, 3]\n" +
				"[4, 1, 0, 3, 2]\n" +
				"[4, 1, 2, 0, 3]\n" +
				"[4, 1, 2, 3, 0]\n" +
				"[4, 1, 3, 0, 2]\n" +
				"[4, 1, 3, 2, 0]\n" +
				"[4, 2, 0, 1, 3]\n" +
				"[4, 2, 0, 3, 1]\n" +
				"[4, 2, 1, 0, 3]\n" +
				"[4, 2, 1, 3, 0]\n" +
				"[4, 2, 3, 0, 1]\n" +
				"[4, 2, 3, 1, 0]\n" +
				"[4, 3, 0, 1, 2]\n" +
				"[4, 3, 0, 2, 1]\n" +
				"[4, 3, 1, 0, 2]\n" +
				"[4, 3, 1, 2, 0]\n" +
				"[4, 3, 2, 0, 1]\n" +
				"[4, 3, 2, 1, 0]\n", sb.toString());
	}
}
