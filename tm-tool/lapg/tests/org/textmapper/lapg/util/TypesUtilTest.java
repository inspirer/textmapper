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
package org.textmapper.lapg.util;

import org.junit.Test;
import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.ast.AstClass;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.AstBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TypesUtilTest {
	@Test
	public void testAllSuperClasses() throws Exception {
		SimplifiedAstBuilder b = new SimplifiedAstBuilder();
		b.addExtends("b", "a");
		b.addExtends("c", "b");
		b.addExtends("d", "c");
		b.addExtends("e", "d");
		assertEquals("d, c, b, a", b.allSuperClasses("e"));

		b.addExtends("e", "a");
		assertEquals("d, a, c, b", b.allSuperClasses("e"));

		b.addExtends("e", "q");
		assertEquals("d, a, q, c, b", b.allSuperClasses("e"));
	}

	@Test
	public void testCommonSuperType() throws Exception {
		SimplifiedAstBuilder b = new SimplifiedAstBuilder();
		b.addExtends("Collection", "Object");
		b.addExtends("List", "Collection");
		b.addExtends("Queue", "Collection");
		b.addExtends("AbstractList", "List");
		b.addExtends("ArrayList", "AbstractList");
		b.addExtends("ArrayQueue", "AbstractList");

		assertEquals("AbstractList", b.joinType("AbstractList", "ArrayQueue"));
		assertEquals("AbstractList", b.joinType("ArrayList", "AbstractList"));
		assertEquals("AbstractList", b.joinType("ArrayList", "ArrayQueue"));
		assertEquals("Collection", b.joinType("ArrayList", "Queue"));
		assertEquals("Collection", b.joinType("List", "Queue"));
		assertEquals("Collection", b.joinType("Collection", "Collection"));

		assertEquals(AstType.ANY, TypesUtil.getJoinType(AstType.ANY, b.get("Collection")));
		assertEquals(AstType.ANY, TypesUtil.getJoinType(AstType.ANY, AstType.BOOL));
	}

	@Test
	public void testNoCommonSuperType() throws Exception {
		SimplifiedAstBuilder b = new SimplifiedAstBuilder();
		assertEquals("<none>", b.joinType("a", "b"));

		assertNull(TypesUtil.getJoinType(AstType.BOOL, AstType.STRING));
	}

	private class SimplifiedAstBuilder {
		Map<String, AstClass> map = new HashMap<>();
		AstBuilder builder = LapgCore.createAstBuilder();


		void addExtends(String cl, String baseClass) {
			builder.addExtends(get(cl), get(baseClass));
		}

		AstClass get(String name) {
			AstClass astClass = map.get(name);
			if (astClass == null) {
				astClass = builder.addClass(name, null, null);
				map.put(name, astClass);
			}
			return astClass;
		}

		String allSuperClasses(String name) {
			AstClass[] result = TypesUtil.getAllSuperClasses(get(name));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < result.length; i++) {
				if (i > 0) sb.append(", ");
				sb.append(result[i].getName());
			}
			return sb.toString();
		}

		String joinType(String t1, String t2) {
			AstType joinType = TypesUtil.getJoinType(get(t1), get(t2));
			return joinType != null ? joinType.toString() : "<none>";
		}
	}
}
