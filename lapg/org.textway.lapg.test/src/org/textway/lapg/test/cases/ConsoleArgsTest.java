/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg.test.cases;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.textway.lapg.gen.LapgOptions;
import org.junit.Assert;

/**
 * Tests for {@link LapgOptions} command-line arguments parsing.
 */
public class ConsoleArgsTest extends LapgTestCase {

	private static class FailingStream extends OutputStream {

		@Override
		public void write(int b) throws IOException {
			Assert.fail("write-protected stream");
		}
	}

	private static class CheckingErrorStream extends OutputStream {
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
			Assert.assertEquals("error not happened: " + new String(bytes, index, bytes.length - index, "utf8"),
					bytes.length, index);
		}
	}

	private CheckingErrorStream current;
	private final PrintStream originalErr = System.err;
	protected PrintStream failingStream = new PrintStream(new FailingStream());

	protected void expectError(String expect) {
		try {
			String nl = System.getProperty("line.separator");
			if (nl != null && !nl.equals("\n")) {
				expect = expect.replaceAll("\n", nl);
			}
			closeError();
			System.setErr(new PrintStream(current = new CheckingErrorStream(expect), true, "utf8"));
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Exception: " + e.getMessage());
		}
	}

	protected void closeError() {
		if (current != null) {
			try {
				System.setErr(originalErr);
				current.close();
			} catch (IOException e) {
				Assert.fail("Exception: " + e.getMessage());
			}
			current = null;
		}
	}

	public void testCheckNoArgs() {
		LapgOptions lo = LapgOptions.parseArguments(new String[0], failingStream);
		Assert.assertNotNull(lo);
		Assert.assertEquals(null, lo.getInput());
		Assert.assertNull(lo.getOutputFolder());
		Assert.assertEquals(0, lo.getDebug());
		Assert.assertNull(lo.getTemplateName());
		Assert.assertEquals(0, lo.getAdditionalOptions().size());
		Assert.assertEquals(0, lo.getIncludeFolders().size());
		Assert.assertEquals(true, lo.isUseDefaultTemplates());
	}

	public void testCheckDebug() {
		LapgOptions lo = LapgOptions.parseArguments("-e".split(" "), failingStream);
		Assert.assertNotNull(lo);
		Assert.assertEquals(null, lo.getInput());
		Assert.assertEquals(LapgOptions.DEBUG_TABLES, lo.getDebug());
		lo = LapgOptions.parseArguments("-d".split(" "), failingStream);
		Assert.assertEquals(LapgOptions.DEBUG_AMBIG, lo.getDebug());
	}

	public void testInput() {
		LapgOptions lo = LapgOptions.parseArguments("-e synt1".split(" "), failingStream);
		Assert.assertNotNull(lo);
		Assert.assertEquals("synt1", lo.getInput());
		Assert.assertEquals(LapgOptions.DEBUG_TABLES, lo.getDebug());
	}

	public void testInput2() {
		expectError("lapg: should be only one input in arguments\n");
		LapgOptions lo = LapgOptions.parseArguments("synt2 synt1".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();
	}

	public void testTwiceArg() {
		expectError("lapg: option cannot be used twice -e\n");
		LapgOptions lo = LapgOptions.parseArguments("-e -e".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: option cannot be used twice -x\n");
		lo = LapgOptions.parseArguments("--no-default-templates -x".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: option cannot be used twice --no-default-templates\n");
		lo = LapgOptions.parseArguments("-x --no-default-templates".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();
	}

	public void testError() {
		expectError("lapg: no value for option -o\n");
		LapgOptions lo = LapgOptions.parseArguments("-o".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: no value for option --output\n");
		lo = LapgOptions.parseArguments("--output".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: no value for option --output=\n");
		lo = LapgOptions.parseArguments("--output=".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: invalid option --output1=we\n");
		lo = LapgOptions.parseArguments("--output1=we".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: invalid option -q\n");
		lo = LapgOptions.parseArguments("-q".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();

		expectError("lapg: key is used twice: a\n");
		lo = LapgOptions.parseArguments("a=2 a=5".split(" "), System.err);
		Assert.assertNull(lo);
		closeError();
	}

	public void testComplicated() {
		LapgOptions lo = LapgOptions.parseArguments("-e -x -o outputY -i folder1;folder2 -i folder3 -t java2 a=5 b=6 syntax.g".split(" "), failingStream);
		Assert.assertNotNull(lo);
		Assert.assertEquals("syntax.g", lo.getInput());
		Assert.assertEquals("outputY", lo.getOutputFolder());
		Assert.assertEquals(LapgOptions.DEBUG_TABLES, lo.getDebug());
		Assert.assertEquals("java2", lo.getTemplateName());
		Assert.assertEquals(3, lo.getIncludeFolders().size());
		Assert.assertEquals("folder1", lo.getIncludeFolders().get(0));
		Assert.assertEquals("folder2", lo.getIncludeFolders().get(1));
		Assert.assertEquals("folder3", lo.getIncludeFolders().get(2));
		Assert.assertEquals(2, lo.getAdditionalOptions().size());
		Assert.assertEquals("5", lo.getAdditionalOptions().get("a"));
		Assert.assertEquals("6", lo.getAdditionalOptions().get("b"));
		Assert.assertEquals(false, lo.isUseDefaultTemplates());
	}
}
