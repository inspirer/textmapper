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
package org.textmapper.templates.eval;

import org.textmapper.templates.api.IEvaluationCache;

import java.util.HashMap;
import java.util.Map;

public class DefaultEvaluationCache implements IEvaluationCache {

	private final Map<CompositeKey, Object> globalCache = new HashMap<>();

	@Override
	public void cache(Object value, Object... keys) {
		globalCache.put(new CompositeKey(keys), value);
	}

	@Override
	public Object lookup(Object... keys) {
		Object result = globalCache.get(new CompositeKey(keys));
		if (result != null) {
			return result;
		}
		return globalCache.containsKey(new CompositeKey(keys)) ? null : MISSED;
	}

	private static class CompositeKey {
		private final Object[] keys;

		public CompositeKey(Object[] keys) {
			this.keys = keys;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof CompositeKey)) {
				return false;
			}
			CompositeKey o = (CompositeKey) obj;
			return arrayEquals(keys, o.keys);
		}

		@Override
		public int hashCode() {
			return arrayHash(keys);
		}
	}

	private static int arrayHash(Object[] arr) {
		int hashCode = arr.length;
		for (Object o : arr) {
			int currHash;
			if (o instanceof Object[]) {
				currHash = arrayHash((Object[]) o);
			} else {
				currHash = o != null ? o.hashCode() : 0;
			}
			hashCode = hashCode * 37 + currHash;
		}
		return hashCode;
	}

	private static boolean arrayEquals(Object[] o1, Object[] o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1.length != o2.length) {
			return false;
		}
		for (int i = 0; i < o1.length; i++) {
			if (!safeEquals(o1[i], o2[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean safeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (o1 instanceof Object[] && o2 instanceof Object[]) {
			return arrayEquals((Object[]) o1, (Object[]) o2);
		}
		return o1.equals(o2);
	}
}