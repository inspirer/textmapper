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
import org.textmapper.lapg.api.rule.RhsOptional;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.lapg.common.FormatUtil;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IType;
import org.textmapper.templates.types.TiExpressionBuilder;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.templates.types.TypesUtil;
import org.textmapper.tool.parser.LapgTree;
import org.textmapper.tool.parser.ast.*;

import java.util.*;

public class TMCompiler {

	private TMResolver resolver;
	private final LapgTree<AstRoot> tree;
	private final TypesRegistry types;
	private String myTypesPackage;

	private final Map<ListDescriptor, Nonterminal> listsMap = new HashMap<ListDescriptor, Nonterminal>();

	private Map<String, Object> options;
	private GrammarBuilder builder;
	private boolean hasInputs = false;

	public TMCompiler(LapgTree<AstRoot> tree, TypesRegistry types) {
		this.tree = tree;
		this.types = types;
	}

	public TMGrammar resolve() {
		if (tree.getRoot() == null) {
			return null;
		}
		myTypesPackage = getTypesPackage();

		builder = LapgCore.createBuilder();
		resolver = new TMResolver(tree, builder);
		resolver.collectSymbols();

		new TMLexerCompiler(resolver).collectLexerRules();

		if (tree.getRoot().getGrammar() != null) {
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

			// TODO collect layout symbols
		}

		collectOptions();
		TextSourceElement templates = getTemplates();
		String copyrightHeader = TMTextUtil.extractCopyright(tree.getSource());

		Grammar g = builder.create();

		UniqueNameHelper helper = new UniqueNameHelper();
		for (Symbol s : g.getSymbols()) {
			String name = s.getName();
			if (FormatUtil.isIdentifier(name)) {
				helper.markUsed(name);
			}
		}
		for (int i = 0; i < g.getSymbols().length; i++) {
			Symbol sym = g.getSymbols()[i];
			TMDataUtil.putId(sym, helper.generateId(sym.getName(), i));
		}

		return new TMGrammar(g, templates, !tree.getErrors().isEmpty(), options, copyrightHeader);
	}

