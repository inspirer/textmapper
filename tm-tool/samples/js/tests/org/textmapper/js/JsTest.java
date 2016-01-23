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
package org.textmapper.js;

import org.textmapper.js.JsLexer.ErrorReporter;
import org.textmapper.js.JsParser.ParseException;

import java.io.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class JsTest {

	private static final String TESTDATA = "org/textmapper/js/testdata/";
	private static final String ENCODING = "utf-8";

	private String read(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, ENCODING))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

	private void parse(String fileName) {
		System.out.println("parsing " + fileName);

		final int[] problems = new int[]{0};
		ErrorReporter reporter = (message, line, offset, endoffset) -> {
			System.out.println("   " + line + ": " + message);
			problems[0]++;
		};

		try {
			String contents = read(getClass().getClassLoader().getResourceAsStream(
					TESTDATA + fileName));
			JsLexer lexer = new JsLexer(new StringReader(contents), reporter);
			JsParser parser = new JsParser(reporter);
			parser.parse(lexer);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			fail("I/O problem: " + ex.getMessage());
			problems[0]++;
		}
		if (problems[0] > 0) {
			fail("not parsed " + fileName + ": " + problems[0] + " problem(s)");
		}
	}

	@org.junit.Test
	public void test1() throws Exception {
		parse("test1.js");
	}
}
