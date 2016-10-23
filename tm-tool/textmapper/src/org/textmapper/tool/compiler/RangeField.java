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
package org.textmapper.tool.compiler;

public interface RangeField {
	/**
	 * @return the name of the field
	 */
	String getName();

	/**
	 * @return all possible range types that can be found behind this field.
	 */
	String[] getTypes();

	/**
	 * @return true if the name was explicitly mentioned in the grammar (via name=symref).
	 */
	boolean hasExplicitName();

	boolean isList();
	boolean isNullable();
}
