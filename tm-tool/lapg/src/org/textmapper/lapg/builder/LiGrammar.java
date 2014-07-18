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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.*;

class LiGrammar implements Grammar {

	private final Symbol[] symbols;
	private final Rule[] rules;
	private final Prio[] priorities;
	private final LexerRule[] lexerRules;
	private final NamedPattern[] patterns;
	private final LexerState[] lexerStates;

	private final InputRef[] inputs;
	private final Symbol eoi;
	private final Symbol error;

	private final int terminals;
	private final int grammarSymbols;
	private final Problem[] problems;

	public LiGrammar(Symbol[] symbols, Rule[] rules, Prio[] priorities, LexerRule[] lexerRules,
					 NamedPattern[] patterns,
					 LexerState[] lexerStates,
					 InputRef[] inputs, Symbol eoi,
					 Symbol error, int terminals, int grammarSymbols,
					 Problem[] problems) {
		this.symbols = symbols;
		this.rules = rules;
		this.priorities = priorities;
		this.lexerRules = lexerRules;
		this.patterns = patterns;
		this.lexerStates = lexerStates;
		this.inputs = inputs;
		this.eoi = eoi;
		this.error = error;
		this.terminals = terminals;
		this.grammarSymbols = grammarSymbols;
		this.problems = problems;
	}

	@Override
	public Symbol[] getSymbols() {
		return symbols;
	}

	@Override
	public Rule[] getRules() {
		return rules;
	}

	@Override
	public Prio[] getPriorities() {
		return priorities;
	}

	@Override
	public LexerState[] getLexerStates() {
		return lexerStates;
	}

	@Override
	public LexerRule[] getLexerRules() {
		return lexerRules;
	}

	@Override
	public NamedPattern[] getPatterns() {
		return patterns;
	}

	@Override
	public InputRef[] getInput() {
		return inputs;
	}

	@Override
	public Symbol getEoi() {
		return eoi;
	}

	@Override
	public Symbol getError() {
		return error;
	}

	@Override
	public Problem[] getProblems() {
		return problems;
	}

	@Override
	public int getTerminals() {
		return terminals;
	}

	@Override
	public int getGrammarSymbols() {
		return grammarSymbols;
	}
}
