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
package org.textmapper.lapg.api;

public interface Name {

	String[] uniqueIds();

	/**
	 * Returns the first alias.
	 */
	String text();

	/**
	 * Returns true if referenceText matches one of the aliases.
	 */
	boolean isReference(String referenceText);

	/**
	 * Returns SomeName or someName.
	 */
	String camelCase(boolean firstUpper);

	/**
	 * some_name or SOME_NAME.
	 */
	String snakeCase(boolean allUpper);

	/**
	 * SOMENAME
	 */
	String allUpper();

	/**
	 * @return the name qualifier
	 */
	Name qualifier();

	/**
	 * Constructs a fully qualified name for `nested`.
	 */
	Name subName(Name nested);
}
