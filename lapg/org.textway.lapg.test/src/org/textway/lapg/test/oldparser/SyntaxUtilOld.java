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
package org.textway.lapg.test.oldparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.test.TestStatus;

public class SyntaxUtilOld {

	public static Grammar parseSyntax(String sourceName, InputStream stream, TestStatus err, Map<String, String> options) {
		String contents = getFileContents(stream);
		contents = contents.replaceAll("(\\w+)\\s*=\\s*(.*)", ".$1 $2");
		CSyntax cs = LapgParser.process(sourceName, contents, options);
		if (cs.hasErrors()) {
			for (String s : cs.getErrors()) {
				err.error(s + "\n");
			}
		}
		return cs;
	}

	public static String getFileContents(InputStream stream) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(stream, "utf8");
			try {
				while ((count = in.read(buffer)) > 0) {
					contents.append(buffer, 0, count);
				}
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			return null;
		}
		return contents.toString();
	}
}
