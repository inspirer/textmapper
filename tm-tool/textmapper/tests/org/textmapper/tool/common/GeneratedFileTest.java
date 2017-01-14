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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.textmapper.lapg.common.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GeneratedFileTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testCreate() throws Exception {
		File baseFolder = tempFolder.newFolder("testCreate" + System.nanoTime());

		GeneratedFile file = new GeneratedFile(baseFolder, "A.java",
				"package p;\r\n" +
						"\r\n" +
						"import xxx.A;\r\n" +
						"import xxx.C;\r\n" +
						"\r\n" +
						"class X extends xxx.@B {\r\n" +
						"\tpublic final xxx.@D A;\r\n" +
						"}\r\n", "utf-8", true, 0);
		file.create();

		String written = FileUtil.getFileContents(new FileInputStream(new File(baseFolder, "A.java")), "utf-8");
		assertEquals("package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"import xxx.B;\n" +
				"import xxx.C;\n" +
				"import xxx.D;\n" +
				"\n" +
				"class X extends B {\n" +
				"\tpublic final D A;\n" +
				"}\n", written);
	}

	@Test
	public void testFolderFileConflict() throws Exception {
		File baseFolder = tempFolder.newFolder("testFolderFileConflict" + System.nanoTime());
		assertTrue(new File(baseFolder, "A.txt").mkdir());

		GeneratedFile file = new GeneratedFile(baseFolder, "A.txt", "content", "utf-8", true, 0);
		try {
			file.create();
			fail("no exception");
		} catch(FileNotFoundException ex) {
			// good
		} catch (Throwable ex) {
			fail("unexpected exception: " + ex.getMessage());
		}
	}

	@Test
	public void testUpperDirectory() throws Exception {
		File baseFolder = tempFolder.newFolder("testUpperDirectory" + System.nanoTime());

		GeneratedFile file = new GeneratedFile(baseFolder, "../subfolder/A.txt", "content", "utf-8", true, 0);
		try {
			file.create();
			fail("no exception");
		} catch(IOException ex) {
			assertEquals("bad file name: ../subfolder/A.txt", ex.getMessage());
		} catch (Throwable ex) {
			fail("unexpected exception: " + ex.getMessage());
		}
	}

	@Test
	public void testSubFolder() throws Exception {
		File baseFolder = tempFolder.newFolder("testSubFolder" + System.nanoTime());
		new GeneratedFile(baseFolder, "AAA", "content", "utf-8", true, 0).create();

		GeneratedFile file = new GeneratedFile(baseFolder, "AAA/BB/A.txt", "content", "utf-8", true, 0);
		try {
			file.create();
			fail("no exception");
		} catch(IOException ex) {
			assertEquals("cannot create folders for `AAA/BB'", ex.getMessage());
		} catch (Throwable ex) {
			fail("unexpected exception: " + ex.getMessage());
		}
	}
}
