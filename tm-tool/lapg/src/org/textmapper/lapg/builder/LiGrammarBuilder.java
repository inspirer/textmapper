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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.rule.*;

import java.util.*;

/**
 * evgeny, 14.12.11
 */
class LiGrammarBuilder implements GrammarBuilder {

	private final Map<String, LiSymbol> symbolsMap = new HashMap<String, LiSymbol>();

	private final Set<Symbol> symbolsSet = new HashSet<Symbol>();
	private final List<LiSymbol> symbols = new ArrayList<LiSymbol>();
	private final List<LiLexicalRule> lexicalRules = new ArrayList<LiLexicalRule>();
	private final List<LiNamedPattern> namedPatterns = new ArrayList<LiNamedPattern>();
	private final Set<String> namedPatternsSet = new HashSet<String>();
	private final Set<String> stateNamesSet = new HashSet<String>();
	private final Set<LexerState> statesSet = new LinkedHashSet<LexerState>();
	private final List<LiRule> rules = new ArrayList<LiRule>();
	private final List<LiPrio> priorities = new ArrayList<LiPrio>();
	private final Set<RhsPart> rhsSet = new HashSet<RhsPart>();

	private final List<LiInputRef> inputs = new ArrayList<LiInputRef>();
	private final Terminal eoi;

	public LiGrammarBuilder() {
		eoi = addTerminal(Symbol.EOI, null, null);
	}

	@Override
	public Terminal addTerminal(String name, String type, SourceElement origin) {
		return addSymbol(new LiTerminal(name, type, origin));
	}

	@Override
	public Nonterminal addNonterminal(String name, String type, SourceElement origin) {
		return addSymbol(new LiNonterminal(name, type, origin));
	}

	@Override
	public Terminal addSoftTerminal(String name, Terminal softClass, SourceElement origin) {
		if (name == null || softClass == null) {
			throw new NullPointerException();
		}
		return addSymbol(new LiTerminal(name, softClass, origin));
	}

	private <T extends LiSymbol> T addSymbol(T sym) {
		String name = sym.getName();
		if (name == null) {
			throw new NullPointerException();
		}
		if (symbolsMap.containsKey(name)) {
			throw new IllegalStateException("symbol `" + name + "' already exists");
		}
		symbols.add(sym);
		symbolsSet.add(sym);
		symbolsMap.put(name, sym);
		return sym;
	}

	@Override
	public Terminal getEoi() {
		return eoi;
	}

	@Override
	public NamedPattern addPattern(String name, RegexPart regexp, SourceElement origin) {
		if (name == null || regexp == null) {
			throw new NullPointerException();
		}
		if (!namedPatternsSet.add(name)) {
			throw new IllegalStateException("named pattern `" + name + "' already exists");
		}
		LiNamedPattern pattern = new LiNamedPattern(name, regexp, origin);
		namedPatterns.add(pattern);
		return pattern;
	}

