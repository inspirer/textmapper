/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
package org.textmapper.tool.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class GoPostProcessorTest {

	@Test
	public void simple() throws Exception {
		assertEquals("", new GoPostProcessor("").process());
		assertEquals("abcd a.b", new GoPostProcessor("abcd a.b").process());
		assertEquals(
				"package b\n\n" +
						"import (\n" +
						"\t\"abc\"\n" +
						")\n\n" +
						"func abc() []abc.def {}",
				new GoPostProcessor("package b\n\n" +
						"func abc() []\"abc\".def {}").process());

		assertEquals(
				"package b\n\n" +
						"import (\n" +
						"\t\"abc/ddd\"\n" +
						")\n\n" +
						"func abc() []ddd.def {}",
				new GoPostProcessor("package b\n\n" +
						"func abc() []\"abc/ddd\".def {}").process());

		assertEquals(
				"package b\n\n" +
						"import (\n" +
						"\tp \"abc/ddd\"\n" +
						")\n\n" +
						"func abc() []p.def {}",
				new GoPostProcessor("package b\n\n" +
						"func abc() []\"abc/ddd as p\".def {}").process());

		assertEquals(
				"package b\n\n" +
						"import (\n" +
						"\tp \"abc/ddd\"\n" +
						")\n\n" +
						"func abc(i p.xyz) []p.def {}",
				new GoPostProcessor("package b\n\n" +
						"func abc(i \"abc/ddd as p\".xyz) []\"abc/ddd as p\".def {}").process());

		assertEquals(
				"package b\n\n" +
						"import (\n" +
						"\t\"abc/def\"\n" +
						"\n" +
						"\tp \"abc/ddd\"\n" +
						")\n\n" +
						"func abc(i def.xyz) []p.def {}",
				new GoPostProcessor("package b\n\n" +
						"func abc(i \"abc/def\".xyz) []\"abc/ddd as p\".def {}").process());
	}

	@Test
	public void localReference() throws Exception {
		assertEquals(
				"package b\n\n",
				new GoPostProcessor("package abc/def/b  \n").process());

		assertEquals(
				"package b\n\nfunc abc() []def {}",
				new GoPostProcessor("package b  \n" +
						"func abc() []\"b\".def {}").process());

		assertEquals(
				"package b\n\n" +
						"func abc() []def {}",
				new GoPostProcessor("package abc/def/b  \n\n" +
						"func abc() []\"abc/def/b\".def {}").process());

		assertEquals(
				"// abc\n//   def \n\n" +
				"package b\n\n" +
						"func abc() []def {}",
				new GoPostProcessor("// abc\n//   def \n\n"+
						"package abc/def/b  \n\n" +
						"func abc() []\"abc/def/b as q\".def {}").process());
	}
}
