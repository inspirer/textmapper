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
package org.textway.lapg.test.cases;

import org.junit.Test;
import org.textway.lapg.api.Lexem;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.gen.SyntaxUtil;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.parser.LapgGrammar;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.test.TestStatus;
import org.textway.templates.api.SourceElement;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TypesRegistry;

import static org.junit.Assert.*;

@SuppressWarnings({"deprecation"})
public class InputTest extends LapgTestCase {

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		return new TypesRegistry(resources, new TemplatesStatus() {
			@Override
			public void report(int kind, String message, SourceElement... anchors) {
				fail(message);
			}
		});
	}

	@Test
	public void testOptions() {
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax1options", FileUtil.getFileContents(openStream("syntax1options", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(g);
		assertEquals(0, g.getGrammar().getEoi().getIndex());

		Object container = g.getOptions().get("container");
		assertNotNull(container);
	}

	@Test
	public void testCheckSimple1() {
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax1", FileUtil.getFileContents(openStream("syntax1", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(g);
		assertEquals(0, g.getGrammar().getEoi().getIndex());

		Symbol[] syms = g.getGrammar().getSymbols();
		assertEquals(7, syms.length);
		assertEquals("eoi", syms[0].getName());
		assertEquals("identifier", syms[1].getName());
		assertEquals("Licon", syms[2].getName());
		assertEquals("_skip", syms[3].getName()); // TODO do not
		// collect skip
		// symbols
		assertEquals("input", syms[4].getName());
		assertEquals("list", syms[5].getName());
		assertEquals("list_item", syms[6].getName());

		Rule[] rules = g.getGrammar().getRules();
		assertEquals(5, rules.length);
		assertEquals("input", rules[0].getLeft().getName());
		assertEquals("list", rules[0].getRight()[0].getTarget().getName());
		assertEquals(1, rules[0].getRight().length);

		Lexem[] lexems = g.getGrammar().getLexems();
		assertEquals(3, lexems.length);
		assertEquals("@?[a-zA-Z_][A-Za-z_0-9]*", lexems[0].getRegexp().toString());
		assertEquals("([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?", lexems[1]
				.getRegexp().toString());
		assertEquals("[\\t\\r\\n ]+", lexems[2].getRegexp().toString());
		assertEquals(" continue; ", g.getCode(lexems[2]).getText());
	}

	@Test
	public void testCheckSimple2() {
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax2", FileUtil.getFileContents(openStream("syntax2", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(g);
		assertEquals(0, g.getGrammar().getEoi().getIndex());

		Symbol[] syms = g.getGrammar().getSymbols();
		assertEquals(11, syms.length);
		assertEquals("eoi", syms[0].getName());
		assertEquals("a", syms[1].getName());
		assertEquals("b", syms[2].getName());
		assertEquals("'('", syms[3].getName());
		assertEquals("')'", syms[4].getName());
		assertEquals("input", syms[5].getName());
		assertEquals("list", syms[6].getName());
		assertEquals("item", syms[7].getName());
		assertEquals("item3", syms[8].getName());
		assertEquals("subitem", syms[9].getName());
		assertEquals("listopt", syms[10].getName());
		assertEquals(13, g.getGrammar().getRules().length);
		assertEquals("  ${for a in b}..!..$$  ", g.getCode(g.getGrammar().getRules()[7]).getText());
		assertEquals(1, g.getGrammar().getRules()[9].getRight().length);
		assertNotNull(g.getGrammar().getRules()[9].getRight()[0].getNegativeLA());
		assertEquals(1, g.getGrammar().getRules()[9].getRight()[0].getNegativeLA().getUnwantedSet().length);
		assertEquals(1, g.getGrammar().getRules()[9].getRight()[0].getNegativeLA().getUnwantedSet()[0].getIndex());
	}

	@Test
	public void testTextSource() {
		TextSource source = new TextSource("file", "aa\nbb\n\nc".toCharArray(), 7);
		int[] expected = new int[]{7, 7, 7, 8, 8, 8, 9, 10};

		for (int i = 0; i < expected.length; i++) {
			assertEquals("offset #" + i, expected[i], source.lineForOffset(i));
		}
	}

	@Test
	public void testClassLexems() {
		TestStatus notifier = new TestStatus("",
				"syntax_lexems,22: lexem matches two classes `identifier' and `identifierX', using first\n" +
						"syntax_lexems,25: soft lexem `L0choice' doesn't match any class lexem\n" +
						"syntax_lexems,28: soft lexem `int' should have a constant regexp\n" +
						"syntax_lexems,36: redeclaration of soft class: icon instead of identifier\n" +
						"syntax_lexems,39: redeclaration of soft-terminal: ssss\n" +
						"syntax_lexems,42: soft lexem `wact' cannot have a semantic action\n" +
						"syntax_lexems,45: soft terminal `wtype' overrides base type: expected `<no type>', found `int'\n" +
						"syntax_lexems,52: soft terminal `comma' overrides base type: expected `char', found `Character'\n"
		);
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_lexems", FileUtil.getFileContents(openStream("syntax_lexems", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), notifier, createDefaultTypesRegistry());
		notifier.assertDone();
		assertNull(g);
	}

	@Test
	public void testNamedPatterns() {
		TestStatus notifier = new TestStatus("",
				"syntax_patterns,7: unfinished regexp\n" +
						"syntax_patterns,16: redeclaration of named pattern `WORD'\n"
		);
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_patterns", FileUtil.getFileContents(openStream("syntax_patterns", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), notifier, createDefaultTypesRegistry());
		notifier.assertDone();
		assertNull(g);
	}

	@Test
	public void testCheckCSharpGrammar() {
		TestStatus ts = new TestStatus();
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("input", FileUtil.getFileContents(openStream("syntax_cs", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), ts, createDefaultTypesRegistry());
		assertNotNull(g);

		ts.reset("input,3: symbol `error` is useless\n" + "input,44: symbol `Lfixed` is useless\n"
				+ "input,76: symbol `Lstackalloc` is useless\n"
				+ "input,149: symbol `comment` is useless\n" + "input,155: symbol `'/*'` is useless\n"
				+ "input,157: symbol `anysym1` is useless\n" + "input,159: symbol `'*/'` is useless\n",

				"input,481: input: using_directivesopt attributesopt modifiersopt Lclass ID class_baseopt '{' attributesopt modifiersopt operator_declarator '{' Lif '(' expression ')' embedded_statement\n"
						+ "shift/reduce conflict (next: Lelse)\n"
						+ "    embedded_statement ::= Lif '(' expression ')' embedded_statement\n"
						+ "\n"
						+ "conflicts: 1 shift/reduce and 0 reduce/reduce\n");

		LexicalBuilder.compile(g.getGrammar().getLexems(), g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);

		ts.assertDone();

		assertTrue(g.getTemplates().getText().startsWith("\n//#define DEBUG_syntax"));
	}

	@Test
	public void testCheckConflictsHandling() {
		TestStatus ts = new TestStatus();
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_conflict1", FileUtil.getFileContents(openStream("syntax_conflict1", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), ts, createDefaultTypesRegistry());
		assertNotNull(g);

		ts.reset(
				"",
				"syntax_conflict1,17: input: Licon\n" +
						"reduce/reduce conflict (next: fix1, fix2, fix3)\n" +
						"    input1 ::= Licon\n" +
						"    list_item ::= Licon\n" +
						"\n" +
						"conflicts: 0 shift/reduce and 1 reduce/reduce\n");
		LexicalBuilder.compile(g.getGrammar().getLexems(), g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);

		ts.assertDone();
	}

	@Test
	public void testCheckConflictsResolving() {
		final boolean[] isDebug = new boolean[]{false};
		TestStatus ts = new TestStatus("", "") {
			@Override
			public void handle(int kind, String text) {
				if (kind == KIND_DEBUG && isDebug[0]) {
					return; // ignore
				}
				super.handle(kind, text);
			}

			@Override
			public boolean isAnalysisMode() {
				return isDebug[0];
			}
		};
		LapgGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_conflict2resolved", FileUtil.getFileContents(openStream("syntax_conflict2resolved", TESTCONTAINER), FileUtil.DEFAULT_ENCODING).toCharArray(), 1), ts, createDefaultTypesRegistry());
		assertNotNull(g);

		ts.reset("", "");
		LexicalBuilder.compile(g.getGrammar().getLexems(), g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);
		ts.assertDone();

		isDebug[0] = true;
		ts.reset("syntax_conflict2resolved,42: input: Lid '=' expr '*' expr\n" +
				"resolved as reduce conflict (next: '*', '+', '-', '/')\n" +
				"    expr ::= expr '*' expr\n" +
				"\n" +
				"syntax_conflict2resolved,44: input: Lid '=' expr '+' expr\n" +
				"resolved as shift conflict (next: '*', '/')\n" +
				"    expr ::= expr '+' expr\n" +
				"\n" +
				"syntax_conflict2resolved,44: input: Lid '=' expr '+' expr\n" +
				"resolved as reduce conflict (next: '+', '-')\n" +
				"    expr ::= expr '+' expr\n" +
				"\n" +
				"syntax_conflict2resolved,45: input: Lid '=' expr '-' expr\n" +
				"resolved as shift conflict (next: '*', '/')\n" +
				"    expr ::= expr '-' expr\n" +
				"\n" +
				"syntax_conflict2resolved,45: input: Lid '=' expr '-' expr\n" +
				"resolved as reduce conflict (next: '+', '-')\n" +
				"    expr ::= expr '-' expr\n" +
				"\n" +
				"syntax_conflict2resolved,43: input: Lid '=' expr '/' expr\n" +
				"resolved as reduce conflict (next: '*', '+', '-', '/')\n" +
				"    expr ::= expr '/' expr\n" +
				"\n", "");
		LexicalBuilder.compile(g.getGrammar().getLexems(), g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);

		ts.assertDone();
	}
}
