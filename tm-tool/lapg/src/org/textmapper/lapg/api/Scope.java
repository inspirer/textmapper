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
import java.util.function.Predicate;

public interface Scope<T extends NamedElement> {

	boolean insert(T element, T anchor);

	/**
	 * Forwards the request to the parent scope if not found in this scope.
	 */
	T resolve(String name);

	boolean contains(T element);

	/**
	 * Tries to reserve a name and returns true on success. All future attempts to
	 * <tt>insert</tt> this name will fail.
	 */
	boolean reserve(String name);

	/**
	 * Generates a unique name that does not conflict with existing elements in the scope.
	 */
	String newName(String nameHint);

	/**
	 * Generates new names for all anonymous elements in the scope and sorts the result.
	 */
	void assignNames();

	/**
	 * Puts elements next to their anchor and sorts element around the same anchor.
	 */
	void sort();

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

	/**
	 * Removes all elements from the scope for which "filter" returns true.
	 */
	void removeIf(Predicate<? super T> filter);

	int size();
}
