/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
package org.textmapper.tool.test.bootstrap;

import org.junit.Test;
import org.textmapper.lapg.api.ParserConflict;
import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TextSourceElement;
import org.textmapper.lapg.common.AbstractProcessingStatus;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.gen.TMGenerator;
import org.textmapper.tool.gen.TMOptions;
import org.textmapper.tool.parser.TMTree.TextSource;
import org.textmapper.tool.test.CheckingErrorStream;
import org.textmapper.tool.test.CheckingFileBasedStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.HashMap;

import static org.junit.Assert.*;

public class BootstrapTest {

	@Test
	public void testTextmapperTm() {
		bootstrap("src/org/textmapper/tool/parser", "textmapper.tm", new String[0], new String[]{
				"TMParser.java", "TMLexer.java", "TMTree.java"}, 4);
	}

	@Test
	public void testSAction() {
		bootstrap("src/org/textmapper/tool/parser/action", "saction.tm", new String[0],
				new String[]{"SActionLexer.java"}, 0);
	}

	@Test
	public void testRegexTm() {
		bootstrap("../lapg/src/org/textmapper/lapg/regex", "regex.tm", new String[0], new String[]{
				"RegexDefParser.java", "RegexDefLexer.java", "RegexDefTree.java"}, 1);
	}

	@Test
	public void testTypesTm() {
		bootstrap("../templates/src/org/textmapper/templates/types", "types.tm", new String[0], new String[]{
				"TypesParser.java", "TypesLexer.java", "TypesTree.java", "ast/AstNode.java", "ast/AstNode.java",
				"ast/AstType.java", "ast/AstFeatureDeclaration.java", "ast/AstConstraint.java",
				"ast/IAstExpression.java", "ast/AstInput.java", "ast/AstLiteralExpression.java",
				"ast/AstListOfIdentifierAnd2ElementsCommaSeparatedItem.java", "ast/AstMultiplicity.java",
				"ast/AstTypeEx.java",
				"ast/AstMethodDeclaration.java"}, 0);
	}

	@Test
	public void testTemplatesTm() {
		bootstrap("../templates/src/org/textmapper/templates/ast", "templates.tm", new String[0], new String[]{
				"TemplatesParser.java", "TemplatesTree.java", "TemplatesLexer.java"}, 18);
	}

	@Test
	public void testJavaTm() {
		bootstrap("../templates/src/org/textmapper/templates/java", "java.tm", new String[0], new String[]{
				"JavaParser.java", "JavaLexer.java"}, 25);
	}

	@Test
	public void testRewriteTm() {
		bootstrap("tests/org/textmapper/tool/compiler/input", "rewrite.tm", new String[0],
				new String[0], 0);
	}

	@Test
	public void testXmlTm() {
		bootstrap("../templates/src/org/textmapper/xml", "xml.tm", new String[0], new String[]{"XmlParser.java",
				"XmlTree.java", "XmlLexer.java"}, 0);
	}

	@Test
	public void testBisonTm() {
		bootstrap("src/org/textmapper/tool/importer", "bison.tm", new String[0],
				new String[]{"BisonLexer.java", "BisonParser.java", "BisonTree.java"}, 0);
	}


	@Test
	public void testSampleA() {
		bootstrap("tests/org/textmapper/tool/test/bootstrap/a", "sample1.tm", new String[0],
				new String[]{"SampleALexer.java", "SampleATree.java", "SampleAParser.java", "ast/IAstNode.java",
						"ast/IAstClassdefNoEoi.java"}, 0);
	}

	@Test
	public void testSampleB() {
		bootstrap("tests/org/textmapper/tool/test/bootstrap/b", "sample2.tm", new String[0],
				new String[]{"SampleBLexer.java", "SampleBTree.java", "SampleBParser.java", "SampleBMain.java",
						"ast/IAstNode.java", "ast/IAstClassdefNoEoi.java"}, 0);
	}

