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
package org.textmapper.tool.compiler;

/**
 * evgeny, 7/21/13
 */
public class TMTypeHint {

	private final Kind kind;
	private final String nameHint;

	public TMTypeHint(Kind kind, String nameHint) {
		this.kind = kind;
		this.nameHint = nameHint;
	}

	public Kind getKind() {
		return kind;
	}

	public String getNameHint() {
		return nameHint;
	}

	public enum Kind {
		VOID, INTERFACE, CLASS
	}
}
