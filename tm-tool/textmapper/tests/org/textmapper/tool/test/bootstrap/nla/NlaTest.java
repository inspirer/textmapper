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
package org.textmapper.tool.test.bootstrap.nla;

import org.junit.Test;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.gen.SyntaxUtil;
import org.textmapper.lapg.lalr.Builder;
import org.textmapper.lapg.lex.LexicalBuilder;
import org.textmapper.tool.compiler.LapgGrammar;
import org.textmapper.tool.parser.LapgTree.TextSource;
import org.textmapper.lapg.test.TestStatus;
import org.textmapper.tool.test.cases.LapgTestCase;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 9/28/11
 */
public class NlaTest extends LapgTestCase {

	private static final String NLA_INPUT = "org/textmapper/tool/test/bootstrap/nla/input";

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			@Override
			public void report(int kind, String message, SourceElement... anchors) {
				fail(message);
			}
		});
	}

	@Test
	public void testNla1() {
		String contents = FileUtil.getFileContents(openStream("syntax_nla1.st", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_nla1.st", contents.toCharArray(), 1), new TestStatus(),
				createDefaultTypesRegistry());
		assertNotNull(g);

		final StringBuilder debugText = new StringBuilder();
		TestStatus er = new TestStatus(
				"",
				"", false, true) {
			@Override
			public void debug(String info) {
				debugText.append(info);
			}
		};
		LexicalBuilder.compile(g.getGrammar().getLexerStates(), g.getGrammar().getLexicalRules(), g.getGrammar().getPatterns(), er);
		Builder.compile(g.getGrammar(), er);
		er.assertDone();

		String expectedDebug = FileUtil.getFileContents(openStream("syntax_nla1.out", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		expectedDebug = FileUtil.fixLineSeparators(expectedDebug, "\n");
		assertEquals(expectedDebug, debugText.toString());
	}

	@Test
	public void testInputAll() throws UnsupportedEncodingException {
		String contents = FileUtil.getFileContents(openStream("all.in", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		String output;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream savedOut = System.out;
		try {
			System.setOut(new PrintStream(out));

			NlaTestTree<Object> parse = NlaTestTree.parse(new NlaTestTree.TextSource("all.in", contents.toCharArray(), 1));
			if (parse.hasErrors()) {
				assertEquals("syntax error before line 9", parse.getErrors().get(0).getMessage());
			}

		} finally {
			System.setOut(savedOut);
			output = out.toString("utf-8");
		}

		String expected = FileUtil.getFileContents(openStream("all.out", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		assertEquals(expected, output);
	}

	@Test
	public void testExoticInput() throws UnsupportedEncodingException {
		String contents = FileUtil.getFileContents(openStream("exotic.in", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		String output;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream savedOut = System.out;
		try {
			System.setOut(new PrintStream(out));

			NlaTestTree<Object> parse = NlaTestTree.parse(new NlaTestTree.TextSource("exotic.in", contents.toCharArray(), 1));
			if (parse.hasErrors()) {
				assertEquals("syntax error before line 3", parse.getErrors().get(0).getMessage());
			}

		} finally {
			System.setOut(savedOut);
			output = out.toString("utf-8");
		}

		String expected = FileUtil.getFileContents(openStream("exotic.out", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		assertEquals(expected, output);
	}
}
