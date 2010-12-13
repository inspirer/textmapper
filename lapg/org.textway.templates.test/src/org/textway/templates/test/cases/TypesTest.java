/**
 * Copyright 2002-2010 Evgeny Gryaznov
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

import org.junit.Assert;
import org.textway.templates.api.types.IClass;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.test.TemplateTestCase;
import org.textway.templates.types.TypesRegistry;

public class TypesTest extends TemplateTestCase {

	private static final String TEMPLATES_LOCATION = "org/textway/templates/test/ltp";

	private static final String TEMPLATES_CHARSET = "utf8";

	// eval.ltp
	public void testEval() {
		TestProblemCollector collector = new TestProblemCollector() {
			@Override
			public void fireError(ILocatedEntity referer, String error) {
				super.fireError(null, (referer != null ? referer.getLocation() + ": " : "") + error);
			}
		};

		collector.addErrors(
			"test1,14: two multiplicity constraints found (feature `name`)",
			"test1,16: only string type can have constraints (feature `term`)",
			"test1,23: trying to initialize unknown feature `a` in class `test1.Symbol`",
			"test1,24: expected value of type `string` instead of `int`",
			"test1,25: expected value of type `test1.Symbol[]` instead of literal",
			"test1,26: expected value of type `test1.Parser` instead of array",
			"test1,31: cannot instantiate `test1.Parser1`: class not found",
			"test1,32: `test1.Parser` is not a subtype of `test1.Symbol`"
		);

		ResourceRegistry resources = new ResourceRegistry(new ClassResourceLoader(getClass().getClassLoader(), TEMPLATES_LOCATION, TEMPLATES_CHARSET));
		TypesRegistry types = new TypesRegistry(resources, collector);

		IClass iClass = types.loadClass("test1.Defaults", null);
		Assert.assertNotNull(iClass);
		collector.assertEmptyErrors();
	}
}
