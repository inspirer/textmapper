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
package org.textmapper.lapg.api;

import java.util.Collection;
import java.util.function.IntFunction;

public interface Scope<T extends NamedElement> {

	/**
	 * Generates a unique name that does not conflict with existing elements in the scope.
	 */
	String newName(String nameHint);

	/**
	 * Forwards the request to the parent scope if not found in this scope.
	 */
	T resolve(String name);

	boolean insert(T element, T anchor);

	boolean contains(T element);

	/**
	 * Elements without an anchor are reported in the order of appearance,
	 * while anchored elements are returned next to their anchor, sorted by name.
	 */
	Collection<T> elements();

	/**
	 * Returns an array containing the elements of this scope, using the
	 * provided {@code creator} function to allocate the returned array.
	 */
	T[] toArray(IntFunction<T[]> creator);

	int size();
}
