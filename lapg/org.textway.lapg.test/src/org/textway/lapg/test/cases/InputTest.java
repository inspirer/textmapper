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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import junit.framework.Assert;
import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.Lexem;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;
import org.textway.lapg.gen.SyntaxUtil;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lalr.ParserTables;
import org.textway.lapg.lex.LexerTables;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.parser.LiGrammar;
import org.textway.lapg.parser.LiRule;
import org.textway.lapg.parser.LiSymbol;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.test.TestStatus;
import org.textway.lapg.test.oldparser.SyntaxUtilOld;
import org.textway.templates.api.IProblemCollector;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.IResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TypesRegistry;

@SuppressWarnings({"deprecation"})
public class InputTest extends LapgTestCase {

	private void checkGenTables(Grammar g, String outputId, TestStatus er) {
		LexerTables lt = LexicalBuilder.compile(g.getLexems(), er);
		ParserTables pt = Builder.compile(g, er);

		StringBuffer sb = new StringBuffer();

		OutputUtils.printTables(sb, lt);
		OutputUtils.printTables(sb, pt);

		String expected = removeSpaces(SyntaxUtilOld.getFileContents(openStream(outputId, RESULTCONTAINER)).trim());
		String actual = removeSpaces(sb.toString().trim());

		Assert.assertEquals(expected, actual);
	}

	private TypesRegistry createDefaultTypesRegistry() {
		ResourceRegistry resources = new ResourceRegistry(new IResourceLoader[]{new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8")});
		return new TypesRegistry(resources, new IProblemCollector() {
			public void fireError(ILocatedEntity referer, String error) {
				Assert.fail(error);
			}
		});
	}

	public void testCheckSimple1() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax1", openStream("syntax1", TESTCONTAINER), new TestStatus(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi().getIndex());

		Symbol[] syms = g.getSymbols();
		Assert.assertEquals(7, syms.length);
		Assert.assertEquals("eoi", syms[0].getName());
		Assert.assertEquals("identifier", syms[1].getName());
		Assert.assertEquals("Licon", syms[2].getName());
		Assert.assertEquals("_skip", syms[3].getName()); // TODO do not
		// collect skip
		// symbols
		Assert.assertEquals("input", syms[4].getName());
		Assert.assertEquals("list", syms[5].getName());
		Assert.assertEquals("list_item", syms[6].getName());

		Rule[] rules = g.getRules();
		Assert.assertEquals(5, rules.length);
		Assert.assertEquals("input", rules[0].getLeft().getName());
		Assert.assertEquals("list", rules[0].getRight()[0].getTarget().getName());
		Assert.assertEquals(1, rules[0].getRight().length);

		Lexem[] lexems = g.getLexems();
		Assert.assertEquals(3, lexems.length);
		Assert.assertEquals("@?[a-zA-Z_][A-Za-z_0-9]*", lexems[0].getRegexp());
		Assert.assertEquals("([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?", lexems[1]
				.getRegexp());
		Assert.assertEquals("[\\t\\r\\n ]+", lexems[2].getRegexp());
		Assert.assertEquals(" continue; ", lexems[2].getAction().getContents());

		checkGenTables(g, "syntax1.tbl", new TestStatus());
	}

	public void testCheckSimple2() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestStatus(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi().getIndex());

		Symbol[] syms = g.getSymbols();
		Assert.assertEquals(9, syms.length);
		Assert.assertEquals("eoi", syms[0].getName());
		Assert.assertEquals("a", syms[1].getName());
		Assert.assertEquals("b", syms[2].getName());
		Assert.assertEquals("'('", syms[3].getName());
		Assert.assertEquals("')'", syms[4].getName());
		Assert.assertEquals("input", syms[5].getName());
		Assert.assertEquals("list", syms[6].getName());
		Assert.assertEquals("item", syms[7].getName());
		Assert.assertEquals("listopt", syms[8].getName());
		Assert.assertEquals(8, g.getRules().length);
		Assert.assertEquals("  ${for a in b}..!..$$  ", g.getRules()[7].getAction().getContents());

		checkGenTables(g, "syntax2.tbl", new TestStatus());
	}

	public void testCheckCSharpGrammar() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestStatus(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		TestStatus er = new TestStatus(
				"syntax_cs,3: symbol `error` is useless\n" + "syntax_cs,44: symbol `Lfixed` is useless\n"
						+ "syntax_cs,76: symbol `Lstackalloc` is useless\n"
						+ "syntax_cs,149: symbol `comment` is useless\n" + "syntax_cs,155: symbol `'/*'` is useless\n"
						+ "syntax_cs,157: symbol `anysym1` is useless\n" + "syntax_cs,159: symbol `'*/'` is useless\n",

				"input: using_directivesopt attributesopt modifiersopt Lclass ID class_baseopt '{' attributesopt modifiersopt operator_declarator '{' Lif '(' expression ')' embedded_statement\n"
						+ "shift/reduce conflict (next: Lelse)\n"
						+ "    embedded_statement ::= Lif '(' expression ')' embedded_statement\n"
						+ "\n"
						+ "conflicts: 1 shift/reduce and 0 reduce/reduce\n");

		checkGenTables(g, "syntax_cs.tbl", er);
		er.assertDone();

		Assert.assertTrue(g.getTemplates().startsWith("\n//#define DEBUG_syntax"));
	}

