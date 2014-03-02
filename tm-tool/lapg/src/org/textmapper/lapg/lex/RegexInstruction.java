/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
package org.textmapper.lapg.lex;

/**
 * evgeny, 6/10/12
 */
public class RegexInstruction {

	private final RegexInstructionKind kind;
	private final int value;

	public RegexInstruction(RegexInstructionKind kind, int value) {
		this.kind = kind;
		this.value = value;
	}

	public RegexInstructionKind getKind() {
		return kind;
	}

	public int getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RegexInstruction that = (RegexInstruction) o;

		return kind == that.kind && value == that.value;

	}

	@Override
	public int hashCode() {
		int result = kind != null ? kind.hashCode() : 0;
		result = 31 * result + value;
		return result;
	}

	@Override
	public String toString() {
		switch (kind) {
			case LeftParen:
			case Set:
			case Done:
			case Symbol:
				return kind + "(" + value + ")";
		}
		return kind.toString();
	}
}
