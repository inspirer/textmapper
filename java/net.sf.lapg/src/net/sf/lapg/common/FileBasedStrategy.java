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
import java.io.IOException;

import net.sf.lapg.api.ProcessingStatus;
import net.sf.lapg.api.ProcessingStrategy;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.impl.FolderTemplateLoader;

public class FileBasedStrategy implements ProcessingStrategy {

	public void createFile(String name, String contents, ProcessingStatus status) {
		try {
			// FIXME encoding, newline
			new GeneratedFile(name, contents, "utf8", true).create();
		} catch (IOException e) {
			status.report(ProcessingStatus.KIND_ERROR, "cannot create file `" + name + "': " + e.getMessage());
		}
	}

	public IBundleLoader createTemplateLoader(String path) {
		File folder = new File(path);
		if (folder.isDirectory()) {
			// FIXME charset
			return new FolderTemplateLoader(new File[] { folder }, "utf8");
		}
		return null;
	}
}