	@Test
	public void testStates() {
		bootstrap("tests/org/textmapper/tool/test/bootstrap/states", "states.tm", new String[0],
				new String[]{"StatesLexer.java"}, 0);
	}

	@Test
	public void testSampleNoParser() {
		bootstrap("tests/org/textmapper/tool/test/bootstrap/lexeronly", "noparser.tm",
				new String[0], new String[]{"NoparserLexer.java"}, 0);
	}

	@Test
	public void testSets() {
		bootstrap("tests/org/textmapper/tool/test/bootstrap/set", "set_test.tm",
				new String[0], new String[]{"SetLexer.java", "SetParser.java"}, 0);
	}

	@Test
	public void testUnicodeS() {
		bootstrap("tests/org/textmapper/tool/test/bootstrap/unicode", "unicode.tm", new String[0],
				new String[]{"UnicodeTestLexer.java"}, 0);
	}

	@Test
	public void testJsLexer1() {
		bootstrap("../tests/javascript/lexer", "lexer1.tm", new String[0],
				new String[]{"lexer1.js"}, 0);
	}

	private void bootstrap(String folder, String syntaxFile, String[] args, String[] createdFiles,
						   int expectedResolvedConflicts) {
		try {
			TMOptions options = TMOptions.parseArguments(args, new PrintStream(new CheckingErrorStream("")));
			assertNotNull("cannot parse options", options);

			options.setInput(syntaxFile);

			File root = new File(folder);
			assertTrue("folder doesn't exist: " + root.getAbsolutePath(), root.exists() && root.isDirectory());

			File source = new File(root, syntaxFile);
			assertTrue("grammar source doesn't exist", source.exists() && source.isFile());

			String contents = FileUtil.getFileContents(new FileInputStream(source), FileUtil.DEFAULT_ENCODING);
			assertNotNull("cannot read " + syntaxFile, contents);

			TextSource input = new TextSource(options.getInput(), contents.toCharArray(), 1);
			CheckingFileBasedStrategy strategy = new CheckingFileBasedStrategy(root);
			BootstrapTestStatus status = new BootstrapTestStatus(expectedResolvedConflicts,
					options.getDebug() >= TMOptions.DEBUG_TABLES, options.getDebug() >= TMOptions.DEBUG_AMBIG);

			boolean success = new TMGenerator(options, status, strategy).compileGrammar(input, false);
			assertTrue(success);

			if (status.isDebugMode()) {
				strategy.createFile("tables", status.tablesFile != null ? status.tablesFile.toString() : "",
						new HashMap<String, Object>(), status);
			}
			if (status.isAnalysisMode()) {
				strategy.createFile("errors", status.errorsFile != null ? status.errorsFile.toString() : "",
						new HashMap<String, Object>(), status);
			}
			for (String s : createdFiles) {
				assertTrue("file is not generated: " + s, strategy.getCreated().contains(s));
			}

			assertEquals(expectedResolvedConflicts - status.conflictCount + " conflicts instead of "
					+ expectedResolvedConflicts, 0, status.conflictCount);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			fail(ex.getMessage());
		}
	}

	private static class BootstrapTestStatus extends AbstractProcessingStatus {

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
			if (anchors != null && anchors.length >= 1 && anchors[0] instanceof TextSourceElement) {
				TextSourceElement textElement = (TextSourceElement) anchors[0];
				message = textElement.getResourceName() + "," + textElement.getLine() + ": " + message;
			}
			fail("error reported: " + message);
		}

		@Override
		public void report(String message, Throwable th) {
			th.printStackTrace(System.err);
			fail("exception happend: " + message + ", " + th.toString());
		}

		@Override
		public void report(ParserConflict conflict) {
			super.report(conflict);
			if (conflict.getKind() == ParserConflict.FIXED) {
				if (conflictCount-- > 0) {
					return;
				}
			}
			fail("parser conflict is not expected: " + conflict.getText());
		}

		@Override
		public void debug(String info) {
			super.debug(info);
			if (!isDebugMode()) {
				fail("debug is forbidden: " + info);
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
}
