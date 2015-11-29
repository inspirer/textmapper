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
package org.textmapper.tool.test.cases;

import org.junit.Test;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.lapg.lex.LexerGenerator;
import org.textmapper.tool.compiler.TMGrammar;
import org.textmapper.tool.gen.SyntaxUtil;
import org.textmapper.lapg.lalr.Builder;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.lapg.test.TestStatus;
import org.textmapper.tool.bootstrap.b.SampleBTree;
import org.textmapper.tool.bootstrap.b.SampleBTree.TextSource;
import org.textmapper.tool.bootstrap.b.ast.AstClassdef;
import org.textmapper.tool.bootstrap.b.ast.AstClassdeflistItem;
import org.textmapper.tool.bootstrap.b.ast.AstVisitor;
import org.textmapper.tool.bootstrap.b.ast.IAstClassdefNoEoi;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 6/3/11
 */
public class SoftTermsTest extends LapgTestCase {

	@Test
	public void testSampleB() {
		checkParsed(
				"class P {\n" +
						" class Q { }\n" +
						" extends ()\n" +
						" class E extends D { }\n" +
						" xyzzz ()\n" +
						" class E25 extends D25 { }\n" +
						" q ()\n" +
						" \n" +
						"}",

				"class:'P',class:'Q',meth:extends,class:(extends D)'E',meth:xyzzz,class:(extends D25)'E25',meth:q,");
	}

	@Test
	public void testSoftConflictsHandling_ShiftShift() {
		TestStatus er = new TestStatus();
		TMGrammar g = SyntaxUtil.parseSyntax(new TMTree.TextSource("syntax_softconflicts_ss", FileUtil.getFileContents(openStream("syntax_softconflicts_ss", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), er, createDefaultTypesRegistry());
		assertNotNull(g);

		er.reset(
				"",
				"syntax_softconflicts_ss,31: input: Lclass identifier '('\n" +
						"shift soft/class conflict (next: identifier, Lclass)\n" +
						"    member ::= identifier '(' ')'\n" +
						"    classdef ::= Lclass identifier '(' memberslist ')'\n" +
						"    classdef ::= Lclass identifier Lextends identifier '(' memberslist ')'\n" +
						"\n" +
						"syntax_softconflicts_ss,31: input: Lclass identifier '(' memberslist\n" +
						"shift soft/class conflict (next: identifier, Lclass)\n" +
						"    member ::= identifier '(' ')'\n" +
						"    classdef ::= Lclass identifier '(' memberslist ')'\n" +
						"    classdef ::= Lclass identifier Lextends identifier '(' memberslist ')'\n" +
						"\n" +
						"syntax_softconflicts_ss,31: input: Lclass identifier Lextends identifier '('\n" +
						"shift soft/class conflict (next: identifier, Lclass)\n" +
						"    member ::= identifier '(' ')'\n" +
						"    classdef ::= Lclass identifier '(' memberslist ')'\n" +
						"    classdef ::= Lclass identifier Lextends identifier '(' memberslist ')'\n" +
						"\n" +
						"syntax_softconflicts_ss,31: input: Lclass identifier Lextends identifier '(' memberslist\n" +
						"shift soft/class conflict (next: identifier, Lclass)\n" +
						"    member ::= identifier '(' ')'\n" +
						"    classdef ::= Lclass identifier '(' memberslist ')'\n" +
						"    classdef ::= Lclass identifier Lextends identifier '(' memberslist ')'\n" +
						"\n");
		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), er);
		Builder.compile(g.getGrammar(), er);

		er.assertDone();
	}

	@Test
	public void testSoftConflictsHandling_ShiftReduce() {
		TestStatus er = new TestStatus();
		TMGrammar g = SyntaxUtil.parseSyntax(new TMTree.TextSource("syntax_softconflicts_sr", FileUtil.getFileContents(openStream("syntax_softconflicts_sr", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), er, createDefaultTypesRegistry());
		assertNotNull(g);

		er.reset(
				"",
				"syntax_softconflicts_sr,35: input: Lclass identifier '(' identifier\n" +
						"soft shift/reduce conflict (next: Lof)\n" +
						"    typename ::= identifier\n" +
						"\n" +
						"conflicts: 1 shift/reduce and 0 reduce/reduce\n");
		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), er);
		Builder.compile(g.getGrammar(), er);

		er.assertDone();
	}

	@Test
	public void testSoftConflictsHandling_ReduceReduce() {
		TestStatus er = new TestStatus();
		TMGrammar g = SyntaxUtil.parseSyntax(new TMTree.TextSource("syntax_softconflicts_rr", FileUtil.getFileContents(openStream("syntax_softconflicts_rr", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), er, createDefaultTypesRegistry());
		assertNotNull(g);

		er.reset(
				"",
				"syntax_softconflicts_rr,35: input: Lclass identifier '(' identifier\n" +
						"soft reduce/reduce conflict (next: Lof)\n" +
						"    varname ::= identifier\n" +
						"    typename ::= identifier\n" +
						"\n" +
						"conflicts: 0 shift/reduce and 1 reduce/reduce\n");
		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), er);
		Builder.compile(g.getGrammar(), er);

		er.assertDone();
	}

	private void checkParsed(String text, String expected) {
		SampleBTree<IAstClassdefNoEoi> tree = SampleBTree.parse(new TextSource("input", text, 1));
		assertFalse(tree.hasErrors());
		assertNotNull(tree.getRoot());
		final StringBuilder sb = new StringBuilder();
		tree.getRoot().accept(new AstVisitor() {
			@Override
			protected boolean visit(AstClassdef n) {
				sb.append("class:");
				if (n.getIdentifier() != null) {
					sb.append("(extends ").append(n.getIdentifier()).append(")");
				}
				sb.append("'").append(n.getID()).append("'").append(',');
				return true;
			}

			@Override
			protected boolean visit(AstClassdeflistItem n) {
				if (n.getClassdef() == null) {
					sb.append("meth:").append(n.getIdentifier()).append(',');
				}
				return true;
			}
		});
		assertEquals(expected, sb.toString());
	}

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/templates", "utf8"));
		return new TypesRegistry(resources, (kind, message, anchors) -> fail(message));
	}
}
