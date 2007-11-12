package net.sf.lapg.test.cases;

import java.io.InputStream;
import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.input.SyntaxUtil;
import net.sf.lapg.test.ErrorReporter;

public class InputTest extends TestCase {

	private static final String TESTCONTAINER = "net/sf/lapg/test/cases/input";

	private InputStream openCase(String name) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(TESTCONTAINER + "/" + name);
		Assert.assertNotNull(is);
		return is;
	}

	public void testCheckSimple1() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1", openCase("syntax1"), new ErrorReporter(), new HashMap<String,String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi());

		Symbol[] syms = g.getSymbols();
		Assert.assertEquals(7, syms.length);
		Assert.assertEquals("eoi", syms[0].getName());
		Assert.assertEquals("identifier", syms[1].getName());
		Assert.assertEquals("Licon", syms[2].getName());
		Assert.assertEquals("_skip", syms[3].getName());  // TODO do not collect skip symbols
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
		Assert.assertEquals("([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?", lexems[1].getRegexp());
		Assert.assertEquals("[\\t\\r\\n ]+", lexems[2].getRegexp());
		Assert.assertEquals(" continue; ", lexems[2].getAction());
	}

	public void testCheckSimple2() {
		Grammar g = SyntaxUtil.parseSyntax("syntax2", openCase("syntax2"), new ErrorReporter(), new HashMap<String,String>());
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
		Assert.assertEquals("  ${for a in b}..!..$$  ", g.getRules()[7].getAction());
	}
}
