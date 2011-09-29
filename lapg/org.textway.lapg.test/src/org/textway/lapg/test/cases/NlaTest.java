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

import junit.framework.Assert;
import org.textway.lapg.api.Grammar;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.gen.SyntaxUtil;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.test.TestStatus;
import org.textway.lapg.test.cases.bootstrap.nla.NlaTestTree;
import org.textway.templates.api.SourceElement;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TypesRegistry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Gryaznov Evgeny, 9/28/11
 */
public class NlaTest extends LapgTestCase {

	private static final String NLA_INPUT = "org/textway/lapg/test/cases/bootstrap/nla/input";

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			public void report(int kind, String message, SourceElement... anchors) {
				Assert.fail(message);
			}
		});
	}

	public void testNla1() {
		String contents = FileUtil.getFileContents(openStream("syntax_nla1", TESTCONTAINER), FileUtil.DEFAULT_ENCODING);
		Grammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_nla1", contents.toCharArray(), 1), new TestStatus(),
				createDefaultTypesRegistry());
		Assert.assertNotNull(g);

		final StringBuilder debugText = new StringBuilder();
		TestStatus er = new TestStatus(
				"",
				"", 1) {
			@Override
			public void debug(String info) {
				debugText.append(info);
			}
		};
		LexicalBuilder.compile(g.getLexems(), g.getPatterns(), er);
		Builder.compile(g, er);
		er.assertDone();

		String expectedDebug = FileUtil.getFileContents(openStream("syntax_nla1.debug", TESTCONTAINER), FileUtil.DEFAULT_ENCODING);
		Assert.assertEquals(expectedDebug, debugText.toString());
	}

	public void testInputAll() throws UnsupportedEncodingException {
		String contents = FileUtil.getFileContents(openStream("all.in", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		String output;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream savedOut = System.out;
		try {
			System.setOut(new PrintStream(out));

			NlaTestTree<Object> parse = NlaTestTree.parse(new NlaTestTree.TextSource("all.in", contents.toCharArray(), 1));
			Assert.assertFalse(parse.hasErrors());

		} finally {
			System.setOut(savedOut);
			output = out.toString("utf-8");
		}

		String expected = FileUtil.getFileContents(openStream("all.out", NLA_INPUT), FileUtil.DEFAULT_ENCODING);
		Assert.assertEquals(expected, output);
	}

}
