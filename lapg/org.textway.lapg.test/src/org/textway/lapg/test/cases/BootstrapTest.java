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
package org.textway.lapg.test.cases;

import junit.framework.TestCase;
import org.junit.Assert;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.ProcessingStrategy;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.common.GeneratedFile;
import org.textway.lapg.gen.LapgGenerator;
import org.textway.lapg.gen.LapgOptions;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.templates.storage.FileBasedResourceLoader;
import org.textway.templates.storage.IResourceLoader;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class BootstrapTest extends TestCase {

	public void testLapgS() {
		bootstrap(
				"org.textway.lapg/src/org/textway/lapg/parser",
				"lapg.s",
				new String[0],
				new String[]{
						"LapgParser.java", "LapgLexer.java", "LapgTree.java"
				}, 0);
	}

	public void testTypesS() {
		bootstrap(
				"org.textway.templates/src/org/textway/templates/types",
				"types.s",
				new String[0],
				new String[]{
						"TypesParser.java", "TypesLexer.java", "TypesTree.java",
						"ast/AstNode.java", "ast/AstNode.java",
						"ast/Type.java", "ast/FeatureDeclaration.java",
						"ast/IConstraint.java", "ast/IExpression.java",
						"ast/Input.java", "ast/LiteralExpression.java",
						"ast/MapEntriesItem.java", "ast/Multiplicity.java"
				}, 0);
	}

	public void testTemplatesS() {
		bootstrap(
				"org.textway.templates/src/org/textway/templates/ast",
				"templates.s",
				new String[0],
				new String[]{
						"TemplatesParser.java", "TemplatesTree.java", "TemplatesLexer.java"
				}, 15);
	}

	public void testXmlS() {
		bootstrap(
				"org.textway.templates/src/org/textway/xml",
				"xml.s",
				new String[0],
				new String[]{
						"XmlParser.java", "XmlTree.java", "XmlLexer.java"
				}, 0);
	}

	private void bootstrap(String folder, String syntaxFile, String[] args, String[] createdFiles, int expectedResolvedConflicts) {
		try {
			LapgOptions options = LapgOptions.parseArguments(args, new PrintStream(new CheckingErrorStream("")));
			Assert.assertNotNull("cannot parse options", options);

			options.setInput(syntaxFile);

			File root = new File(folder);
			Assert.assertTrue("folder doesn't exist", root.exists() && root.isDirectory());

			File source = new File(root, syntaxFile);
			Assert.assertTrue("grammar source doesn't exist", source.exists() && source.isFile());

			String contents = FileUtil.getFileContents(new FileInputStream(source), FileUtil.DEFAULT_ENCODING);
			Assert.assertNotNull("cannot read " + syntaxFile, contents);

			BootstrapTestStatus status = new BootstrapTestStatus(expectedResolvedConflicts);
			TextSource input = new TextSource(options.getInput(), contents.toCharArray(), 1);
			CheckingFileBasedStrategy strategy = new CheckingFileBasedStrategy(root);

			boolean success = new LapgGenerator(options, status, strategy).compileGrammar(input);
			Assert.assertTrue(success);

			for (String s : createdFiles) {
				Assert.assertTrue("file is not generated: " + s, strategy.created.contains(s));
			}

			Assert.assertEquals((expectedResolvedConflicts-status.conflictCount) + " conflicts instead of " + expectedResolvedConflicts, 0, status.conflictCount);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	public static class BootstrapTestStatus implements ProcessingStatus {

		int conflictCount;

		public BootstrapTestStatus(int conflictCount) {
			this.conflictCount = conflictCount;
		}

		public void report(int kind, String message, SourceElement... anchors) {
			if(kind == ProcessingStatus.KIND_INFO && message.startsWith("lalr: ")) {
				return;
			}
			if(kind == ProcessingStatus.KIND_WARN && message.startsWith("symbol") && message.endsWith("is useless")) {
				return;
			}
			Assert.fail("error reported: " + message);
		}

		public void report(String message, Throwable th) {
			Assert.fail("exception happend: " + message + ", " + th.toString());
		}

		public void report(ParserConflict conflict) {
			if(conflict.getKind() == ParserConflict.FIXED) {
				if(conflictCount-- > 0) {
					return;
				}
			}
			Assert.fail("parser conflict is not expected: " + conflict.getText());
		}

		public void debug(String info) {
			Assert.fail("debug is forbidden: " + info);
		}

		public boolean isDebugMode() {
			return false;
		}

		public boolean isAnalysisMode() {
			return false;
		}
	}

	public static class CheckingFileBasedStrategy implements ProcessingStrategy {

		Set<String> created = new HashSet<String>();
		private final File root;

		public CheckingFileBasedStrategy(File root) {
			this.root = root;
		}

		public void createFile(String name, String contents, ProcessingStatus status) {
			try {
				// FIXME encoding, newline
				new GeneratedFile(name, contents, "utf8", false) {
					public void check() throws IOException {
						String name = getName();
						checkName(name);
						InputStream os = new FileInputStream(new File(root,name));
						String expected = FileUtil.getFileContents(os, charset);
						String data = getData();
						Assert.assertEquals(expected, data);
						created.add(name);
					}
				}.check();
			} catch (IOException e) {
				Assert.fail(e.getMessage());
			}
		}

		public IResourceLoader createResourceLoader(String qualifiedName) {
			File folder = new File(qualifiedName);
			if (folder.isDirectory()) {
				return new FileBasedResourceLoader(new File[]{folder}, "utf8");
			}
			return null;
		}
	}
}
