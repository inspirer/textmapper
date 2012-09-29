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
package org.textmapper.lapg.test.cases;

import org.junit.Test;
import org.textmapper.lapg.api.LexerData;
import org.textmapper.lapg.api.ParserData;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.lapg.eval.GenericNode;
import org.textmapper.lapg.eval.GenericParseContext;
import org.textmapper.lapg.eval.GenericParseContext.ParseProblem;
import org.textmapper.lapg.eval.GenericParseContext.Result;
import org.textmapper.lapg.gen.SyntaxUtil;
import org.textmapper.lapg.lalr.Builder;
import org.textmapper.lapg.lex.LexicalBuilder;
import org.textmapper.lapg.compiler.LapgGrammar;
import org.textmapper.lapg.parser.LapgTree.TextSource;
import org.textmapper.lapg.test.TestStatus;
import org.textmapper.lapg.test.bootstrap.a.SampleAParseContext;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 3/17/11
 */
public class GrammarTest extends LapgTestCase {

	private GenericParseContext loadGrammar(String grammarName) {
		String contents = FileUtil.getFileContents(openStream(grammarName, TESTCONTAINER), FileUtil.DEFAULT_ENCODING);
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource(grammarName, contents.toCharArray(), 1), new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(g);
		assertNotNull(g.getGrammar());

		LexerData l = LexicalBuilder.compile(g.getGrammar().getLexerStates(), g.getGrammar().getLexems(), g.getGrammar().getPatterns(), new TestStatus());
		ParserData r = Builder.compile(g.getGrammar(), new TestStatus());
		return new GenericParseContext(g.getGrammar(), r, l);
	}

	private void testParser(GenericParseContext context, int inputIndex, String text, String expectedAst) {
		Result root = context.parse(text, inputIndex);
		if (root.getErrors().size() > 0) {
			ParseProblem parseProblem = root.getErrors().get(0);
			fail(parseProblem.toString());
		}
		GenericNode node = (GenericNode) root.getRoot();
		assertNotNull(node);
		assertEquals(expectedAst, node.toSignature());
	}

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/lapg/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			@Override
			public void report(int kind, String message, SourceElement... anchors) {
				fail(message);
			}
		});
	}

	@Test
	public void testMultiInputStates() {
		GenericParseContext context = loadGrammar("syntaxmultiinput");

		// A1 ::= 'aaa' | A2 identifier ;
		// A2 ::= A1 | A2 'aaa' ;

		testParser(context, 0, "    aaa    ", "[aaa]");
		testParser(context, 0, "    aaa x    ", "[[aaa] x]");
		testParser(context, 1, "    aaa xyz aaa   ", "[[[aaa] xyz] aaa]");
		testParser(context, 0, "   aaa x y z a  b c   ", "[[[[[[[aaa] x] y] z] a]  b] c]");
		testParser(context, 1, "    aaa    ", "[aaa]");
		testParser(context, 1, "    aaa aaa aaa aaa    ", "[[[[aaa] aaa] aaa] aaa]");
		testParser(context, 1, "  aaa x  aaa aaa aaa aaa    ", "[[[[[[aaa] x]  aaa] aaa] aaa] aaa]");
		testParser(context, 1, "  aaa x  aaa y aaa aaa aaa    ", "[[[[[[[aaa] x]  aaa] y] aaa] aaa] aaa]");
	}

	@Test
	public void testNoEoi() {
		GenericParseContext context = loadGrammar("syntaxNoEoi");

		testParser(context, 0, " class A {}  ###  ", "[class A {}]");
		testParser(context, 0, " class A {class B{} class C{} class D{}}  ####  ", "[class A {[[[class B{}] [class C{}]] [class D{}]]}]");
	}


	// sample1 test

	private void testSample1(String text, String expectedAst, boolean eoi) {
		Result root = new SampleAParseContext().parse(text, eoi);
		if (root.getErrors().size() > 0) {
			ParseProblem parseProblem = root.getErrors().get(0);
			fail(parseProblem.toString());
		}
		GenericNode node = (GenericNode) root.getRoot();
		assertNotNull(node);
		assertEquals(expectedAst, node.toSignature());
	}

	@Test
	public void testNoEoiOnSample1() {
		testSample1(" class A {}  class B { } ", "[class A {}]", false);
		testSample1(" class A {}  ###  ", "[class A {}]", false);
		testSample1(" class A {class B{} class C{} class D{}}  ####  ", "[class A {[class B{}] [class C{}] [class D{}]}]", false);
	}

	@Test
	public void testEoiOnSample1() {
		testSample1(" class A {}  ", "[class A {}]", true);
		testSample1(" class A {class B{} class C{} class D{}}  ", "[class A {[class B{}] [class C{}] [class D{}]}]", true);
	}
}
