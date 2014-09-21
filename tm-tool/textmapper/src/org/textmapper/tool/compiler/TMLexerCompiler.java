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
import org.textmapper.lapg.api.ast.AstRawType;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexMatcher;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.ast.*;

import java.util.*;

/**
 * evgeny, 1/21/13
 */
public class TMLexerCompiler {

	private final TMTree<TmaInput> tree;
	private final TMResolver resolver;
	private final GrammarBuilder builder;

	private final Map<TmaLexeme, RuleAttributes> attributes = new HashMap<TmaLexeme, RuleAttributes>();

	public TMLexerCompiler(TMResolver resolver) {
		this.resolver = resolver;
		this.tree = resolver.getTree();
		this.builder = resolver.getBuilder();
	}

	private void error(ITmaNode n, String message) {
		resolver.error(n, message);
	}

	private List<LexerState> convertApplicableStates(TmaStateSelector selector) {
		List<LexerState> result = new ArrayList<LexerState>();
		for (TmaLexerState state : selector.getStates()) {
			LexerState applicable = resolver.getState(state.getName().getID());
			result.add(applicable);
		}
		return result;
	}

	private TMStateTransitionSwitch convertTransitions(TmaStateSelector selector) {
		boolean noDefault = false;
		for (TmaLexerState state : selector.getStates()) {
			if (state.getDefaultTransition() == null) {
				noDefault = true;
			}
		}

		LexerState defaultTransition = null;
		Map<LexerState, LexerState> stateSwitch = new LinkedHashMap<LexerState, LexerState>();
		for (TmaLexerState state : selector.getStates()) {
			if (state.getDefaultTransition() == null) {
				continue;
			}
			String targetName = state.getDefaultTransition().getName();
			LexerState target = resolver.getState(targetName);
			if (target == null) {
				error(state.getDefaultTransition(), targetName + " cannot be resolved");
				continue;
			}

			if (defaultTransition == null && !(noDefault)) {
				defaultTransition = target;
			} else if (defaultTransition != target) {
				LexerState source = resolver.getState(state.getName().getID());
				stateSwitch.put(source, target);
			}
		}
		return stateSwitch.isEmpty() && defaultTransition == null ? null
				: new TMStateTransitionSwitch(stateSwitch.isEmpty() ? null : stateSwitch, defaultTransition);
	}

	private TMStateTransitionSwitch getTransition(TmaLexeme lexeme, TMStateTransitionSwitch active) {
		TmaStateref transition = lexeme.getTransition();
		if (transition != null) {
			String targetName = transition.getName();
			LexerState target = resolver.getState(targetName);
			if (target == null) {
				error(transition, targetName + " cannot be resolved");
			} else {
				return new TMStateTransitionSwitch(target);
			}
		}
		return active;
	}

	private LexerRule getClassRule(Map<LexerRule, RegexMatcher> classMatchers, TmaLexeme l, RegexPart regex) {
		LexerRule result = null;
		TmaLexemeAttrs attrs = l.getAttrs();
		boolean isClass = attrs != null && attrs.getKind() == TmaLexemeAttribute.LCLASS;
		if (regex.isConstant() && !isClass) {
			for (LexerRule rule : classMatchers.keySet()) {
				TmaLexeme astClassLexeme = (TmaLexeme) ((DerivedSourceElement) rule).getOrigin();
				if (!attributes.get(astClassLexeme).canBeClassFor(attributes.get(l))) {
					continue;
				}
				RegexMatcher m = classMatchers.get(rule);
				if (m.matches(regex.getConstantValue())) {
					if (result != null) {
						error(l, "regex matches two classes `" + result.getSymbol().getName() + "' and `"
								+ rule.getSymbol().getName() + "', using first");
					} else {
						result = rule;
					}
				}
			}
		}
		return result;
	}

	public int getLexerRuleKind(TmaLexemeAttrs attr) {
		if (attr == null) {
			return LexerRule.KIND_NONE;
		}
		switch (attr.getKind()) {
			case LCLASS:
				return LexerRule.KIND_CLASS;
			case LLAYOUT:
				return LexerRule.KIND_LAYOUT;
			case LSOFT:
				return LexerRule.KIND_SOFT;
			case LSPACE:
				return LexerRule.KIND_SPACE;
		}

		return LexerRule.KIND_NONE;
	}

