/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.tool.compiler;

import org.junit.Test;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.builder.GrammarFacade;
import org.textmapper.lapg.common.AbstractProcessingStatus;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.tool.gen.SyntaxUtil;
import org.textmapper.tool.gen.TemplatesStatusAdapter;
import org.textmapper.tool.parser.TMTree.TextSource;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

/**
 * evgeny, 2/7/13
 */
public class RewriteTest {

	private static final String PREFIX = "# test: ";

	@Test
	public void testRewriteTm() throws Exception {
		String contents = loadContent("tests/org/textmapper/tool/compiler/input/rewrite.tm");
		TMGrammar grammar = asGrammar(contents);
		for (Symbol s : grammar.getGrammar().getSymbols()) {
			if (s instanceof Nonterminal) {
				GrammarFacade.rewriteAsList((Nonterminal) s);
			}
		}
		Map<String, String> tests = loadTests(contents);

		for (Entry<String, String> entry : tests.entrySet()) {
			Nonterminal left = resolve(grammar, entry.getKey());
			String expected = entry.getValue();
			String actual = left.getDefinition().toString();
			assertEquals("failed for " + left.getName(), expected, actual);
		}
		assertEquals(14, tests.size());
	}

	private Nonterminal resolve(TMGrammar grammar, String name) {
		for (Symbol sym : grammar.getGrammar().getSymbols()) {
			if (name.equals(sym.getName())) {
				assertTrue(sym instanceof Nonterminal);
				return (Nonterminal) sym;
			}
		}
		fail("cannot resolve " + name);
		return null;
	}

	private Map<String, String> loadTests(String contents) throws IOException {
		final BufferedReader bufferedReader = new BufferedReader(new StringReader(contents));
		Map<String, String> result = new LinkedHashMap<>();
		String line;
		String expectedValue = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (expectedValue != null) {
				assertTrue("bad line: " + line, line.matches("\\w+ ::="));
				result.put(line.substring(0, line.indexOf(" ")), expectedValue);
				expectedValue = null;
				continue;
			}
			if (line.startsWith(PREFIX)) {
				expectedValue = line.substring(PREFIX.length()).trim();
			} else {
				assertFalse("bad comment: " + line, line.startsWith("#") && line.contains("test"));
			}
		}
		assertNull(expectedValue);
		return result;
	}

	private TMGrammar asGrammar(String contents) {
		try {
			TextSource input = new TextSource("input", contents, 1);
			LoadStatus status = new LoadStatus();

			TemplatesStatusAdapter templatesStatus = new TemplatesStatusAdapter(status);
			ResourceRegistry resources = new ResourceRegistry(new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/templates", "utf8"));
			TypesRegistry types = new TypesRegistry(resources, templatesStatus);

			TMGrammar s = SyntaxUtil.parseSyntax(input, status, types);
			assertNotNull(s);
			assertFalse(s.hasErrors());
			return s;

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			fail(ex.getMessage());
			return null;
		}
	}

	private String loadContent(String syntaxFile) throws FileNotFoundException {
		File source = new File(syntaxFile);
		assertTrue("grammar source doesn't exist: " + syntaxFile, source.exists() && source.isFile());

		String contents = FileUtil.getFileContents(new FileInputStream(source), FileUtil.DEFAULT_ENCODING);
		assertNotNull("cannot read " + syntaxFile, contents);
		return contents;
	}

	private static class LoadStatus extends AbstractProcessingStatus {

		protected LoadStatus() {
			super(false, false);
		}

		@Override
		public void report(String message, Throwable th) {
			th.printStackTrace(System.err);
			fail(message);
		}

		@Override
		public void handle(int kind, String text) {
			fail("error reported: " + text);
		}
	}
}
