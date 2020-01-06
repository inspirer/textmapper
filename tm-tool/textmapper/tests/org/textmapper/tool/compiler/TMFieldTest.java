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
package org.textmapper.tool.compiler;

import org.junit.Test;

import static org.junit.Assert.*;

public class TMFieldTest {

	@Test
	public void mergeUnnamed() throws Exception {
		// Mergeable types.
		TMField f1 = new TMField("type1");
		TMField f2 = new TMField("type2");
		TMField f3 = new TMField("type3");

		TMField result = TMField.merge("abc", f1, f2, f3);
		assertNotNull(result);
		assertEquals("abc=(type1 | type2 | type3)", result.toString());
		assertFalse(result.hasExplicitName());

		result = result.withName("a");
		assertEquals("a=(type1 | type2 | type3)", result.toString());
		assertTrue(result.hasExplicitName());

		// If all names are equal, name hint is ignored.
		f1 = f1.withName("type");
		f2 = f2.withName("type");
		f3 = f3.withName("type");

		result = TMField.merge("q", f1, f2, f3);
		assertNotNull(result);
		assertEquals("type=(type1 | type2 | type3)", result.toString());
		assertTrue(result.hasExplicitName());

		// Same field multiple times is fine.
		f2 = f1.withName("type");
		f3 = f1.withName("type");

		result = TMField.merge("q", f1, f2, f3);
		assertNotNull(result);
		assertEquals("type=type1", result.toString());
		assertTrue(result.hasExplicitName());

		result = TMField.merge("q", f1, f2, f3).withName("abc");
		assertNotNull(result);
		assertEquals("abc=type1", result.toString());
		assertTrue(result.hasExplicitName());
	}

	@Test
	public void mergeNamed() throws Exception {
		// Types with explicit names.
		TMField f1 = new TMField("type1").withName("type");
		TMField f2 = new TMField("type2").withName("type");
		TMField f3 = new TMField("type3");

		TMField result = TMField.merge("abc", f1, f2, f3);
		assertNotNull(result);
		assertEquals("abc=(type1 | type2 | type3)", result.toString());
		assertFalse(result.hasExplicitName());

		// Hint is ignored.
		result = TMField.merge("abc", f1, f2);
		assertNotNull(result);
		assertEquals("type=(type1 | type2)", result.toString());
		assertTrue(result.hasExplicitName());
	}

	@Test
	public void mergeOrTypes() throws Exception {
		// Types with explicit names.
		TMField f1 = new TMField("type1");
		TMField f2 = new TMField("type2");
		TMField f3 = new TMField("type3");

		TMField f12 = TMField.merge("abc", f1, f2);
		assertEquals("abc=(type1 | type2)", f12.toString());

		TMField f23 = TMField.merge("abc", f2, f3).withName("f23");
		assertEquals("f23=(type2 | type3)", f23.toString());

		TMField f123 = TMField.merge("abc", f12, f23).makeList().withName("f123");
		assertEquals("f123=(type1 | type2 | type3)+", f123.toString());

		TMField f123opt = TMField.merge("abc", f12, f23.makeNullable()).withName("f123opt");
		assertEquals("f123opt=(type1 | type2 | type3)?", f123opt.toString());

		TMField f123star = TMField.merge("abc", f12, f23.makeNullable()).makeList().withName("f123star");
		assertEquals("f123star=(type1 | type2 | type3)*", f123star.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotMergeEmptyList() throws Exception {
		TMField.merge("abc");
	}

	@Test(expected = IllegalStateException.class)
	public void unnamedMerge() throws Exception {
		TMField f1 = new TMField("type1");
		TMField f2 = new TMField("type2");

		TMField.merge(null, f1, f2);
	}
}
