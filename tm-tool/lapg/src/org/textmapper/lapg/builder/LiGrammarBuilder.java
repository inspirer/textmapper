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
import org.textmapper.lapg.api.TemplateParameter.Type;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsPart.Kind;
import org.textmapper.lapg.api.rule.RhsSet.Operation;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * evgeny, 14.12.11
 */
class LiGrammarBuilder extends LiGrammarMapper implements GrammarBuilder {

	// The value is either Symbol, or TemplateParameter
	private final Map<String, Object> symbolsMap = new HashMap<String, Object>();

	private final List<LiSymbol> symbols = new ArrayList<LiSymbol>();
	private final List<LiTemplateParameter> params = new ArrayList<LiTemplateParameter>();
	private final List<LiLexerRule> lexerRules = new ArrayList<LiLexerRule>();
	private final List<LiNamedPattern> namedPatterns = new ArrayList<LiNamedPattern>();
	private final List<LiNamedSet> namedSets = new ArrayList<LiNamedSet>();
	private final Set<String> namedPatternsSet = new HashSet<String>();
	private final Set<String> namedSetNames = new HashSet<String>();
	private final Set<String> stateNamesSet = new HashSet<String>();
	private final Set<LexerState> statesSet = new LinkedHashSet<LexerState>();
	private final Set<RhsPredicate> predicateSet = new HashSet<RhsPredicate>();
	private final List<LiRule> rules = new ArrayList<LiRule>();
	private final List<LiPrio> priorities = new ArrayList<LiPrio>();
	private final Set<RhsPart> rhsSet = new HashSet<RhsPart>();
	private final Set<Terminal> sealedTerminals = new HashSet<Terminal>();
	private final Map<Object, Nonterminal> instantiations = new HashMap<Object, Nonterminal>();
	private final Map<Nonterminal, String> anonymousNames = new LinkedHashMap<Nonterminal, String>();
	private final List<Problem> problems = new ArrayList<Problem>();

	private final List<LiInputRef> inputs = new ArrayList<LiInputRef>();
	private final LiTemplateEnvironment env = new LiTemplateEnvironment();
	private final Terminal eoi;

	public LiGrammarBuilder() {
		super(null);
		eoi = addTerminal(Symbol.EOI, null, null);
	}

	@Override
	public Terminal addTerminal(String name, AstType type, SourceElement origin) {
		return addSymbol(new LiTerminal(name, type, origin), false);
	}

	@Override
	public Nonterminal addNonterminal(String name, SourceElement origin) {
		return addSymbol(new LiNonterminal(name, origin), false);
	}

