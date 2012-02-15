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
package org.textway.lapg.test.cases;

import org.junit.Test;
import org.textway.lapg.common.JavaPostProcessor;

import static org.junit.Assert.assertEquals;

public class JavaPostProcessorTest {

	@Test
	public void testSimple() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
						"\n" +
						"import xxx.A;\n" +
						"\n" +
						"class B extends xxx.@C {}").process();
		assertEquals(
				"package p;\n" +
						"\n" +
						"import xxx.A;\n" +
						"import xxx.C;\n" +
						"\n" +
						"class B extends C {}", res);
	}

	@Test
	public void testSeveral() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
						"\n" +
						"import xxx.A;\n" +
						"\n" +
						"class B extends xxx.@C implements yyy.@QQ {}").process();
		assertEquals(
				"package p;\n" +
						"\n" +
						"import xxx.A;\n" +
						"import xxx.C;\n" +
						"import yyy.QQ;\n" +
						"\n" +
						"class B extends C implements QQ {}", res);
	}

	@Test
	public void testConflict() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
						"\n" +
						"import xxx.A;\n" +
						"\n" +
						"class B extends aaa.@A implements qqq.@A {}").process();
		assertEquals(
				"package p;\n" +
						"\n" +
						"import xxx.A;\n" +
						"\n" +
						"class B extends aaa.A implements qqq.A {}", res);
	}

	@Test
	public void testNoImports() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
						"\n" +
						"class B extends aaa.@A implements qqq.@B {}").process();
		assertEquals(
				"package p;\n" +
						"\n" +
						"import aaa.A;\n" +
						"import qqq.B;\n" +
						"\n" +
						"class B extends A implements B {}", res);
	}
}
