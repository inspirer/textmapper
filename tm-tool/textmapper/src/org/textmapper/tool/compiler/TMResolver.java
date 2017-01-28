/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
import org.textmapper.lapg.api.TemplateParameter.Modifier;
import org.textmapper.lapg.api.TemplateParameter.Type;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.rule.RhsArgument;
import org.textmapper.lapg.builder.GrammarFacade;
import org.textmapper.tool.common.ObjectUtil;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.TMTree.TMProblem;
import org.textmapper.tool.parser.ast.*;
import org.textmapper.tool.parser.ast.TmaArgument.TmaBoolKind;

import java.util.*;
import java.util.stream.Collectors;

/**
 * evgeny, 1/16/13
 */
public class TMResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver";
	public static final Name INITIAL_STATE = LapgCore.name("initial");

	private final TMTree<TmaInput> tree;
	private final GrammarBuilder builder;
	private final AstBuilder rawTypesBuilder;

	private final Namespace<NamedElement> namespace = new Namespace<>();
	private final Namespace<LexerState> lexerStates = new Namespace<>();
	private final Map<String, RegexPart> namedPatternsMap = new HashMap<>();

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
		return lexerStates.resolve(name, null, LexerState.class);
	}

	public List<LexerState> allStates() {
		return lexerStates.getElements();
	}

	public List<LexerState> inclusiveStates() {
		return lexerStates.getElements()
				.stream()
				.filter(s -> !TMDataUtil.isExclusive(s))
				.collect(Collectors.toList());
	}

	public Symbol getSymbol(String name) {
		return namespace.resolve(name, null, Symbol.class);
	}

	public RegexContext createRegexContext() {
		return LapgCore.createContext(namedPatternsMap);
	}

	public void collectSymbols() {
		namespace.insert(builder.getEoi());

		collectLexerStates();
		collectLexerSymbols();

		if (tree.getRoot().getParser() != null) {
			collectNonterminals();
			collectParameters();
		}
	}

	private void collectLexerStates() {
		TmaIdentifier initialOrigin = null;
		boolean initialExclusive = false;

		for (TmaStatesClause clause : getLexerParts(TmaStatesClause.class)) {
			for (TmaLexerState state : clause.getStates()) {
				if (state.getName().getID().equals(INITIAL_STATE.text())) {
					if (initialOrigin != null) {
						error(state, "redeclaration of `initial', ignored");
						continue;
					}
					initialOrigin = state.getName();
					initialExclusive = clause.isExclusive();
				}
			}

		}

		LexerState initial = builder.addState(INITIAL_STATE, initialOrigin);
		lexerStates.insert(initial);
		if (initialExclusive) {
			TMDataUtil.makeExclusive(initial);
		}

		for (TmaStatesClause clause : getLexerParts(TmaStatesClause.class)) {
			boolean exclusive = clause.isExclusive();
			for (TmaLexerState state : clause.getStates()) {
				Name name = name(state.getName().getID(), state.getName());
				if (name == null || state.getName().getID().equals(INITIAL_STATE.text())) {
					continue;
				}

				LexerState lexerState =
						lexerStates.resolve(name.text(), null, LexerState.class);
				if (lexerState == null) {
					lexerState = builder.addState(name, state.getName());
					lexerStates.insert(lexerState);
					if (exclusive) {
						TMDataUtil.makeExclusive(lexerState);
					}
				} else {
					error(state, "redeclaration of `" + name.text() + "', ignored");
				}
			}
		}
	}

	<T extends ITmaLexerPart> Iterable<T> getLexerParts(Class<T> c) {
		return () -> new Iterator<T>() {
			Stack<Iterator<ITmaLexerPart>> stack = new Stack<>();
			T next;

			{
				stack.add(tree.getRoot().getLexer().iterator());
				fetch();
			}

			private void fetch() {
				next = null;
				while (!stack.empty()) {
					if (!stack.peek().hasNext()) {
						stack.pop();
						continue;
					}
					ITmaLexerPart next = stack.peek().next();
					if (c.isInstance(next)) {
						this.next = (T) next;
						return;
					}
					if (next instanceof TmaStartConditionsScope) {
						stack.push(((TmaStartConditionsScope) next).getLexerParts().iterator());
					}
				}
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public T next() {
				T r = next;
				fetch();
				return r;
			}
		};
	}

	private void collectLexerSymbols() {
		for (TmaNamedPattern astpattern : getLexerParts(TmaNamedPattern.class)) {
			Name name = name(astpattern.getName(), astpattern);
			if (name == null) continue;
			RegexPart regex;
			try {
				regex = LapgCore.parse(name.text(), astpattern.getPattern().getRegexp());
			} catch (RegexParseException e) {
				error(astpattern.getPattern(), e.getMessage());
				continue;
			}
			if (namedPatternsMap.get(name.text()) != null) {
				error(astpattern, "redeclaration of named pattern `" + name + "', ignored");
			} else {
				builder.addPattern(name, regex, astpattern);
				namedPatternsMap.put(name.text(), regex);
			}
		}
		for (TmaLexeme lexeme : getLexerParts(TmaLexeme.class)) {
			create(lexeme.getName(), convertRawType(lexeme.getRawType(), lexeme), true);
		}
	}

	private AstType convertRawType(ITmaNontermType type, SourceElement origin) {
		if (!(type instanceof TmaRawType)) {
			return null;
		}
		String text = type.getText();
		return rawTypesBuilder.rawType(text.substring(1, text.length() - 1), origin);
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
			if (clause instanceof TmaTemplateParam) {
				TmaTemplateParam param = (TmaTemplateParam) clause;
				Name name = name(param.getName().getID(), param.getName());
				if (name == null) continue;

				Type type = (param.getParamType() == TmaParamType.LFLAG) ? Type.Flag : Type.Symbol;

				Modifier mod = Modifier.Default;
				if (param.getModifier() != null) {
					switch (param.getModifier()) {
						case LEXPLICIT:
							mod = Modifier.Explicit;
							break;
						case LGLOBAL:
							mod = Modifier.Global;
							break;
						case LLOOKAHEAD:
							mod = Modifier.Lookahead;
							break;
					}
				}
				createParameter(name, type, mod, param.getParamValue(), param);
			}
		}
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNonterm) {
				TmaNonterm nonterm = (TmaNonterm) clause;
				Symbol s = getSymbol(nonterm.getName().getID());

				// Error is already reported.
				if (!(s instanceof Nonterminal)) continue;
				if (nonterm.getParams() == null || nonterm.getParams().getList() == null) continue;

				List<TemplateParameter> parameters = new ArrayList<>();
				for (ITmaNontermParam param : nonterm.getParams().getList()) {
					if (param instanceof TmaParamRef) {
						TemplateParameter p = resolveParam((TmaParamRef) param, null /* global */);
						if (p != null) parameters.add(p);
					} else if (param instanceof TmaInlineParameter) {
						TmaInlineParameter tmaParam = (TmaInlineParameter) param;
						Name subname = name(tmaParam.getName().getID(), tmaParam.getName());
						if (subname == null) continue;
						Name name = s.getName().subName(subname);
						Type type;
						switch (tmaParam.getParamType()) {
							case "flag":
								type = Type.Flag;
								break;
							case "param":
								type = Type.Symbol;
								break;
							default:
								error(param, "Syntax error.");
								continue;
						}

						TemplateParameter p = createParameter(name, type, Modifier.Explicit,
								tmaParam.getParamValue(), param);
						if (p == null) continue;

						parameters.add(p);
					} else {
						throw new IllegalStateException();
					}
				}
				if (!parameters.isEmpty()) {
					s.putUserData(Nonterminal.UD_TEMPLATE_PARAMS, parameters);
				}
			}
		}
	}

	private String asString(TemplateParameter.Type type) {
		switch (type) {
			case Flag:
				return "bool";
			case Symbol:
				return "symbol";
		}

		throw new IllegalStateException();
	}

	Object getParamValue(TemplateParameter.Type expectedType, ITmaParamValue paramValue) {
		if (paramValue instanceof TmaLiteral) {
			Object literalVal = ((TmaLiteral) paramValue).getValue();
			if (literalVal instanceof Boolean && expectedType == Type.Flag) {
				return literalVal;
			}
			error(paramValue, "type error: " + asString(expectedType) + " is expected");

		} else if (paramValue instanceof TmaSymref) {
			if (expectedType == Type.Symbol) {
				return resolve((TmaSymref) paramValue);
			}
			error(paramValue, "type error: " + asString(expectedType) + " is expected");
		} else {
			throw new IllegalStateException();
		}
		return null;
	}

	private TemplateParameter createParameter(Name name, TemplateParameter.Type type,
											  TemplateParameter.Modifier m,
											  ITmaParamValue paramValue,
											  TextSourceElement origin) {
		NamedElement existing = namespace.canInsert(name);
		if (existing != null) {
			nameConflict(name.text(), origin, existing);
			return null;
		}
		Object defaultValue = paramValue == null ? null :
				getParamValue(type, paramValue);
		TemplateParameter p = builder.addParameter(type, name, defaultValue, m, origin);
		namespace.insert(p);
		return p;
	}


	private Symbol create(TmaIdentifier id, AstType type, boolean isTerm) {
		Name name = name(id.getID(), id);
		if (name == null) return null;

		NamedElement existing = namespace.canInsert(name);
		if (existing != null && !(existing instanceof Symbol)) {
			nameConflict(id.getID(), id, existing);
			existing = null;
		}

		Symbol sym = (Symbol) existing;
		if (sym != null) {
			if (sym.isTerm() != isTerm) {
				String symKind = sym.isTerm() ? "terminal" : "non-terminal";
				error(id, "redeclaration of " + symKind + ": " + name);
			} else if (!(ObjectUtil.safeEquals(sym.getType(), type))) {
				String newType = type == null ? "<empty>" : type.toString();
				String existingType = sym.getType() == null ? "<empty>" : sym.getType().toString();
				error(id, "redeclaration of type: " + newType + " instead of " + existingType);
			}
		} else {
			sym = isTerm
					? builder.addTerminal(name, type, id)
					: builder.addNonterminal(name, id);
			if (type != null && !isTerm) {
				builder.map((Nonterminal) sym, type);
			}
			namespace.insert(sym);
		}
		return sym;
	}

	private Map<String, Integer> lastIndex = new HashMap<>();

	Nonterminal createNestedNonTerm(Nonterminal outer, ITmaNode source) {
		final String base_ = outer.getNameText() + "$";
		int index = lastIndex.containsKey(base_) ? lastIndex.get(base_) : 1;

		Name name = outer.getName().subName(LapgCore.name("n" + index));
		while (namespace.canInsert(name) != null) {
			index++;
			name = outer.getName().subName(LapgCore.name("n" + index));
		}
		lastIndex.put(base_, index + 1);

		Nonterminal sym = builder.addNonterminal(name, source);
		namespace.insert(sym);
		return sym;
	}

	Collection<RhsArgument> resolveArgs(TmaSymref ref, Nonterminal context) {
		TemplateParameter targetParam = tryResolveParam(ref, context);
		Symbol target = targetParam != null ? null : getSymbol(ref.getName());
		TmaSymrefArgs args = ref.getArgs();
		if (target != null && !(target instanceof Nonterminal)) {
			if (args != null) {
				error(args, "Only nonterminals and template parameters can be templated.");
			}
			return null;
		}

		Nonterminal nonterm = (Nonterminal) target;
		List<TemplateParameter> requiredParameters = templateParams(nonterm);
		List<RhsArgument> result = null;

		Set<TemplateParameter> provided = new HashSet<>();
		if (args != null && args.getArgList() != null) {
			result = new ArrayList<>(args.getArgList().size());
			for (TmaArgument arg : args.getArgList()) {
				TemplateParameter param = resolveParam(arg.getName(), nonterm);
				if (param == null) continue;

				if (param.getModifier() != Modifier.Global
						&& param.getModifier() != Modifier.Lookahead
						&& (requiredParameters == null || !requiredParameters.contains(param))) {
					error(arg, "Template parameter " + arg.getName() + " is not expected by "
							+ ref.getName());
					continue;
				}

				provided.add(param);
				if (arg.getVal() != null) {
					if (arg.getVal() instanceof TmaSymref) {
						TemplateParameter source = tryResolveParam(
								(TmaSymref) arg.getVal(), context);
						if (source != null) {
							result.add(builder.argument(param, source, null, arg));
							continue;
						}
					}
					Object val = getParamValue(param.getType(), arg.getVal());
					if (val != null) result.add(builder.argument(param, null, val, arg));

				} else if (arg.getBool() != null) {
					boolean val = (arg.getBool() == TmaBoolKind.PLUS);
					if (param.getType() != Type.Flag) {
						error(arg, "type error: " + asString(param.getType()) + " is expected");
						continue;
					}
					result.add(builder.argument(param, null, val, arg));
				} else {
					TemplateParameter source = resolveParam(arg.getName(), context);
					if (source == null) {
						error(arg, "cannot resolve " + arg.getName()
								+ " in " + context.getNameText());
						continue;
					} else if (source != param) {
						error(arg, arg.getName() + " has different meanings in "
								+ context.getNameText() + " and "
								+ (nonterm != null ? nonterm.getNameText() : "globally"));
						continue;
					}
					result.add(builder.argument(param, source, null, arg));
				}
			}
			if (result.isEmpty()) result = null;
		}

		if (requiredParameters != null) {
			// Forward context parameters, or use defaults.
			List<TemplateParameter> availParams = templateParams(context);
			for (TemplateParameter p : requiredParameters) {
				if (provided.contains(p) || p.getModifier() == Modifier.Global ||
						p.getModifier() == Modifier.Lookahead) {
					continue;
				}
				if (availParams != null && availParams.contains(p) &&
						p.getModifier() != Modifier.Explicit) {
					provided.add(p);
					if (result == null) {
						result = new ArrayList<>(requiredParameters.size());
					}
					result.add(builder.argument(p, p, null, ref));
				} else if (p.getDefaultValue() != null) {
					provided.add(p);
					if (result == null) {
						result = new ArrayList<>(requiredParameters.size());
					}
					result.add(builder.argument(p, null, p.getDefaultValue(), ref));
				}
			}

			// Report unfulfilled parameters.
			String unfulfilled = requiredParameters.stream()
					.filter(p -> !provided.contains(p))
					.map(TemplateParameter::getNameText)
					.collect(Collectors.joining(", "));

			if (!unfulfilled.isEmpty()) {
				error(ref, "Required parameters are not provided: " + unfulfilled);
			}
		}

		if (targetParam != null && targetParam.getModifier() == Modifier.Global &&
				!provided.contains(targetParam)) {
			// One should use X<X> to make X available in X.
			return Collections.singleton(builder.argument(targetParam, null, null, ref));
		}
		return result;
	}

	TemplateParameter resolveParam(TmaParamRef ref, Nonterminal context) {
		TemplateParameter param =
				namespace.resolve(ref.getRef().getID(),
						context == null ? null : context.getName(), TemplateParameter.class);
		if (param != null) return param;

		error(ref, ref.getRef().getID() + " cannot be resolved");
		return null;
	}

	List<TemplateParameter> templateParams(Nonterminal nonterm) {
		@SuppressWarnings("unchecked")
		List<TemplateParameter> params = (nonterm == null)
				? null
				: (List<TemplateParameter>) nonterm.getUserData(Nonterminal.UD_TEMPLATE_PARAMS);
		return params == null || params.isEmpty() ? null : params;
	}

	TemplateParameter tryResolveParam(TmaSymref id, Nonterminal context) {
		return namespace.resolve(id.getName(),
				context == null ? null : context.getName(), TemplateParameter.class);
	}

	Symbol resolve(TmaSymref id) {
		String name = id.getName();
		Symbol sym = namespace.resolve(name, null, Symbol.class);
		if (sym == null) {
			// TODO make "opt" configurable in options
			if (name.length() > 3 && name.endsWith("opt")) {
				sym = namespace.resolve(name.substring(0, name.length() - 3), null, Symbol.class);
				if (sym != null) {
					TmaIdentifier tmaId = new TmaIdentifier(id.getName(), id.getSource(),
							id.getLine(), id.getOffset(), id.getEndoffset());
					Nonterminal symopt = (Nonterminal) create(tmaId, sym.getType(), false);
					if (symopt == null) return null;
					builder.addRule(symopt,
							builder.asSequence(
									builder.optional(
											builder.symbolFwdAll(sym, id), id)));
					symopt.putUserData(Nonterminal.UD_TEMPLATE_PARAMS,
							sym.getUserData(Nonterminal.UD_TEMPLATE_PARAMS));
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	void error(TextSourceElement n, String message) {
		if (n == null || message == null) return;
		tree.getErrors().add(new LapgResolverProblem(
				TMTree.KIND_ERROR, n.getLine(), n.getOffset(), n.getEndoffset(), message));
	}

	void nameConflict(String newName, TextSourceElement anchor, NamedElement existing) {
		error(anchor, newName + " conflicts with " + existing.toString());
	}

	Name name(String name, TextSourceElement anchor) {
		try {
			return LapgCore.name(name);
		} catch (NameParseException ex) {
			error(anchor, ex.getMessage());
			return null;
		}
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
