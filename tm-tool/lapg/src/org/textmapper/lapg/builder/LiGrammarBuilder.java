/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.api.ast.AstClass;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstModel;
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
	private final List<LiLexerRule> lexerRules = new ArrayList<LiLexerRule>();
	private final List<LiNamedPattern> namedPatterns = new ArrayList<LiNamedPattern>();
	private final Set<String> namedPatternsSet = new HashSet<String>();
	private final Set<String> stateNamesSet = new HashSet<String>();
	private final Set<LexerState> statesSet = new LinkedHashSet<LexerState>();
	private final List<LiRule> rules = new ArrayList<LiRule>();
	private final List<LiPrio> priorities = new ArrayList<LiPrio>();
	private final Set<RhsPart> rhsSet = new HashSet<RhsPart>();
	private final Set<Terminal> sealedTerminals = new HashSet<Terminal>();
	private final Set<AstField> knownFields = new HashSet<AstField>();
	private AstModel astModel;

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
	public void makeSoft(Terminal terminal, Terminal softClass) {
		check(terminal);
		check(softClass);
		if (terminal == softClass) {
			throw new IllegalArgumentException("terminal cannot be a class of itself");
		}
		if (terminal.isSoft()) {
			throw new IllegalStateException("terminal is already soft");
		}
		if (terminal.getType() != null && !terminal.getType().equals(softClass.getType())) {
			throw new IllegalStateException("soft terminal cannot override class terminal's type");
		}
		if (sealedTerminals.contains(terminal)) {
			throw new IllegalArgumentException("cannot convert terminal into a soft terminal (was already used as non-soft)");
		}
		if (softClass.isSoft()) {
			throw new IllegalArgumentException("cannot use soft terminal as a class terminal");
		}
		sealedTerminals.add(softClass);
		((LiTerminal) terminal).setSoftClass(softClass);
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
	public LexerRule addLexerRule(int kind, Terminal sym, RegexPart regexp, Iterable<LexerState> states, int priority, LexerRule classLexerRule, SourceElement origin) {
		check(sym);
		if (regexp == null) {
			throw new NullPointerException();
		}
		sealedTerminals.add(sym);
		if (sym.isSoft() != (kind == LexerRule.KIND_SOFT)) {
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
		LiLexerRule l = new LiLexerRule(kind, lexerRules.size(), sym, regexp, liStates, priority, classLexerRule, origin);
		lexerRules.add(l);
		((LiTerminal) sym).addRule(l);
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
		if (inputSymbol.isTerm()) {
			throw new IllegalArgumentException("input symbol should be non-terminal");
		}
		LiInputRef inp = new LiInputRef(inputSymbol, hasEoi, origin);
		inputs.add(inp);
		return inp;
	}

	@Override
	public Collection<Rule> addRule(String alias, Nonterminal left, RhsPart rhs, Terminal prio) {
		check(left);
		check(rhs, false);
		if (prio != null) {
			check(prio);
		}

		LiRhsPart right = (LiRhsPart) rhs;
		List<RhsSymbol[]> expanded = right.expand();
		List<Rule> result = new ArrayList<Rule>(expanded.size());
		for (RhsSymbol[] r : expanded) {
			LiRule rule = new LiRule(rules.size(), alias, left, r, prio, rhs);
			rules.add(rule);
			result.add(rule);
		}

		final LiNonterminal liLeft = (LiNonterminal) left;
		if (right instanceof LiRhsRoot) {
			liLeft.setDefinition((LiRhsRoot) right);
		} else {
			liLeft.addRule(right);
		}
		return result;
	}

	@Override
	public RhsAssignment assignment(String name, RhsPart inner, boolean isAddition, SourceElement origin) {
		check(inner, true);
		if (name == null) {
			throw new NullPointerException("name is null");
		}
		LiRhsAssignment result = new LiRhsAssignment(name, (LiRhsPart) inner, isAddition, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsCast cast(Symbol asSymbol, RhsPart inner, SourceElement origin) {
		check(inner, true);
		check(asSymbol);
		LiRhsCast result = new LiRhsCast(asSymbol, (LiRhsPart) inner, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsSymbol symbol(Symbol sym, Collection<Terminal> unwanted, SourceElement origin) {
		check(sym);
		NegativeLookahead nla = null;
		if (unwanted != null && unwanted.size() > 0) {
			for (Terminal u : unwanted) {
				check(u);
			}
			nla = new LiNegativeLookahead(unwanted.toArray(new Terminal[unwanted.size()]));
		}
		LiRhsSymbol result = new LiRhsSymbol(sym, nla, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsChoice choice(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p, true);
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
			check(p, true);
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
			check(p, true);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsUnordered result = new LiRhsUnordered(liparts, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsOptional optional(RhsPart inner, SourceElement origin) {
		check(inner, true);
		LiRhsOptional result = new LiRhsOptional((LiRhsPart) inner, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsList list(RhsPart inner, RhsPart separator, boolean nonEmpty, SourceElement origin) {
		check(inner, true);
		if (separator != null) {
			check(separator, true);
		}
		if (!nonEmpty && separator != null) {
			throw new IllegalArgumentException("list with separator should have at least one element");
		}
		LiRhsList result = new LiRhsList((LiRhsPart) inner, (LiRhsPart) separator, nonEmpty, null, false, origin);
		rhsSet.add(result);
		return result;
	}

	void check(RhsPart part, boolean asChild) {
		if (part == null) {
			throw new NullPointerException();
		}
		if (!rhsSet.contains(part)) {
			throw new IllegalArgumentException("unknown right-hand side element passed");
		}
		if (asChild && part instanceof RhsRoot) {
			throw new IllegalArgumentException("right-hand side element cannot be nested");
		}
	}


	void check(AstField field) {
		if (field == null) {
			throw new NullPointerException();
		}
		if (!knownFields.contains(field)) {
			throw new IllegalArgumentException("unknown ast field passed");
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
	public void link(AstModel ast) {
		if (astModel != null) {
			throw new IllegalStateException("cannot re-link ast model");
		}
		astModel = ast;
		for (AstClass cl : astModel.getClasses()) {
			link(cl);
		}
	}

	private void link(AstClass cl) {
		Collections.addAll(knownFields, cl.getFields());
		for (AstClass inner : cl.getInner()) {
			link(inner);
		}
	}

	@Override
	public void map(RhsSymbol symbol, AstField field) {
		check(symbol, false);
		check(field);
		((LiRhsSymbol) symbol).setMapping(field);
	}

	@Override
	public Grammar create() {
		LiSymbol[] symbolArr = symbols.toArray(new LiSymbol[symbols.size()]);
		Arrays.sort(symbolArr, new Comparator<LiSymbol>() {
			@Override
			public int compare(LiSymbol o1, LiSymbol o2) {
				int kind1 = o1.isTerm() ? 0 : 1;
				int kind2 = o2.isTerm() ? 0 : 1;
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

		LiLexerRule[] lexerRulesArr = lexerRules.toArray(new LiLexerRule[lexerRules.size()]);
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

		annotateNullables();
		new LiRewriter(symbolArr).rewriteLists();
		return new LiGrammar(symbolArr, ruleArr, prioArr, lexerRulesArr, patternsArr, statesArr, inputArr, eoi, error, terminals, grammarSymbols);
	}

	public void annotateNullables() {
		// a set of non-empty rules without terminals on the right side
		Set<Rule> candidates = new HashSet<Rule>();
		for (Rule r : rules) {
			if (r.getRight().length == 0) {
				((LiNonterminal) r.getLeft()).setNullable(true);
				continue;
			}
			boolean candidate = true;
			for (RhsSymbol rhsSymbol : r.getRight()) {
				if (rhsSymbol.getTarget().isTerm()) {
					candidate = false;
					break;
				}
			}
			if (candidate) {
				candidates.add(r);
			}
		}

		// effectively invalidate potential nullable nonterminals
		Queue<Rule> queue = new LinkedList<Rule>(candidates);
		Set<Rule> inQueue = new HashSet<Rule>(candidates);
		Rule next;
		while ((next = queue.poll()) != null) {
			if (next.getLeft().isNullable()) {
				inQueue.remove(next);
				continue;
			}

			boolean isEmpty = true;
			for (RhsSymbol rhsSymbol : next.getRight()) {
				if (!((Nonterminal) rhsSymbol.getTarget()).isNullable()) {
					isEmpty = false;
					break;
				}
			}

			if (isEmpty) {
				final LiNonterminal left = (LiNonterminal) next.getLeft();
				left.setNullable(true);
				for (RhsSymbol usage : left.getUsages()) {
					if (usage.getLeft().isNullable()) continue;

					for (Rule usingRule : usage.getLeft().getRules()) {
						if (!inQueue.contains(usingRule) && candidates.contains(usingRule)) {
							queue.add(usingRule);
							inQueue.add(usingRule);
						}
					}
				}
			}
			inQueue.remove(next);
		}
	}
}
