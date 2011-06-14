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


public interface ParserConflict {

	static final int FIXED = 0;
	static final int SHIFT_REDUCE = 1;
	static final int REDUCE_REDUCE = 2;
	static final int SHIFT_SOFT = 3;
	static final int SHIFT_REDUCE_SOFT = 4;
	static final int REDUCE_REDUCE_SOFT = 5;

	int getKind();

	String getKindAsText();

	Rule[] getRules();

	Symbol[] getSymbols();

	Input getInput();

	String getText();

	public interface Input {
		int getState();

		Symbol[] getSymbols();

		String getText();
	}
}
