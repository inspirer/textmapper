/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.NamedElement;

import java.util.*;

public class Namespace<T extends NamedElement> {

	Set<Name> knownNames = new HashSet<>();
	Map<String, T> reservedIds = new HashMap<>();
	List<T> allElements = new ArrayList<>();

	NamedElement canInsert(Name name) {
		String idPrefix = name.qualifier() != null ? qualifiedId(name.qualifier()) + "/" : "";
		for (String id : name.uniqueIds()) {
			NamedElement namedElement = reservedIds.get(idPrefix + id);
			if (namedElement != null) {
				return namedElement;
			}
		}
		return null;
	}

	List<T> getElements() {
		return Collections.unmodifiableList(this.allElements);
	}

	boolean insert(T element) {
		Name name = element.getName();
		if (canInsert(name) != null) return false;
		knownNames.add(name);
		allElements.add(element);
		String idPrefix = name.qualifier() != null ? qualifiedId(name.qualifier()) + "/" : "";
		for (String id : name.uniqueIds()) {
			reservedIds.put(idPrefix + id, element);
		}
		return true;
	}

	T resolve(String referenceText, Name context) {
		for (; context != null; context = context.qualifier()) {
			T namedElement = reservedIds.get(qualifiedId(context) + "/" + referenceText);
			if (namedElement != null) {
				if (!namedElement.getName().isReference(referenceText)) {
					return null;
				}
				return namedElement;
			}
		}
		T namedElement = reservedIds.get(referenceText);
		if (namedElement != null && namedElement.getName().isReference(referenceText)) {
			return namedElement;
		}
		return null;
	}

	<E extends T> E resolve(String referenceText, Name context, Class<E> type) {
		T result = resolve(referenceText, context);
		if (type.isInstance(result)) {
			return (E) result;
		}
		return null;
	}

	private String qualifiedId(Name name) {
		String[] strings = name.uniqueIds();
		if (strings == null || strings.length == 0) throw new IllegalStateException();
		return name.qualifier() != null
				? qualifiedId(name.qualifier()) + '/' + strings[0] : strings[0];
	}
}
