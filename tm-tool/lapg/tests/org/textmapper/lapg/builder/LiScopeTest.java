/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.junit.Test;
import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.NamedElement;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LiScopeTest {

	@Test
	public void testNewName() throws Exception {
		LiScope<NameOnly> scope = new LiScope<>();
		NameOnly ab = new NameOnly("ab");
		assertTrue(scope.insert(ab, null));
		assertTrue(scope.insert(new NameOnly("bc"), null));

		assertEquals(ab, scope.resolve("ab"));
		assertNotNull(scope.resolve("bc"));
		assertNull(scope.resolve("cd"));

		assertEquals("qq", scope.newName("qq"));
		assertEquals("ab1", scope.newName("ab"));
		assertEquals("ab1", scope.newName("ab"));
		assertEquals("ab1", scope.newName("ab1"));

		assertTrue(scope.insert(new NameOnly("ab1"), null));
		assertEquals("ab2", scope.newName("ab"));
		assertEquals("ab2", scope.newName("ab"));
		assertEquals("ab11", scope.newName("ab1"));
	}

	@Test
	public void testReserve() throws Exception {
		LiScope<NameOnly> scope = new LiScope<>();
		add(scope, "abc");
		assertFalse(scope.reserve("abc"));
		assertTrue(scope.reserve("foo"));
		assertFalse(scope.insert(new NameOnly("foo"), null));
		NameOnly[] result = scope.toArray(NameOnly[]::new);
		assertEquals(1, result.length);
		assertEquals("abc", result[0].getNameText());
	}

	@Test
	public void testParent() throws Exception {
		LiScope<NameOnly> parent = new LiScope<>();
		LiScope<NameOnly> scope = new LiScope<>(parent);

		NameOnly aa = add(parent, "aa");
		assertEquals(aa, scope.resolve("aa"));
		assertFalse(parent.insert(new NameOnly("aa"), null));

		NameOnly aa2 = add(scope, "aa");
		assertEquals(aa, parent.resolve("aa"));
		assertEquals(aa2, scope.resolve("aa"));

		assertFalse(parent.insert(new NameOnly("aa"), null));
		assertFalse(scope.insert(new NameOnly("aa"), null));
	}

	@Test
	public void testOrdering() throws Exception {
		LiScope<NameOnly> scope = new LiScope<>();
		NameOnly qef = add(scope, "qef");
		NameOnly abc = add(scope, "abc");

		addAfter(scope, "qef000", qef);
		addAfter(scope, "qef900", qef);
		addAfter(scope, "qef200", qef);
		addAfter(scope, "qef800", qef);

		NameOnly zxy = add(scope, "zxy");
		NameOnly foo = add(scope, "foo");
		add(scope, "bar");
		add(scope, "last");

		addAfter(scope, "aaa_bc1", abc);
		addAfter(scope, "zzzzzzz", abc);

		addAfter(scope, "zxyopt", zxy);

		addAfter(scope, "foo_b_type", foo);
		addAfter(scope, "foo_a_type_1", foo);
		addAfter(scope, "foo_a_type", foo);
		addAfter(scope, "foo_a_type_2", foo);

		scope.sort();

		List<String> result = scope.elements().stream()
				.map(NameOnly::getNameText)
				.collect(Collectors.toList());

		assertArrayEquals(new String[]{
				"qef", "qef000", "qef200", "qef800", "qef900",
				"aaa_bc1", "abc", "zzzzzzz",
				"zxy", "zxyopt",
				"foo", "foo_a_type", "foo_a_type_1", "foo_a_type_2", "foo_b_type",
				"bar", "last"
		}, result.toArray(new String[result.size()]));
	}

	static NameOnly add(LiScope<NameOnly> s, String name) {
		NameOnly res = new NameOnly(name);
		assertTrue(s.insert(res, null));
		return res;
	}

	static NameOnly addAfter(LiScope<NameOnly> s, String name, NameOnly anchor) {
		NameOnly res = new NameOnly(name);
		assertTrue(s.insert(res, anchor));
		return res;
	}

	private static class NameOnly implements NamedElement {
		private Name name;

		public NameOnly(String name) {
			this.name = LapgCore.name(name);
		}

		@Override
		public Name getName() {
			return name;
		}

		@Override
		public String getNameText() {
			return name.text();
		}

		@Override
		public String toString() {
			return name.text();
		}
	}
}