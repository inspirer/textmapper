package net.sf.lapg.test.cases;

import java.io.InputStream;
import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.input.SyntaxUtil;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lex.LexicalBuilder;
import net.sf.lapg.test.TestNotifier;

public class InputTest extends TestCase {

	private static final String TESTCONTAINER = "net/sf/lapg/test/cases/input";
	private static final String RESULTCONTAINER = "net/sf/lapg/test/cases/expected";

	private InputStream openStream(String name, String root) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(root + "/" + name);
		Assert.assertNotNull(is);
		return is;
	}

	private static String removeSpaces(String input) {
		char[] c = new char[input.length()];
		input.getChars(0, input.length(), c, 0);

		int to = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] != ' ' && c[i] != '\t') {
				c[to++] = c[i];
			}
		}

		return new String(c, 0, to);
	}

	private void checkGenTables(Grammar g, String outputId, TestNotifier er) {
		LexerTables lt = LexicalBuilder.compile(g.getLexems(), er, 0);
		ParserTables pt = Builder.compile(g, er, 0);

		StringBuffer sb = new StringBuffer();

		OutputUtils.printTables(sb, lt);
		OutputUtils.printTables(sb, pt);

		String expected = removeSpaces(SyntaxUtil.getFileContents(openStream(outputId, RESULTCONTAINER)).trim());
		String actual = removeSpaces(sb.toString().trim());

		Assert.assertEquals(expected, actual);
	}

	public void testCheckSimple1() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1", openStream("syntax1", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi());

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
		Assert.assertEquals("list", rules[0].getRight()[0].getName());
		Assert.assertEquals(1, rules[0].getRight().length);

		Lexem[] lexems = g.getLexems();
		Assert.assertEquals(3, lexems.length);
		Assert.assertEquals("@?[a-zA-Z_][A-Za-z_0-9]*", lexems[0].getRegexp());
		Assert.assertEquals("([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?", lexems[1]
				.getRegexp());
		Assert.assertEquals("[\\t\\r\\n ]+", lexems[2].getRegexp());
		Assert.assertEquals(" continue; ", lexems[2].getAction().getContents());

		checkGenTables(g, "syntax1.tbl", new TestNotifier());
	}

	public void testCheckSimple2() {
		Grammar g = SyntaxUtil.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi());

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

		checkGenTables(g, "syntax2.tbl", new TestNotifier());
	}

	public void testCheckCSharpGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		TestNotifier er = new TestNotifier(
				"lapg: symbol `error` is useless\n"
						+ "lapg: symbol `Lfixed` is useless\n"
						+ "lapg: symbol `Lstackalloc` is useless\n"
						+ "lapg: symbol `comment` is useless\n"
						+ "lapg: symbol `'/*'` is useless\n"
						+ "lapg: symbol `anysym1` is useless\n"
						+ "lapg: symbol `'*/'` is useless\n"
						+ "\n"
						+ "input: using_directivesopt attributesopt modifiersopt Lclass ID class_baseopt '{' attributesopt modifiersopt operator_declarator '{' Lif '(' expression ')' embedded_statement\n"
						+ "conflict: shift/reduce (684, next Lelse)\n"
						+ "  embedded_statement ::= Lif '(' expression ')' embedded_statement\n",
				"conflicts: 1 shift/reduce and 0 reduce/reduce\n");

		checkGenTables(g, "syntax_cs.tbl", er);
		er.assertDone();

		Assert.assertTrue(g.getTemplates().startsWith("\n//#define DEBUG_syntax"));
	}

	public void testLapgGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		checkGenTables(g, "syntax_lapg.tbl", new TestNotifier());
	}

	public void testLapgTemplatesGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		checkGenTables(g, "syntax_tpl.tbl", new TestNotifier());
	}
}
