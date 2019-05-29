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
package org.textmapper.tool.test;

import org.junit.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class CheckingErrorStream extends OutputStream {
	byte[] bytes;
	int index;

	public CheckingErrorStream(String expect) throws UnsupportedEncodingException {
		bytes = expect.getBytes("utf8");
		index = 0;
	}

	@Override
	public void write(int b) throws IOException {
		if (index >= bytes.length) {
			Assert.fail("unknown System.err message: starts with " + Character.toString((char) b));
		}
		if (b != bytes[index++]) {
			Assert.fail("wrong message, expected: " + new String(bytes, "utf8"));
		}
	}

	@Override
	public void close() throws IOException {
		Assert.assertEquals("error was not happened: " + new String(bytes, index, bytes.length - index, "utf8"),
				bytes.length, index);
	}
}
