/**
 * Copyright 2002-2018 Evgeny Gryaznov
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

	private final TMTree<TmaInput1> tree;
	private final TMResolver resolver;
	private final GrammarBuilder builder;

	private final Map<TmaLexeme, RuleAttributes> attributes = new HashMap<>();

	public TMLexerCompiler(TMResolver resolver) {
		this.resolver = resolver;
		this.tree = resolver.getTree();
		this.builder = resolver.getBuilder();
	}

	private void error(ITmaNode n, String message) {
		resolver.error(n, message);
	}

	private List<LexerState> resolveStates(TmaStartConditions conditions) {
		List<LexerState> result = new ArrayList<>();
		List<TmaStateref> refs = conditions.getStaterefListCommaSeparated();
		if (refs == null) {
			return resolver.allStates();
		}
		for (TmaStateref ref : refs) {
			LexerState applicable = resolver.getState(ref.getName().getText());
			if (applicable != null) {
				result.add(applicable);
			} else {
				error(ref, ref.getName() + " cannot be resolved");
			}
		}
		if (result.isEmpty()) {
			result.addAll(resolver.allStates());
		}
		return result;
	}

	private LexerRule getClassRule(Map<LexerRule, RegexMatcher> classMatchers, TmaLexeme l,
								   RegexPart regex) {
		LexerRule result = null;
		TmaLexemeAttrs attrs = l.getAttrs();
		boolean isClass = attrs != null && attrs.getKind() == TmaLexemeAttribute.CLASS;
		if (regex.isConstant() && !isClass) {
			for (LexerRule rule : classMatchers.keySet()) {
				TmaLexeme astClassLexeme = (TmaLexeme) ((DerivedSourceElement) rule).getOrigin();
				if (!attributes.get(astClassLexeme).canBeClassFor(attributes.get(l))) {
					continue;
				}
				RegexMatcher m = classMatchers.get(rule);
				if (m.matches(regex.getConstantValue())) {
					if (result != null) {
						error(l, "regex matches two classes `" + result.getSymbol().getNameText() +
								"' and `" + rule.getSymbol().getNameText() + "', using first");
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
			case CLASS:
				return LexerRule.KIND_CLASS;
			case LAYOUT:
				return LexerRule.KIND_LAYOUT;
			case SPACE:
				return LexerRule.KIND_SPACE;
		}

		return LexerRule.KIND_NONE;
	}

	private void collectAttributes(List<LexerState> states, ITmaLexerPart part) {
		if (part instanceof TmaLexeme) {
			TmaStartConditions conditions = ((TmaLexeme) part).getStartConditions();
			if (conditions != null) {
				states = resolveStates(conditions);
			}
			attributes.put((TmaLexeme) part, new RuleAttributes(states));
		} else if (part instanceof TmaStartConditionsScope) {
			TmaStartConditionsScope scope = (TmaStartConditionsScope) part;
			states = resolveStates(scope.getStartConditions());
			for (ITmaLexerPart p : scope.getLexerParts()) {
				collectAttributes(states, p);
			}
		}
	}

	public void compile() {
		// Step 1. Collect states.

		List<LexerState> defaultStates = resolver.inclusiveStates();
		for (ITmaLexerPart clause : tree.getRoot().getLexer()) {
			collectAttributes(defaultStates, clause);
		}

		// Step 2. Process class lexical rules.

		RegexContext context = resolver.createRegexContext();
		Map<LexerRule, RegexMatcher> classMatchers = new LinkedHashMap<>();

		int order = 0;
		for (TmaLexeme lexeme : resolver.getLexerParts(TmaLexeme.class)) {
			order++;
			TmaLexemeAttrs attrs = lexeme.getAttrs();
			if (attrs == null || attrs.getKind() != TmaLexemeAttribute.CLASS) {
				continue;
			}
			if (lexeme.getPattern() == null) {
				error(lexeme, "class lexeme rule without regular expression, ignored");
				continue;
			}

			Symbol s = resolver.getSymbol(lexeme.getName().getText());
			if (!(s instanceof Terminal)) {
				// not a terminal? already reported, ignore
				continue;
			}
			Terminal classTerm = (Terminal) s;

			RegexPart regex;
			RegexMatcher matcher;
			try {
				regex = LapgCore.parse(s.getNameText(), lexeme.getPattern().getRegexp());
				matcher = LapgCore.createMatcher(regex, context);
			} catch (RegexParseException e) {
				error(lexeme.getPattern(), e.getMessage());
				continue;
			}

			int priority = lexeme.getPriority() == null ? 0 : lexeme.getPriority();
			List<LexerState> states = attributes.get(lexeme).getApplicableInStates();
			if (states.isEmpty()) {
				error(lexeme, "lexer rule is never applicable, ignored");
				continue;
			}

			LexerRule liLexerRule = builder.addLexerRule(LexerRule.KIND_CLASS, classTerm, regex,
					states, priority, order, null, lexeme);
			classMatchers.put(liLexerRule, matcher);
			TMDataUtil.putCodeTemplate(liLexerRule, lexeme.getCommand());
		}

		// Step 3. Process other lexical rules.

		order = 0;
		for (TmaLexeme lexeme : resolver.getLexerParts(TmaLexeme.class)) {
			order++;
			TmaLexemeAttrs attrs = lexeme.getAttrs();
			int kind = getLexerRuleKind(attrs);
			if (kind == LexerRule.KIND_CLASS) {
				continue;
			}

			Symbol s = resolver.getSymbol(lexeme.getName().getText());
			if (!(s instanceof Terminal)) {
				// not a terminal? already reported, ignore
				continue;
			}
			Terminal term = (Terminal) s;
			if (lexeme.getPattern() == null) {
				continue;
			}

			String name = lexeme.getName().getText();
			RegexPart regex;
			try {
				regex = LapgCore.parse(name, lexeme.getPattern().getRegexp());
			} catch (RegexParseException e) {
				error(lexeme.getPattern(), e.getMessage());
				continue;
			}

			LexerRule classRule = getClassRule(classMatchers, lexeme, regex);
			int priority = lexeme.getPriority() == null ? 0 : lexeme.getPriority();
			List<LexerState> states = attributes.get(lexeme).getApplicableInStates();
			if (states.isEmpty()) {
				error(lexeme, "lexer rule is never applicable, ignored");
				continue;
			}

			LexerRule liLexerRule = builder.addLexerRule(kind, term, regex,
					states, priority, order, classRule, lexeme);
			TMDataUtil.putCodeTemplate(liLexerRule, lexeme.getCommand());
		}
	}

	private static class RuleAttributes {

		private final List<LexerState> applicableInStates;

		public RuleAttributes(List<LexerState> applicableInStates) {
			this.applicableInStates = applicableInStates;
		}

		public List<LexerState> getApplicableInStates() {
			return applicableInStates;
		}

		public boolean canBeClassFor(RuleAttributes l) {
			if (applicableInStates.size() != l.getApplicableInStates().size()) {
				return false;
			}
			Collection<LexerState> applicableInStatesSet = applicableInStates.size() > 4
					? new HashSet<>(applicableInStates) : applicableInStates;

			return applicableInStatesSet.containsAll(l.getApplicableInStates());
		}

	}
}
