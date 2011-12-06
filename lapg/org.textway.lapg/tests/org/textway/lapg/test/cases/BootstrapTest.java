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
package org.textway.lapg.test.cases;

import junit.framework.TestCase;
import org.junit.Assert;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.ProcessingStrategy;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.common.AbstractProcessingStatus;
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
				}, 2);
	}

	public void testSAction() {
		bootstrap(
				"org.textway.lapg/src/org/textway/lapg/parser/action",
				"saction.s",
				new String[0],
				new String[]{
						"SActionLexer.java"
				}, 0);
	}


	public void testRegexS() {
		bootstrap(
				"org.textway.lapg/src/org/textway/lapg/regex",
				"regex.s",
				new String[0],
				new String[]{
						"RegexDefParser.java", "RegexDefLexer.java", "RegexDefTree.java"
				}, 2);
	}

	public void testTypesS() {
		bootstrap(
				"org.textway.templates/src/org/textway/templates/types",
				"types.s",
				new String[0],
				new String[]{
						"TypesParser.java", "TypesLexer.java", "TypesTree.java",
						"ast/AstNode.java", "ast/AstNode.java",
						"ast/AstType.java", "ast/AstFeatureDeclaration.java",
						"ast/AstConstraint.java", "ast/IAstExpression.java",
						"ast/AstInput.java", "ast/AstLiteralExpression.java",
						"ast/AstMapEntriesItem.java", "ast/AstMultiplicity.java",
						"ast/AstTypeEx.java", "ast/AstMethodDeclaration.java"
				}, 0);
	}

	public void testTemplatesS() {
		bootstrap(
				"org.textway.templates/src/org/textway/templates/ast",
				"templates.s",
				new String[0],
				new String[]{
						"TemplatesParser.java", "TemplatesTree.java", "TemplatesLexer.java"
				}, 18);
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

	public void testSampleA() {
		bootstrap(
				"org.textway.lapg.test/src/org/textway/lapg/test/cases/bootstrap/a",
				"sample1.s",
				new String[0],
				new String[]{
						"SampleALexer.java", "SampleATree.java", "SampleAParser.java",
						"ast/IAstNode.java", "ast/IAstClassdefNoEoi.java"
				}, 0);
	}

	public void testSampleB() {
		bootstrap(
				"org.textway.lapg.test/src/org/textway/lapg/test/cases/bootstrap/b",
				"sample2.s",
				new String[0],
				new String[]{
						"SampleBLexer.java", "SampleBTree.java", "SampleBParser.java",
						"ast/IAstNode.java", "ast/IAstClassdefNoEoi.java"
				}, 0);
	}

	public void testSampleNoParser() {
		bootstrap(
				"org.textway.lapg.test/src/org/textway/lapg/test/cases/bootstrap/lexeronly",
				"noparser.s",
				new String[0],
				new String[]{
						"NoparserLexer.java"
				}, 0);
	}

	public void testNLA() {
		bootstrap(
				"org.textway.lapg.test/src/org/textway/lapg/test/cases/bootstrap/nla",
				"nla.s",
				new String[]{"-e"},
				new String[]{
						"NlaTestLexer.java", "NlaTestParser.java", "NlaTestTree.java", "errors", "tables"
				}, 6);
	}

	private void bootstrap(String folder, String syntaxFile, String[] args, String[] createdFiles, int expectedResolvedConflicts) {
		try {
			LapgOptions options = LapgOptions.parseArguments(args, new PrintStream(new CheckingErrorStream("")));
			Assert.assertNotNull("cannot parse options", options);

			options.setInput(syntaxFile);

			File root = new File(folder);
			Assert.assertTrue("folder doesn't exist: " + root.getAbsolutePath(), root.exists() && root.isDirectory());

			File source = new File(root, syntaxFile);
			Assert.assertTrue("grammar source doesn't exist", source.exists() && source.isFile());

			String contents = FileUtil.getFileContents(new FileInputStream(source), FileUtil.DEFAULT_ENCODING);
			Assert.assertNotNull("cannot read " + syntaxFile, contents);

			TextSource input = new TextSource(options.getInput(), contents.toCharArray(), 1);
			CheckingFileBasedStrategy strategy = new CheckingFileBasedStrategy(root);
			BootstrapTestStatus status = new BootstrapTestStatus(expectedResolvedConflicts, options.getDebug() >= LapgOptions.DEBUG_TABLES, options.getDebug() >= LapgOptions.DEBUG_AMBIG);

			boolean success = new LapgGenerator(options, status, strategy).compileGrammar(input);
			Assert.assertTrue(success);

			if (status.isDebugMode()) {
				strategy.createFile("tables", status.tablesFile != null ? status.tablesFile.toString() : "", status);
			}
			if (status.isAnalysisMode()) {
				strategy.createFile("errors", status.errorsFile != null ? status.errorsFile.toString() : "", status);
			}
			for (String s : createdFiles) {
				Assert.assertTrue("file is not generated: " + s, strategy.created.contains(s));
			}

			Assert.assertEquals(expectedResolvedConflicts - status.conflictCount + " conflicts instead of " + expectedResolvedConflicts, 0, status.conflictCount);

		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	public static class BootstrapTestStatus extends AbstractProcessingStatus {

		private int conflictCount;
		private StringBuilder tablesFile;
		private StringBuilder errorsFile;

		public BootstrapTestStatus(int conflictCount, boolean debug, boolean analysis) {
			super(debug, analysis);
			this.conflictCount = conflictCount;
		}

		@Override
		public void report(int kind, String message, SourceElement... anchors) {
			super.report(kind, message, anchors);

			if (kind == KIND_WARN && isAnalysisMode()) {
				return;
			}
			if (kind == ProcessingStatus.KIND_INFO && message.startsWith("lalr: ")) {
				return;
			}
			if (kind == ProcessingStatus.KIND_WARN && message.startsWith("symbol") && message.endsWith("is useless")) {
				return;
			}
			if (anchors != null && anchors.length >= 1 && anchors[0] != null) {
				message = anchors[0].getResourceName() + "," + anchors[0].getLine() + ": " + message;
			}
			Assert.fail("error reported: " + message);
		}

		public void report(String message, Throwable th) {
			Assert.fail("exception happend: " + message + ", " + th.toString());
		}

		@Override
		public void report(ParserConflict conflict) {
			super.report(conflict);
			if (conflict.getKind() == ParserConflict.FIXED) {
				if (conflictCount-- > 0) {
					return;
				}
			}
			Assert.fail("parser conflict is not expected: " + conflict.getText());
		}

		@Override
		public void debug(String info) {
			super.debug(info);
			if (!isDebugMode()) {
				Assert.fail("debug is forbidden: " + info);
			}
		}

		@Override
		public void handle(int kind, String text) {
			if (kind == KIND_DEBUG) {
				if (!isDebugMode()) {
					return;
				}
				if (tablesFile == null) {
					tablesFile = new StringBuilder();
				}
				tablesFile.append(text);
			} else if (kind == KIND_WARN) {
				if (!isAnalysisMode()) {
					return;
				}
				if (errorsFile == null) {
					errorsFile = new StringBuilder();
				}
				errorsFile.append(text);
			}
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
						String expected;
						try {
							InputStream os = new FileInputStream(new File(root, name));
							expected = FileUtil.getFileContents(os, charset);
						} catch (IOException ex) {
							expected = "# Original data is not available (new file is created):\n# " + ex.getMessage();
						}
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
