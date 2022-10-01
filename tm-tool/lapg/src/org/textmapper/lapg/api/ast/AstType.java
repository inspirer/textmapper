/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
package org.textmapper.lapg.api.ast;

public interface AstType {

	AstType ANY = new PrimitiveType("any");

	AstType STRING = new PrimitiveType("string");
	AstType BOOL = new PrimitiveType("boolean");
	AstType INT = new PrimitiveType("int");

	boolean isSubtypeOf(AstType another);

	final class PrimitiveType implements AstType {
		private final String name;

		public PrimitiveType(String name) {
			this.name = name;
		}

		@Override
		public boolean isSubtypeOf(AstType another) {
			return another == this || another == ANY;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
