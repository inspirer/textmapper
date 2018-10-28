/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.lapg.lex.LexerGenerator;
import org.textmapper.tool.compiler.TMDataUtil;
import org.textmapper.tool.compiler.TMGrammar;
import org.textmapper.tool.gen.SyntaxUtil;
import org.textmapper.lapg.lalr.Builder;
import org.textmapper.tool.parser.TMTree.TextSource;
import org.textmapper.lapg.test.TestStatus;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;

import static org.junit.Assert.*;

@SuppressWarnings({"deprecation"})
public class InputTest extends LapgTestCase {

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/test/cases/templates", "utf8"),
				new ClassResourceLoader(getClass().getClassLoader(), "org/textmapper/tool/templates", "utf8"));
		return new TypesRegistry(resources, (kind, message, anchors) -> fail(message));
	}

	@Test
	public void testCheckSimple1() {
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax1", FileUtil.getFileContents(openStream("syntax1", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(g);
		assertEquals(0, g.getGrammar().getEoi().getIndex());

		Symbol[] syms = g.getGrammar().getSymbols();
		assertEquals(7, syms.length);
		assertEquals(Symbol.EOI.text(), syms[0].getNameText());
		assertEquals("identifier", syms[1].getNameText());
		assertEquals("Licon", syms[2].getNameText());
		assertEquals("_skip", syms[3].getNameText()); // TODO do not
		// collect skip
		// symbols
		assertEquals("input", syms[4].getNameText());
		assertEquals("list", syms[5].getNameText());
		assertEquals("list_item", syms[6].getNameText());

		Rule[] rules = g.getGrammar().getRules();
		assertEquals(5, rules.length);
		assertEquals("input", rules[0].getLeft().getNameText());
		assertEquals("list", rules[0].getRight()[0].getTarget().getNameText());
		assertEquals(1, rules[0].getRight().length);

		LexerRule[] lexerRules = g.getGrammar().getLexerRules();
		assertEquals(3, lexerRules.length);
		assertEquals("@?[a-zA-Z_][A-Za-z_0-9]*", lexerRules[0].getRegexp().toString());
		assertEquals("([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?", lexerRules[1]
				.getRegexp().toString());
		assertEquals("[\\t\\r\\n ]+", lexerRules[2].getRegexp().toString());
		assertEquals("{ continue; }", TMDataUtil.getCodeTemplate(lexerRules[2]).getText());
	}

	@Test
	public void testCheckSimple2() {
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax2", FileUtil.getFileContents(openStream("syntax2", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), new TestStatus(), createDefaultTypesRegistry());
		assertNotNull(g);
		assertEquals(0, g.getGrammar().getEoi().getIndex());

		Symbol[] syms = g.getGrammar().getSymbols();
		assertEquals(11, syms.length);
		assertEquals(Symbol.EOI.text(), syms[0].getNameText());
		assertEquals("a", syms[1].getNameText());
		assertEquals("b", syms[2].getNameText());
		assertEquals("'('", syms[3].getNameText());
		assertEquals("')'", syms[4].getNameText());
		assertEquals("input", syms[5].getNameText());
		assertEquals("list", syms[6].getNameText());
		assertEquals("item", syms[7].getNameText());
		assertEquals("item3", syms[8].getNameText());
		assertEquals("subitem", syms[9].getNameText());
		assertEquals("listopt", syms[10].getNameText());
		assertEquals(13, g.getGrammar().getRules().length);
		assertEquals("{  ${for a in b}..!..$$  }", TMDataUtil.getCodeTemplate(g.getGrammar().getRules()[5]).getText());
		assertEquals(1, g.getGrammar().getRules()[9].getRight().length);
	}

	@Test
	public void testTextSource() {
		TextSource source = new TextSource("file", "aa\nbb\n\nc", 7);
		int[] expected = new int[]{7, 7, 7, 8, 8, 8, 9, 10};

		for (int i = 0; i < expected.length; i++) {
			assertEquals("offset #" + i, expected[i], source.lineForOffset(i));
		}
	}

	@Test
	public void testClassLexemes() {
		TestStatus notifier = new TestStatus("",
				"syntax_lexemes,25: regex matches two classes `identifier' and `identifierX', using first\n" +
						"syntax_lexemes,28: soft lexeme rule `L0choice' doesn't match any class rule\n" +
						"syntax_lexemes,31: soft lexeme rule `int' should have a constant regexp\n" +
						"syntax_lexemes,39: redeclaration of soft class for `abcde': found icon instead of identifier\n" +
						"syntax_lexemes,42: redeclaration of soft terminal: ssss\n" +
						"syntax_lexemes,45: soft lexeme rule `wact' cannot have a semantic action\n" +
						"syntax_lexemes,48: soft terminal `wtype' overrides base type: expected `<no type>', found `int'\n" +
						"syntax_lexemes,55: soft terminal `comma' overrides base type: expected `char', found `Character'\n"
		);
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_lexemes", FileUtil.getFileContents(openStream("syntax_lexemes", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), notifier, createDefaultTypesRegistry());
		notifier.assertDone();
		assertNull(g);
	}

	@Test
	public void testNamedPatterns() {
		TestStatus notifier = new TestStatus("",
				"syntax_patterns,10: regexp is incomplete\n" +
						"syntax_patterns,19: redeclaration of named pattern `WORD', ignored\n"
		);
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_patterns", FileUtil.getFileContents(openStream("syntax_patterns", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), notifier, createDefaultTypesRegistry());
		notifier.assertDone();
		assertNull(g);
	}

	@Test
	public void testCheckCSharpGrammar() {
		TestStatus ts = new TestStatus();
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("input", FileUtil.getFileContents(openStream("syntax_cs", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), ts, createDefaultTypesRegistry());
		assertNotNull(g);

		ts.reset("input,8: symbol `error` is useless\n" + "input,49: symbol `Lfixed` is useless\n"
				+ "input,81: symbol `Lstackalloc` is useless\n"
				+ "input,154: symbol `comment` is useless\n" + "input,160: symbol `'/*'` is useless\n"
				+ "input,162: symbol `anysym1` is useless\n" + "input,164: symbol `'*/'` is useless\n",

				"input,486: input: using_directivesopt attributesopt modifiersopt Lclass ID class_baseopt '{' attributesopt modifiersopt operator_declarator '{' Lif '(' expression ')' embedded_statement\n"
						+ "shift/reduce conflict (next: Lelse)\n"
						+ "    embedded_statement : Lif '(' expression ')' embedded_statement\n"
						+ "\n"
						+ "conflicts: 1 shift/reduce and 0 reduce/reduce\n");

		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);

		ts.assertDone();

		assertTrue(g.getTemplates().getText().startsWith("//#define DEBUG_syntax"));
	}

	@Test
	public void testCheckConflictsHandling() {
		TestStatus ts = new TestStatus();
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_conflict1", FileUtil.getFileContents(openStream("syntax_conflict1", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), ts, createDefaultTypesRegistry());
		assertNotNull(g);

		ts.reset(
				"",
				"syntax_conflict1,22: input: Licon\n" +
						"reduce/reduce conflict (next: fix1, fix2, fix3)\n" +
						"    input1 : Licon\n" +
						"    list_item : Licon\n" +
						"\n" +
						"conflicts: 0 shift/reduce and 1 reduce/reduce\n");
		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), ts);
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
		TMGrammar g = SyntaxUtil.parseSyntax(new TextSource("syntax_conflict2resolved", FileUtil.getFileContents(openStream("syntax_conflict2resolved", TESTCONTAINER), FileUtil.DEFAULT_ENCODING), 1), ts, createDefaultTypesRegistry());
		assertNotNull(g);

		ts.reset("", "");
		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);
		ts.assertDone();

		isDebug[0] = true;
		ts.reset("syntax_conflict2resolved,42: input: Lid '=' expr '*' expr\n" +
				"resolved as reduce conflict (next: '*', '+', '-', '/')\n" +
				"    expr : expr '*' expr\n" +
				"\n" +
				"syntax_conflict2resolved,44: input: Lid '=' expr '+' expr\n" +
				"resolved as shift conflict (next: '*', '/')\n" +
				"    expr : expr '+' expr\n" +
				"\n" +
				"syntax_conflict2resolved,44: input: Lid '=' expr '+' expr\n" +
				"resolved as reduce conflict (next: '+', '-')\n" +
				"    expr : expr '+' expr\n" +
				"\n" +
				"syntax_conflict2resolved,45: input: Lid '=' expr '-' expr\n" +
				"resolved as shift conflict (next: '*', '/')\n" +
				"    expr : expr '-' expr\n" +
				"\n" +
				"syntax_conflict2resolved,45: input: Lid '=' expr '-' expr\n" +
				"resolved as reduce conflict (next: '+', '-')\n" +
				"    expr : expr '-' expr\n" +
				"\n" +
				"syntax_conflict2resolved,43: input: Lid '=' expr '/' expr\n" +
				"resolved as reduce conflict (next: '*', '+', '-', '/')\n" +
				"    expr : expr '/' expr\n" +
				"\n", "");
		LexerGenerator.generate(g.getGrammar().getLexerStates(), g.getGrammar().getLexerRules(),
				g.getGrammar().getPatterns(), ts);
		Builder.compile(g.getGrammar(), ts);

		ts.assertDone();
	}
}
