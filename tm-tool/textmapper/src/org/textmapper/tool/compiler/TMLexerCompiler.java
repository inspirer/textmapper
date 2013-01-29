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
import org.textmapper.lapg.api.*;
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

	private final TMTree<AstRoot> tree;
	private final TMResolver resolver;
	private final GrammarBuilder builder;

	private final Map<AstLexeme, RuleAttributes> attributes = new HashMap<AstLexeme, RuleAttributes>();

	public TMLexerCompiler(TMResolver resolver) {
		this.resolver = resolver;
		this.tree = resolver.getTree();
		this.builder = resolver.getBuilder();
	}

	private void error(IAstNode n, String message) {
		resolver.error(n, message);
	}

	private List<LexerState> convertApplicableStates(AstStateSelector selector) {
		List<LexerState> result = new ArrayList<LexerState>();
		for (AstLexerState state : selector.getStates()) {
			LexerState applicable = resolver.getState(state.getName().getName());
			result.add(applicable);
		}
		return result;
	}

	private TMStateTransitionSwitch convertTransitions(AstStateSelector selector) {
		boolean noDefault = false;
		for (AstLexerState state : selector.getStates()) {
			if (state.getDefaultTransition() == null) {
				noDefault = true;
			}
		}

		LexerState defaultTransition = null;
		Map<LexerState, LexerState> stateSwitch = new LinkedHashMap<LexerState, LexerState>();
		for (AstLexerState state : selector.getStates()) {
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
				LexerState source = resolver.getState(state.getName().getName());
				stateSwitch.put(source, target);
			}
		}
		return stateSwitch.isEmpty() && defaultTransition == null ? null
				: new TMStateTransitionSwitch(stateSwitch.isEmpty() ? null : stateSwitch, defaultTransition);
	}

	private TMStateTransitionSwitch getTransition(AstLexeme lexeme, TMStateTransitionSwitch active) {
		AstReference transition = lexeme.getTransition();
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

	private LexicalRule getClassRule(Map<LexicalRule, RegexMatcher> classMatchers, AstLexeme l, RegexPart regex) {
		LexicalRule result = null;
		AstLexemAttrs attrs = l.getAttrs();
		int kind = attrs == null ? LexicalRule.KIND_NONE : attrs.getKind();
		if (regex.isConstant() && kind != LexicalRule.KIND_CLASS) {
			for (LexicalRule rule : classMatchers.keySet()) {
				AstLexeme astClassLexeme = (AstLexeme) ((DerivedSourceElement) rule).getOrigin();
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

	public void compile() {
		Map<Terminal, Terminal> softToClass = new HashMap<Terminal, Terminal>();
		Set<Terminal> nonSoft = new HashSet<Terminal>();

		// Step 1. Collect states & transitions (attributes).

		TMStateTransitionSwitch activeTransitions = null;
		List<LexerState> activeStates = Collections.singletonList(resolver.getState(TMResolver.INITIAL_STATE));

		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				attributes.put(lexeme, new RuleAttributes(getTransition(lexeme, activeTransitions), activeStates));
			} else if (clause instanceof AstStateSelector) {
				activeStates = convertApplicableStates((AstStateSelector) clause);
				activeTransitions = convertTransitions((AstStateSelector) clause);
			}
		}

		// Step 2. Process class lexical rules.

		RegexContext context = resolver.createRegexContext();
		Map<LexicalRule, RegexMatcher> classMatchers = new LinkedHashMap<LexicalRule, RegexMatcher>();

		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (!(clause instanceof AstLexeme)) {
				continue;
			}
			AstLexeme lexeme = (AstLexeme) clause;
			AstLexemAttrs attrs = lexeme.getAttrs();
			if (attrs == null || attrs.getKind() != LexicalRule.KIND_CLASS) {
				continue;
			}
			if (lexeme.getRegexp() == null) {
				error(lexeme, "class lexeme rule without regular expression, ignored");
				continue;
			}

			Symbol s = resolver.getSymbol(lexeme.getName().getName());
			if (!(s instanceof Terminal)) {
				// not a terminal? already reported, ignore
				continue;
			}
			Terminal classTerm = (Terminal) s;
			nonSoft.add(classTerm);

			RegexPart regex;
			try {
				regex = LapgCore.parse(s.getName(), lexeme.getRegexp().getRegexp());
			} catch (RegexParseException e) {
				error(lexeme.getRegexp(), e.getMessage());
				continue;
			}

			LexicalRule liLexicalRule = builder.addLexicalRule(LexicalRule.KIND_CLASS, classTerm, regex,
					attributes.get(lexeme).getApplicableInStates(), lexeme.getPriority(),
					null, lexeme);
			classMatchers.put(liLexicalRule, LapgCore.createMatcher(liLexicalRule.getRegexp(), context));
			TMDataUtil.putCode(liLexicalRule, lexeme.getCode());
			TMDataUtil.putTransition(liLexicalRule, attributes.get(lexeme).getTransitions());
		}

		// Step 3. Process other lexical rules. Match soft lexemes with their classes.

		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (!(clause instanceof AstLexeme)) {
				continue;
			}
			AstLexeme lexeme = (AstLexeme) clause;
			AstLexemAttrs attrs = lexeme.getAttrs();
			int kind = attrs == null ? LexicalRule.KIND_NONE : attrs.getKind();
			if (kind == LexicalRule.KIND_CLASS) {
				continue;
			}

			Symbol s = resolver.getSymbol(lexeme.getName().getName());
			if (!(s instanceof Terminal)) {
				// not a terminal? already reported, ignore
				continue;
			}
			Terminal term = (Terminal) s;

			boolean isSoft = (kind == LexicalRule.KIND_SOFT);
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

			if (lexeme.getRegexp() == null) {
				if (isSoft) {
					error(lexeme, "soft lexeme rule `" + lexeme.getName().getName() + "' should have a regular expression");
				}
				continue;
			}

			String name = lexeme.getName().getName();
			RegexPart regex;
			try {
				regex = LapgCore.parse(name, lexeme.getRegexp().getRegexp());
			} catch (RegexParseException e) {
				error(lexeme.getRegexp(), e.getMessage());
				continue;
			}

			if (isSoft && lexeme.getCode() != null) {
				// TODO really?
				error(lexeme.getCode(), "soft lexeme rule `" + lexeme.getName().getName()
						+ "' cannot have a semantic action");
			}
			LexicalRule classRule = getClassRule(classMatchers, lexeme, regex);
			if (isSoft) {
				if (classRule == null) {
					error(lexeme, "soft lexeme rule `" + name + "' " +
							(regex.isConstant() ? "doesn't match any class rule" : "should have a constant regexp"));
					continue;
				}
				Terminal softClass = classRule.getSymbol();

				String type = lexeme.getType();
				String classtype = softClass.getType();
				if (type != null && !type.equals(classtype)) {
					error(lexeme, "soft terminal `" + name + "' overrides base type: expected `"
							+ (classtype == null ? "<no type>" : classtype) + "', found `" + type + "'");
					continue;
				}

				final Terminal oldClass = softToClass.get(term);
				if (oldClass != null && oldClass != softClass) {
					error(lexeme, "redeclaration of soft class for `" + term.getName() + "': found " + softClass.getName()
							+ " instead of " + oldClass.getName());
					continue;
				} else if (oldClass == null) {
					builder.makeSoft(term, softClass);
					softToClass.put(term, softClass);
				}
			}

			LexicalRule liLexicalRule = builder.addLexicalRule(kind, term, regex, attributes.get(lexeme).getApplicableInStates(), lexeme.getPriority(),
					classRule, lexeme);
			TMDataUtil.putCode(liLexicalRule, lexeme.getCode());
			TMDataUtil.putTransition(liLexicalRule, attributes.get(lexeme).getTransitions());
		}
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
			Collection<LexerState> applicableInStatesSet = applicableInStates.size() > 4 ? new HashSet<LexerState>(applicableInStates) : applicableInStates;
			if (!(applicableInStatesSet.containsAll(l.getApplicableInStates()))) {
				return false;
			}
			return this.transitions == null ? l.getTransitions() == null : this.transitions.equals(l.getTransitions());
		}

	}
}
