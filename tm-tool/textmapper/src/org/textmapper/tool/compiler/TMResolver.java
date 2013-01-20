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

import org.textmapper.lapg.api.LexerState;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.tool.parser.LapgTree;
import org.textmapper.tool.parser.LapgTree.LapgProblem;
import org.textmapper.tool.parser.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * evgeny, 1/16/13
 */
public class TMResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver"; //$NON-NLS-1$
	static final String INITIAL_STATE = "initial";

	private final LapgTree<AstRoot> tree;
	private final GrammarBuilder builder;

	private final Map<String, LexerState> statesMap = new HashMap<String, LexerState>();
	private final Map<String, Symbol> symbolsMap = new HashMap<String, Symbol>();

	public TMResolver(LapgTree<AstRoot> tree, GrammarBuilder builder) {
		this.tree = tree;
		this.builder = builder;
	}

	public void collectSymbols() {
		symbolsMap.put(Symbol.EOI, builder.getEoi());

		collectLexerStates();
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

	public LexerState getState(String name) {
		return statesMap.get(name);
	}

	public Symbol getSymbol(String name) {
		return symbolsMap.get(name);
	}

	// TODO make private
	Symbol create(AstIdentifier id, String type, int kind, Terminal softClass) {
		String name = id.getName();
		if (symbolsMap.containsKey(name)) {
			Symbol sym = symbolsMap.get(name);
			if (sym.getKind() != kind) {
				error(id, "redeclaration of " + sym.kindAsString() + ": " + name);
			} else if (!UniqueNameHelper.safeEquals(sym.getType(), type) && !(kind == Symbol.KIND_SOFTTERM && type == null)) {
				error(id,
						"redeclaration of type: " + (type == null ? "<empty>" : type) + " instead of "
								+ (sym.getType() == null ? "<empty>" : sym.getType()));
			} else if (kind == Symbol.KIND_SOFTTERM && softClass != ((Terminal) sym).getSoftClass()) {
				Symbol symSoftClass = ((Terminal) sym).getSoftClass();
				error(id, "redeclaration of soft class: " + (softClass == null ? "<undefined>" : softClass.getName())
						+ " instead of " + (symSoftClass == null ? "<undefined>" : symSoftClass.getName()));
			}
			return sym;
		} else {
			Symbol sym;
			if (kind == Symbol.KIND_SOFTTERM) {
				sym = builder.addSoftTerminal(name, softClass, id);
			} else if (kind == Symbol.KIND_NONTERM) {
				sym = builder.addNonterminal(name, type, id);
			} else if (kind == Symbol.KIND_TERM) {
				sym = builder.addTerminal(name, type, id);
			} else {
				throw new IllegalArgumentException();
			}
			symbolsMap.put(name, sym);
			return sym;
		}
	}

	private Map<String, Integer> lastIndex = new HashMap<String, Integer>();

	Symbol createNested(int kind, String type, Symbol outer, Terminal softClass, IAstNode source) {
		final String base_ = outer.getName() + "$";
		int index = lastIndex.containsKey(base_) ? lastIndex.get(base_) : 1;
		while (symbolsMap.containsKey(base_ + index)) {
			index++;
		}
		String name = base_ + index;

		Symbol sym;
		if (kind == Symbol.KIND_SOFTTERM) {
			sym = builder.addSoftTerminal(name, softClass, source);
		} else if (kind == Symbol.KIND_NONTERM) {
			sym = builder.addNonterminal(name, type, source);
		} else if (kind == Symbol.KIND_TERM) {
			sym = builder.addTerminal(name, type, source);
		} else {
			throw new IllegalArgumentException();
		}

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
							sym.getType(), Symbol.KIND_NONTERM, null);
					// TODO replace next 2 lines with: ... builder.optional(builder.symbol(null, sym, null, id), id)
					builder.addRule(null, symopt, builder.empty(id), null);
					builder.addRule(null, symopt, builder.symbol(null, sym, null, id), null);
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	void error(IAstNode n, String message) {
		tree.getErrors().add(new LapgResolverProblem(LapgTree.KIND_ERROR, n.getOffset(), n.getEndOffset(), message));
	}

	private static class LapgResolverProblem extends LapgProblem {
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
