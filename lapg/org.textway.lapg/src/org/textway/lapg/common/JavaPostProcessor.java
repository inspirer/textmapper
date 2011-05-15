/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaPostProcessor {

	Pattern QUALIFIED_REFERENCE = Pattern.compile("((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+)@([a-zA-Z_][a-zA-Z_0-9]*)");
	Pattern IMPORT = Pattern.compile("import\\s*((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+)([a-zA-Z_][a-zA-Z_0-9]*|\\*)\\s*;");
	Pattern PACKAGE = Pattern.compile("package\\s*((?:[a-zA-Z_][a-zA-Z_0-9]*\\s*\\.\\s*)*[a-zA-Z_][a-zA-Z_0-9]*)\\s*;[ \\t]*[\\n\\r]{1,2}");

	private String text;
	private String currentPackage;
	private final Map<String, String> toimport = new HashMap<String, String>();
	private final Set<String> massimport = new HashSet<String>();

	private final Map<String, Integer> topos = new HashMap<String, Integer>();
	private final List<String> existingImports = new ArrayList<String>();

	private int lastImportLocation = 0;
	private int nextAfterPackage = 0;

	public JavaPostProcessor(String text) {
		this.text = text;
	}

	public String process() {
		collectExistingImports();
		collectQualifiedNames();
		addImports();
		return text;
	}

	private void addImports() {
		ArrayList<String> imports = new ArrayList<String>();
		for(Entry<String, String> entry : toimport.entrySet()) {
			String imp = entry.getValue() + "." + entry.getKey();
			if(!topos.containsKey(imp)) {
				imports.add(imp);
			}
		}
		Collections.sort(imports);

		Map<Integer, String> toinsert = new HashMap<Integer, String>();
		Iterator<String> it = existingImports.iterator();
		String current = it.hasNext() ? it.next() : null;

		for(String i : imports) {
			while(current != null && current.compareTo(i) < 0) {
				current = it.hasNext() ? it.next() : null;
			}
			int insertloc = current != null ? topos.get(current) : lastImportLocation;
			if(insertloc == 0) {
				insertloc = nextAfterPackage;
			}
			String s = current != null || lastImportLocation == 0 ? "import " + i + ";\n" : "\nimport " + i + ";";
			if(toinsert.containsKey(insertloc)) {
				toinsert.put(insertloc, toinsert.get(insertloc) + s);
			} else {
				toinsert.put(insertloc, s);
			}
		}

		List<Integer> locations = new ArrayList<Integer>(toinsert.keySet());
		Collections.sort(locations);
		StringBuilder sb = new StringBuilder(text.length());
		int lastStart = 0;
		for(Integer inspos : locations) {
			sb.append(text.substring(lastStart, inspos));
			lastStart = inspos;
			sb.append(toinsert.get(inspos));
		}
		String next = text.substring(lastStart);
		if(lastImportLocation == 0 && locations.size() > 0 && !next.startsWith("\r") && !next.startsWith("\n")) {
			sb.append('\n');
		}
		sb.append(next);
		text = sb.toString();
	}

	private void collectExistingImports() {
		Matcher p = PACKAGE.matcher(text);
		if(p.find()) {
			nextAfterPackage = p.end();
			currentPackage = p.group(1);
			massimport.add(currentPackage);
		}

		massimport.add("java.lang");
		Matcher m = IMPORT.matcher(text);
		while (m.find()) {
			String name = m.group(2);
			String qualifier = trimLastDot(m.group(1));
			existingImports.add(qualifier+ "." +name);
			topos.put(qualifier+ "." +name, m.start());
			lastImportLocation = Math.max(lastImportLocation, m.end());
			if (name.equals("*")) {
				massimport.add(qualifier);
			} else {
				toimport.put(name, qualifier);
			}
		}
	}

	private void collectQualifiedNames() {
		StringBuilder sb = new StringBuilder(text.length());
		int lastStart = 0;
		Matcher m = QUALIFIED_REFERENCE.matcher(text);
		m.region(lastImportLocation, text.length());
		while (m.find()) {
			sb.append(text.substring(lastStart, m.start(1)));
			lastStart = m.start(2);
			String name = m.group(2);
			String qualifier = trimLastDot(m.group(1));
			if (massimport.contains(qualifier)) {
				continue;
			}
			String oldqualifier = toimport.get(name);
			if (oldqualifier == null) {
				toimport.put(name, qualifier);
			} else if (!oldqualifier.equals(qualifier)) {
				sb.append(qualifier+".");
			}
		}
		sb.append(text.substring(lastStart));
		text = sb.toString();
	}

	private static String trimLastDot(String s) {
		if (s.endsWith(".")) {
			return s.substring(0, s.length() - 1);
		}
		return s;
	}
}
