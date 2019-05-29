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
package org.textmapper.tool.bootstrap.b;

import java.io.*;
import org.textmapper.tool.bootstrap.b.SampleBLexer.ErrorReporter;
import org.textmapper.tool.bootstrap.b.SampleBParser.ParseException;

public class SampleBMain {

	private static final String EXTENSION = ".b";
	private static final String ENCODING = "utf-8";

	private int counter = 0;
	private int errors = 0;

	public static String getFileContents(InputStream stream) throws IOException {
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[2048];
		try (Reader in = new InputStreamReader(stream, ENCODING)) {
			int count;
			while ((count = in.read(buffer)) > 0) {
				contents.append(buffer, 0, count);
			}
		}
		return contents.toString();
	}

	private void parse(File file) {
		System.out.println("parsing " + file.getPath());
		counter++;

		final int[] problems = new int[]{0};
		ErrorReporter reporter = (message, offset, endoffset) -> {
			System.out.println("   " + message);
			problems[0]++;
		};

		try {
			String contents = getFileContents(new FileInputStream(file));
			SampleBLexer lexer = new SampleBLexer(contents, reporter);
			SampleBParser parser = new SampleBParser(reporter);
			parser.parse(lexer);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			System.out.println("   I/O problem: " + ex.getMessage());
			problems[0]++;
		}
		if (problems[0] > 0) {
			System.out.println("not parsed " + file.getPath() + ", " + problems[0] + " problem(s)");
			errors++;
		}
	}

	private void walk(File dir) {
		File[] list = dir.listFiles();
		if (list == null) return;

		for (File f : list) {
			if (f.getName().endsWith(EXTENSION) && f.isFile()) {
				parse(f);
			}

			if (f.isDirectory()) {
				walk(f);
			}
		}
	}

	private void printStatistics() {
		System.out.println("Processed " + counter + " files with " + errors + " errors.");
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Provide a list of directories with " + EXTENSION + " files.");
			return;
		}

		SampleBMain instance = new SampleBMain();
		for (String path : args) {
			instance.walk(new File(path));
		}
		instance.printStatistics();
	}
}
