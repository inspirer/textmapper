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
package org.textmapper.tool.common;

import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.tool.gen.ProcessingStrategy;
import org.textmapper.templates.storage.FileBasedResourceLoader;
import org.textmapper.templates.storage.IResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileBasedStrategy implements ProcessingStrategy {

	private final File rootFolder;

	public FileBasedStrategy(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	@Override
	public void createFile(String name, String contents, Map<String, Object> options, ProcessingStatus status) {
		try {
			new GeneratedFile(rootFolder, name, contents, options).create();
		} catch (IOException e) {
			status.report(ProcessingStatus.KIND_ERROR, "cannot create file `" + name + "': " + e.getMessage());
		}
	}

	@Override
	public IResourceLoader createResourceLoader(String qualifiedName) {
		File folder = new File(qualifiedName);
		if (folder.isDirectory()) {
			// FIXME charset
			return new FileBasedResourceLoader(new File[]{folder}, "utf8");
		}
		return null;
	}
}
