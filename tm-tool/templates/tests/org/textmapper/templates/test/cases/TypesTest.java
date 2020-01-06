/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;

import static org.junit.Assert.assertNotNull;

public class TypesTest {

	private static final String TEMPLATES_LOCATION = "org/textmapper/templates/test/ltp";

	private static final String TEMPLATES_CHARSET = "utf8";

	// eval.ltp
	@Test
	public void testEval() {
		TestProblemCollector collector = new TestProblemCollector();

		collector.addErrors(
				"test1.types,14: several multiplicity constraints found (feature `name`)",
				"test1.types,15: cannot combine 1 or 0..1 with other multiplicities",
				"test1.types,17: only string type can have constraints (feature `term`)",
				"test1.types,24: trying to initialize unknown feature/method `a` in class `test1.Symbol`",
				"test1.types,25: expected value of type `string` instead of `int`",
				"test1.types,26: expected value of type `test1.Symbol[]` instead of literal",
				"test1.types,27: expected value of type `test1.Parser` instead of array",
				"test1.types,32: cannot instantiate `test1.Parser1`: class not found",
				"test1.types,33: `test1.Parser` is not a subtype of `test1.Symbol`"
		);

		ResourceRegistry resources = new ResourceRegistry(new ClassResourceLoader(getClass().getClassLoader(), TEMPLATES_LOCATION, TEMPLATES_CHARSET));
		TypesRegistry types = new TypesRegistry(resources, collector);

		IClass iClass = types.getClass("test1.Defaults", null);
		assertNotNull(iClass);
		collector.assertEmptyErrors();
	}
}
