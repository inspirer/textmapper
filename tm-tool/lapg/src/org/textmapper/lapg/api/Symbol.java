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

public interface Symbol extends SourceElement {

	static final String EOI = "eoi";
	static final String ERROR = "error";

	static final int KIND_TERM = 0;
	static final int KIND_SOFTTERM = 1;
	static final int KIND_NONTERM = 2;
	static final int KIND_LAYOUT = 3;

	int getKind();

	String kindAsString();

	int getIndex();

	String getName();

	String getType();

	boolean isTerm();

	boolean isSoft();

	Symbol getSoftClass();
}