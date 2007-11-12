package net.sf.lapg.test.cases;

import java.io.InputStream;
import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.sf.lapg.api.Grammar;
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

	public void testCheckSimple() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1", openCase("syntax1"), new ErrorReporter(), new HashMap<String,String>());
		Assert.assertNotNull(g);
		Assert.assertEquals(0, g.getEoi());

		Symbol[] syms = g.getSymbols();
		Assert.assertEquals(7, syms.length);
		Assert.assertEquals("eoi", syms[0].getName());
		Assert.assertEquals("identifier", syms[1].getName());
		Assert.assertEquals("Licon", syms[2].getName());
		Assert.assertEquals("_skip", syms[3].getName());
		Assert.assertEquals("input", syms[4].getName());
		Assert.assertEquals("list", syms[5].getName());
		Assert.assertEquals("list_item", syms[6].getName());
	}
}