	@Override
	public Nonterminal addAnonymous(String nameHint, SourceElement origin) {
		LiNonterminal nonterm = addSymbol(new LiNonterminal(null, origin), true);
		anonymousNames.put(nonterm, nameHint);
		nonterm.putUserData(Nonterminal.UD_NAME_HINT, nameHint);
		return nonterm;
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
			throw new IllegalArgumentException("cannot convert terminal into a soft terminal (was already used as " +
					"non-soft)");
		}
		if (softClass.isSoft()) {
			throw new IllegalArgumentException("cannot use soft terminal as a class terminal");
		}
		sealedTerminals.add(softClass);
		((LiTerminal) terminal).setSoftClass(softClass);
	}

	private <T extends LiSymbol> T addSymbol(T sym, boolean anonymous) {
		if (!anonymous) {
			String name = sym.getName();
			if (name == null) {
				throw new NullPointerException();
			}
			if (symbolsMap.containsKey(name)) {
				throw new IllegalStateException("symbol (or template parameter) `" + name + "' already exists");
			}
			symbolsMap.put(name, sym);
		}
		symbols.add(sym);
		symbolsSet.add(sym);
		return sym;
	}

	@Override
	public Terminal getEoi() {
		return eoi;
	}

	@Override
	public TemplateParameter addParameter(Type type, String name, Object defaultValue, SourceElement origin) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (name.equals("error")) {
			throw new IllegalArgumentException("parameters cannot have name `error'");
		}
		if (defaultValue != null) {
			check(type, defaultValue);
		}
		if (symbolsMap.containsKey(name)) {
			throw new IllegalStateException("symbol (or template parameter) `" + name + "' already exists");
		}
		LiTemplateParameter param = new LiTemplateParameter(type, name, defaultValue, origin);
		symbolsMap.put(name, param);
		params.add(param);
		return param;
	}

	@Override
	public TemplateEnvironment getRootEnvironment() {
		return env;
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
	public LexerRule addLexerRule(int kind, Terminal sym, RegexPart regexp, Iterable<LexerState> states, int priority,
								  LexerRule classLexerRule, SourceElement origin) {
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
		LiLexerRule l = new LiLexerRule(kind, lexerRules.size(), sym, regexp, liStates, priority, classLexerRule,
				origin);
		lexerRules.add(l);
		((LiTerminal) sym).addRule(l);
		return l;
	}

	@Override
	public NamedSet addSet(String name, RhsSet set, SourceElement origin) {
		if (name == null || set == null) {
			throw new NullPointerException();
		}
		check(set);
		// mark as used
		rhsSet.remove(set);

		if (!namedSetNames.add(name)) {
			throw new IllegalArgumentException("named set `" + name + "' already exists");
		}
		LiNamedSet namedSet = new LiNamedSet(name, set, origin);
		namedSets.add(namedSet);
		return namedSet;
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
	public RhsSequence addPrecedence(RhsPart p, Terminal prec) {
		check(prec);
		LiRhsSequence result = (LiRhsSequence) asSequence(p);
		result.setPrio(prec);
		return result;
	}

	@Override
	public void define(Nonterminal left, RhsRoot rhs) {
		check(left);
		check(rhs);

		LiNonterminal liLeft = (LiNonterminal) left;
		liLeft.setDefinition((LiRhsRoot) rhs);
	}

	@Override
	public void addRule(Nonterminal left, RhsRule rhs) {
		check(left);
		checkInner(rhs, Kind.Choice);
		LiNonterminal liLeft = (LiNonterminal) left;
		liLeft.addRule((LiRhsPart) rhs);
	}

	@Override
	public RhsAssignment assignment(String name, RhsPart inner, boolean isAddition, SourceElement origin) {
		checkInner(inner, Kind.Assignment);
		if (name == null) {
			throw new NullPointerException("name is null");
		}
		LiRhsAssignment result = new LiRhsAssignment(name, (LiRhsPart) inner, isAddition, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsCast cast(Symbol asSymbol, Collection<RhsArgument> args, RhsPart inner, SourceElement origin) {
		checkInner(inner, Kind.Cast);
		check(asSymbol);
		LiRhsCast result = new LiRhsCast(asSymbol, convertArgs(args), (LiRhsPart) inner, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsArgument argument(TemplateParameter param, Object value, SourceElement origin) {
		check(param);
		if (value != null) {
			check(param.getType(), value);
		}
		return new LiRhsArgument(param, value, origin);
	}

	@Override
	public RhsSymbol symbol(Symbol sym, Collection<RhsArgument> args, SourceElement origin) {
		check(sym);
		LiRhsSymbol result = new LiRhsSymbol(sym, convertArgs(args), origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsSymbol templateSymbol(TemplateParameter parameter, Collection<RhsArgument> args, SourceElement origin) {
		check(parameter);
		LiRhsSymbol result = new LiRhsSymbol(parameter, convertArgs(args), origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsChoice choice(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			checkInner(p, Kind.Choice);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsChoice result = new LiRhsChoice(liparts, false, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public LiRhsConditional conditional(RhsPredicate predicate, RhsSequence inner, SourceElement origin) {
		checkInner(inner, Kind.Conditional);
		check(predicate);
		LiRhsConditional result = new LiRhsConditional(
				(LiRhsPredicate) predicate, (LiRhsSequence) inner, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsPredicate predicate(RhsPredicate.Operation operation, Collection<RhsPredicate> inner,
								  TemplateParameter param, Object value, SourceElement origin) {
		LiRhsPredicate[] liinner = null;
		switch (operation) {
			case Equals:
				check(param);
				check(param.getType(), value);
				if (inner != null) {
					throw new IllegalArgumentException("inner");
				}
				break;
			case Or:
			case And:
			case Not:
				if (param != null) {
					throw new IllegalArgumentException("param");
				}
				if (value != null) {
					throw new IllegalArgumentException("value");
				}
				if (inner.size() == 0 || operation == RhsPredicate.Operation.Not && inner.size() != 1) {
					throw new IllegalArgumentException("inner");
				}
				liinner = new LiRhsPredicate[inner.size()];
				int index = 0;
				for (RhsPredicate p : inner) {
					check(p);
					liinner[index++] = (LiRhsPredicate) p;
				}
				break;
		}
		LiRhsPredicate result = new LiRhsPredicate(operation, liinner, param, value, origin);
		predicateSet.add(result);
		return result;
	}

	@Override
	public RhsSequence sequence(String name, Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			checkInner(p, Kind.Sequence);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsSequence result = new LiRhsSequence(name, liparts, false, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsSequence asSequence(RhsPart part) {
		if (part instanceof RhsSequence) {
			check(part);
			return (RhsSequence) part;
		}
		return sequence(null, Collections.singleton(part), part);
	}

	@Override
	public RhsSequence empty(SourceElement origin) {
		return sequence(null, Collections.<RhsPart>emptyList(), origin);
	}

	@Override
	public RhsUnordered unordered(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			checkInner(p, Kind.Unordered);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsUnordered result = new LiRhsUnordered(liparts, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsOptional optional(RhsPart inner, SourceElement origin) {
		checkInner(inner, Kind.Optional);
		LiRhsOptional result = new LiRhsOptional((LiRhsPart) inner, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public RhsList list(RhsSequence inner, RhsPart separator, boolean nonEmpty, SourceElement origin) {
		checkInner(inner, Kind.List);
		if (separator != null) {
			checkInner(separator, Kind.List);
		}
		if (!nonEmpty && separator != null) {
			throw new IllegalArgumentException("list with separator should have at least one element");
		}
		LiRhsList result = new LiRhsList((LiRhsSequence) inner, (LiRhsPart) separator, nonEmpty, null, false, false,
				origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public void addParentheses(Terminal opening, Terminal closing) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RhsIgnored ignored(RhsPart inner, SourceElement origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RhsSet set(Operation operation, Symbol symbol, Collection<RhsArgument> args, Collection<RhsSet> parts,
					  SourceElement origin) {
		LiRhsSet[] liparts = null;
		switch (operation) {
			case Any:
			case First:
			case Follow:
				check(symbol);
				if (parts != null) {
					throw new IllegalArgumentException("parts");
				}
				break;
			case Union:
			case Intersection:
			case Complement:
				if (symbol != null) {
					throw new IllegalArgumentException("symbol");
				}
				if (parts.size() == 0 || operation == Operation.Complement && parts.size() != 1) {
					throw new IllegalArgumentException("parts");
				}
				liparts = new LiRhsSet[parts.size()];
				int index = 0;
				for (RhsPart p : parts) {
					checkInner(p, Kind.Set);
					liparts[index++] = (LiRhsSet) p;
				}
				break;
		}
		LiRhsSet result = new LiRhsSet(operation, symbol, convertArgs(args), liparts, origin);
		rhsSet.add(result);
		return result;
	}

	@Override
	public Nonterminal addShared(RhsPart part, String nameHint) {
		check(part);
		if (nameHint == null) {
			throw new NullPointerException("nameHint");
		}
		if (part instanceof RhsConditional) {
			throw new IllegalArgumentException("part");
		}
		Object id = part.structuralNode();
		Nonterminal symbol = instantiations.get(id);
		if (symbol == null) {
			symbol = addAnonymous(nameHint, ((LiRhsPart) part).getOrigin());
			if (part instanceof RhsRoot) {
				define(symbol, (RhsRoot) part);
			} else {
				if (!(part instanceof RhsRule)) {
					part = asSequence(part);
				}
				addRule(symbol, (RhsRule) part);
			}
			instantiations.put(id, symbol);
		} else {
			// mark as used
			rhsSet.remove(part);
		}
		return symbol;
	}

	LiRhsArgument[] convertArgs(Collection<RhsArgument> args) {
		if (args == null) return null;

		LiRhsArgument[] liargs = new LiRhsArgument[args.size()];
		int index = 0;
		for (RhsArgument a : args) {
			check(a);
			liargs[index++] = (LiRhsArgument) a;
		}
		return liargs;
	}

	@Override
	void check(RhsPart part) {
		if (part == null) {
			throw new NullPointerException();
		}
		if (!rhsSet.contains(part)) {
			throw new IllegalArgumentException("unknown right-hand side element passed");
		}
	}

	void checkInner(RhsPart part, Kind parent) {
		check(part);
		if (part instanceof RhsRoot) {
			throw new IllegalArgumentException("right-hand side element cannot be nested");
		}
		if (parent != Kind.Choice && part instanceof RhsConditional) {
			throw new IllegalArgumentException("conditionals can only be used as direct children of a choice");
		}
	}

	void check(TemplateParameter param) {
		if (param == null) {
			throw new NullPointerException();
		}
		if (symbolsMap.get(param.getName()) != param) {
			throw new IllegalArgumentException("unknown template parameter passed");
		}
	}

	void check(RhsPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException();
		}
		if (!predicateSet.contains(predicate)) {
			throw new IllegalArgumentException("unknown predicate passed");
		}
	}

	void check(RhsArgument arg) {
		if (arg == null) {
			throw new NullPointerException();
		}
		check(arg.getParameter());
	}

	void check(Type type, Object value) {
		switch (type) {
			case Symbol:
				if (!(value instanceof Symbol))
					throw new IllegalArgumentException("symbol default value is expected");
				check((Symbol) value);
				break;
			case String:
				if (!(value instanceof String))
					throw new IllegalArgumentException("string default value is expected");
				break;
			case Integer:
				if (!(value instanceof Integer))
					throw new IllegalArgumentException("integer default value is expected");
				break;
			case Bool:
				if (!(value instanceof Boolean))
					throw new IllegalArgumentException("boolean default value is expected");
				break;
		}
	}

	@Override
	public Grammar create() {
		instantiateTemplates();
		annotateNullables();

		ExpansionContext expansionContext = new ExpansionContext();
		LiNamedSet[] setsArr = namedSets.toArray(new LiNamedSet[namedSets.size()]);
		computeSets(expansionContext, setsArr);

		LiSymbol[] symbolArr = new LiSymbol[symbols.size()];
		int terminals = sortAndEnumerateSymbols(symbolArr);
		int grammarSymbols = symbolArr.length;
		for (int i = terminals; i < grammarSymbols; i++) {
			expandNonterminal((Nonterminal) symbolArr[i], expansionContext);
		}

		LiLexerRule[] lexerRulesArr = lexerRules.toArray(new LiLexerRule[lexerRules.size()]);
		NamedPattern[] patternsArr = namedPatterns.toArray(new NamedPattern[namedPatterns.size()]);

		LiSymbol error = (LiSymbol) symbolsMap.get("error");

		final LiRule[] ruleArr;
		final LiPrio[] prioArr;
		final LiInputRef[] inputArr;

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
		Problem[] problemsArr = problems.toArray(new Problem[problems.size()]);

		assignNames();
		return new LiGrammar(symbolArr, ruleArr, prioArr, lexerRulesArr,
				patternsArr, setsArr,
				statesArr, inputArr, eoi, error,
				terminals, grammarSymbols, problemsArr);
	}

	private void instantiateTemplates() {
		LiTemplateParameter[] paramsArr = params.toArray(new LiTemplateParameter[params.size()]);
		LiSymbol[] symbolArr = new LiSymbol[symbols.size()];
		int terminals = sortAndEnumerateSymbols(symbolArr);

		TemplateInstantiator instantiator = new TemplateInstantiator(paramsArr, symbolArr, terminals, problems);
		instantiator.instantiate(this, inputs);
	}

	private void computeSets(ExpansionContext expansionContext, LiNamedSet[] setsArr) {
		LiSymbol[] symbolArr = new LiSymbol[symbols.size()];
		int terminals = sortAndEnumerateSymbols(symbolArr);
		LiSetResolver resolver = new LiSetResolver(symbolArr, terminals, setsArr);
		resolver.resolve(expansionContext, problems);
	}

	private void expandNonterminal(Nonterminal n, ExpansionContext context) {
		for (RhsSequence r : ((LiRhsRoot) n.getDefinition()).preprocess()) {
			List<RhsSymbol[]> expanded = ((LiRhsPart) r).expand(context);
			for (RhsSymbol[] arr : expanded) {
				rules.add(new LiRule(rules.size(), n, arr, ((LiRhsSequence) r).getPrio(), r));
			}
		}
	}

	private int sortAndEnumerateSymbols(LiSymbol[] result) {
		int terminals = 0;
		for (LiSymbol s : symbols) {
			if (s.isTerm()) terminals++;
		}
		int term_index = 0, nonterm_index = terminals;
		for (LiSymbol s : symbols) {
			if (s.isTerm()) {
				result[term_index++] = s;
			} else {
				result[nonterm_index++] = s;
			}
		}
		assert term_index == terminals;
		assert nonterm_index == result.length;
		for (int i = 0; i < result.length; i++) {
			result[i].setIndex(i);
		}
		return terminals;
	}

	private void assignNames() {
		Map<String, Integer> lastIndex = new HashMap<String, Integer>();
		for (Entry<Nonterminal, String> e : anonymousNames.entrySet()) {
			String baseName = e.getValue();
			int index = lastIndex.containsKey(baseName) ? lastIndex.get(baseName) : 0;
			String name = index == 0 ? baseName : baseName + index;
			while (symbolsMap.containsKey(name)) {
				name = baseName + (++index);
			}
			lastIndex.put(baseName, index + 1);
			((LiNonterminal) e.getKey()).setName(name);
		}
	}

	private void annotateNullables() {
		// build back dependencies and mark first layer of nullable nonterminals
		Map<Nonterminal, List<Nonterminal>> backDependencies = new HashMap<Nonterminal, List<Nonterminal>>();
		List<Nonterminal> dependencies = new ArrayList<Nonterminal>();
		Set<Nonterminal> candidates = new HashSet<Nonterminal>();
		for (Symbol s : symbols) {
			if (s.isTerm()) continue;

			Nonterminal n = (Nonterminal) s;
			dependencies.clear();
			boolean nullable = RhsUtil.isNullable(n.getDefinition(), dependencies);
			if (nullable) {
				((LiNonterminal) n).setNullable(true);
			} else if (!dependencies.isEmpty()) {
				candidates.add(n);
				for (Nonterminal dep : dependencies) {
					if (dep == n) continue;
					List<Nonterminal> dependOn = backDependencies.get(dep);
					if (dependOn == null) {
						dependOn = new ArrayList<Nonterminal>();
						backDependencies.put(dep, dependOn);
					}
					dependOn.add(n);
				}
			}
		}

		// effectively invalidate potentially nullable nonterminals
		Queue<Nonterminal> queue = new LinkedList<Nonterminal>(candidates);
		Set<Nonterminal> inQueue = new HashSet<Nonterminal>(candidates);
		Nonterminal next;
		while ((next = queue.poll()) != null) {
			assert !next.isNullable();
			if (RhsUtil.isNullable(next.getDefinition(), null)) {
				((LiNonterminal) next).setNullable(true);

				List<Nonterminal> deps = backDependencies.get(next);
				if (deps != null) {
					for (Nonterminal dep : deps) {
						if (!dep.isNullable() && !inQueue.contains(dep) && candidates.contains(dep)) {
							queue.add(dep);
							inQueue.add(dep);
						}
					}
				}
			}
			inQueue.remove(next);
		}
	}
}
