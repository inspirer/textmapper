/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.test;

import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.common.GeneratedFile;
import org.textmapper.tool.gen.ProcessingStrategy;
import org.textmapper.templates.storage.FileBasedResourceLoader;
import org.textmapper.templates.storage.IResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
* Gryaznov Evgeny, 2/24/12
*/
public class CheckingFileBasedStrategy implements ProcessingStrategy {

	private final Set<String> created = new HashSet<>();
	private final File root;

	public CheckingFileBasedStrategy(File root) {
		this.root = root;
	}

	@Override
	public void createFile(String name, String contents, Map<String, Object> options, ProcessingStatus status) {
		try {
			new GeneratedFile(root, name, contents, options) {
				public void check() throws IOException {
					checkName(name);
					String expected;
					try {
						InputStream is = new FileInputStream(new File(root, name));
						expected = FileUtil.getFileContents(is, charset);
					} catch (IOException ex) {
						expected = "# Original data is not available (new file is created):\n# " + ex.getMessage();
					}
					String data = getData();
					assertEquals("diff for " + name, expected, data);
					created.add(name);
				}
			}.check();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Override
	public IResourceLoader createResourceLoader(String path) {
		File folder = new File(path);
		if (folder.isDirectory()) {
			return new FileBasedResourceLoader(new File[]{folder}, "utf8");
		}
		return null;
	}

	public Set<String> getCreated() {
		return created;
	}
}
