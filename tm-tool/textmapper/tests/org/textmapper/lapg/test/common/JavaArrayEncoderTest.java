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
package org.textmapper.lapg.test.common;

import org.junit.Test;
import org.textmapper.lapg.common.JavaArrayEncoder;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Gryaznov Evgeny, 2/23/12
 */
public class JavaArrayEncoderTest {

	@Test
	public void testSimple() {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		int[] test = new int[]{5, 4, 3, 2, 1};
		for (int i : test) {
			enc.appendChar(i);
		}
		List<List<String>> res = enc.getResult();
		assertEquals(1, res.size());
		assertEquals(1, res.get(0).size());
		String val = res.get(0).get(0);
		assertEquals("\"\\5\\4\\3\\2\\1\"", val);
	}

	@Test
	public void testEmpty() {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		List<List<String>> res = enc.getResult();
		assertEquals(1, res.size());
		assertEquals(1, res.get(0).size());
		String val = res.get(0).get(0);
		assertEquals("\"\"", val);
	}

	@Test
	public void testNewLine() {
		JavaArrayEncoder enc = new JavaArrayEncoder(40);
		int[] test = new int[]{0xaaaa, 0xbbbb, 0xcccc, 0xdddd, 0xeeee, 0xffff, 0xaaaa, 0xbbbb, 0xcccc,
				0xdddd, 0xeeee, 0xffff, 0xaaaa, 0xbbbb, 0xcccc, 0xdddd, 0xeeee, 0xffff};
		for (int i : test) {
			enc.appendChar(i);
		}
		List<List<String>> res = enc.getResult();
		assertEquals(1, res.size());
		assertEquals(3, res.get(0).size());
		assertEquals("\"\\uaaaa\\ubbbb\\ucccc\\udddd\\ueeee\\uffff\\uaaaa\"", res.get(0).get(0));
		assertEquals("\"\\ubbbb\\ucccc\\udddd\\ueeee\\uffff\\uaaaa\\ubbbb\"", res.get(0).get(1));
		assertEquals("\"\\ucccc\\udddd\\ueeee\\uffff\"", res.get(0).get(2));
	}

	@Test
	public void testIntEncoding() {
		JavaArrayEncoder enc = new JavaArrayEncoder(40);
		enc.appendInt(0x12abcdef);
		List<List<String>> res = enc.getResult();
		assertEquals(1, res.size());
		assertEquals(1, res.get(0).size());
		assertEquals("\"\\ucdef\\u12ab\"", res.get(0).get(0));
		
		char[] s = "\ucdef\u12ab".toCharArray();
		assertEquals(0x12abcdef, s[0]+ (s[1] << 16));
	}

	@Test
	public void testVeryLong1() {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		for(int i = 0; i < 0xfff0 + 1; i++) {
			enc.appendChar(1);
		}
		List<List<String>> res = enc.getResult();
		assertEquals(2, res.size());
		// first chunk
		assertEquals(1638, res.get(0).size());
		assertEquals("\"\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\\1\"", res.get(0).get(0));
		// second chunk
		assertEquals(1, res.get(1).size());
		assertEquals("\"\\1\"", res.get(1).get(0));

		// one char less
		enc = new JavaArrayEncoder(40);
		for(int i = 0; i < 0xfff0; i++) {
			enc.appendChar(1);
		}
		res = enc.getResult();
		assertEquals(1, res.size());
	}

	@Test
	public void testVeryLong2() {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		for(int i = 0; i < 0xfff0/2 + 1; i++) {
			enc.appendChar(0);
		}
		List<List<String>> res = enc.getResult();
		assertEquals(2, res.size());
		// first chunk
		assertEquals(819, res.get(0).size());
		assertEquals("\"\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\"", res.get(0).get(0));
		// second chunk
		assertEquals(1, res.get(1).size());
		assertEquals("\"\\0\"", res.get(1).get(0));

		// one char less
		enc = new JavaArrayEncoder(40);
		for(int i = 0; i < 0xfff0/2; i++) {
			enc.appendChar(0);
		}
		res = enc.getResult();
		assertEquals(1, res.size());
	}

	@Test
	public void testVeryLong3() {
		JavaArrayEncoder enc = new JavaArrayEncoder(40);
		for(int i = 0; i < 0xfff0/3 + 1; i++) {
			enc.appendChar(0x800);
		}
		List<List<String>> res = enc.getResult();
		assertEquals(2, res.size());
		// first chunk
		assertEquals(3120, res.get(0).size());
		assertEquals("\"\\u0800\\u0800\\u0800\\u0800\\u0800\\u0800\\u0800\"", res.get(0).get(0));
		// second chunk
		assertEquals(1, res.get(1).size());
		assertEquals("\"\\u0800\"", res.get(1).get(0));

		// one char less
		enc = new JavaArrayEncoder(40);
		for(int i = 0; i < 0xfff0/3; i++) {
			enc.appendChar(0x800);
		}
		res = enc.getResult();
		assertEquals(1, res.size());
	}
}
