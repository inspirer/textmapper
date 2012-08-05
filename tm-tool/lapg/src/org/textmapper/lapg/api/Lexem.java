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
package org.textmapper.lapg.api;

import org.textmapper.lapg.api.regex.RegexPart;

/**
 * Lexem rule.
 */
public interface Lexem extends SourceElement {

	static final int KIND_NONE = 0;
	static final int KIND_CLASS = 1;
	static final int KIND_SOFT = 2;
	static final int KIND_SPACE = 3;
	static final int KIND_LAYOUT = 4;

	int getIndex();

	Symbol getSymbol();

	RegexPart getRegexp();

	int getPriority();

	int getGroups();

	int getKind();

	String getKindAsText();

	Lexem getClassLexem();

	boolean isExcluded();
}
