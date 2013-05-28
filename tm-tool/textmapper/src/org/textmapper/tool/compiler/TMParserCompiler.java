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

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.ast.*;

import java.util.*;

/**
 * evgeny, 1/29/13
 */
public class TMParserCompiler {

	private final Map<ListDescriptor, Nonterminal> listsMap = new HashMap<ListDescriptor, Nonterminal>();

	private final TMTree<AstRoot> tree;
	private final TMResolver resolver;
	private TMExpressionResolver expressionResolver;
	private final GrammarBuilder builder;

	private boolean hasInputs = false;

	public TMParserCompiler(TMResolver resolver, TMExpressionResolver expressionResolver) {
		this.resolver = resolver;
		this.expressionResolver = expressionResolver;
		this.tree = resolver.getTree();
		this.builder = resolver.getBuilder();
	}

	public void compile() {
		collectAnnotations();
		collectRules();
		collectDirectives();

		if (!hasInputs) {
			Symbol input = resolver.getSymbol("input");
			if (input == null) {
				error(tree.getRoot(), "no input non-terminal");
			} else if (!(input instanceof Nonterminal)) {
				error(tree.getRoot(), "input should be non-terminal");
			} else {
				builder.addInput((Nonterminal) input, true, input);
			}
		}
	}

	private void collectRules() {
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				Symbol left = resolver.getSymbol(nonterm.getName().getName());
				if (left == null || !(left instanceof Nonterminal)) {
					continue; /* error is already reported */
				}
				for (AstRule right : nonterm.getRules()) {
					if (!right.hasSyntaxError()) {
						createRule((Nonterminal) left, right);
					}
				}
			}
		}
	}

	private void collectDirectives() {
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstDirective) {
				AstDirective directive = (AstDirective) clause;
				String key = directive.getKey();
				List<Terminal> val = resolveTerminals(directive.getSymbols());
				int prio;
				if (key.equals("left")) {
					prio = Prio.LEFT;
				} else if (key.equals("right")) {
					prio = Prio.RIGHT;
				} else if (key.equals("nonassoc")) {
					prio = Prio.NONASSOC;
				} else {
					error(directive, "unknown directive identifier used: `" + key + "`");
					continue;
				}
				builder.addPrio(prio, val, directive);
			} else if (clause instanceof AstInputDirective) {
				List<AstInputRef> refs = ((AstInputDirective) clause).getInputRefs();
				for (AstInputRef inputRef : refs) {
					Symbol sym = resolver.resolve(inputRef.getReference());
					boolean hasEoi = !inputRef.isNonEoi();
					if (sym instanceof Nonterminal) {
						builder.addInput((Nonterminal) sym, hasEoi, inputRef);
						hasInputs = true;
					} else if (sym != null) {
						error(inputRef, "input should be non-terminal");
					}
				}
			}
		}
	}

	private void addSymbolAnnotations(AstIdentifier id, Map<String, Object> annotations) {
		if (annotations != null) {
			Symbol sym = resolver.getSymbol(id.getName());
			Map<String, Object> symAnnotations = TMDataUtil.getAnnotations(sym);
			if (symAnnotations == null) {
				symAnnotations = new HashMap<String, Object>();
				TMDataUtil.putAnnotations(sym, symAnnotations);
			}
			for (Map.Entry<String, Object> ann : annotations.entrySet()) {
				if (symAnnotations.containsKey(ann.getKey())) {
					error(id, "redeclaration of annotation `" + ann.getKey() + "' for non-terminal: " + id.getName()
							+ ", skipped");
				} else {
					symAnnotations.put(ann.getKey(), ann.getValue());
				}
			}
		}
	}

	private void collectAnnotations() {
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				addSymbolAnnotations(nonterm.getName(), expressionResolver.convert(nonterm.getAnnotations(), "AnnotateSymbol"));
			}
		}
	}

	private void createRule(Nonterminal left, AstRule right) {
		List<RhsPart> rhs = new ArrayList<RhsPart>();
		List<TmaRhsPart> list = right.getList();
		AstCode lastAction = null;
		if (list != null) {
			TmaRhsPart last = list.size() > 0 ? list.get(list.size() - 1) : null;
			if (last instanceof AstCode) {
				lastAction = (AstCode) last;
				list = list.subList(0, list.size() - 1);
			}

			for (TmaRhsPart part : list) {
				RhsPart rulePart = convertPart(left, part);
				if (rulePart != null) {
					rhs.add(rulePart);
				}
			}
		}
		TmaRhsSuffix ruleAttribute = right.getSuffix();
		AstReference rulePrio = ruleAttribute instanceof TmaRhsPrio ? ((TmaRhsPrio) ruleAttribute).getReference()
				: null;
		Terminal prio = null;
		if (rulePrio != null) {
			Symbol prioSym = resolver.resolve(rulePrio);
			if (prioSym instanceof Terminal) {
				prio = (Terminal) prioSym;
			} else if (prioSym != null) {
				error(rulePrio, "symbol `" + prioSym.getName() + "' is not a terminal");
			}
		}

		// TODO store %shift attribute
		// TODO check right.getAnnotations().getNegativeLA() == null
		Collection<Rule> result = builder.addRule(left, builder.sequence(right.getAlias(), rhs, right), prio);
		Map<String, Object> annotations = expressionResolver.convert(right.getAnnotations(), "AnnotateRule");
		for (Rule r : result) {
			TMDataUtil.putAnnotations(r, annotations);
			TMDataUtil.putCode(r, lastAction);
		}
	}

	private RhsPart convertPart(Symbol outer, TmaRhsPart part) {
		if (part instanceof AstCode) {
			AstCode astCode = (AstCode) part;
			Nonterminal codeSym = (Nonterminal) resolver.createNestedNonTerm(outer, astCode);
			Collection<Rule> actionRules = builder.addRule(codeSym, builder.empty(astCode), null);
			for (Rule actionRule : actionRules) {
				TMDataUtil.putCode(actionRule, astCode);
			}
			return builder.symbol(codeSym, null, astCode);

		} else if (part instanceof TmaRhsUnordered) {
			List<TmaRhsPart> refParts = new ArrayList<TmaRhsPart>();
			extractUnorderedParts(part, refParts);
			if (refParts.size() < 2 || refParts.size() > 5) {
				error(part, "max 5 elements are allowed for permutation");
				return null;
			}
			List<RhsPart> resolved = new ArrayList<RhsPart>(refParts.size());
			for (TmaRhsPart refPart : refParts) {
				RhsPart rulePart = convertPart(outer, refPart);
				if (rulePart == null) {
					return null;
				}
				resolved.add(rulePart);
			}
			return builder.unordered(resolved, part);

		}

		Collection<Terminal> nla = null;
		Map<String, Object> annotations = null;
		if (part instanceof TmaRhsAnnotated) {
			final AstRuleAnnotations rhsAnnotations = ((TmaRhsAnnotated) part).getAnnotations();
			nla = convertLA(rhsAnnotations);
			annotations = expressionResolver.convert(rhsAnnotations, "AnnotateReference");
			part = ((TmaRhsAnnotated) part).getInner();
		}

		TmaRhsAssignment assignment = null;
		if (part instanceof TmaRhsAssignment) {
			assignment = (TmaRhsAssignment) part;
			part = assignment.getInner();
		}

		TmaRhsQuantifier optional = null;
		if (part instanceof TmaRhsQuantifier && ((TmaRhsQuantifier) part).isOptional()) {
			optional = (TmaRhsQuantifier) part;
			part = optional.getInner();
		}

		TmaRhsCast cast = null;
		if (part instanceof TmaRhsCast) {
			cast = (TmaRhsCast) part;
			part = cast.getInner();
		}

		boolean canInline = nla == null && annotations == null;
		RhsPart result;

		// inline (...)
		if (canInline && isGroupPart(part)) {
			List<TmaRhsPart> groupPart = getGroupPart(part);
			result = convertGroup(outer, groupPart, part);

			// inline (...|...|...)
		} else if (canInline && isChoicePart(part)) {
			List<AstRule> rules = ((TmaRhsNested) part).getRules();
			result = convertChoice(outer, rules, part);
		} else {
			Symbol sym = convertPrimary(outer, part);
			if (sym == null) {
				return null;
			}
			result = builder.symbol(sym, nla, part);
			TMDataUtil.putAnnotations(result, annotations);
		}

		if (cast != null) {
			final Symbol asSymbol = resolver.resolve(cast.getTarget());
			if (asSymbol != null) {
				result = builder.cast(asSymbol, result, cast);
			}
		}

		if (optional != null) {
			result = builder.optional(result, optional);
		}

		if (assignment != null) {
			result = builder.assignment(assignment.getId().getName(), result, assignment.isAddition(), assignment);
		}

		return result;
	}

	private Symbol convertPrimary(Symbol outer, TmaRhsPart part) {

		if (part instanceof TmaRhsSymbol) {
			return resolver.resolve(((TmaRhsSymbol) part).getReference());

		} else if (part instanceof TmaRhsNested) {
			Nonterminal nested = (Nonterminal) resolver.createNestedNonTerm(outer, part);
			List<AstRule> rules = ((TmaRhsNested) part).getRules();
			for (AstRule right : rules) {
				if (!right.hasSyntaxError()) {
					createRule(nested, right);
				}
			}
			return nested;

		} else if (part instanceof TmaRhsList) {
			TmaRhsList listWithSeparator = (TmaRhsList) part;

			RhsSequence inner = convertGroup(outer, listWithSeparator.getRuleParts(), listWithSeparator);
			List<RhsPart> sep = new ArrayList<RhsPart>();
			for (AstReference ref : listWithSeparator.getSeparator()) {
				Symbol s = resolver.resolve(ref);
				if (s == null) {
					continue;
				}
				if (s instanceof Terminal) {
					sep.add(builder.symbol(s, null, ref));
				} else {
					error(ref, "separator should be terminal symbol");
				}
			}
			RhsPart separator = builder.sequence(null, sep, listWithSeparator);
			return createList(outer, inner, listWithSeparator.isAtLeastOne(), separator, part);

		} else if (part instanceof TmaRhsQuantifier) {
			TmaRhsQuantifier nestedQuantifier = (TmaRhsQuantifier) part;

			RhsSequence inner;
			TmaRhsPart innerSymRef = nestedQuantifier.getInner();
			if (isGroupPart(innerSymRef)) {
				List<TmaRhsPart> groupPart = getGroupPart(innerSymRef);
				inner = convertGroup(outer, groupPart, innerSymRef);
			} else {
				Symbol innerTarget = convertPrimary(outer, innerSymRef);
				final RhsSymbol symref = builder.symbol(innerTarget, null, innerSymRef);
				inner = builder.sequence(null, Arrays.<RhsPart>asList(symref), innerSymRef);
			}
			int quantifier = nestedQuantifier.getQuantifier();
			if (quantifier == TmaRhsQuantifier.KIND_OPTIONAL) {
				error(part, "? cannot be a child of another quantifier");
				return null;
			}
			return createList(outer, inner, quantifier == TmaRhsQuantifier.KIND_ONEORMORE, null, part);
		}

		error(part, "internal error: unknown right-hand side part found");
		return null;
	}

	private RhsPart convertChoice(Symbol outer, List<AstRule> rules, SourceElement origin) {
		Collection<RhsPart> result = new ArrayList<RhsPart>(rules.size());
		for (AstRule rule : rules) {
			RhsPart abstractRulePart = convertGroup(outer, rule.getList(), rule);
			if (abstractRulePart == null) {
				return null;
			}
			result.add(abstractRulePart);
		}
		return builder.choice(result, origin);
	}

	private RhsSequence convertGroup(Symbol outer, List<TmaRhsPart> groupPart, SourceElement origin) {
		List<RhsPart> groupResult = new ArrayList<RhsPart>();
		if (groupPart == null) {
			return null;
		}
		for (TmaRhsPart innerPart : groupPart) {
			RhsPart rulePart = convertPart(outer, innerPart);
			if (rulePart != null) {
				groupResult.add(rulePart);
			}
		}
		return groupResult.isEmpty() ? null : builder.sequence(null, groupResult, origin);
	}

	private Symbol createList(Symbol outer, RhsSequence inner, boolean atLeastOne, RhsPart separator, TmaRhsPart origin) {
		ListDescriptor descr = new ListDescriptor(inner, separator, atLeastOne);
		Nonterminal listSymbol = listsMap.get(descr);
		if (listSymbol != null) {
			return listSymbol;
		}

		Symbol representative = RhsUtil.getRepresentative(inner);
		listSymbol = representative != null
				? resolver.createDerived(representative, atLeastOne || separator != null ? "_list" : "_optlist", origin) /* TODO type? */
				: (Nonterminal) resolver.createNestedNonTerm(outer, origin);

		// list rule
		builder.addRule(listSymbol, builder.list(inner, separator, (separator != null && !atLeastOne) || atLeastOne, origin), null);

		if (separator != null && !atLeastOne) {
			// (a separator ',')*   => alistopt ::= alist | ; alist ::= a | alist ',' a ;
			Nonterminal opt = resolver.createDerived(listSymbol, "_opt", origin);
			builder.addRule(opt, builder.optional(builder.symbol(listSymbol, null, origin), origin), null);
			listSymbol = opt;
		}

		listsMap.put(descr, listSymbol);
		return listSymbol;
	}

	private boolean isGroupPart(TmaRhsPart symbolRef) {
		if (!(symbolRef instanceof TmaRhsNested)) {
			return false;
		}
		List<AstRule> innerRules = ((TmaRhsNested) symbolRef).getRules();
		if (innerRules.size() == 1) {
			AstRule first = innerRules.get(0);
			return isSimpleNonEmpty(first);
		}
		return false;
	}

	private boolean isChoicePart(TmaRhsPart symbolRef) {
		if (!(symbolRef instanceof TmaRhsNested)) {
			return false;
		}
		List<AstRule> innerRules = ((TmaRhsNested) symbolRef).getRules();
		if (innerRules.size() < 2) {
			return false;
		}
		for (AstRule rule : innerRules) {
			if (!(isSimpleNonEmpty(rule))) {
				return false;
			}
		}
		return true;
	}

	private boolean isSimpleNonEmpty(AstRule rule) {
		return rule != null
				&& rule.getPrefix() == null
				&& rule.getSuffix() == null
				&& rule.getList() != null
				&& !rule.getList().isEmpty()
				&& !rule.hasSyntaxError();
	}

	private List<TmaRhsPart> getGroupPart(TmaRhsPart symbolRef) {
		return ((TmaRhsNested) symbolRef).getRules().get(0).getList();
	}

	private void extractUnorderedParts(TmaRhsPart unorderedRulePart, List<TmaRhsPart> result) {
		if (unorderedRulePart instanceof TmaRhsUnordered) {
			extractUnorderedParts(((TmaRhsUnordered) unorderedRulePart).getLeft(), result);
			extractUnorderedParts(((TmaRhsUnordered) unorderedRulePart).getRight(), result);
		} else if (unorderedRulePart instanceof AstCode) {
			error(unorderedRulePart, "semantic action cannot be used as a part of unordered group");
		} else if (!(unorderedRulePart instanceof AstError)) {
			result.add(unorderedRulePart);
		}
	}

	private Collection<Terminal> convertLA(AstRuleAnnotations astAnnotations) {
		if (astAnnotations == null || astAnnotations.getNegativeLA() == null) {
			return null;
		}

		List<AstReference> unwantedSymbols = astAnnotations.getNegativeLA().getUnwantedSymbols();
		List<Terminal> resolved = resolveTerminals(unwantedSymbols);
		if (resolved.size() == 0) {
			return null;
		}

		return resolved;
	}

	private List<Terminal> resolveTerminals(List<AstReference> input) {
		List<Terminal> result = new ArrayList<Terminal>(input.size());
		for (AstReference id : input) {
			Symbol sym = resolver.resolve(id);
			if (sym instanceof Terminal) {
				result.add((Terminal) sym);
			} else if (sym != null) {
				error(id, "terminal is expected");
			}
		}
		return result;
	}

	private void error(IAstNode n, String message) {
		resolver.error(n, message);
	}

	private static class ListDescriptor {
		private final Object inner;
		private final Object separator;
		private final boolean atLeastOne;

		private ListDescriptor(RhsPart inner, RhsPart separator, boolean atLeastOne) {
			this.inner = inner.structuralNode();
			this.separator = separator == null ? null : separator.structuralNode();
			this.atLeastOne = atLeastOne;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ListDescriptor that = (ListDescriptor) o;

			if (atLeastOne != that.atLeastOne) return false;
			if (!inner.equals(that.inner)) return false;
			if (separator != null ? !separator.equals(that.separator) : that.separator != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = inner.hashCode();
			result = 31 * result + (separator != null ? separator.hashCode() : 0);
			result = 31 * result + (atLeastOne ? 1 : 0);
			return result;
		}
	}
}
