/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.templates.storage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

public class ClassResourceLoader implements IResourceLoader {

	private final ClassLoader loader;
	private final String rootPackage;
	private final String charsetName;

	public ClassResourceLoader(ClassLoader loader, String rootPackage, String charsetName) {
		this.loader = loader;
		this.rootPackage = rootPackage;
		this.charsetName = charsetName;
	}

	private static String getStreamContents(URL url, String charsetName) {
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[2048];
		int count;
		try {
			try (Reader in = new InputStreamReader(url.openStream(), charsetName)) {
				while ((count = in.read(buffer)) > 0) {
					contents.append(buffer, 0, count);
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return contents.toString();
	}

	@Override
	public Resource loadResource(String qualifiedName, String kind) {
		String name = rootPackage + "/" + qualifiedName.replace('.', '/') + "." + kind;
		URL url = loader.getResource(name);
		if (url == null) {
			return null;
		}
		String contents = getStreamContents(url, charsetName);
		try {
			return contents != null ? new Resource(url.toURI(), contents) : null;
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