	@Override
	public LexerState addState(String name, SourceElement origin) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (!stateNamesSet.add(name)) {
			throw new IllegalStateException("state `" + name + "' already exists");
		}
		LiLexerState state = new LiLexerState(statesSet.size(), name, origin);
		statesSet.add(state);
		return state;
	}

	@Override
	public LexicalRule addLexicalRule(int kind, Terminal sym, RegexPart regexp, Iterable<LexerState> states, int priority, LexicalRule classLexicalRule, SourceElement origin) {
		check(sym);
		if (regexp == null) {
			throw new NullPointerException();
		}
		int symKind = sym.getKind();
		if (symKind == Symbol.KIND_SOFTTERM != (kind == LexicalRule.KIND_SOFT)) {
			throw new IllegalArgumentException("wrong rule kind, doesn't match symbol kind");
		}
		List<LexerState> liStates = new ArrayList<LexerState>();
		for (LexerState state : states) {
			if (!statesSet.contains(state)) {
				throw new IllegalArgumentException("unknown state passed `" + state.getName() + "'");
			}
			liStates.add(state);
		}
		if (liStates.isEmpty()) {
			throw new IllegalArgumentException("no states passed");
		}
		LiLexicalRule l = new LiLexicalRule(kind, lexicalRules.size(), sym, regexp, liStates, priority, classLexicalRule, origin);
		lexicalRules.add(l);
		return l;
	}

	@Override
	public Prio addPrio(int prio, Collection<Terminal> symbols, SourceElement origin) {
		if (prio != Prio.LEFT && prio != Prio.RIGHT && prio != Prio.NONASSOC) {
			throw new IllegalArgumentException("wrong priority");
		}
		for (Terminal s : symbols) {
			check(s);
		}
		LiPrio liprio = new LiPrio(prio, symbols.toArray(new Terminal[symbols.size()]), origin);
		priorities.add(liprio);
		return liprio;
	}

	@Override
	public InputRef addInput(Nonterminal inputSymbol, boolean hasEoi, SourceElement origin) {
		check(inputSymbol);
		if (inputSymbol.getKind() != Symbol.KIND_NONTERM) {
			throw new IllegalArgumentException("input symbol should be non-terminal");
		}
		LiInputRef inp = new LiInputRef(inputSymbol, hasEoi, origin);
		inputs.add(inp);
		return inp;
	}

	@Override
	public Collection<Rule> addRule(String alias, Nonterminal left, RhsPart rhs, Terminal prio) {
		check(left);
		check(rhs);
		if (prio != null) {
			check(prio);
		}

		LiRhsPart right = (LiRhsPart) rhs;
		right.attach(new Object());

		List<RhsSymbol[]> rules = right.expand();
		List<Rule> result = new ArrayList<Rule>(rules.size());
		for (RhsSymbol[] r : rules) {
			result.add(addRule(alias, left, r, prio, right));
		}
		((LiNonterminal) left).definition.addRule(right);
		return result;
	}

	private Rule addRule(String alias, Nonterminal left, RhsSymbol[] right, Symbol priority, SourceElement origin) {
		LiRule rule = new LiRule(rules.size(), alias, left, right, priority, origin);
		rules.add(rule);
		return rule;
	}

	@Override
	public RhsSymbol symbol(String alias, Symbol sym, Collection<Terminal> unwanted, SourceElement origin) {
		check(sym);
		NegativeLookahead nla = null;
		if (unwanted != null && unwanted.size() > 0) {
			for (Terminal u : unwanted) {
				check(u);
			}
			nla = new LiNegativeLookahead(unwanted.toArray(new Terminal[unwanted.size()]));
		}
		LiRhsSymbol result = new LiRhsSymbol(sym, alias, nla, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsChoice choice(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsChoice result = new LiRhsChoice(liparts, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsSequence sequence(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsSequence result = new LiRhsSequence(liparts, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsSequence empty(SourceElement origin) {
		return sequence(Collections.<RhsPart>emptyList(), origin);
	}

	@Override
	public RhsUnordered unordered(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsUnordered result = new LiRhsUnordered(liparts, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsOptional optional(RhsPart inner, SourceElement origin) {
		check(inner);
		LiRhsOptional result = new LiRhsOptional((LiRhsPart) inner, origin);
		rhsSet.add(result);
		return result;
	}

	void check(RhsPart part) {
		if (part == null) {
			throw new NullPointerException();
		}
		if (!rhsSet.contains(part)) {
			throw new IllegalArgumentException("unknown right-hand side element passed");
		}
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
		int grammarSymbols = symbolArr.length;

		LiLexicalRule[] lexicalRulesArr = lexicalRules.toArray(new LiLexicalRule[lexicalRules.size()]);
		NamedPattern[] patternsArr = namedPatterns.toArray(new NamedPattern[namedPatterns.size()]);

		LiSymbol error = symbolsMap.get("error");

		LiRule[] ruleArr;
		LiPrio[] prioArr;
		LiInputRef[] inputArr;

		if (rules.size() != 0) {
			ruleArr = rules.toArray(new LiRule[rules.size()]);
			prioArr = priorities.toArray(new LiPrio[priorities.size()]);
			inputArr = inputs.toArray(new LiInputRef[inputs.size()]);
		} else {
			ruleArr = null;
			prioArr = null;
			inputArr = null;
		}
		LexerState[] statesArr = statesSet.toArray(new LexerState[statesSet.size()]);

		return new LiGrammar(symbolArr, ruleArr, prioArr, lexicalRulesArr, patternsArr, statesArr, inputArr, eoi, error, terminals, grammarSymbols);
	}
}
