/**
 * Copyright (c) 2010-2012 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.compiler;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.tool.gen.ProcessingStrategy;
import org.textmapper.tool.common.GeneratedFile;
import org.textmapper.tool.gen.LapgGenerator;
import org.textmapper.tool.gen.LapgOptions;
import org.textmapper.tool.parser.LapgTree.TextSource;
import org.textmapper.templates.storage.FileBasedResourceLoader;
import org.textmapper.templates.storage.IResourceLoader;

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
			new GeneratedFile(null, name, contents, "utf8", false) {
				@Override
				public void create() throws IOException {
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
