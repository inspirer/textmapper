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
package org.textway.lapg.builder;

import org.textway.lapg.api.*;
import org.textway.lapg.api.builder.GrammarBuilder;
import org.textway.lapg.api.builder.RuleBuilder;
import org.textway.lapg.api.regex.RegexPart;

import java.util.*;

/**
 * evgeny, 14.12.11
 */
class LiGrammarBuilder implements GrammarBuilder {

	private final Map<String, LiSymbol> symbolsMap = new HashMap<String, LiSymbol>();

	private final Set<Symbol> symbolsSet = new HashSet<Symbol>();
	private final List<LiSymbol> symbols = new ArrayList<LiSymbol>();
	private final List<LiLexem> lexems = new ArrayList<LiLexem>();
	private final List<LiNamedPattern> namedPatterns = new ArrayList<LiNamedPattern>();
	private final Set<LiNamedPattern> namedPatternsSet = new HashSet<LiNamedPattern>();
	private final List<LiRule> rules = new ArrayList<LiRule>();
	private final List<LiPrio> priorities = new ArrayList<LiPrio>();

	private final List<LiInputRef> inputs = new ArrayList<LiInputRef>();
	private final Symbol eoi;

	public LiGrammarBuilder() {
		eoi = addSymbol(Symbol.KIND_TERM, "eoi", null);
	}

	@Override
	public Symbol addSymbol(int kind, String name, String type) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (symbolsMap.containsKey(name)) {
			throw new IllegalStateException("symbol `" + name + "' already exists");
		}
		if (kind != Symbol.KIND_TERM && kind != Symbol.KIND_NONTERM && kind != Symbol.KIND_LAYOUT) {
			throw new IllegalArgumentException("wrong symbol kind");
		}
		LiSymbol s = new LiSymbol(kind, name, type);
		symbols.add(s);
		symbolsSet.add(s);
		symbolsMap.put(name, s);
		return s;
	}

	@Override
	public Symbol addSoftSymbol(String name, Symbol softClass) {
		if (name == null || softClass == null) {
			throw new NullPointerException();
		}
		if (symbolsMap.containsKey(name)) {
			throw new IllegalStateException("symbol `" + name + "' already exists");
		}
		LiSymbol s = new LiSymbol(name, softClass);
		symbols.add(s);
		symbolsSet.add(s);
		symbolsMap.put(name, s);
		return s;
	}

	@Override
	public Symbol getEoi() {
		return eoi;
	}

	@Override
	public NamedPattern addPattern(String name, RegexPart regexp) {
		if (name == null || regexp == null) {
			throw new NullPointerException();
		}
		if (namedPatternsSet.contains(name)) {
			throw new IllegalStateException("named pattern `" + name + "' already exists");
		}
		return new LiNamedPattern(name, regexp);
	}

	@Override
	public Lexem addLexem(int kind, Symbol sym, RegexPart regexp, int groups, int priority, Lexem classLexem) {
		check(sym);
		if (regexp == null) {
			throw new NullPointerException();
		}
		int symKind = sym.getKind();
		if (symKind != Symbol.KIND_TERM && symKind != Symbol.KIND_SOFTTERM) {
			throw new IllegalArgumentException("symbol `" + sym.getName() + "' is not a terminal");
		}
		if (symKind == Symbol.KIND_SOFTTERM != (kind == Lexem.KIND_SOFT)) {
			throw new IllegalArgumentException("wrong lexem kind, doesn't match symbol kind");
		}
		LiLexem l = new LiLexem(kind, lexems.size(), sym, regexp, groups, priority, classLexem);
		lexems.add(l);
		return l;
	}

	@Override
	public Prio addPrio(int prio, Collection<Symbol> symbols) {
		if (prio != Prio.LEFT && prio != Prio.RIGHT && prio != Prio.NONASSOC) {
			throw new IllegalArgumentException("wrong priority");
		}
		for (Symbol s : symbols) {
			check(s);
			if (s.getKind() != Symbol.KIND_TERM && s.getKind() != Symbol.KIND_SOFTTERM) {
				throw new IllegalArgumentException("symbol `" + s.getName() + "' is not a terminal");
			}
		}
		LiPrio liprio = new LiPrio(prio, symbols.toArray(new Symbol[symbols.size()]));
		priorities.add(liprio);
		return liprio;
	}

	@Override
	public InputRef addInput(Symbol inputSymbol, boolean hasEoi) {
		check(inputSymbol);
		if (inputSymbol.getKind() != Symbol.KIND_NONTERM) {
			throw new IllegalArgumentException("input symbol should be non-terminal");
		}
		LiInputRef inp = new LiInputRef(inputSymbol, hasEoi);
		inputs.add(inp);
		return inp;
	}

	@Override
	public RuleBuilder rule(String alias, Symbol left) {
		check(left);
		if (left.getKind() != Symbol.KIND_NONTERM) {
			throw new IllegalArgumentException("left symbol of rule should be non-terminal");
		}
		return new LiRuleBuilder(this, left, alias);
	}

	Rule addRule(String alias, Symbol left, SymbolRef[] right, Symbol priority) {
		LiRule rule = new LiRule(rules.size(), alias, left, right, priority);
		rules.add(rule);
		return rule;
	}

	void check(Symbol sym) {
		if (sym == null) {
			throw new NullPointerException();
		}
		if (!symbolsSet.contains(sym)) {
			throw new IllegalArgumentException("unknown symbol passed");
		}
	}

	@Override
	public Grammar create() {
		LiSymbol[] symbolArr = symbols.toArray(new LiSymbol[symbols.size()]);
		Arrays.sort(symbolArr, new Comparator<LiSymbol>() {
			@Override
			public int compare(LiSymbol o1, LiSymbol o2) {
				// TODO do not merge Soft term & term
				int kind1 = o1.getKind() == Symbol.KIND_SOFTTERM ? Symbol.KIND_TERM : o1.getKind();
				int kind2 = o2.getKind() == Symbol.KIND_SOFTTERM ? Symbol.KIND_TERM : o2.getKind();
				return new Integer(kind1).compareTo(kind2);
			}
		});
		for (int i = 0; i < symbolArr.length; i++) {
			symbolArr[i].setIndex(i);
		}
		int terminals = 0;
		while (terminals < symbolArr.length && symbolArr[terminals].isTerm()) {
			terminals++;
		}
		int grammarSymbols = terminals;
		while (grammarSymbols < symbolArr.length && symbolArr[grammarSymbols].getKind() != Symbol.KIND_LAYOUT) {
			grammarSymbols++;
		}

		LiLexem[] lexemArr = lexems.toArray(new LiLexem[lexems.size()]);
		NamedPattern[] patternsArr = namedPatterns.toArray(new NamedPattern[namedPatterns.size()]);

		LiSymbol error = symbolsMap.get("error");

		LiRule[] ruleArr;
		LiPrio[] prioArr;
		LiInputRef[] inputArr;

		if(rules.size() != 0) {
			ruleArr = rules.toArray(new LiRule[rules.size()]);
			prioArr = priorities.toArray(new LiPrio[priorities.size()]);
			inputArr = inputs.toArray(new LiInputRef[inputs.size()]);
		} else {
			ruleArr = null;
			prioArr = null;
			inputArr = null;
		}

		return new LiGrammar(symbolArr, ruleArr, prioArr, lexemArr, patternsArr, inputArr, eoi, error, terminals, grammarSymbols);
	}
}
