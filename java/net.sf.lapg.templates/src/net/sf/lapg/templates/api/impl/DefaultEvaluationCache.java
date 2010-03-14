package net.sf.lapg.templates.api.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.api.IEvaluationCache;

public class DefaultEvaluationCache implements IEvaluationCache {

	private final Map<CompositeKey, Object> globalCache = new HashMap<CompositeKey, Object>();

	@Override
	public void cache(Object value, Object... keys) {
		if(value == null) {
			return;
		}
		globalCache.put(new CompositeKey(keys), value);
	}

	@Override
	public Object lookup(Object... keys) {
		return globalCache.get(new CompositeKey(keys));
	}

	private static class CompositeKey {
		private final Object[] keys;

		public CompositeKey(Object[] keys) {
			this.keys = keys;
		}

		@Override
		public boolean equals(Object obj) {
			if(false == obj instanceof CompositeKey) {
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
		for(Object o : arr) {
			int currHash;
			if(o instanceof Object[]) {
				currHash = arrayHash((Object[]) o);
			} else {
				currHash = o != null ? o.hashCode() : 0;
			}
			hashCode = hashCode*37 + currHash;
		}
		return hashCode;
	}

	private static boolean arrayEquals(Object[] o1, Object[] o2) {
		if(o1 == o2) {
			return true;
		}
		if(o1.length != o2.length) {
			return false;
		}
		for (int i = 0; i < o1.length; i++) {
			if(!safeEquals(o1[i], o2[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean safeEquals(Object o1, Object o2) {
		if(o1 == o2) {
			return true;
		}
		if(o1 == null || o2 == null) {
			return o1 == o2;
		}
		if(o1 instanceof Object[] && o2 instanceof Object[]) {
			return arrayEquals((Object[])o1, (Object[]) o2);
		}
		return o1.equals(o2);
	}
}