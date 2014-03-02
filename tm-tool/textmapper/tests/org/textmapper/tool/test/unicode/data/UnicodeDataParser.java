/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
package org.textmapper.tool.test.unicode.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Gryaznov Evgeny, 4/22/12
 */
public class UnicodeDataParser {

	public static final Set<String> GENERAL_CATEGORIES = new HashSet<String>(Arrays.asList(
			"lu", "ll", "lt", "lm", "lo",
			"mn", "mc", "me",
			"nd", "nl", "no",
			"pc", "pd", "ps", "pe", "pi", "pf", "po",
			"sm", "sc", "sk", "so",
			"zs", "zl", "zp",
			"cc", "cf", "cs", "co", "cn"));

	private int parseOptionalChar(int line, String charCode) throws IOException {
		if (charCode.isEmpty()) {
			return -1;
		}
		return parseChar(line, charCode);
	}

	private int parseChar(int line, String charCode) throws IOException {
		if (charCode.length() < 4 || charCode.length() > 6) {
			throw new IOException(line + ": wrong character `" + charCode + "'");
		}
		try {
			return Integer.parseInt(charCode, 16);
		} catch (NumberFormatException ex) {
			throw new IOException(line + ": wrong character `" + charCode + "'", ex);
		}
	}

	private String parseCategory(int line, String categoryName) throws IOException {
		if (!GENERAL_CATEGORIES.contains(categoryName.toLowerCase())) {
			throw new IOException(line + ": wrong category name: " + categoryName);
		}
		return categoryName.toLowerCase();
	}

	private String parseRangeName(int line, String fullName, boolean isFirst) throws IOException {
		String suffix = isFirst ? "First>" : "Last>";
		if (!fullName.startsWith("<") || !fullName.endsWith(suffix)) {
			throw new IOException(line + ": wrong range name `" + fullName + "'");
		}
		String charName = fullName.substring(1, fullName.length() - suffix.length()).trim();
		if (charName.endsWith(",")) {
			charName = charName.substring(0, charName.length() - 1);
		}
		return charName;
	}

	private String[] split(String s) {
		List<String> result = new ArrayList<String>();
		int start = 0;
		int nextSC = s.indexOf(';', start);
		while (nextSC >= 0) {
			result.add(s.substring(start, nextSC));
			start = nextSC + 1;
			nextSC = s.indexOf(';', start);
		}
		result.add(s.substring(start));
		return result.toArray(new String[result.size()]);
	}

	public void parseData(URL unicodeData, UnicodeDataBuilder builder) throws IOException {
		URLConnection yc = unicodeData.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;

		int line = 0;
		int prevsym = -1;
		while ((inputLine = in.readLine()) != null) {
			line++;
			String[] row = split(inputLine);
			int c = parseChar(line, row[0]);
			String charName = row[1];
			String category = parseCategory(line, row[2]);
			if (prevsym >= c) {
				throw new IOException(line + ": not ordered `" + row[0] + "'");
			}
			if (charName.startsWith("<") && !(charName.equals("<control>"))) {
				String rangeName = parseRangeName(line, charName, true);
				inputLine = in.readLine();
				line++;
				if (inputLine == null) {
					throw new IOException(line + ": incomplete range, unexpected end-of-file");
				}
				row = split(inputLine);
				if (!parseRangeName(line, row[1], false).equals(rangeName) || !parseCategory(line, row[2]).equals(category)) {
					throw new IOException(line + ": bad range, different properties for first/last characters");
				}
				builder.range(c, parseChar(line, row[0]), rangeName, category);
			} else {
				try {
					builder.character(c, charName, category, parseOptionalChar(line, row[12]), parseOptionalChar(line, row[13]), parseOptionalChar(line, row[14]));
				} catch (Exception ex) {
					throw new IOException(line + ": " + ex.getMessage(), ex);
				}
			}
			prevsym = c;
		}
		builder.done();
		in.close();
	}

	public interface UnicodeDataBuilder {

		void character(int code, String name, String category, int upper, int lower, int title);

		void range(int start, int end, String rangeName, String category);

		void done();
	}
}
