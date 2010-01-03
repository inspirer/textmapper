package net.sf.lapg.test.cases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.parser.SyntaxUtil;
import net.sf.lapg.test.TestNotifier;

public class AnnotationsTest extends LapgTestCase {

	public void testAllAnnotations() {
		Grammar g = SyntaxUtil.parseSyntax("syntax1annotated", openStream("syntax1annotated", TESTCONTAINER),
				new TestNotifier(), new HashMap<String, String>());
		Assert.assertNotNull(g);

		Rule[] listItemRules = rulesForName(g.getRules(), "list_item");
		Assert.assertEquals(2, listItemRules.length);

		Symbol s = listItemRules[0].getLeft();
		Assert.assertEquals(5, s.getAnnotation("weight"));
		Assert.assertEquals("wwo", s.getAnnotation("name"));

		Assert.assertEquals("rule1", listItemRules[0].getAnnotation("name"));
		Assert.assertEquals("rule2", listItemRules[1].getAnnotation("name"));

		Object val = listItemRules[0].getRight()[0].getAnnotation("ids");
		Assert.assertTrue(val instanceof List<?>);

		List<?> list = (List<?>) val;
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(4, list.get(0));
		Assert.assertEquals(2, list.get(1));
		Assert.assertEquals(3, list.get(2));

		Rule[] inputRules = rulesForName(g.getRules(), "input");
		Assert.assertEquals(1, inputRules.length);
		Symbol input = inputRules[0].getLeft();

		Assert.assertNotNull(input);
		Object refval = input.getAnnotation("ref");
		Assert.assertNotNull(refval);
		Assert.assertTrue(refval instanceof Symbol);
		Assert.assertTrue(refval == s);
	}

	public void testBadAnnotations() {
		TestNotifier notifier = new TestNotifier("", "notexistingsym cannot be resolved\n"
				+ "redeclaration of annotation `name' for non-terminal: tempanno, skipped\n");
		Grammar g = SyntaxUtil.parseSyntax("syntax1errannotated", openStream("syntax1errannotated", TESTCONTAINER),
				notifier, new HashMap<String, String>());
		Assert.assertNull(g);
	}

	private static Rule[] rulesForName(Rule[] rules, String name) {
		ArrayList<Rule> result = new ArrayList<Rule>();
		for (Rule r : rules) {
			Assert.assertNotNull(r.getLeft());
			Assert.assertNotNull(r.getLeft().getName());
			if (r.getLeft().getName().equals(name)) {
				result.add(r);
			}
		}
		return result.toArray(new Rule[result.size()]);
	}
}
