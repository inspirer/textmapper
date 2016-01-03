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

import org.textmapper.lapg.api.NamedElement;
import org.textmapper.lapg.api.Scope;

import java.util.*;

class LiScope<T extends NamedElement> implements Scope<T> {

	private final LiScope<T> parent;
	private final Map<String, T> elementMap = new HashMap<>();
	private final Map<T, Integer> elementIndex = new HashMap<>();
	private final Map<String, Integer> lastIndex = new HashMap<>();
	private List<T> cache;

	public LiScope(LiScope<T> parent) {
		this.parent = parent;
	}

	@Override
	public T resolve(String name) {
		T result = elementMap.get(name);
		if (result != null) return result;

		return parent == null ? null : parent.resolve(name);
	}

	@Override
	public String newName(String baseName) {
		if (!elementMap.containsKey(baseName)) return baseName;

		int index = lastIndex.containsKey(baseName) ? lastIndex.get(baseName) : 1;
		String name = baseName + index;
		while (elementMap.containsKey(name)) {
			name = baseName + (++index);
		}
		lastIndex.put(baseName, index);
		return name;
	}

	@Override
	public boolean insert(T element, T anchor) {
		cache = null;

		String name = element.getName();
		if (elementMap.containsKey(name)) return false;

		elementMap.put(name, element);

		int index;
		if (anchor != null) {
			if (!elementIndex.containsKey(anchor)) {
				throw new IllegalArgumentException("unknown anchor");
			}
			index = elementIndex.get(anchor);
		} else {
			index = elementMap.size();
		}
		elementIndex.put(element, index);
		return true;
	}

	@Override
	public Collection<T> elements() {
		if (cache != null) return cache;

		cache = new ArrayList<>(elementMap.values());
		Comparator<T> cmp = Comparator.<T>comparingInt(elementIndex::get)
				.thenComparing(NamedElement::getName, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(cache, cmp);
		return cache;
	}
}
