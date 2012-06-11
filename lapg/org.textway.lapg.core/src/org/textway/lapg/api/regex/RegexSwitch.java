/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.api.regex;

/**
 * evgeny, 6/11/12
 */
public class RegexSwitch<T> {

	public T caseAny(RegexAny c) {
		return null;
	}

	public T caseChar(RegexChar c) {
		return null;
	}

	public T caseEmpty(RegexEmpty c) {
		return null;
	}

	public T caseExpand(RegexExpand c) {
		return null;
	}

	public T caseList(RegexList c) {
		return null;
	}

	public T caseOr(RegexOr c) {
		return null;
	}

	public T caseQuantifier(RegexQuantifier c) {
		return null;
	}

	public T caseRange(RegexRange c) {
		return null;
	}

	public T caseSet(RegexSet c) {
		return null;
	}
}
