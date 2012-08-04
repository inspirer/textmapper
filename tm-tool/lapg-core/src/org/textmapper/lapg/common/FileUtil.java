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
package org.textmapper.lapg.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileUtil {

	public static final String DEFAULT_ENCODING = "utf8";

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

	public static String fixLineSeparators(String contents, String separator) {
		StringBuilder sb = new StringBuilder(contents.length());
		int size = contents.length();
		for (int i = 0; i < size; i++) {
			char c = contents.charAt(i);
			if (c == '\n') {
				sb.append(separator);
			} else if (c == '\r') {
				sb.append(separator);
				if (i + 1 < size && contents.charAt(i + 1) == '\n') {
					i++;
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
