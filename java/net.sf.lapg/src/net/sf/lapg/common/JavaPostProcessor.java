package net.sf.lapg.common;

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

	private String text;
	private Map<String, String> toimport = new HashMap<String, String>();
	private Set<String> massimport = new HashSet<String>();

	private Map<String, Integer> topos = new HashMap<String, Integer>();
	private List<String> existingImports = new ArrayList<String>();
	
	private int lastImportLocation = 0;

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
			String s = current != null ? "import " + i + ";\n" : "\nimport " + i + ";";
			if(toinsert.containsKey(insertloc)) {
				toinsert.put(insertloc, toinsert.get(insertloc) + s);
			} else {
				toinsert.put(insertloc, s);
			}
		}
		
		List<Integer> locations = new ArrayList<Integer>(toinsert.keySet());
		Collections.sort(locations);
		StringBuffer sb = new StringBuffer(text.length());
		int lastStart = 0;
		for(Integer inspos : locations) {
			sb.append(text.substring(lastStart, inspos));
			lastStart = inspos;
			sb.append(toinsert.get(inspos));
		}
		sb.append(text.substring(lastStart));
		text = sb.toString();
	}

	private void collectExistingImports() {
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
		StringBuffer sb = new StringBuffer(text.length());
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
