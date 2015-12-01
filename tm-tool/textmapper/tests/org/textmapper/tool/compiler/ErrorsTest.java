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
package org.textmapper.tool.compiler;

import org.junit.Test;
import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TextSourceElement;
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

import static org.junit.Assert.*;

public class ErrorsTest {

	private static final String PREFIX = "# ERR: @";

	@Test
	public void testEmptySetErr() {
		process("tests/org/textmapper/tool/compiler/error/empty_set_err.tm2", 4);
	}

	@Test
	public void testRecursiveSetErr() {
		process("tests/org/textmapper/tool/compiler/error/recursive_set_err.tm2", 2);
	}

	@Test
	public void testTemplateErr() {
		process("tests/org/textmapper/tool/compiler/error/template_usage_err.tm2", 10);
	}

	private void process(String filename, int errors) {
		String contents;
		try {
			contents = loadContent(filename);
			ProblemStatus status = new ProblemStatus();
			processGrammar(contents, status);

			Set<ReportedProblem> tests = loadTests(contents);
			assertEquals(errors, tests.size());

			for (ReportedProblem p : status.getProblems()) {
				assertTrue("Unexpected error was reported: " + p.toString() + "; " +
								"expected: " + Arrays.toString(tests.toArray()),
						tests.remove(p));
			}
			assertTrue("Expected errors were not reported: " + Arrays.toString(tests.toArray()), tests.isEmpty());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private static TextSourceElement unwrap(SourceElement e) {
		while (!(e instanceof TextSourceElement) && e instanceof DerivedSourceElement) {
			e = ((DerivedSourceElement) e).getOrigin();
		}
		assertTrue(e instanceof TextSourceElement);
		return (TextSourceElement) e;
	}

	private LinkedHashSet<ReportedProblem> loadTests(String contents) throws IOException {
		final BufferedReader bufferedReader = new BufferedReader(new StringReader(contents));
		LinkedHashSet<ReportedProblem> result = new LinkedHashSet<>();
		int lineNumber = 0;
		List<ReportedProblem> pending = new ArrayList<>(4);

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			lineNumber++;
			if (line.startsWith(PREFIX)) {
				line = line.substring(PREFIX.length()).trim();
				int i = line.indexOf(": ");
				assertTrue(i > 0);
				pending.add(new ReportedProblem(0,
						line.substring(0, i),
						line.substring(i + 2)));

			} else if (!pending.isEmpty()) {
				for (ReportedProblem p : pending) {
					assertTrue("bad line: " + line + " for " + p.toString(), line.contains(p.getElement()));
					p.setLine(lineNumber);
					result.add(p);
				}
				pending.clear();
			} else {
				assertFalse("bad comment: " + line, line.startsWith("#") && line.contains("ERR"));
			}
		}
		assertTrue(pending.isEmpty());
		return result;
	}

	private void processGrammar(String contents, ProblemStatus status) {
		try {
			TextSource input = new TextSource("input", contents, 1);

			TemplatesStatusAdapter templatesStatus = new TemplatesStatusAdapter(status);
			ResourceRegistry resources = new ResourceRegistry(new ClassResourceLoader(getClass().getClassLoader(),
					"org/textmapper/tool/templates", "utf8"));
			TypesRegistry types = new TypesRegistry(resources, templatesStatus);

			SyntaxUtil.parseSyntax(input, status, types);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			fail(ex.getMessage());
		}
	}

	private String loadContent(String syntaxFile) throws FileNotFoundException {
		File source = new File(syntaxFile);
		assertTrue("grammar source doesn't exist: " + syntaxFile, source.exists() && source.isFile());

		String contents = FileUtil.getFileContents(new FileInputStream(source), FileUtil.DEFAULT_ENCODING);
		assertNotNull("cannot read " + syntaxFile, contents);
		return contents;
	}

	private static class ProblemStatus extends AbstractProcessingStatus {
		private List<ReportedProblem> problems = new ArrayList<>();

		protected ProblemStatus() {
			super(false, false);
		}

		@Override
		public void report(int kind, String message, SourceElement... anchors) {
			if (kind != KIND_ERROR || anchors.length != 1) {
				super.report(kind, message, anchors);
				return;
			}
			TextSourceElement e = unwrap(anchors[0]);
			problems.add(new ReportedProblem(e.getLine(), e.getText(), message));

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

		public List<ReportedProblem> getProblems() {
			return problems;
		}
	}

	private static class ReportedProblem {
		private int line;
		private String element;
		private String message;

		private ReportedProblem(int line, String element, String message) {
			this.line = line;
			this.element = element;
			this.message = message;
		}

		public String getElement() {
			return element;
		}

		public void setLine(int line) {
			this.line = line;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ReportedProblem that = (ReportedProblem) o;

			if (line != that.line) return false;
			if (!element.equals(that.element)) return false;
			if (!message.equals(that.message)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = line;
			result = 31 * result + element.hashCode();
			result = 31 * result + message.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "problem{" + line + ", '" + element + '\'' +
					", message='" + message + "'}";
		}
	}
}