	public void testLapgGrammar() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER),
				new TestStatus(), new HashMap<String, String>());
		Assert.assertNotNull(g);

		checkGenTables(g, "syntax_lapg.tbl", new TestStatus());
	}

	public void testLapgTemplatesGrammar() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER),
				new TestStatus(), new HashMap<String, String>());
		Assert.assertNotNull(g);

		checkGenTables(g, "syntax_tpl.tbl", new TestStatus());
	}

	public void testNewTemplatesGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER),
				new TestStatus(), new HashMap<String, String>());
		Assert.assertNotNull(g);

		sortGrammar((LiGrammar) g, go);
		checkGenTables(g, "syntax_tpl.tbl", new TestStatus());
	}

	public void testNewLapgGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER),
				new TestStatus(), new HashMap<String, String>());
		Assert.assertNotNull(g);

		sortGrammar((LiGrammar) g, go);
		checkGenTables(g, "syntax_lapg.tbl", new TestStatus());
	}

	public void testNewCheckCSharpGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestStatus(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		sortGrammar((LiGrammar) g, go);

		TestStatus er = new TestStatus(
				"syntax_cs,3: symbol `error` is useless\n" + "syntax_cs,44: symbol `Lfixed` is useless\n"
						+ "syntax_cs,76: symbol `Lstackalloc` is useless\n"
						+ "syntax_cs,149: symbol `comment` is useless\n"
						+ "syntax_cs,155: symbol `'/*'` is useless\n"
						+ "syntax_cs,157: symbol `anysym1` is useless\n"
						+ "syntax_cs,159: symbol `'*/'` is useless\n",

				"syntax_cs,481: input: using_directivesopt attributesopt modifiersopt Lclass ID class_baseopt '{' attributesopt modifiersopt operator_declarator '{' Lif '(' expression ')' embedded_statement\n"
						+ "shift/reduce conflict (next: Lelse)\n"
						+ "    embedded_statement ::= Lif '(' expression ')' embedded_statement\n"
						+ "\n"
						+ "conflicts: 1 shift/reduce and 0 reduce/reduce\n");

		checkGenTables(g, "syntax_cs.tbl", er);
		er.assertDone();

		Assert.assertTrue(g.getTemplates().startsWith("\n//#define DEBUG_syntax"));
	}

	public void testNewCheckSimple1() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1", openStream("syntax1", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi().getIndex());

		Symbol[] syms = g.getSymbols();
		Assert.assertEquals(7, syms.length);
		Assert.assertEquals("eoi", syms[0].getName());
		Assert.assertEquals("identifier", syms[1].getName());
		Assert.assertEquals("Licon", syms[2].getName());
		Assert.assertEquals("_skip", syms[3].getName()); // TODO do not
		// collect skip
		// symbols
		Assert.assertEquals("input", syms[4].getName());
		Assert.assertEquals("list", syms[5].getName());
		Assert.assertEquals("list_item", syms[6].getName());

		Rule[] rules = g.getRules();
		Assert.assertEquals(5, rules.length);
		Assert.assertEquals("input", rules[0].getLeft().getName());
		Assert.assertEquals("list", rules[0].getRight()[0].getTarget().getName());
		Assert.assertEquals(1, rules[0].getRight().length);

		Lexem[] lexems = g.getLexems();
		Assert.assertEquals(3, lexems.length);
		Assert.assertEquals("@?[a-zA-Z_][A-Za-z_0-9]*", lexems[0].getRegexp());
		Assert.assertEquals("([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?", lexems[1]
				.getRegexp());
		Assert.assertEquals("[\\t\\r\\n ]+", lexems[2].getRegexp());
		Assert.assertEquals(" continue; ", lexems[2].getAction().getContents());

		checkGenTables(g, "syntax1.tbl", new TestStatus());
	}

	public void testNewCheckSimple2() {
		Grammar g = SyntaxUtil.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestStatus(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi().getIndex());

		Symbol[] syms = g.getSymbols();
		Assert.assertEquals(9, syms.length);
		Assert.assertEquals("eoi", syms[0].getName());
		Assert.assertEquals("a", syms[1].getName());
		Assert.assertEquals("b", syms[2].getName());
		Assert.assertEquals("'('", syms[3].getName());
		Assert.assertEquals("')'", syms[4].getName());
		Assert.assertEquals("input", syms[5].getName());
		Assert.assertEquals("list", syms[6].getName());
		Assert.assertEquals("item", syms[7].getName());
		Assert.assertEquals("listopt", syms[8].getName());
		Assert.assertEquals(8, g.getRules().length);
		Assert.assertEquals("  ${for a in b}..!..$$  ", g.getRules()[7].getAction().getContents());

		sortGrammar((LiGrammar) g, go);
		checkGenTables(g, "syntax2.tbl", new TestStatus());
	}

	private void sortGrammar(LiGrammar g, Grammar go) {
		final HashMap<String, Integer> index = new HashMap<String, Integer>();
		for (Symbol s : go.getSymbols()) {
			index.put(s.getName(), s.getIndex());
		}
		final HashMap<String, Integer> ruleind = new HashMap<String, Integer>();
		for (Rule r : go.getRules()) {
			String ind = getSignature(r);
			ruleind.put(ind, r.getIndex());
		}

		Arrays.sort(g.getSymbols(), new Comparator<Symbol>() {
			public int compare(Symbol o1, Symbol o2) {
				Integer i1 = index.get(o1.getName());
				Integer i2 = index.get(o2.getName());
				return i1.compareTo(i2);
			}
		});
		for (int i = 0; i < g.getSymbols().length; i++) {
			((LiSymbol) g.getSymbols()[i]).setId(i, g.getSymbols()[i].getName());
		}

		Arrays.sort(g.getRules(), new Comparator<Rule>() {
			public int compare(Rule o1, Rule o2) {
				Integer i1 = ruleind.get(getSignature(o1));
				Integer i2 = ruleind.get(getSignature(o2));
				return i1.compareTo(i2);
			}
		});
		for (int i = 0; i < g.getRules().length; i++) {
			((LiRule) g.getRules()[i]).setIndex(i);
		}
	}

	private String getSignature(Rule r) {
		String ind = Integer.toString(r.getLeft().getIndex());
		for (SymbolRef rt : r.getRight()) {
			ind += ":" + rt.getTarget().getIndex();
		}
		return ind;
	}

	public void testTextSource() {
		TextSource source = new TextSource("file", "aa\nbb\n\nc".toCharArray(), 7);
		int[] expected = new int[] { 7, 7, 7, 8, 8, 8, 9, 10 };

		for (int i = 0; i < expected.length; i++) {
			Assert.assertEquals("offset #" + i, expected[i], source.lineForOffset(i));
		}
	}

	public void testCheckConflictsHandling() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_conflict1", openStream("syntax_conflict1", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Assert.assertNotNull(g);

		TestStatus er = new TestStatus(
				"",
				"syntax_conflict1,17: input: Licon\n" +
				"reduce/reduce conflict (next: fix1, fix2, fix3)\n" +
				"    input1 ::= Licon\n" +
				"    list_item ::= Licon\n" +
				"\n" +
				"conflicts: 0 shift/reduce and 1 reduce/reduce\n");
		LexicalBuilder.compile(g.getLexems(), er);
		Builder.compile(g, er);

		er.assertDone();
	}

	public void testCheckConflictsResolving() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_conflict2resolved", openStream("syntax_conflict2resolved", TESTCONTAINER), new TestStatus(),
				createDefaultTypesRegistry());
		Assert.assertNotNull(g);

		TestStatus er = new TestStatus(
				"",
				"", 0);
		LexicalBuilder.compile(g.getLexems(), er);
		Builder.compile(g, er);
		er.assertDone();

		er = new TestStatus("syntax_conflict2resolved,46: input: Lid '=' expr '*' expr\n" +
				"resolved as reduce conflict (next: '*', '+', '-', '/')\n" +
				"    expr ::= expr '*' expr\n" +
				"\n" +
				"syntax_conflict2resolved,48: input: Lid '=' expr '+' expr\n" +
				"resolved as shift conflict (next: '*', '/')\n" +
				"    expr ::= expr '+' expr\n" +
				"\n" +
				"syntax_conflict2resolved,48: input: Lid '=' expr '+' expr\n" +
				"resolved as reduce conflict (next: '+', '-')\n" +
				"    expr ::= expr '+' expr\n" +
				"\n" +
				"syntax_conflict2resolved,49: input: Lid '=' expr '-' expr\n" +
				"resolved as shift conflict (next: '*', '/')\n" +
				"    expr ::= expr '-' expr\n" +
				"\n" +
				"syntax_conflict2resolved,49: input: Lid '=' expr '-' expr\n" +
				"resolved as reduce conflict (next: '+', '-')\n" +
				"    expr ::= expr '-' expr\n" +
				"\n" +
				"syntax_conflict2resolved,47: input: Lid '=' expr '/' expr\n" +
				"resolved as reduce conflict (next: '*', '+', '-', '/')\n" +
				"    expr ::= expr '/' expr\n" +
				"\n", "", 1) {
			@Override
			public void debug(String info) {
				// ignore
			}
		};
		LexicalBuilder.compile(g.getLexems(), er);
		Builder.compile(g, er);

		er.assertDone();
	}
}
