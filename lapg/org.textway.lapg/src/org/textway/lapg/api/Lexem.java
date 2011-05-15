/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.api;

/**
 * Lexem rule.
 */
public interface Lexem extends SourceElement {

	public static final int KIND_NONE = 0;
	public static final int KIND_CLASS = 1;
	public static final int KIND_INSTANCE = 2;
	public static final int KIND_SOFT = 3;

	int getIndex();
	Symbol getSymbol();
	String getRegexp();
	int getPriority();
	int getGroups();
	SourceElement getAction();

	int getKind();
	String getKindAsText();
	Lexem getClassLexem();
	boolean isExcluded();
}
