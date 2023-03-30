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
package org.textmapper.tool.common;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoPostProcessor {

	private static Pattern QUALIFIED_REFERENCE = Pattern.compile("\"((?:[^\"]+/)?(\\w+))(?: as " +
			"(\\w+))?\"\\.([a-zA-Z_][a-zA-Z_0-9]*)");
	private static Pattern PACKAGE = Pattern.compile(
			"^((?:(?://[^\\r\\n]*)?\\s*\\r?\\n)*)package\\s*((?:[.\\w-]+/)*" +
			"([a-zA-Z_][a-zA-Z_0-9]*))\\s*(\\r?\\n){1,2}");

	private String text;
	private final Set<String> imports = new HashSet<>();
	private final Set<String> namedImports = new HashSet<>();
	private String newPackageStmt;
	private String currentPackage;

	public GoPostProcessor(String text) {
		this.text = text;
	}

	public String process() {
		Matcher p = PACKAGE.matcher(text);
		if (p.find()) {
			int offset = p.end();
			newPackageStmt = p.group(1) + "package " + p.group(3) + "\n\n";
			text = text.substring(offset);
			currentPackage = p.group(2);
		} else {
			newPackageStmt = "";
		}
		collectQualifiedNames();
		addImports();
		return text;
	}

	private void addImports() {
		StringBuilder sb = new StringBuilder(
				text.length() + imports.size() * 80 + namedImports.size() * 80);
		sb.append(newPackageStmt);

		if (!this.imports.isEmpty() || !this.namedImports.isEmpty()) {
			sb.append("import (\n");
			ArrayList<String> imports = new ArrayList<>(this.imports);
			Collections.sort(imports);
			imports.forEach(s -> sb.append("\t").append(s).append("\n"));

			if (!this.imports.isEmpty() && !this.namedImports.isEmpty()) {
				sb.append("\n");
			}

			imports = new ArrayList<>(this.namedImports);
			Collections.sort(imports);
			imports.forEach(s -> sb.append("\t").append(s).append("\n"));
			sb.append(")\n\n");
		}
		sb.append(text);
		text = sb.toString();
	}

	private void collectQualifiedNames() {
		StringBuilder sb = new StringBuilder(text.length());
		int lastStart = 0;
		Matcher m = QUALIFIED_REFERENCE.matcher(text);
		while (m.find()) {
			sb.append(text.substring(lastStart, m.start()));
			lastStart = m.start(4);
			String import_ = m.group(1);
			if (currentPackage != null && currentPackage.equals(import_)) {
				continue;
			}

			String shortPackage = m.group(2);
			String importAs = m.group(3);
			if (importAs != null) {
				namedImports.add(importAs + " \"" + import_ + "\"");
				sb.append(importAs);
			} else {
				imports.add("\"" + import_ + "\"");
				sb.append(shortPackage);
			}
			sb.append(".");
		}
		sb.append(text.substring(lastStart));
		text = sb.toString();
	}
}
