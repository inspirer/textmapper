/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.lapg.unicode.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * evgeny, 7/9/12
 */
public class NamedRangesParser {

	private static final Pattern ROW = Pattern.compile("^([0-9a-fA-F]{4,6})(..([0-9a-fA-F]{4,6}))?\\s*;\\s*([\\w -]+)(\\s*#.*)?$");

	private final boolean isOrdered;
	private final boolean rangeOnly;

	public NamedRangesParser(boolean ordered, boolean rangeOnly) {
		this.isOrdered = ordered;
		this.rangeOnly = rangeOnly;
	}

	public void parseData(URL unicodeFile, NamedRangesBuilder builder) throws IOException {
		URLConnection yc = unicodeFile.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;

		int line = 0;
		int prev = -1;
		while ((inputLine = in.readLine()) != null) {
			line++;
			if (inputLine.startsWith("#") || inputLine.trim().isEmpty()) {
				continue;
			}
			Matcher m = ROW.matcher(inputLine);
			if (!m.matches()) {
				throw new IOException(line + ": unknown line: " + inputLine);
			}
			try {
				int start = Integer.parseInt(m.group(1), 16);
				if (prev >= start && isOrdered) {
					throw new IOException(line + ": not ordered `" + m.group(1) + "'");
				}
				String to = m.group(3);
				if (rangeOnly && to == null) {
					throw new IOException(line + ": range is expected");
				}
				int end = to != null ? Integer.parseInt(to, 16) : start;
				if (end < start) {
					throw new IOException(line + ": end < start");
				}
				prev = end;
				builder.block(start, end, m.group(4));
			} catch (Exception ex) {
				throw ex instanceof IOException
						? (IOException) ex
						: new IOException(line + ": " + ex.getMessage(), ex);
			}
		}
		builder.done();
		in.close();
	}


	public interface NamedRangesBuilder {

		void block(int start, int end, String name);

		void done();
	}
}
