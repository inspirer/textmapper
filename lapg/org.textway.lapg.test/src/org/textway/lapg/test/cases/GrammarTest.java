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

import junit.framework.Assert;
import org.textway.lapg.api.Grammar;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.eval.GenericNode;
import org.textway.lapg.eval.GenericParseContext;
import org.textway.lapg.eval.GenericParseContext.ParseProblem;
import org.textway.lapg.eval.GenericParseContext.Result;
import org.textway.lapg.gen.SyntaxUtil;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lalr.ParserTables;
import org.textway.lapg.lex.LexerTables;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.test.TestStatus;
import org.textway.lapg.test.cases.bootstrap.a.SampleAParseContext;
import org.textway.templates.api.SourceElement;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TypesRegistry;

/**
 * Gryaznov Evgeny, 3/17/11
 */
public class GrammarTest extends LapgTestCase {

	private GenericParseContext loadGrammar(String grammarName) {
		String contents = FileUtil.getFileContents(openStream(grammarName, TESTCONTAINER), FileUtil.DEFAULT_ENCODING);
		Grammar g = SyntaxUtil.parseSyntax(new TextSource(grammarName, contents.toCharArray(), 1), new TestStatus(), createDefaultTypesRegistry());
		Assert.assertNotNull(g);

		LexerTables l = LexicalBuilder.compile(g.getLexems(), new TestStatus());
		ParserTables r = Builder.compile(g, new TestStatus());
		return new GenericParseContext(g, r, l);
	}

	private void testParser(GenericParseContext context, int inputIndex, String text, String expectedAst) {
		Result root = context.parse(text, inputIndex);
		if(root.getErrors().size() > 0) {
			ParseProblem parseProblem = root.getErrors().get(0);
			Assert.fail(parseProblem.toString());
		}
		GenericNode node = (GenericNode) root.getRoot();
		Assert.assertNotNull(node);
		Assert.assertEquals(expectedAst, node.toSignature());
	}

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			public void report(int kind, String message, SourceElement... anchors) {
				Assert.fail(message);
			}
		});
	}

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

	public void testNoEoi() {
		GenericParseContext context = loadGrammar("syntaxNoEoi");

		testParser(context, 0, " class A {}  ###  ", "[class A {}]");
		testParser(context, 0, " class A {class B{} class C{} class D{}}  ####  ", "[class A {[[[class B{}] [class C{}]] [class D{}]]}]");
	}


	// sample1 test

	private void testSample1(String text, String expectedAst, boolean eoi) {
		Result root = new SampleAParseContext().parse(text, eoi);
		if(root.getErrors().size() > 0) {
			ParseProblem parseProblem = root.getErrors().get(0);
			Assert.fail(parseProblem.toString());
		}
		GenericNode node = (GenericNode) root.getRoot();
		Assert.assertNotNull(node);
		Assert.assertEquals(expectedAst, node.toSignature());
	}

	public void testNoEoiOnSample1() {
		testSample1(" class A {}  class B { } ", "[class A {}]", false);
		testSample1(" class A {}  ###  ", "[class A {}]", false);
		testSample1(" class A {class B{} class C{} class D{}}  ####  ", "[class A {[class B{}] [class C{}] [class D{}]}]", false);
	}

	public void testEoiOnSample1() {
		testSample1(" class A {}  ", "[class A {}]", true);
		testSample1(" class A {class B{} class C{} class D{}}  ", "[class A {[class B{}] [class C{}] [class D{}]}]", true);
	}
}
