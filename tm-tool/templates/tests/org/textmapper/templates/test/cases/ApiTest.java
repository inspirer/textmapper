/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.templates.test.cases;

import org.junit.Test;
import org.textmapper.templates.api.IEvaluationCache;
import org.textmapper.templates.eval.DefaultEvaluationCache;
import org.textmapper.templates.eval.DefaultStaticMethods;

import static org.junit.Assert.assertEquals;

public class ApiTest {

	@Test
	public void testCache() {
		DefaultEvaluationCache cache = new DefaultEvaluationCache();
		cache.cache(3, 1, 2, 5);
		assertEquals(3, cache.lookup(1, 2, 5));
		assertEquals(IEvaluationCache.MISSED, cache.lookup(1, 2, 6));
		cache.cache(8, new Object[]{3, 4, 7}, 9);
		assertEquals(8, cache.lookup(new Object[]{3, 4, 7}, 9));
		assertEquals(IEvaluationCache.MISSED, cache.lookup(new Object[]{3, 5, 7}, 9));
	}

	@Test
	public void testIds() {
		DefaultStaticMethods util = new DefaultStaticMethods();
		assertEquals("CamelCase", util.toCamelCase("camel_case", true));
		assertEquals("camelCase", util.toCamelCase("camel_case", false));
		assertEquals("camelCase", util.toCamelCase("_camel_case", false));
		assertEquals("toDo", util.toCamelCase("_to_do_", false));
		assertEquals("ToDo", util.toCamelCase("_to_do_", true));
	}
}
