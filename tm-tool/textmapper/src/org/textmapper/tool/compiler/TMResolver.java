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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.LexerState;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.TMTree.TMProblem;
import org.textmapper.tool.parser.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * evgeny, 1/16/13
 */
public class TMResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver"; //$NON-NLS-1$
	public static final String INITIAL_STATE = "initial"; //$NON-NLS-1$

	private final TMTree<AstRoot> tree;
	private final GrammarBuilder builder;

	private final Map<String, LexerState> statesMap = new HashMap<String, LexerState>();
	private final Map<String, Symbol> symbolsMap = new HashMap<String, Symbol>();
	private final Map<String, RegexPart> namedPatternsMap = new HashMap<String, RegexPart>();

	public TMResolver(TMTree<AstRoot> tree, GrammarBuilder builder) {
		this.tree = tree;
		this.builder = builder;
	}

	public TMTree<AstRoot> getTree() {
		return tree;
	}

	public GrammarBuilder getBuilder() {
		return builder;
	}

	public LexerState getState(String name) {
		return statesMap.get(name);
	}

	public Symbol getSymbol(String name) {
		return symbolsMap.get(name);
	}

	public RegexContext createRegexContext() {
		return LapgCore.createContext(namedPatternsMap);
	}

	public void collectSymbols() {
		symbolsMap.put(Symbol.EOI, builder.getEoi());

		collectLexerStates();
		collectLexerSymbols();

		if (tree.getRoot().getGrammar() != null) {
			collectNonterminals();
		}
	}

	private void collectLexerStates() {
		AstIdentifier initialOrigin = null;
		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstStateSelector) {
				for (AstLexerState state : ((AstStateSelector) clause).getStates()) {
					if (state.getName().getName().equals(INITIAL_STATE)) {
						initialOrigin = state.getName();
						break;
					}
				}
				if (initialOrigin != null) {
					break;
				}
			}
		}

		statesMap.put(INITIAL_STATE, builder.addState(INITIAL_STATE, initialOrigin));
		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstStateSelector) {
				AstStateSelector selector = (AstStateSelector) clause;
				for (AstLexerState state : selector.getStates()) {
					String name = state.getName().getName();
					if (!statesMap.containsKey(name)) {
						statesMap.put(name, builder.addState(name, state.getName()));
					}
				}
			}
		}
	}

	private void collectLexerSymbols() {
		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				create(lexeme.getName(), lexeme.getType(), true);

			} else if (clause instanceof AstNamedPattern) {
				AstNamedPattern astpattern = (AstNamedPattern) clause;
				String name = astpattern.getName();
				RegexPart regex;
				try {
					regex = LapgCore.parse(name, astpattern.getRegexp().getRegexp());
				} catch (RegexParseException e) {
					error(astpattern.getRegexp(), e.getMessage());
					continue;
				}
				if (namedPatternsMap.get(name) != null) {
					error(astpattern, "redeclaration of named pattern `" + name + "', ignored");
				} else {
					builder.addPattern(name, regex, astpattern);
					namedPatternsMap.put(name, regex);
				}
			}
		}
	}

	private void collectNonterminals() {
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				create(nonterm.getName(), nonterm.getType(), false);
			}
		}
	}

	private Symbol create(AstIdentifier id, String type, boolean isTerm) {
		String name = id.getName();
		if (symbolsMap.containsKey(name)) {
			Symbol sym = symbolsMap.get(name);
			if (sym.isTerm() != isTerm) {
				error(id, "redeclaration of " + (sym.isTerm() ? "terminal" : "non-terminal") + ": " + name);
			} else if (!(UniqueNameHelper.safeEquals(sym.getType(), type))) {
				error(id,
						"redeclaration of type: " + (type == null ? "<empty>" : type) + " instead of "
								+ (sym.getType() == null ? "<empty>" : sym.getType()));
			}
			return sym;
		} else {
			Symbol sym = isTerm ? builder.addTerminal(name, type, id) : builder.addNonterminal(name, type, id);
			symbolsMap.put(name, sym);
			return sym;
		}
	}

	private Map<String, Integer> lastIndex = new HashMap<String, Integer>();

	Symbol createNestedNonTerm(Symbol outer, IAstNode source) {
		final String base_ = outer.getName() + "$";
		int index = lastIndex.containsKey(base_) ? lastIndex.get(base_) : 1;
		while (symbolsMap.containsKey(base_ + index)) {
			index++;
		}
		String name = base_ + index;

		Symbol sym = builder.addNonterminal(name, null, source);
		symbolsMap.put(name, sym);
		lastIndex.put(base_, index + 1);
		return sym;
	}

	Nonterminal createDerived(Symbol element, String suffix, IAstNode source) {
		final String base_ = element.getName() + suffix;
		int index = lastIndex.containsKey(base_) ? lastIndex.get(base_) : 0;
		while (symbolsMap.containsKey(index == 0 ? base_ : base_ + index)) {
			index++;
		}
		String name = index == 0 ? base_ : base_ + index;
		Nonterminal sym = builder.addNonterminal(name, null, source);
		symbolsMap.put(name, sym);
		lastIndex.put(base_, index + 1);
		return sym;
	}

	Symbol resolve(AstReference id) {
		String name = id.getName();
		Symbol sym = symbolsMap.get(name);
		if (sym == null) {
			if (name.length() > 3 && name.endsWith("opt")) {
				sym = symbolsMap.get(name.substring(0, name.length() - 3));
				if (sym != null) {
					Nonterminal symopt = (Nonterminal) create(
							new AstIdentifier(id.getName(), id.getInput(), id.getOffset(), id.getEndOffset()),
							sym.getType(), false);
					builder.addRule(null, symopt, builder.optional(builder.symbol(null, sym, null, id), id), null);
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	void error(IAstNode n, String message) {
		tree.getErrors().add(new LapgResolverProblem(TMTree.KIND_ERROR, n.getOffset(), n.getEndOffset(), message));
	}

	private static class LapgResolverProblem extends TMProblem {
		private static final long serialVersionUID = 3810706800688899470L;

		public LapgResolverProblem(int kind, int offset, int endoffset, String message) {
			super(kind, offset, endoffset, message, null);
		}

		@Override
		public String getSource() {
			return RESOLVER_SOURCE;
		}
	}
}
