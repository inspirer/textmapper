/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.templates.test.cases;

import junit.framework.Assert;
import org.textway.templates.api.IEvaluationCache;
import org.textway.templates.eval.DefaultEvaluationCache;
import org.textway.templates.eval.DefaultStaticMethods;
import org.textway.templates.test.TemplateTestCase;

public class ApiTests extends TemplateTestCase {

	public void testCache() {
		DefaultEvaluationCache cache = new DefaultEvaluationCache();
		cache.cache(3, 1, 2, 5);
		Assert.assertEquals(3, cache.lookup(1, 2, 5));
		Assert.assertEquals(IEvaluationCache.MISSED, cache.lookup(1, 2, 6));
		cache.cache(8, new Object[] { 3,4,7}, 9);
		Assert.assertEquals(8, cache.lookup(new Object[] { 3,4,7}, 9));
		Assert.assertEquals(IEvaluationCache.MISSED, cache.lookup(new Object[] { 3,5,7}, 9));
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
