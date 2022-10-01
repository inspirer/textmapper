/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * evgeny, 7/11/12
 */
public class AliasesParser {

	private static final Pattern NAME_ROW = Pattern.compile("([a-zA-Z0-9_-]+)\\s*;\\s*([a-zA-Z0-9_-]+)((\\s*;\\s*([a-zA-Z0-9_-]+))*)(\\s*#.*)?");
	private static final Pattern VALUE_ROW = Pattern.compile("([a-zA-Z0-9_-]+)\\s*;\\s*([a-zA-Z0-9_\\.-]+)\\s*;\\s*([a-zA-Z0-9_-]+)((\\s*;\\s*([a-zA-Z0-9_-]+))*)(\\s*#.*)?");

	private final boolean valueAliases;

	public AliasesParser(boolean isValueAliases) {
		valueAliases = isValueAliases;
	}

	public void parseData(URL unicodeFile, AliasBuilder builder) throws IOException {
		URLConnection yc = unicodeFile.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;

		int line = 0;
		while ((inputLine = in.readLine()) != null) {
			line++;
			if (inputLine.startsWith("#") || inputLine.trim().isEmpty()) {
				continue;
			}
			Matcher m = (valueAliases ? VALUE_ROW : NAME_ROW).matcher(inputLine);
			if (!m.matches()) {
				throw new IOException(line + ": unknown line: " + inputLine);
			}
			try {
				Collection<String> aliases = new ArrayList<>();
				aliases.add(m.group(valueAliases ? 2 : 1));
				String propertyName = valueAliases ? m.group(1) : null;
				String rest = m.group(valueAliases ? 4 : 3);
				if(rest != null && rest.trim().length() > 0) {
					for(String extraAlias : rest.split(";")) {
						if(extraAlias.trim().length() == 0) {
							continue;
						}
						aliases.add(extraAlias.trim());
					}
				}

				String longName = m.group(valueAliases ? 3 : 2);
				builder.alias(aliases, longName, propertyName);
			} catch (Exception ex) {
				throw new IOException(line + ": " + ex.getMessage(), ex);
			}
		}
		builder.done();
		in.close();
	}

	public interface AliasBuilder {
		void alias(Collection<String> aliases, String longName, String propertyName);

		void done();
	}
}
