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
package net.sf.lapg.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileCreator {

	private static final Pattern FILENAME = Pattern.compile("([\\w-]+/)*[\\w-]+(\\.\\w+)?");

	private final String name;
	private final String contents;
	private final String charset;
	private final boolean forceLF;

	public static String NL = System.getProperty("line.separator");

	public FileCreator(String name, String contents, String charset, boolean forceLF) {
		this.name = name;
		this.contents = contents;
		this.charset = charset;
		this.forceLF = forceLF;
	}

	protected String getData() {
		String data = contents;
		if (name.endsWith(".java")) {
			data = new JavaPostProcessor(data).process();
		}
		return fixLineSeparators(data);
	}

	protected String getName() {
		return name;
	}

	public void create() throws IOException {
		String name = getName();
		checkName(name);
		OutputStream os = new FileOutputStream(new File(name));
		String data = getData();
		os.write(data.getBytes(charset));
		os.close();
	}

	private void checkName(String name) throws IOException {
		Matcher m = FILENAME.matcher(name);
		if (!m.matches()) {
			throw new IOException("bad file name");
		}
		int lastSlash = name.lastIndexOf('/');
		if (lastSlash != -1) {
			File pf = new File(name.substring(0, lastSlash));
			if (!pf.exists() && !pf.mkdirs()) {
				throw new IOException("cannot create folders for `" + name.substring(0, lastSlash) + "'");
			}
		}
	}

	private String fixLineSeparators(String contents) {
		StringBuilder sb = new StringBuilder(contents.length());
		int size = contents.length();
		for (int i = 0; i < size; i++) {
			char c = contents.charAt(i);
			if (c == '\n') {
				sb.append(forceLF ? "\n" : NL);
			} else if (c == '\r') {
				sb.append(forceLF ? "\n" : NL);
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
