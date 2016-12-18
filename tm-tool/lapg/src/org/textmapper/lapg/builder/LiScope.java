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
import org.textmapper.lapg.api.NamedElement.Anonymous;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;

class LiScope<T extends NamedElement> implements Scope<T> {

	private final LiScope<T> parent;
	private final Map<String, T> nameToElement = new HashMap<>();
	private final Map<T, Integer> elementIndex = new HashMap<>();
	private final Map<String, Integer> lastIndex = new HashMap<>();
	private List<T> allElements = new ArrayList<>();

	public LiScope() {
		this(null);
	}

	public LiScope(LiScope<T> parent) {
		this.parent = parent;
	}

	@Override
	public T resolve(String name) {
		T result = nameToElement.get(name);
		if (result != null) return result;

		return parent == null ? null : parent.resolve(name);
	}

	@Override
	public String newName(String baseName) {
		if (!nameToElement.containsKey(baseName)) return baseName;

		int index = lastIndex.containsKey(baseName) ? lastIndex.get(baseName) : 1;
		String name = baseName + index;
		while (nameToElement.containsKey(name)) {
			name = baseName + (++index);
		}
		lastIndex.put(baseName, index);
		return name;
	}

	@Override
	public void assignNames() {
		for (T element : allElements) {
			if (!(element instanceof NamedElement.Anonymous)) {
				if (element.getName() == null) throw new IllegalStateException("oops");
				continue;
			}

			String name = element.getName();
			if (name != null) continue;

			name = newName(((Anonymous) element).getNameHint());
			nameToElement.put(name, element);
			((Anonymous) element).setName(name);
		}
	}

	@Override
	public void sort() {
		Comparator<T> cmp = Comparator.<T>comparingInt(elementIndex::get)
				.thenComparing(NamedElement::getName,
						Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		allElements.sort(cmp);
	}

	@Override
	public boolean insert(T element, T anchor) {
		String name = element.getName();
		if (name != null) {
			if (nameToElement.containsKey(name)) return false;
			nameToElement.put(name, element);
		} else if (anchor == null) {
			throw new NullPointerException("both `name' and `anchor' cannot be null");
		}

		int index;
		if (anchor != null) {
			if (!elementIndex.containsKey(anchor)) {
				throw new IllegalArgumentException("unknown anchor");
			}
			index = elementIndex.get(anchor);
		} else {
			index = allElements.size();
		}
		elementIndex.put(element, index);
		allElements.add(element);
		return true;
	}

	@Override
	public boolean reserve(String name) {
		if (nameToElement.containsKey(name)) return false;

		nameToElement.put(name, null);
		return true;
	}

	@Override
	public boolean contains(T element) {
		return elementIndex.containsKey(element);
	}

	@Override
	public Collection<T> elements() {
		return allElements;
	}

	@Override
	public T[] toArray(IntFunction<T[]> creator) {
		Collection<T> elements = elements();
		return elements.toArray(creator.apply(elements.size()));
	}

	@Override
	public void removeIf(Predicate<? super T> filter) {
		final Iterator<T> each = allElements.iterator();
		while (each.hasNext()) {
			T next = each.next();
			if (filter.test(next)) {
				String name = next.getName();
				if (name != null) nameToElement.remove(name);
				elementIndex.remove(next);
				each.remove();
			}
		}
	}

	@Override
	public int size() {
		return allElements.size();
	}
}
