/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.test;

import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.common.GeneratedFile;
import org.textway.lapg.gen.ProcessingStrategy;
import org.textway.templates.storage.FileBasedResourceLoader;
import org.textway.templates.storage.IResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
* Gryaznov Evgeny, 2/24/12
*/
public class CheckingFileBasedStrategy implements ProcessingStrategy {

	private final Set<String> created = new HashSet<String>();
	private final File root;

	public CheckingFileBasedStrategy(File root) {
		this.root = root;
	}

	@Override
	public void createFile(String name, String contents, ProcessingStatus status) {
		try {
			// FIXME encoding, newline
			new GeneratedFile(name, contents, "utf8", false) {
				public void check() throws IOException {
					String name = getName();
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
