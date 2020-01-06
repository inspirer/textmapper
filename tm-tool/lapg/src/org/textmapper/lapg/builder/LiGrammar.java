/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

class LiGrammar extends LiUserDataHolder implements Grammar {

	private final Symbol[] symbols;
	private final Rule[] rules;
	private final Prio[] priorities;
	private final LexerRule[] lexerRules;
	private final NamedPattern[] patterns;
	private final NamedSet[] sets;
	private final LexerState[] lexerStates;

	private final InputRef[] inputs;
	private final Symbol eoi;
	private final Symbol error;
	private final Symbol invalidToken;

	private final int terminals;
	private final int grammarSymbols;
	private final Problem[] problems;

	public LiGrammar(Symbol[] symbols, Rule[] rules, Prio[] priorities, LexerRule[] lexerRules,
					 NamedPattern[] patterns, NamedSet[] sets,
					 LexerState[] lexerStates,
					 InputRef[] inputs, Symbol eoi,
					 Symbol error, Symbol invalidToken, int terminals, int grammarSymbols,
					 Problem[] problems) {
		this.symbols = symbols;
		this.rules = rules;
		this.priorities = priorities;
		this.lexerRules = lexerRules;
		this.patterns = patterns;
		this.sets = sets;
		this.lexerStates = lexerStates;
		this.inputs = inputs;
		this.eoi = eoi;
		this.error = error;
		this.invalidToken = invalidToken;
		this.terminals = terminals;
		this.grammarSymbols = grammarSymbols;
		this.problems = problems;
	}

	public LiGrammar(Problem[] problems) {
		this(new Symbol[0], new Rule[0], new Prio[0], new LexerRule[0], new NamedPattern[0], new NamedSet[0],
				new LexerState[0], new InputRef[0], null, null, null, 0, 0, problems);
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
	public NamedSet[] getSets() {
		return sets;
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
	public Symbol getInvalidToken() {
		return invalidToken;
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
