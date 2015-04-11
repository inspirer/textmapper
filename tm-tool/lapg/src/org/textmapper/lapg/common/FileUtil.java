/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileUtil {

	public static final String DEFAULT_ENCODING = "utf8";
	public static final String SPACES = "                            ";

	public static String getFileContents(InputStream stream, String encoding) {
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(stream, encoding);
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

	public static String fixWhitespaces(String contents, String lineSeparator, int expandTabs) {
		int size = contents.length();
		int outsize = size;
		if (expandTabs > 0) {
			for (int i = 0; i < size; i++) {
				if (contents.charAt(i) == '\t') {
					outsize += expandTabs - 1;
				}
			}
		}
		StringBuilder sb = new StringBuilder(outsize);
		for (int i = 0; i < size; i++) {
			char c = contents.charAt(i);
			if (c == '\n') {
				sb.append(lineSeparator);
			} else if (c == '\r') {
				sb.append(lineSeparator);
				if (i + 1 < size && contents.charAt(i + 1) == '\n') {
					i++;
				}
			} else if (c == '\t' && expandTabs > 0) {
				for (int sp = expandTabs; sp > 0; ) {
					int ins = Math.min(SPACES.length(), sp);
					sp -= ins;
					sb.append(SPACES, 0, ins);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
