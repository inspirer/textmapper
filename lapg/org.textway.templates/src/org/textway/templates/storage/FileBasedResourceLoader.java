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
package org.textway.templates.storage;

import java.io.*;

public class FileBasedResourceLoader implements IResourceLoader {

	private final File[] myFolders;
	private final String charsetName;

	public FileBasedResourceLoader(File[] folders, String charsetName) {
		this.myFolders = folders;
		this.charsetName = charsetName;
	}

	private static String getFileContents(File file, String charsetName) {
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(new FileInputStream(file), charsetName);
			try {
				while ((count = in.read(buffer)) > 0) {
					contents.append(buffer, 0, count);
				}
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			return null;
		}
		return contents.toString();
	}

	public Resource loadResource(String resourceName, String kind) {
		String fileName = resourceName.replace('.', '/') + "." + kind;
		for (File f : myFolders) {
			File file = new File(f, fileName);
			if (file.exists()) {
				String contents = getFileContents(file, charsetName);
				if (contents != null) {
					return new Resource(file.toURI(), contents);
				}
			}
		}
		return null;
	}
}