	public void compile() {
		Map<Terminal, Terminal> softToClass = new HashMap<Terminal, Terminal>();
		Set<Terminal> nonSoft = new HashSet<Terminal>();

		// Step 1. Collect states & transitions (attributes).

		TMStateTransitionSwitch activeTransitions = null;
		List<LexerState> activeStates = Collections.singletonList(resolver.getState(TMResolver.INITIAL_STATE));

		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof TmaLexeme) {
				TmaLexeme lexeme = (TmaLexeme) clause;
				attributes.put(lexeme, new RuleAttributes(getTransition(lexeme, activeTransitions), activeStates));
			} else if (clause instanceof TmaStateSelector) {
				activeStates = convertApplicableStates((TmaStateSelector) clause);
				activeTransitions = convertTransitions((TmaStateSelector) clause);
			}
		}

		// Step 2. Process class lexical rules.

		RegexContext context = resolver.createRegexContext();
		Map<LexerRule, RegexMatcher> classMatchers = new LinkedHashMap<LexerRule, RegexMatcher>();

		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			if (!(clause instanceof TmaLexeme)) {
				continue;
			}
			TmaLexeme lexeme = (TmaLexeme) clause;
			TmaLexemeAttrs attrs = lexeme.getAttrs();
			if (attrs == null || attrs.getKind() != TmaLexemeAttribute.LCLASS) {
				continue;
			}
			if (lexeme.getPattern() == null) {
				error(lexeme, "class lexeme rule without regular expression, ignored");
				continue;
			}

			Symbol s = resolver.getSymbol(lexeme.getName().getID());
			if (!(s instanceof Terminal)) {
				// not a terminal? already reported, ignore
				continue;
			}
			Terminal classTerm = (Terminal) s;
			nonSoft.add(classTerm);

			RegexPart regex;
			RegexMatcher matcher;
			try {
				regex = LapgCore.parse(s.getName(), lexeme.getPattern().getRegexp());
				matcher = LapgCore.createMatcher(regex, context);
			} catch (RegexParseException e) {
				error(lexeme.getPattern(), e.getMessage());
				continue;
			}

			int priority = lexeme.getPriority() == null ? 0 : lexeme.getPriority();
			LexerRule liLexerRule = builder.addLexerRule(LexerRule.KIND_CLASS, classTerm, regex,
					attributes.get(lexeme).getApplicableInStates(), priority,
					null, lexeme);
			classMatchers.put(liLexerRule, matcher);
			TMDataUtil.putCode(liLexerRule, lexeme.getCommand());
			TMDataUtil.putTransition(liLexerRule, attributes.get(lexeme).getTransitions());
		}

		// Step 3. Process other lexical rules. Match soft lexemes with their classes.

		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			if (!(clause instanceof TmaLexeme)) {
				continue;
			}
			TmaLexeme lexeme = (TmaLexeme) clause;
			TmaLexemeAttrs attrs = lexeme.getAttrs();
			int kind = getLexerRuleKind(attrs);
			if (kind == LexerRule.KIND_CLASS) {
				continue;
			}

			Symbol s = resolver.getSymbol(lexeme.getName().getID());
			if (!(s instanceof Terminal)) {
				// not a terminal? already reported, ignore
				continue;
			}
			Terminal term = (Terminal) s;

			boolean isSoft = (kind == LexerRule.KIND_SOFT);
			if (isSoft && nonSoft.contains(term)) {
				error(lexeme, "redeclaration of non-soft terminal: " + lexeme.getName());
				continue;
			} else if (!isSoft) {
				if (softToClass.containsKey(term)) {
					error(lexeme, "redeclaration of soft terminal: " + lexeme.getName());
					continue;
				}
				nonSoft.add(term);
			}

			if (lexeme.getPattern() == null) {
				if (isSoft) {
					error(lexeme, "soft lexeme rule `" + lexeme.getName().getID() +
							"' should have a regular expression");
				}
				continue;
			}

			String name = lexeme.getName().getID();
			RegexPart regex;
			try {
				regex = LapgCore.parse(name, lexeme.getPattern().getRegexp());
			} catch (RegexParseException e) {
				error(lexeme.getPattern(), e.getMessage());
				continue;
			}

			if (isSoft && lexeme.getCommand() != null) {
				// TODO Note: soft lexeme is able to override the code
				error(lexeme.getCommand(), "soft lexeme rule `" + lexeme.getName().getID()
						+ "' cannot have a semantic action");
			}
			LexerRule classRule = getClassRule(classMatchers, lexeme, regex);
			if (isSoft) {
				if (classRule == null) {
					error(lexeme, "soft lexeme rule `" + name + "' " +
							(regex.isConstant() ? "doesn't match any class rule" : "should have a constant regexp"));
					continue;
				}
				Terminal softClass = classRule.getSymbol();

				String type = lexeme.getType();
				String classtype = getSymbolType(softClass);
				if (type != null && !type.equals(classtype)) {
					error(lexeme, "soft terminal `" + name + "' overrides base type: expected `"
							+ (classtype == null ? "<no type>" : classtype) + "', found `" + type + "'");
					continue;
				}

				final Terminal oldClass = softToClass.get(term);
				if (oldClass != null && oldClass != softClass) {
					error(lexeme, "redeclaration of soft class for `" + term.getName() + "': found " + softClass
							.getName()
							+ " instead of " + oldClass.getName());
					continue;
				} else if (oldClass == null) {
					builder.makeSoft(term, softClass);
					softToClass.put(term, softClass);
				}

				// TODO check applicable states
			}

			int priority = lexeme.getPriority() == null ? 0 : lexeme.getPriority();
			LexerRule liLexerRule = builder.addLexerRule(kind, term, regex,
					attributes.get(lexeme).getApplicableInStates(), priority,
					classRule, lexeme);
			TMDataUtil.putCode(liLexerRule, lexeme.getCommand());
			TMDataUtil.putTransition(liLexerRule, attributes.get(lexeme).getTransitions());
		}
	}

	private static String getSymbolType(Symbol s) {
		final AstType type = s.getType();
		return type instanceof AstRawType ? ((AstRawType) type).getRawType() : null;
	}

	private static class RuleAttributes {

		private final TMStateTransitionSwitch transitions;
		private final List<LexerState> applicableInStates;

		public RuleAttributes(TMStateTransitionSwitch transitions, List<LexerState> applicableInStates) {
			this.transitions = transitions;
			this.applicableInStates = applicableInStates;
		}

		public TMStateTransitionSwitch getTransitions() {
			return transitions;
		}

		public List<LexerState> getApplicableInStates() {
			return applicableInStates;
		}

		public boolean canBeClassFor(RuleAttributes l) {
			if (applicableInStates.size() != l.getApplicableInStates().size()) {
				return false;
			}
			Collection<LexerState> applicableInStatesSet = applicableInStates.size() > 4
					? new HashSet<LexerState>(applicableInStates) : applicableInStates;
			if (!(applicableInStatesSet.containsAll(l.getApplicableInStates()))) {
				return false;
			}
			return this.transitions == null ? l.getTransitions() == null : this.transitions.equals(l.getTransitions());
		}

	}
}
