/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package net.sf.lapg.api;

import java.util.List;
import java.util.Map;

/**
 * Input grammar definition.
 */
public interface Grammar {

	Symbol[] getSymbols();
	Rule[] getRules();
	Prio[] getPriorities();

	Map<String, Object> getOptions();

	Lexem[] getLexems();

	int getTerminals();
	Symbol[] getInput();
	Symbol getEoi();
	Symbol getError();

	String getTemplates();

	boolean hasActions();
	boolean hasLexemActions();
	boolean hasErrors();
	Map<Symbol,List<Rule>> getRulesBySymbol();
}
