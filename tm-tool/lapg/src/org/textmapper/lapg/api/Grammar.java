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

/**
 * Input grammar definition.
 */
public interface Grammar {

	Symbol[] getSymbols();

	Rule[] getRules();

	Prio[] getPriorities();

	LexerState[] getLexerStates();

	Lexem[] getLexems();

	NamedPattern[] getPatterns();

	int getTerminals();

	int getGrammarSymbols();

	InputRef[] getInput();

	Symbol getEoi();

	Symbol getError();
}
