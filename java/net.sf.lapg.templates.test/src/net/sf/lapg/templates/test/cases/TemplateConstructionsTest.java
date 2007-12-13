package net.sf.lapg.templates.test.cases;

import java.util.Hashtable;

import junit.framework.Assert;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.api.impl.ClassTemplateLoader;
import net.sf.lapg.templates.api.impl.DefaultNavigationFactory;
import net.sf.lapg.templates.api.impl.DefaultStaticMethods;
import net.sf.lapg.templates.test.TemplateTestCase;

public class TemplateConstructionsTest extends TemplateTestCase {

	private static final String TEMPLATES_LOCATION = "net/sf/lapg/templates/test/ltp";

	// loop.ltp
	public void testLoops() {
		Hashtable<String,String[]> h = new Hashtable<String,String[]>();
		h.put("list", new String[] { "a", "b" });

		IEvaluationEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));

		// test 1
		String q = env.executeTemplate("loop.loop1", new EvaluationContext(h), null);
		Assert.assertEquals("Hmm: \r\n\r\n0: a\r\n1: b\r\n\r\n", q);

		// test 2
		q = env.executeTemplate("loop.loop2", new EvaluationContext(h), null);
		Assert.assertEquals("\r\nHmm: \r\n\r\n0: a\r\n1: b\r\n\r\n", q);

		// test 3
		h.put("list", new String[] {});
		q = env.executeTemplate("loop.loop2", new EvaluationContext(h), null);
		Assert.assertEquals("\r\nHmm: \r\n\r\n\r\n", q);
	}

	// eval.ltp
	public void testEval() {
		IEvaluationEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));

		// test 1
		String q = env.executeTemplate("eval.eval1", null, null);
		Assert.assertEquals("w1 is bad\nw2 is bad\nt4 is bad\n", q);
	}

	// dollar.ltp
	public void testDollar() {
		TestEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));

		// test 1
		String q = env.executeTemplate("dollar.testdollar", null, null);
		Assert.assertEquals("$ is dollar\n", q);

		// test 2
		EvaluationContext context = new EvaluationContext(null);
		context.setVariable("$", "My");
		q = env.executeTemplate("dollar.testdollarvar", context, null);
		Assert.assertEquals("My is value\n", q);

		// test 3
		q = env.executeTemplate("dollar.testdollarindex", null, null);
		Assert.assertEquals("ww-yt-7\n", q);

		// test 4
		env.addErrors("Evaluation of `this[2]` failed for java.lang.Object[]: 2 is out of 0..1");
		q = env.executeTemplate("dollar.testdollarindexerr", null, null);
		env.assertEmptyErrors();
		Assert.assertEquals("ww-yt-\n", q);

		// test 5
		q = env.executeTemplate("dollar.testsharp", null, null);
		Assert.assertEquals("\n88 - 67\n99 - 45\n77 - 54\n\n", q);
	}

	// filter.ltp
	public void testMap() {
		IEvaluationEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));
		EvaluationContext context = new EvaluationContext(null);
		context.setVariable("util", new DefaultStaticMethods());

		// test 1
		String q = env.executeTemplate("filter.map1", context, null);
		Assert.assertEquals("[nbsss -> a3,ano -> yes,a45 -> 943]", q);
	}

	// arithm.ltp
	public void testArithm() {
		IEvaluationEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));

		// test 1
		String q = env.executeTemplate("arithm.arithm1", new EvaluationContext(null), null);
		Assert.assertEquals("\n2 = 2\n\n", q);

		// test 2
		q = env.executeTemplate("arithm.arithm2", new EvaluationContext(null), null);
		Assert.assertEquals("\n10 = 10\n\n", q);

		// test 3
		q = env.executeTemplate("arithm.arithm3", new EvaluationContext(null), null);
		Assert.assertEquals("\n-1 = -1\n\n", q);

		// test 4
		q = env.executeTemplate("arithm.arithm4", new EvaluationContext(null), null);
		Assert.assertEquals("2 = 2\n", q);

		// test 5
		q = env.executeTemplate("arithm.arithm5", new EvaluationContext(null), null);
		Assert.assertEquals("true false true true false -2\n", q);

		// test 6
		q = env.executeTemplate("arithm.arithm6", new EvaluationContext(null), null);
		Assert.assertEquals("uh: lite1\noh: okey\n", q);
	}

	// assert.ltp
	public void testAssert() {
		Hashtable<String,String[]> h = new Hashtable<String,String[]>();
		h.put("list", new String[] { "w1", "w2" });
		TestEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));

		// test 1
		env.addErrors("Evaluation of `l` failed for java.util.Hashtable: null");
		env.addErrors("Assertion `list.length > 5` failed for java.util.Hashtable");
		env.addErrors("Assertion `list[1] == 'w4'` failed for java.util.Hashtable");
		env.executeTemplate("assert.assertit", new EvaluationContext(h), null);
		env.assertEmptyErrors();
	}

	// TODO split call & format
	public void testCall() {
		Hashtable<String,String[]> h = new Hashtable<String,String[]>();
		h.put("list", new String[] { "a", "b" });
		IEvaluationEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));
		EvaluationContext context = new EvaluationContext(h);
		context.setVariable("util", new DefaultStaticMethods());

		// test 1
		String q = env.executeTemplate("format.callTempl", context, null);
		Assert.assertEquals("\r\nstatic int a[] {\r\n	0,\r\n1,\r\n2,\r\n3\r\n};\r\n\r\n", q);

		// test 2
		q = env.executeTemplate("format.useformat", context, null);
		Assert.assertEquals("\r\nstatic int a[] {\r\n	1,2,aa,4,5,\n6,7,8,9,10,\n11,12,13,14,\n15,16,17,19,\n20,21,22,23,\n24,25\r\n};\r\n\r\n", q);

		// test 3
		q = env.executeTemplate("format.useCall2", context, null);
		Assert.assertEquals("Table is mine\r\n", q);

		// test 4
		q = env.executeTemplate("format.useCall3", context, null);
		Assert.assertEquals("site is mine\r\n", q);
	}

	public void testOverrides() {
		Hashtable<String,String[]> h = new Hashtable<String,String[]>();
		h.put("list", new String[] { "a", "b" });
		IEvaluationEnvironment env = new TestEnvironment(new DefaultNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), TEMPLATES_LOCATION));
		EvaluationContext context = new EvaluationContext(h);
		context.setVariable("util", new DefaultStaticMethods());

		// test 1
		String q = env.executeTemplate("overrides.my1", context, null);
		Assert.assertEquals("my2\n", q);

		// test 2
		env.loadPackage(null, "overrides2");
		q = env.executeTemplate("overrides.my1", context, null);
		Assert.assertEquals("go next my2\n\n", q);
	}
}
