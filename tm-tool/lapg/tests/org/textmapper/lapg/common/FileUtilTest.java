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
package org.textmapper.lapg.common;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FileUtilTest {

	@Test
	public void testGetFileContent() {
		byte[] arr = new byte[17000];
		Arrays.fill(arr, (byte) 'c');
		String content = FileUtil.getFileContents(new ByteArrayInputStream(arr), "utf-8");
		assertEquals(arr.length, content.length());
		for (int i = 0; i < arr.length; i++) {
			assertEquals('c', content.charAt(i));
		}
	}

	private static String string(char c, int count) {
		char[] arr = new char[count];
		Arrays.fill(arr, c);
		return new String(arr);
	}

	@Test
	public void testFixWhitespaces() {
		assertEquals("\t\n\n\n", FileUtil.fixWhitespaces("\t\n\r\n\r", "\n", 0));
		assertEquals(" \n", FileUtil.fixWhitespaces("\t\n", "\n", 1));
		assertEquals("    \n", FileUtil.fixWhitespaces("\t\t\n", "\n", 2));
		assertEquals("    \n", FileUtil.fixWhitespaces("\t\n", "\n", 4));
		assertEquals(string(' ', 100), FileUtil.fixWhitespaces("\t", "\n", 100));
	}
}
