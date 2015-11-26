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
package org.textmapper.lapg.util;

import org.textmapper.lapg.api.ast.AstClass;
import org.textmapper.lapg.api.ast.AstType;

import java.util.*;

public class TypesUtil {

	/**
	 * Returns transitive closure of all superclasses of c.
	 */
	static AstClass[] getAllSuperClasses(AstClass c) {
		LinkedList<AstClass> queue = new LinkedList<>();
		Set<AstClass> seen = new LinkedHashSet<>();
		queue.addAll(Arrays.asList(c.getSuper()));
		seen.addAll(queue);
		AstClass next;
		while ((next = queue.poll()) != null) {
			for (AstClass s : next.getSuper()) {
				if (seen.add(s)) queue.add(s);
			}
		}
		return seen.toArray(new AstClass[seen.size()]);
	}

	/**
	 * Returns a superclass of both c1 and c2.
	 */
	static AstClass getCommonSuperClass(AstClass c1, AstClass c2) {
		AstClass[] c1Super = getAllSuperClasses(c1);
		AstClass[] c2Super = getAllSuperClasses(c2);
		if (c1Super.length == 0 || c2Super.length == 0) return null;

		Map<AstClass, Integer> map = new HashMap<>();
		for (int i = 0; i < c1Super.length; i++) {
			map.put(c1Super[i], i);
		}
		int bestScore = Integer.MAX_VALUE;
		AstClass result = null;
		for (int i = 0; i < c2Super.length; i++) {
			if (!map.containsKey(c2Super[i])) continue;

			int score = map.get(c2Super[i]) + i;
			if (score < bestScore) {
				bestScore = score;
				result = c2Super[i];
			}
		}
		return result;
	}

	/**
	 * Note: returns null instead of AstType.ANY if there is no common supertype.
	 */
	public static AstType getJoinType(AstType t1, AstType t2) {
		if (t1.isSubtypeOf(t2)) {
			return t2;
		}
		if (t2.isSubtypeOf(t1)) {
			return t1;
		}
		if (t1 instanceof AstClass && t2 instanceof AstClass) {
			return getCommonSuperClass((AstClass) t1, (AstClass) t2);
		}
		return null;
	}

}
