package net.sf.lapg.test.cases;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import junit.framework.Assert;
import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lex.LexicalBuilder;
import net.sf.lapg.lex.RegexpParser;
import net.sf.lapg.parser.LiGrammar;
import net.sf.lapg.parser.LiRule;
import net.sf.lapg.parser.LiSymbol;
import net.sf.lapg.parser.SyntaxUtil;
import net.sf.lapg.test.TestNotifier;
import net.sf.lapg.test.oldparser.SyntaxUtilOld;

public class InputTest extends LapgTestCase {

	private void checkGenTables(Grammar g, String outputId, TestNotifier er) {
		LexerTables lt = LexicalBuilder.compile(g.getLexems(), er, 0);
		ParserTables pt = Builder.compile(g, er, 0);

		StringBuffer sb = new StringBuffer();

		OutputUtils.printTables(sb, lt);
		OutputUtils.printTables(sb, pt);

		String expected = removeSpaces(SyntaxUtilOld.getFileContents(openStream(outputId, RESULTCONTAINER)).trim());
		String actual = removeSpaces(sb.toString().trim());

		Assert.assertEquals(expected, actual);
	}

	private static char[] HEX = new char[] { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

	private String toHex4(int i) {
		return "" + HEX[i>>12&0xf] + HEX[i>>8&0xf] +HEX[i>>4&0xf]+HEX[i&0xf];
	}

	public void testRegexpParserTime() {

		Assert.assertEquals("0000", toHex4(0));
		Assert.assertEquals("abcf", toHex4(0xabcf));
		Assert.assertEquals("75e1", toHex4(0x75e1));

		RegexpParser rp = new RegexpParser(null);
		for(int i = 0; i < 2000; i++) {
			rp.compile(i, "n"+i, "[\\x"+toHex4(i*10)+"-\\x"+toHex4(i*10+6)+"]");
		}
		for(int i = 0; i < 2000; i++) {
			rp.compile(i, "nb"+i, "[\\x"+toHex4(i*10+3)+"-\\x"+toHex4(i*10+8)+"]");
		}
		rp.buildSets();

		Assert.assertEquals(6002, rp.getSymbolCount());
	}

	public void testRegexpParserInvertedSet() {
		RegexpParser rp = new RegexpParser(null);

		rp.compile(0, "string", "'[^'\n]+'");
		rp.compile(1, "percent", "%");
		rp.buildSets();
		Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));

		int[] character2sym = rp.getCharacterMap();
		Assert.assertEquals(character2sym['%'], 3);
		Assert.assertEquals(character2sym['\''], 2);