	private TextSourceElement getTemplates() {
		int offset = tree.getRoot() != null ? tree.getRoot().getTemplatesStart() : -1;
		char[] text = tree.getSource().getContents();
		if (offset < text.length && offset != -1) {
			return new AstNode(tree.getSource(), offset, text.length) {
				@Override
				public void accept(AbstractVisitor v) {
				}
			};
		}
		return null;
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
				addSymbolAnnotations(nonterm.getName(), convert(nonterm.getAnnotations(), "AnnotateSymbol"));
			}
		}
	}

	private void createRule(Nonterminal left, AstRule right) {
		List<RhsPart> rhs = new ArrayList<RhsPart>();
		List<AstRulePart> list = right.getList();
		AstCode lastAction = null;
		if (list != null) {
			AstRulePart last = list.size() > 0 ? list.get(list.size() - 1) : null;
			if (last instanceof AstCode) {
				lastAction = (AstCode) last;
				list = list.subList(0, list.size() - 1);
			}

			for (AstRulePart part : list) {
				RhsPart rulePart = convertRulePart(left, part);
				if (rulePart != null) {
					rhs.add(rulePart);
				}
			}
		}
		AstRuleAttribute ruleAttribute = right.getAttribute();
		AstReference rulePrio = ruleAttribute instanceof AstPrioClause ? ((AstPrioClause) ruleAttribute).getReference()
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
		Collection<Rule> result = builder.addRule(right.getAlias(), left, builder.sequence(rhs, right), prio);
		Map<String, Object> annotations = convert(right.getAnnotations(), "AnnotateRule");
		for (Rule r : result) {
			TMDataUtil.putAnnotations(r, annotations);
			TMDataUtil.putCode(r, lastAction);
		}
	}

	private RhsPart convertRulePart(Symbol outer, AstRulePart part) {
		if (part instanceof AstCode) {
			AstCode astCode = (AstCode) part;
			Nonterminal codeSym = (Nonterminal) resolver.createNestedNonTerm(outer, astCode);
			Collection<Rule> actionRules = builder.addRule(null, codeSym, builder.empty(astCode), null);
			for (Rule actionRule : actionRules) {
				TMDataUtil.putCode(actionRule, astCode);
			}
			return builder.symbol(null, codeSym, null, astCode);

		} else if (part instanceof AstUnorderedRulePart) {
			List<AstRefRulePart> refParts = new ArrayList<AstRefRulePart>();
			extractUnorderedParts(part, refParts);
			if (refParts.size() < 2 || refParts.size() > 5) {
				error(part, "max 5 elements are allowed for permutation");
				return null;
			}
			List<RhsPart> resolved = new ArrayList<RhsPart>(refParts.size());
			for (AstRefRulePart refPart : refParts) {
				RhsPart rulePart = convertRulePart(outer, refPart);
				if (rulePart == null) {
					return null;
				}
				resolved.add(rulePart);
			}
			return builder.unordered(resolved, part);

		} else if (!(part instanceof AstRefRulePart)) {
			error(part, "unknown rule part");
			return null;
		}

		AstRefRulePart refPart = (AstRefRulePart) part;
		String alias = refPart.getAlias();
		Collection<Terminal> nla = convertLA(refPart.getAnnotations());
		Map<String, Object> annotations = convert(refPart.getAnnotations(), "AnnotateReference");

		// inline ...? and (...)?
		if (isOptionalPart(refPart)) {
			AstRuleSymbolRef optionalPart = getOptionalPart(refPart);
			if (alias == null &&
					refPart.getAnnotations() == null) {
				if (isGroupPart(optionalPart)) {
					List<AstRulePart> groupPart = getGroupPart(optionalPart);
					return builder.optional(convertGroup(outer, groupPart, optionalPart), refPart.getReference());
				} else if (isChoicePart(optionalPart)) {
					List<AstRule> rules = ((AstRuleNestedNonTerm) optionalPart).getRules();
					return builder.optional(convertChoice(outer, rules, optionalPart), refPart.getReference());
				}
			}

			Symbol optsym = resolve(outer, optionalPart);
			RhsSymbol symbol = builder.symbol(alias, optsym, nla, optionalPart);
			TMDataUtil.putAnnotations(symbol, annotations);
			return builder.optional(symbol, refPart);
		}

		// inline (...)
		if (isGroupPart(refPart)) {
			List<AstRulePart> groupPart = getGroupPart(refPart.getReference());
			return convertGroup(outer, groupPart, refPart.getReference());

			// inline (...|...|...)
		} else if (isChoicePart(refPart)) {
			List<AstRule> rules = ((AstRuleNestedNonTerm) refPart.getReference()).getRules();
			return convertChoice(outer, rules, refPart.getReference());
		}

		Symbol sym = resolve(outer, refPart.getReference());
		RhsSymbol rhsSymbol = builder.symbol(alias, sym, nla, refPart.getReference());
		TMDataUtil.putAnnotations(rhsSymbol, annotations);
		return rhsSymbol;
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

	private RhsPart convertGroup(Symbol outer, List<AstRulePart> groupPart, SourceElement origin) {
		List<RhsPart> groupResult = new ArrayList<RhsPart>();
		for (AstRulePart innerPart : groupPart) {
			RhsPart rulePart = convertRulePart(outer, innerPart);
			if (rulePart != null) {
				groupResult.add(rulePart);
			}
		}
		return groupResult.isEmpty() ? null : builder.sequence(groupResult, origin);
	}

	private Symbol resolve(Symbol outer, AstRuleSymbolRef rulesymref) {
		if (rulesymref instanceof AstRuleDefaultSymbolRef) {
			return resolver.resolve(((AstRuleDefaultSymbolRef) rulesymref).getReference());

		} else if (rulesymref instanceof AstRuleNestedNonTerm) {
			Nonterminal nested = (Nonterminal) resolver.createNestedNonTerm(outer, rulesymref);
			List<AstRule> rules = ((AstRuleNestedNonTerm) rulesymref).getRules();
			for (AstRule right : rules) {
				if (!right.hasSyntaxError()) {
					createRule(nested, right);
				}
			}
			return nested;

		} else if (rulesymref instanceof AstRuleNestedListWithSeparator) {
			AstRuleNestedListWithSeparator listWithSeparator = (AstRuleNestedListWithSeparator) rulesymref;

			RhsPart inner = convertGroup(outer, listWithSeparator.getRuleParts(), listWithSeparator);
			List<RhsPart> sep = new ArrayList<RhsPart>();
			for (AstReference ref : listWithSeparator.getSeparator()) {
				Symbol s = resolver.resolve(ref);
				if (s == null) {
					continue;
				}
				if (s instanceof Terminal) {
					sep.add(builder.symbol(null, s, null, ref));
				} else {
					error(ref, "separator should be terminal symbol");
				}
			}
			RhsPart separator = builder.sequence(sep, listWithSeparator);
			return createList(outer, inner, listWithSeparator.isAtLeastOne(), separator, rulesymref);

		} else if (rulesymref instanceof AstRuleNestedQuantifier) {
			AstRuleNestedQuantifier nestedQuantifier = (AstRuleNestedQuantifier) rulesymref;

			RhsPart inner;
			AstRuleSymbolRef innerSymRef = nestedQuantifier.getInner();
			if (isGroupPart(innerSymRef)) {
				List<AstRulePart> groupPart = getGroupPart(innerSymRef);
				inner = convertGroup(outer, groupPart, innerSymRef);
			} else {
				Symbol innerTarget = resolve(outer, innerSymRef);
				inner = builder.symbol(null, innerTarget, null, innerSymRef);
			}
			int quantifier = nestedQuantifier.getQuantifier();
			if (quantifier == AstRuleNestedQuantifier.KIND_OPTIONAL) {
				error(rulesymref, "? cannot be a child of another quantifier");
				return null;
			}
			return createList(outer, inner, quantifier == AstRuleNestedQuantifier.KIND_ONEORMORE, null, rulesymref);
		}

		return null;
	}

	private Symbol createList(Symbol outer, RhsPart inner, boolean atLeastOne, RhsPart separator, AstRuleSymbolRef origin) {
		ListDescriptor descr = new ListDescriptor(inner, separator, atLeastOne);
		Nonterminal listSymbol = listsMap.get(descr);
		if (listSymbol != null) {
			return listSymbol;
		}

		Symbol representative = RhsUtil.getRepresentative(inner);
		listSymbol = representative != null
				? resolver.createDerived(representative, atLeastOne || separator != null ? "_list" : "_optlist", origin) /* TODO type? */
				: (Nonterminal) resolver.createNestedNonTerm(outer, origin);


		List<RhsPart> list = new ArrayList<RhsPart>();
		// list
		list.add(builder.symbol(null, listSymbol, null, origin));
		// separator
		if (separator != null) {
			list.add(separator);
		}
		List<RhsPart> rhs = new ArrayList<RhsPart>();
		if (atLeastOne || separator != null) {
			// list ::= (list <separator>)? inner
			RhsOptional optional = builder.optional(builder.sequence(list, origin), origin);
			rhs.add(optional);
			rhs.add(inner);
		} else {
			// list ::= (list inner)?
			list.add(inner);
			RhsOptional optional = builder.optional(builder.sequence(list, origin), origin);
			rhs.add(optional);
		}
		builder.addRule(null, listSymbol, builder.sequence(rhs, origin), null);

		if (separator != null && !atLeastOne) {
			// (a separator ',')*   => alistopt ::= alist | ; alist ::= a | alist ',' a ;
			Nonterminal symopt = resolver.createDerived(listSymbol, "_opt", origin);
			builder.addRule(null, symopt, builder.empty(origin), null);
			builder.addRule(null, symopt, builder.symbol(null, listSymbol, null, origin), null);
			listSymbol = symopt;
		}

		listsMap.put(descr, listSymbol);
		return listSymbol;
	}

	private boolean isOptionalPart(AstRefRulePart part) {
		AstRuleSymbolRef ref = part.getReference();
		return ref instanceof AstRuleNestedQuantifier &&
				((AstRuleNestedQuantifier) ref).getQuantifier() == AstRuleNestedQuantifier.KIND_OPTIONAL;
	}

	private AstRuleSymbolRef getOptionalPart(AstRefRulePart part) {
		return ((AstRuleNestedQuantifier) part.getReference()).getInner();
	}

	private boolean isGroupPart(AstRefRulePart part) {
		if (!(part.getReference() instanceof AstRuleNestedNonTerm)) {
			return false;
		}
		return part.getAlias() == null
				&& part.getAnnotations() == null
				&& isGroupPart(part.getReference());

	}

	private boolean isChoicePart(AstRefRulePart part) {
		if (!(part.getReference() instanceof AstRuleNestedNonTerm)) {
			return false;
		}
		return part.getAlias() == null
				&& part.getAnnotations() == null
				&& isChoicePart(part.getReference());

	}

	private boolean isGroupPart(AstRuleSymbolRef symbolRef) {
		if (!(symbolRef instanceof AstRuleNestedNonTerm)) {
			return false;
		}
		List<AstRule> innerRules = ((AstRuleNestedNonTerm) symbolRef).getRules();
		if (innerRules.size() == 1) {
			AstRule first = innerRules.get(0);
			return isSimpleNonEmpty(first);
		}
		return false;
	}

	private boolean isChoicePart(AstRuleSymbolRef symbolRef) {
		if (!(symbolRef instanceof AstRuleNestedNonTerm)) {
			return false;
		}
		List<AstRule> innerRules = ((AstRuleNestedNonTerm) symbolRef).getRules();
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
				&& rule.getAnnotations() == null
				&& rule.getAttribute() == null
				&& rule.getAlias() == null
				&& !rule.getList().isEmpty()
				&& !rule.hasSyntaxError();
	}

	private List<AstRulePart> getGroupPart(AstRuleSymbolRef symbolRef) {
		return ((AstRuleNestedNonTerm) symbolRef).getRules().get(0).getList();
	}

	private void extractUnorderedParts(AstRulePart unorderedRulePart, List<AstRefRulePart> result) {
		if (unorderedRulePart instanceof AstUnorderedRulePart) {
			extractUnorderedParts(((AstUnorderedRulePart) unorderedRulePart).getLeft(), result);
			extractUnorderedParts(((AstUnorderedRulePart) unorderedRulePart).getRight(), result);
		} else if (unorderedRulePart instanceof AstRefRulePart) {
			result.add((AstRefRulePart) unorderedRulePart);
		} else if (unorderedRulePart instanceof AstCode) {
			error(unorderedRulePart, "semantic action cannot be used as a part of unordered group");
		} else if (!(unorderedRulePart instanceof AstError)) {
			error(unorderedRulePart, "cannot be used as a part of unordered group");
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

	private String getTypesPackage() {
		if (tree.getRoot().getOptions() != null) {
			for (AstOptionPart option : tree.getRoot().getOptions()) {
				if (option instanceof AstOption && ((AstOption) option).getKey().equals("lang")) {
					AstExpression expression = ((AstOption) option).getValue();
					if (expression instanceof AstLiteralExpression) {
						return ((AstLiteralExpression) expression).getLiteral().toString();
					}
				}
			}
		}

		return "common";
	}

	private void collectOptions() {
		options = new HashMap<String, Object>();

		// Load class
		IClass optionsClass = types.getClass(myTypesPackage + ".Options", null);
		if (optionsClass == null) {
			error(tree.getRoot(), "cannot load options class `" + myTypesPackage + ".Options`");
			return;
		}

		// fill default values
		for (IFeature feature : optionsClass.getFeatures()) {
			Object value = feature.getDefaultValue();
			if (value != null) {
				options.put(feature.getName(), value);
			}
		}

		// overrides
		if (tree.getRoot().getOptions() == null) {
			return;
		}
		for (AstOptionPart option : tree.getRoot().getOptions()) {
			if (option instanceof AstOption) {
				String key = ((AstOption) option).getKey();
				IFeature feature = optionsClass.getFeature(key);
				if (feature == null) {
					error(option, "unknown option `" + key + "`");
					continue;
				}

				AstExpression value = ((AstOption) option).getValue();
				options.put(key, convertExpression(value, feature.getType()));
			}
		}
	}

	private void error(IAstNode n, String message) {
		resolver.error(n, message);
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> convert(AstAnnotations astAnnotations, String kind) {
		if (astAnnotations == null || astAnnotations.getAnnotations() == null) {
			return null;
		}

		// Load class
		IClass annoClass = types.getClass(myTypesPackage + "." + kind, null);
		if (annoClass == null) {
			error(astAnnotations, "cannot load class `" + myTypesPackage + "." + kind + "`");
			return null;
		}

		List<AstNamedEntry> list = astAnnotations.getAnnotations();
		Map<String, Object> result = new HashMap<String, Object>();
		for (AstNamedEntry entry : list) {
			if (entry.hasSyntaxError()) {
				continue;
			}
			String name = entry.getName();
			IFeature feature = annoClass.getFeature(name);
			if (feature == null) {
				error(entry, "unknown annotation `" + name + "`");
				continue;
			}

			IType expected = feature.getType();

			AstExpression expr = entry.getExpression();
			if (expr == null) {
				if (!TypesUtil.isBooleanType(expected)) {
					error(entry, "expected value of type `" + expected.toString() + "` instead of boolean");
					continue;
				}
				result.put(name, Boolean.TRUE);
			} else {
				result.put(name, convertExpression(expr, expected));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Object convertExpression(AstExpression expression, IType type) {
		return new TiExpressionBuilder<AstExpression>() {
			@Override
			public IClass resolveType(String className) {
				return types.getClass(className, null);
			}

			@Override
			public Object resolve(AstExpression expression, IType type) {
				if (expression instanceof AstInstance) {
					List<AstNamedEntry> list = ((AstInstance) expression).getEntries();
					Map<String, AstExpression> props = new HashMap<String, AstExpression>();
					if (list != null) {
						for (AstNamedEntry entry : list) {
							if (entry.hasSyntaxError()) {
								continue;
							}
							props.put(entry.getName(), entry.getExpression());
						}
					}
					String name = ((AstInstance) expression).getClassName().getName();
					if (name.indexOf('.') < 0) {
						name = myTypesPackage + "." + name;
					}
					return convertNew(expression, name, props, type);
				}
				if (expression instanceof AstArray) {
					List<AstExpression> list = ((AstArray) expression).getExpressions();
					return convertArray(expression, list, type);
				}
				if (expression instanceof AstReference) {
					IClass symbolClass = types.getClass("common.Symbol", null);
					if (symbolClass == null) {
						report(expression, "cannot load class `common.Symbol`");
						return null;
					}
					if (!symbolClass.isSubtypeOf(type)) {
						report(expression, "`" + symbolClass.toString() + "` is not a subtype of `" + type.toString()
								+ "`");
						return null;
					}
					return resolver.resolve((AstReference) expression);
				}
				if (expression instanceof AstLiteralExpression) {
					Object literal = ((AstLiteralExpression) expression).getLiteral();
					return convertLiteral(expression, literal, type);
				}
				return null;
			}

			@Override
			public void report(AstExpression expression, String message) {
				error(expression, message);
			}
		}.resolve(expression, type);
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
