package net.sf.lapg.templates.test.cases;

import junit.framework.Assert;
import net.sf.lapg.templates.api.impl.DefaultEvaluationCache;
import net.sf.lapg.templates.api.impl.DefaultStaticMethods;
import net.sf.lapg.templates.test.TemplateTestCase;

public class ApiTests extends TemplateTestCase {

	public void testCache() {
		DefaultEvaluationCache cache = new DefaultEvaluationCache();
		cache.cache(3, 1, 2, 5);
		Assert.assertEquals(3, cache.lookup(1, 2, 5));
		Assert.assertEquals(null, cache.lookup(1, 2, 6));
		cache.cache(8, new Object[] { 3,4,7}, 9);
		Assert.assertEquals(8, cache.lookup(new Object[] { 3,4,7}, 9));
		Assert.assertEquals(null, cache.lookup(new Object[] { 3,5,7}, 9));
	}

	public void testIds() {
		DefaultStaticMethods util = new DefaultStaticMethods();
		Assert.assertEquals("CamelCase", util.toCamelCase("camel_case", true));
		Assert.assertEquals("camelCase", util.toCamelCase("camel_case", false));
		Assert.assertEquals("camelCase", util.toCamelCase("_camel_case", false));
		Assert.assertEquals("toDo", util.toCamelCase("_to_do_", false));
		Assert.assertEquals("ToDo", util.toCamelCase("_to_do_", true));
	}
}