		int[][] res = rp.getSetToSymbolsMap();
		Assert.assertEquals(1, res.length);
		Assert.assertEquals("[1, 3]", Arrays.toString(res[0]));
	}

	public void testRegexpParser() {
		RegexpParser rp = new RegexpParser(null);

		rp.compile(0, "string", "[a-zA-Z_][a-zA-Z0-9_]*");
		rp.buildSets();
		Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));

		rp = new RegexpParser(null);
		rp.compile(0, "string", "[a-zA-Z_][a-zA-Z0-9_]*");
		rp.compile(1, "keyw", "do");
		rp.buildSets();
		Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 4, 1, 4, 4, 4, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));

		rp = new RegexpParser(null);
		rp.compile(0, "string", "[a-w][p-z]");
		rp.compile(1, "string2", "[b-c][y-z]");
		rp.buildSets();
		Assert.assertEquals("[0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 3, 6, 6, 1, 1, 1, 1, 1]", Arrays.toString(rp.getCharacterMap()));
		int[][] p = rp.getSetToSymbolsMap();
		Assert.assertEquals("[2, 4, 5]", Arrays.toString(p[0]));
		Assert.assertEquals("[3, 4, 6]", Arrays.toString(p[1]));
		Assert.assertEquals("[5]", Arrays.toString(p[2]));
		Assert.assertEquals("[6]", Arrays.toString(p[3]));
	}

	public void testUnicode() {
		RegexpParser rp = new RegexpParser(null);

		rp.compile(0, "string", "[\\x5151-\\x5252][\\x1000-\\x2000]");
		int[] expected = new int[0x8000];
		Arrays.fill(expected, 1);
		Arrays.fill(expected, 0x5151, 0x5252+1, 2);
		Arrays.fill(expected, 0x1000, 0x2000+1, 3);
		expected[0] = 0;
		rp.buildSets();
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(rp.getCharacterMap()));
	}

	public void testCheckSimple1() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax1", openStream("syntax1", TESTCONTAINER), new TestNotifier(),
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

		checkGenTables(g, "syntax1.tbl", new TestNotifier());
	}

	public void testCheckSimple2() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestNotifier(),
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

		checkGenTables(g, "syntax2.tbl", new TestNotifier());
	}

	public void testCheckCSharpGrammar() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestNotifier(),
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
		Grammar g = SyntaxUtilOld.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		checkGenTables(g, "syntax_lapg.tbl", new TestNotifier());
	}

	public void testLapgTemplatesGrammar() {
		Grammar g = SyntaxUtilOld.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		checkGenTables(g, "syntax_tpl.tbl", new TestNotifier());
	}

	public void testHexConverter() {
		Assert.assertEquals(0, RegexpParser.parseHex("0"));
		Assert.assertEquals(10, RegexpParser.parseHex("a"));
		Assert.assertEquals(11, RegexpParser.parseHex("b"));
		Assert.assertEquals(12, RegexpParser.parseHex("C"));
		Assert.assertEquals(16, RegexpParser.parseHex("10"));
		Assert.assertEquals(39664, RegexpParser.parseHex("9aF0"));
		try {
			Assert.assertEquals(39664, RegexpParser.parseHex("9aF0!"));
			Assert.fail("no exception");
		} catch(Throwable th) {
			Assert.assertTrue(th instanceof NumberFormatException);
		}
		try {
			Assert.assertEquals(39664, RegexpParser.parseHex("g"));
			Assert.fail("no exception");
		} catch(Throwable th) {
			Assert.assertTrue(th instanceof NumberFormatException);
		}
		try {
			Assert.assertEquals(39664, RegexpParser.parseHex("G"));
			Assert.fail("no exception");
		} catch(Throwable th) {
			Assert.assertTrue(th instanceof NumberFormatException);
		}
	}

	public void testNewTemplatesGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, Object>());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax_tpl", openStream("syntax_tpl", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		sortGrammar((LiGrammar)g, go);
		checkGenTables(g, "syntax_tpl.tbl", new TestNotifier());
	}

	public void testNewLapgGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, Object>());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax_lapg", openStream("syntax_lapg", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);

		sortGrammar((LiGrammar)g, go);
		checkGenTables(g, "syntax_lapg.tbl", new TestNotifier());
	}

	public void testNewCheckCSharpGrammar() {
		Grammar g = SyntaxUtil.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, Object>());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax_cs", openStream("syntax_cs", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, String>());
		Assert.assertNotNull(g);
		sortGrammar((LiGrammar)g, go);

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

	public void testNewCheckSimple1() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1", openStream("syntax1", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, Object>());
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

		checkGenTables(g, "syntax1.tbl", new TestNotifier());
	}

	public void testNewCheckSimple2() {
		Grammar g = SyntaxUtil.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestNotifier(),
				new HashMap<String, Object>());
		Grammar go = SyntaxUtilOld.parseSyntax("syntax2", openStream("syntax2", TESTCONTAINER), new TestNotifier(),
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

		sortGrammar((LiGrammar)g, go);
		checkGenTables(g, "syntax2.tbl", new TestNotifier());
	}

	private void sortGrammar(LiGrammar g, Grammar go) {
		final HashMap<String,Integer> index = new HashMap<String, Integer>();
		for(Symbol s : go.getSymbols()) {
			index.put(s.getName(), s.getIndex());
		}
		final HashMap<String,Integer> ruleind = new HashMap<String, Integer>();
		for(Rule r : go.getRules()) {
			String ind = getSignature(r);
			ruleind.put(ind, r.getIndex());
		}

		Arrays.sort(g.getSymbols(), new Comparator<Symbol>() {
			@Override
			public int compare(Symbol o1, Symbol o2) {
				Integer i1 = index.get(o1.getName());
				Integer i2 = index.get(o2.getName());
				return i1.compareTo(i2);
			}
		});
		for(int i = 0; i < g.getSymbols().length; i++) {
			((LiSymbol)g.getSymbols()[i]).setIndex(i);
		}

		Arrays.sort(g.getRules(), new Comparator<Rule>() {
			@Override
			public int compare(Rule o1, Rule o2) {
				Integer i1 = ruleind.get(getSignature(o1));
				Integer i2 = ruleind.get(getSignature(o2));
				return i1.compareTo(i2);
			}
		});
		for(int i = 0; i < g.getRules().length; i++) {
			((LiRule)g.getRules()[i]).setIndex(i);
		}
	}

	private String getSignature(Rule r) {
		String ind = Integer.toString(r.getLeft().getIndex());
		for(SymbolRef rt : r.getRight()) {
			ind += ":" + rt.getTarget().getIndex();
		}
		return ind;
	}
}
