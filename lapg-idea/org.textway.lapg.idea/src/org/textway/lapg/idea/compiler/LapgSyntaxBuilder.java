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
package org.textway.lapg.idea.compiler;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.ProcessingStrategy;
import org.textway.lapg.common.GeneratedFile;
import org.textway.lapg.gen.LapgGenerator;
import org.textway.lapg.gen.LapgOptions;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.templates.storage.FileBasedResourceLoader;
import org.textway.templates.storage.IResourceLoader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class LapgSyntaxBuilder implements ProcessingStrategy {

	private static final Pattern FILENAME = Pattern.compile("([\\w-]+/)*[\\w-]+(\\.\\w+)?");
	private final VirtualFile file;
	private final ProcessingStatus status;
	private Map<String, String> myGeneratedContent;

	public LapgSyntaxBuilder(VirtualFile file, ProcessingStatus status) {
		this.file = file;
		this.status = status;
	}

	public Map<String, String> getGeneratedContent() {
		return myGeneratedContent;
	}

	public boolean generate() {
		String contents;
		try {
			contents = VfsUtil.loadText(file);
		} catch (IOException ex) {
			status.report(ProcessingStatus.KIND_ERROR, "cannot read " + file.getName());
			return false;
		}

		myGeneratedContent = new HashMap<String, String>();
		TextSource input = new TextSource(file.getUrl(), contents.toCharArray(), 1);
		return new LapgGenerator(new LapgOptions(), status, this).compileGrammar(input);
	}

	public void createFile(String name, String contents, ProcessingStatus status) {
		try {
			// FIXME encoding, newline
			new GeneratedFile(name, contents, "utf8", false) {
				@Override
				public void create() throws IOException {
					String name = getName();
					checkName(name);
					String data = getData();
					myGeneratedContent.put(name, data);
				}

				@Override
				protected void checkName(String name) throws IOException {
					// FIXME
					Matcher m = FILENAME.matcher(name);
					if (!m.matches()) {
						throw new IOException("bad file name: " + name);
					}
				}
			}.create();
		} catch (IOException e) {
			/* cannot happen */
		}
	}

	public IResourceLoader createResourceLoader(String qualifiedName) {
		// TODO use Idea filesystem?
		File folder = new File(qualifiedName);
		if (folder.isDirectory()) {
			// FIXME charset
			return new FileBasedResourceLoader(new File[]{folder}, "utf-8");
		}
		return null;
	}

	static void writeFile(File file, String content) throws IOException {
		File pf = file.getParentFile();
		if (file.exists()) {
			try {
				String diskContent = readFile(file);
				if (diskContent != null && diskContent.equals(content)) {
					return;
				}
			} catch (IOException e) {
				/* ignore */
			}

		} else if (!pf.exists() && !pf.mkdirs()) {
			throw new IOException("cannot create folders for `" + pf.getPath() + "'");
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.print(content);
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	static String readFile(File file) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
			return result.toString();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				/* ignore */
			}
		}
	}
}
