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
package org.textmapper.tool.compiler;

import org.junit.Test;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.RhsPart.Kind;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.lapg.common.AbstractProcessingStatus;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.tool.gen.SyntaxUtil;
import org.textmapper.tool.gen.TemplatesStatusAdapter;
import org.textmapper.tool.parser.TMTree.TextSource;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class SetTest {

	private static final String PREFIX = "#";

	@Test
	public void testValidSets() {
		process("tests/org/textmapper/tool/compiler/input/set.tm", 12);
	}

	private void process(String filename, int errors) {
		String contents;
		try {
			contents = loadContent(filename);
			TMGrammar grammar = asGrammar(contents);
			Map<String, String> tests = loadExpectedSets(contents);

			for (Entry<String, String> entry : tests.entrySet()) {
				Nonterminal left = resolve(grammar, entry.getKey());
				Set<Terminal> set = new HashSet<Terminal>();
				traverse(left, set);
				List<String> list = new ArrayList<String>();
				for (Terminal t : set) list.add(t.getName());
				Collections.sort(list);
				String expected = entry.getValue();
				assertEquals(expected, Arrays.toString(list.toArray()));
			}
			assertEquals(errors, tests.size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private void traverse(Nonterminal nt, Set<Terminal> result) {
		for (Rule r : nt.getRules()) {
			assertTrue("bad rule: " + r, 1 == r.getRight().length && r.getRight()[0].getKind() == Kind.Symbol);
			RhsSymbol rhsSymbol = r.getRight()[0];
			if (rhsSymbol.getTarget().isTerm()) {
				result.add((Terminal) rhsSymbol.getTarget());
			} else {
				traverse((Nonterminal) rhsSymbol.getTarget(), result);
			}
		}
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

	private Map<String, String> loadExpectedSets(String contents) throws IOException {
		final BufferedReader bufferedReader = new BufferedReader(new StringReader(contents));
		Map<String, String> result = new LinkedHashMap<String, String>();
		String line;
		String expectedValue = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (expectedValue != null) {
				assertFalse("bad line: " + line, line.matches("^\\w+ ::="));
				result.put(line.substring(0, line.indexOf(" ")), expectedValue);
				expectedValue = null;
				continue;
			}
			if (line.startsWith(PREFIX)) {
				expectedValue = line.substring(PREFIX.length()).trim();
			} else {
				assertFalse("bad line: " + line, line.contains("#"));
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
			ResourceRegistry resources = new ResourceRegistry(new ClassResourceLoader(getClass().getClassLoader(),
					"org/textmapper/tool/templates", "utf8"));
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
