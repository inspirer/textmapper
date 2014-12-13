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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.TemplateParameter.Type;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.rule.RhsArgument;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.builder.GrammarFacade;
import org.textmapper.tool.common.ObjectUtil;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.TMTree.TMProblem;
import org.textmapper.tool.parser.ast.*;

import java.util.*;

/**
 * evgeny, 1/16/13
 */
public class TMResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver"; //$NON-NLS-1$
	public static final String INITIAL_STATE = "initial"; //$NON-NLS-1$

	private final TMTree<TmaInput> tree;
	private final GrammarBuilder builder;
	private final AstBuilder rawTypesBuilder;

	private final Map<String, LexerState> statesMap = new HashMap<String, LexerState>();
	private final Map<String, Symbol> symbolsMap = new HashMap<String, Symbol>();
	private final Map<String, TemplateParameter> parametersMap = new HashMap<String, TemplateParameter>();
	private final Map<String, RegexPart> namedPatternsMap = new HashMap<String, RegexPart>();

	public TMResolver(TMTree<TmaInput> tree, GrammarBuilder builder) {
		this.tree = tree;
		this.builder = builder;
		this.rawTypesBuilder = GrammarFacade.createAstBuilder();
	}

	public TMTree<TmaInput> getTree() {
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

		if (tree.getRoot().getParser() != null) {
			collectNonterminals();
			collectParameters();
		}
	}

	private void collectLexerStates() {
		TmaIdentifier initialOrigin = null;
		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof TmaStateSelector) {
				for (TmaLexerState state : ((TmaStateSelector) clause).getStates()) {
					if (state.getName().getID().equals(INITIAL_STATE)) {
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
		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof TmaStateSelector) {
				TmaStateSelector selector = (TmaStateSelector) clause;
				for (TmaLexerState state : selector.getStates()) {
					String name = state.getName().getID();
					if (!statesMap.containsKey(name)) {
						statesMap.put(name, builder.addState(name, state.getName()));
					}
				}
			}
		}
	}

	private void collectLexerSymbols() {
		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof TmaLexeme) {
				TmaLexeme lexeme = (TmaLexeme) clause;
				create(lexeme.getName(), convertRawType(lexeme.getType(), lexeme), true);

			} else if (clause instanceof TmaNamedPattern) {
				TmaNamedPattern astpattern = (TmaNamedPattern) clause;
				String name = astpattern.getName();
				RegexPart regex;
				try {
					regex = LapgCore.parse(name, astpattern.getPattern().getRegexp());
				} catch (RegexParseException e) {
					error(astpattern.getPattern(), e.getMessage());
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

	private AstType convertRawType(String type, SourceElement origin) {
		return type != null
				? rawTypesBuilder.rawType(type, origin)
				: null;
	}

	private AstType convertRawType(ITmaNontermType type, SourceElement origin) {
		return type instanceof TmaNontermTypeRaw
				? rawTypesBuilder.rawType(((TmaNontermTypeRaw) type).getTypeText(), origin)
				: null;
	}

	private void collectNonterminals() {
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNonterm) {
				TmaNonterm nonterm = (TmaNonterm) clause;
				create(nonterm.getName(), convertRawType(nonterm.getType(), nonterm), false);
			}
		}
	}

	private void collectParameters() {
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNontermParam) {
				TmaNontermParam param = (TmaNontermParam) clause;
				String name = param.getName().getID();
				Symbol existingSym = symbolsMap.get(name);
				if (existingSym != null) {
					error(param.getName(), "redeclaration of " + (existingSym.isTerm() ? "terminal" : "non-terminal")
							+ ": " + name);
					continue;
				}

				parametersMap.put(name, create(param));
			}
		}
	}

	private String asString(TemplateParameter.Type type) {
		switch (type) {
			case Bool:
				return "bool";
			case Integer:
				return "int";
			case String:
				return "string";
			case Symbol:
				return "symbol";
		}

		throw new IllegalStateException();
	}

	private Object getParamValue(TemplateParameter.Type expectedType, ITmaParamValue paramValue) {
		if (paramValue instanceof TmaLiteral) {
			Object literalVal = ((TmaLiteral) paramValue).getValue();
			TemplateParameter.Type actualType = (literalVal instanceof Integer) ? Type.Integer :
					(literalVal instanceof String) ? Type.String :
							Type.Bool;
			if (actualType == expectedType) {
				return literalVal;
			}
			error(paramValue, "type error: " + asString(expectedType) + " is expected");

		} else if (paramValue instanceof TmaSymref) {
			if (expectedType == Type.Symbol) {
				return resolve((TmaSymref) paramValue);
			}
			error(paramValue, "type error: " + asString(expectedType) + " is expected");
		}
		return null;
	}

	private TemplateParameter create(TmaNontermParam param) {
		TemplateParameter.Type type;

		switch (param.getParamType()) {
			case LBOOL:
				type = Type.Bool;
				break;
			case LINT:
				type = Type.Integer;
				break;
			case LSTRING:
				type = Type.String;
				break;
			case LSYMBOL:
				type = Type.Symbol;
				break;
			default:
				throw new IllegalStateException();
		}

		return builder.addParameter(
				type, param.getName().getID(),
				getParamValue(type, param.getParamValue()),
				param);
	}

	private Symbol create(TmaIdentifier id, AstType type, boolean isTerm) {
		String name = id.getID();
		if (symbolsMap.containsKey(name)) {
			Symbol sym = symbolsMap.get(name);
			if (sym.isTerm() != isTerm) {
				error(id, "redeclaration of " + (sym.isTerm() ? "terminal" : "non-terminal") + ": " + name);
			} else if (!(ObjectUtil.safeEquals(sym.getType(), type))) {
				error(id,
						"redeclaration of type: " + (type == null ? "<empty>" : type) + " instead of "
								+ (sym.getType() == null ? "<empty>" : sym.getType()));
			}
			return sym;
		} else {
			Symbol sym = isTerm ? builder.addTerminal(name, type, id) : builder.addNonterminal(name, id);
			if (type != null && !isTerm) {
				builder.map((Nonterminal) sym, type);
			}
			symbolsMap.put(name, sym);
			return sym;
		}
	}

	private Map<String, Integer> lastIndex = new HashMap<String, Integer>();

	Symbol createNestedNonTerm(Symbol outer, ITmaNode source) {
		final String base_ = outer.getName() + "$";
		int index = lastIndex.containsKey(base_) ? lastIndex.get(base_) : 1;
		while (symbolsMap.containsKey(base_ + index)) {
			index++;
		}
		String name = base_ + index;

		Symbol sym = builder.addNonterminal(name, source);
		symbolsMap.put(name, sym);
		lastIndex.put(base_, index + 1);
		return sym;
	}

	Collection<RhsArgument> resolveArgs(TmaSymref ref) {
		TmaSymrefArgs args = ref.getArgs();
		if (args == null) return null;

		if (args.getValueList() != null) {
			// TODO
			error(args, "value list is not supported yet, use key:value syntax");
			return null;
		}

		List<RhsArgument> result = new ArrayList<RhsArgument>(args.getKeyvalueList().size());
		for (TmaKeyvalArg arg : args.getKeyvalueList()) {
			TemplateParameter param = resolveParam(arg.getName());
			if (param == null) continue;
			Object val = getParamValue(param.getType(), arg.getVal());
			result.add(builder.argument(param, val, arg));
		}
		return result.isEmpty() ? null : result;
	}

	TemplateParameter resolveParam(TmaIdentifier id) {
		TemplateParameter param = parametersMap.get(id.getID());
		if (param == null) {
			error(id, id.getID() + " cannot be resolved");
		}
		return param;
	}

	Symbol resolve(TmaSymref id) {
		String name = id.getName();
		Symbol sym = symbolsMap.get(name);
		if (sym == null) {
			if (name.length() > 3 && name.endsWith("opt")) {
				sym = symbolsMap.get(name.substring(0, name.length() - 3));
				if (sym != null) {
					TmaIdentifier tmaId = new TmaIdentifier(id.getName(), id.getSource(), id.getLine(),
							id.getOffset(), id.getEndoffset());
					Nonterminal symopt = (Nonterminal) create(tmaId, sym.getType(), false);
					builder.addRule(symopt, builder.sequence(null,
							Collections.<RhsPart>singleton(builder.optional(
									builder.symbol(sym, null, id),
									id)), id), null /* no precedence */);
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	void error(TextSourceElement n, String message) {
		if (n == null || message == null) return;
		tree.getErrors().add(
				new LapgResolverProblem(TMTree.KIND_ERROR, n.getLine(), n.getOffset(), n.getEndoffset(), message));
	}

	private static class LapgResolverProblem extends TMProblem {
		private static final long serialVersionUID = 3810706800688899470L;

		public LapgResolverProblem(int kind, int line, int offset, int endoffset, String message) {
			super(kind, message, line, offset, endoffset, null);
		}

		@Override
		public String getSource() {
			return RESOLVER_SOURCE;
		}
	}
}
