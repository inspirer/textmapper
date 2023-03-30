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

import org.textmapper.lapg.common.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratedFile {

	private static final Pattern FILENAME = Pattern.compile("([\\w-]+/)*[\\w-]+(\\.\\w+)?");

	private static final String OPT_ET = "expandTabs";
	private static final String OPT_FORCE_LF = "forceLF";
	private static final String OPT_ENCODING = "outputEncoding";

	protected final File baseFolder;
	protected final String name;
	protected final String contents;
	protected final String charset;
	protected final boolean forceLF;
	protected final int expandTabs;

	public static String NL = System.getProperty("line.separator");

	public GeneratedFile(File baseFolder, String name, String contents, String charset, boolean forceLF,
						 int expandTabs) {
		this.baseFolder = baseFolder;
		this.name = name;
		this.contents = contents;
		this.charset = charset;
		this.forceLF = forceLF;
		this.expandTabs = expandTabs;
	}

	public GeneratedFile(File baseFolder, String name, String contents, Map<String, Object> options) {
		this(baseFolder, name, contents, getOutputEncoding(options), hasForceLF(options), getExpandTabs(options));
	}

	protected String getData() {
		String data = contents;
		if (name.endsWith(".java")) {
			data = new JavaPostProcessor(data).process();
		} else if (name.endsWith(".go")) {
			data = new GoPostProcessor(data).process();
		}
		return FileUtil.fixWhitespaces(data, forceLF ? "\n" : NL, expandTabs);
	}

	public void create() throws IOException {
		checkName(name);
		OutputStream os = new FileOutputStream(new File(baseFolder, name));
		String data = getData();
		os.write(data.getBytes(charset));
		os.close();
	}

	protected void checkName(String name) throws IOException {
		Matcher m = FILENAME.matcher(name);
		if (!m.matches()) {
			throw new IOException("bad file name: " + name);
		}
		int lastSlash = name.lastIndexOf('/');
		if (lastSlash != -1) {
			File pf = new File(baseFolder, name.substring(0, lastSlash));
			if (!pf.exists() && !pf.mkdirs()) {
				throw new IOException("cannot create folders for `" + name.substring(0, lastSlash) + "'");
			}
		}
	}

	static String getOutputEncoding(Map<String, Object> options) {
		Object val = options.get(OPT_ENCODING);
		return val instanceof String ? (String) val : "utf-8";
	}

	static boolean hasForceLF(Map<String, Object> options) {
		return "true".equals(options.get(OPT_FORCE_LF));
	}

	static int getExpandTabs(Map<String, Object> options) {
		Object val = options.get(OPT_ET);
		int result = 0;
		if (val instanceof Integer) {
			result = (Integer) val;
		} else if (val instanceof String) {
			try {
				result = Integer.parseInt((String) val);
			} catch (NumberFormatException ex) {
			}
		}
		return result;
	}
}
