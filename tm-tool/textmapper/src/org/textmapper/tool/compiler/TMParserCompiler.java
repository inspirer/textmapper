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

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsSet.Operation;
import org.textmapper.tool.compiler.TMTypeHint.Kind;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.ast.*;
import org.textmapper.tool.parser.ast.TmaSetBinary.TmaKindKind;

import java.util.*;

/**
 * evgeny, 1/29/13
 */
public class TMParserCompiler {

	private final TMTree<TmaInput> tree;
	private final TMResolver resolver;
	private final GrammarBuilder builder;
	private TMExpressionResolver expressionResolver;
	private boolean hasInputs = false;

	public TMParserCompiler(TMResolver resolver, TMExpressionResolver expressionResolver) {
		this.resolver = resolver;
		this.expressionResolver = expressionResolver;
		this.tree = resolver.getTree();
		this.builder = resolver.getBuilder();
	}

	public void compile() {
		collectAnnotations();
		collectAstTypes();
		collectRules();
		collectDirectives();

		if (!hasInputs) {
			Symbol input = resolver.getSymbol("input");
			if (input == null) {
				error(tree.getRoot(), "no input nonterminal");
			} else if (!(input instanceof Nonterminal)) {
				error(tree.getRoot(), "input must be a nonterminal");
			} else {
				builder.addInput((Nonterminal) input, true, input);
			}
		}
	}

	private Nonterminal asNonterminalWithoutType(TmaSymref ref, Set<String> withType) {
		String name = ref.getName();
		Symbol type = resolver.getSymbol(name);
		if (type == null) {
			error(ref, name + " cannot be resolved");
		} else if (!(type instanceof Nonterminal)) {
			error(ref, "ast type must be a nonterminal");
		} else if (withType != null && withType.contains(name)) {
			error(ref, "nonterminal without a type is expected (instead of `" + name + "')");
		} else {
			return (Nonterminal) type;
		}
		return null;
	}

	private void collectAstTypes() {
		Set<String> withType = new HashSet<String>();
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNonterm) {
				TmaNonterm nonterm = (TmaNonterm) clause;
				if (nonterm.getType() instanceof TmaNontermTypeRaw) {
					withType.add(nonterm.getName().getID());
				}
			}
		}

		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNonterm) {
				TmaNonterm nonterm = (TmaNonterm) clause;
				Symbol left = resolver.getSymbol(nonterm.getName().getID());
				if (left == null || !(left instanceof Nonterminal)) {
					continue; /* error is already reported */
				}
				if (nonterm.getType() instanceof TmaNontermTypeAST) {
					final TmaNontermTypeAST astType = (TmaNontermTypeAST) nonterm.getType();
					Nonterminal type = asNonterminalWithoutType(astType.getReference(), withType);
					if (type != null) {
						TMDataUtil.putCustomType((Nonterminal) left, type);
					}
				} else if (nonterm.getType() instanceof TmaNontermTypeHint) {
					TmaNontermTypeHint hint = (TmaNontermTypeHint) nonterm.getType();
					if (hint.getIsInline()) {
						error(hint, "inline classes are not supported yet");
						continue;
					}

					TMTypeHint.Kind kind;
					switch (hint.getKind()) {
						case LCLASS:
							kind = Kind.CLASS;
							break;
						case LINTERFACE:
							kind = Kind.INTERFACE;
							break;
						case LVOID:
							kind = Kind.VOID;
							break;
						default:
							throw new IllegalStateException();
					}
					TMDataUtil.putTypeHint((Nonterminal) left, new TMTypeHint(kind,
							hint.getName() == null ? null : hint.getName().getID()));

					if (hint.getImplements() != null && !hint.getImplements().isEmpty()) {
						List<Nonterminal> interfaces = new ArrayList<Nonterminal>();
						for (TmaSymref ref : hint.getImplements()) {
							Nonterminal type = asNonterminalWithoutType(ref, withType);
							if (type != null) {
								interfaces.add(type);
							}
						}

						if (!interfaces.isEmpty()) {
							TMDataUtil.putImplements((Nonterminal) left, interfaces);
						}
					}

				}
			}
		}
	}

	private void collectRules() {
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNonterm) {
				TmaNonterm nonterm = (TmaNonterm) clause;
				Symbol left = resolver.getSymbol(nonterm.getName().getID());
				if (left == null || !(left instanceof Nonterminal)) {
					continue; /* error is already reported */
				}
				for (TmaRule0 right : nonterm.getRules()) {
					if (!right.hasSyntaxError()) {
						createRule((Nonterminal) left, right);
					}
				}
			}
		}
	}

	private void collectDirectives() {
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaDirectivePrio) {
				TmaDirectivePrio directive = (TmaDirectivePrio) clause;
				TmaAssoc key = directive.getAssoc();
				List<Terminal> val = resolveTerminals(directive.getSymbols());
				int prio;
				if (key == TmaAssoc.LLEFT) {
					prio = Prio.LEFT;
				} else if (key == TmaAssoc.LRIGHT) {
					prio = Prio.RIGHT;
				} else if (key == TmaAssoc.LNONASSOC) {
					prio = Prio.NONASSOC;
				} else {
					error(directive, "unknown directive identifier used: `" + key + "`");
					continue;
				}
				builder.addPrio(prio, val, directive);
			} else if (clause instanceof TmaDirectiveInput) {
				List<TmaInputref> refs = ((TmaDirectiveInput) clause).getInputRefs();
				for (TmaInputref inputRef : refs) {
					Symbol sym = resolver.resolve(inputRef.getReference());
					boolean hasEoi = !inputRef.getNoeoi();
					if (sym instanceof Nonterminal) {
						builder.addInput((Nonterminal) sym, hasEoi, inputRef);
						hasInputs = true;
					} else if (sym != null) {
						error(inputRef, "input must be a nonterminal");
					}
				}
			}
		}
	}

	private void addSymbolAnnotations(TmaIdentifier id, Map<String, Object> annotations) {
		if (annotations != null) {
			Symbol sym = resolver.getSymbol(id.getID());
			Map<String, Object> symAnnotations = TMDataUtil.getAnnotations(sym);
			if (symAnnotations == null) {
				symAnnotations = new HashMap<String, Object>();
				TMDataUtil.putAnnotations(sym, symAnnotations);
			}
			for (Map.Entry<String, Object> ann : annotations.entrySet()) {
				if (symAnnotations.containsKey(ann.getKey())) {
					error(id, "redeclaration of annotation `" + ann.getKey() + "' for non-terminal: " + id.getID()
							+ ", skipped");
				} else {
					symAnnotations.put(ann.getKey(), ann.getValue());
				}
			}
		}
	}

	private void collectAnnotations() {
		for (ITmaGrammarPart clause : tree.getRoot().getParser()) {
			if (clause instanceof TmaNonterm) {
				TmaNonterm nonterm = (TmaNonterm) clause;
				addSymbolAnnotations(nonterm.getName(), expressionResolver.convert(nonterm.getAnnotations(),
						"AnnotateSymbol"));
			}
		}
	}

	private void createRule(Nonterminal left, TmaRule0 right) {
		List<RhsPart> rhs = new ArrayList<RhsPart>();
		List<ITmaRhsPart> list = right.getList();
		TmaCommand lastAction = null;
		if (list != null) {
			ITmaRhsPart last = list.size() > 0 ? list.get(list.size() - 1) : null;
			if (last instanceof TmaCommand) {
				lastAction = (TmaCommand) last;
				list = list.subList(0, list.size() - 1);
			}

			for (ITmaRhsPart part : list) {
				RhsPart rulePart = convertPart(left, part);
				if (rulePart != null) {
					rhs.add(rulePart);
				}
			}
		}
		TmaRhsSuffix ruleAttribute = right.getSuffix();
		TmaSymref rulePrio = ruleAttribute != null && ruleAttribute.getKind() == TmaRhsSuffix.TmaKindKind.LPRIO ?
				ruleAttribute.getSymref() : null;
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
		TmaRuleAction action = right.getAction();
		String alias = action != null && action.getAction() != null ? action.getAction().getID() : null;
		RhsSequence rule = builder.sequence(alias, rhs, right);
		builder.addRule(left, rule, prio);
		Map<String, Object> annotations = expressionResolver.convert(right.getAnnotations(), "AnnotateRule");

		TMDataUtil.putAnnotations(rule, annotations);
		TMDataUtil.putCode(rule, lastAction);
	}

	private RhsPart convertPart(Symbol outer, ITmaRhsPart part) {
		if (part instanceof TmaCommand) {
			TmaCommand astCode = (TmaCommand) part;
			Nonterminal codeSym = (Nonterminal) resolver.createNestedNonTerm(outer, astCode);
			RhsSequence actionRule = builder.empty(astCode);
			builder.addRule(codeSym, actionRule, null);
			TMDataUtil.putCode(actionRule, astCode);
			return builder.symbol(codeSym, astCode);

		} else if (part instanceof TmaRhsUnordered) {
			List<ITmaRhsPart> refParts = new ArrayList<ITmaRhsPart>();
			extractUnorderedParts(part, refParts);
			if (refParts.size() < 2 || refParts.size() > 5) {
				error(part, "max 5 elements are allowed for permutation");
				return null;
			}
			List<RhsPart> resolved = new ArrayList<RhsPart>(refParts.size());
			for (ITmaRhsPart refPart : refParts) {
				RhsPart rulePart = convertPart(outer, refPart);
				if (rulePart == null) {
					return null;
				}
				resolved.add(rulePart);
			}
			return builder.unordered(resolved, part);
		}

		Map<String, Object> annotations = null;
		if (part instanceof TmaRhsAnnotated) {
			final TmaAnnotations rhsAnnotations = ((TmaRhsAnnotated) part).getAnnotations();
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
		TmaRhsAsLiteral literalCast = null;
		if (part instanceof TmaRhsCast) {
			cast = (TmaRhsCast) part;
			part = cast.getInner();
		} else if (part instanceof TmaRhsAsLiteral) {
			literalCast = (TmaRhsAsLiteral) part;
			part = literalCast.getInner();
		}

		TmaRhsClass cl = null;
		if (part instanceof TmaRhsClass) {
			cl = (TmaRhsClass) part;
			part = cl.getInner();
		}

		boolean canInline = annotations == null;
		RhsPart result;

		// inline (...)
		if (canInline && isGroupPart(part)) {
			List<ITmaRhsPart> groupPart = getGroupPart(part);
			result = convertGroup(outer, groupPart, part);

			// inline (...|...|...)
		} else if (canInline && isChoicePart(part)) {
			List<TmaRule0> rules = ((TmaRhsNested) part).getRules();
			result = convertChoice(outer, rules, part);
		} else {
			result = convertPrimary(outer, part);
			if (result == null) {
				return null;
			}
			TMDataUtil.putAnnotations(result, annotations);
		}

		if (cast != null) {
			final Symbol asSymbol = resolver.resolve(cast.getTarget());
			if (asSymbol != null) {
				result = builder.cast(asSymbol, result, cast);
			}
		} else if (literalCast != null) {
			if (result instanceof RhsSymbol) {
				TMDataUtil.putLiteral((RhsSymbol) result, literalCast.getLiteral().getLiteral());
			} else {
				error(literalCast, "cannot apply `as literal' to a group");
			}
		}

		if (cl != null) {
			// TODO apply class name to `result'
			error(cl, "internal error: classes are not supported yet");
		}

		if (optional != null) {
			result = builder.optional(result, optional);
		}

		if (assignment != null) {
			result = builder.assignment(assignment.getId().getID(), result, assignment.getAddition(), assignment);
		}

		return result;
	}

	private Collection<RhsSet> asCollection(RhsSet... sets) {
		if (sets.length == 0) return null;
		for (RhsSet s : sets) {
			if (s == null) return null;
		}
		if (sets.length == 1) return Collections.singleton(sets[0]);
		return Arrays.asList(sets);
	}

	private RhsSet convertSet(ITmaSetExpression expr) {
		if (expr instanceof TmaSetBinary) {
			TmaSetBinary binary = (TmaSetBinary) expr;
			boolean is_and = binary.getKind() == TmaKindKind.AMPERSAND;

			Collection<RhsSet> parts = asCollection(convertSet(binary.getLeft()), convertSet(binary.getRight()));
			if (parts == null) return null;

			return builder.set(is_and ? Operation.Intersection : Operation.Union, null, parts, expr);

		} else if (expr instanceof TmaSetComplement) {
			Collection<RhsSet> parts = asCollection(convertSet(((TmaSetComplement) expr).getInner()));
			if (parts == null) return null;

			return builder.set(Operation.Complement, null, parts, expr);

		} else if (expr instanceof TmaSetCompound) {
			return convertSet(((TmaSetCompound) expr).getInner());

		} else if (expr instanceof TmaSetSymbol) {
			TmaSetSymbol ss = (TmaSetSymbol) expr;
			Symbol s = resolver.resolve(ss.getSymbol());
			if (s == null) return null;

			Operation kind = Operation.Any;
			if (ss.getOperator() != null) {
				String op = ss.getOperator();
				if (op.equals("first")) {
					kind = Operation.First;
				} else if (op.equals("follow")) {
					kind = Operation.Follow;
				} else {
					error(ss, "operator can be either 'first', or 'follow'");
				}
			}

			return builder.set(kind, s, null, expr);
		}

		error(expr, "internal error: unknown set expression found");
		return null;
	}

	private RhsSymbol convertPrimary(Symbol outer, ITmaRhsPart part) {

		if (part instanceof TmaRhsSymbol) {
			Symbol resolved = resolver.resolve(((TmaRhsSymbol) part).getReference());
			if (resolved == null) {
				return null;
			}
			return builder.symbol(resolved, part);

		} else if (part instanceof TmaRhsNested) {
			Nonterminal nested = (Nonterminal) resolver.createNestedNonTerm(outer, part);
			List<TmaRule0> rules = ((TmaRhsNested) part).getRules();
			for (TmaRule0 right : rules) {
				if (!right.hasSyntaxError()) {
					createRule(nested, right);
				}
			}
			return builder.symbol(nested, part);

		} else if (part instanceof TmaRhsIgnored) {
			error(part, "$( ) is not supported, yet");
			// TODO
			return null;

		} else if (part instanceof TmaRhsSet) {
			RhsSet set = convertSet(((TmaRhsSet) part).getExpr());
			if (set == null) return null;

			String setName = set.getProvisionalName();
			Nonterminal result = builder.addShared(set, setName);
			return builder.symbol(result, part);

		} else if (part instanceof TmaRhsList) {
			TmaRhsList listWithSeparator = (TmaRhsList) part;

			RhsSequence inner = convertGroup(outer, listWithSeparator.getRuleParts(), listWithSeparator);
			List<RhsPart> sep = new ArrayList<RhsPart>();
			for (TmaSymref ref : listWithSeparator.getSeparator()) {
				Symbol s = resolver.resolve(ref);
				if (s == null) {
					continue;
				}
				if (s instanceof Terminal) {
					sep.add(builder.symbol(s, ref));
				} else {
					error(ref, "separator must be a terminal symbol");
				}
			}
			RhsPart separator = builder.sequence(null, sep, listWithSeparator);
			return createList(inner, listWithSeparator.isAtLeastOne(), separator, part);

		} else if (part instanceof TmaRhsQuantifier) {
			TmaRhsQuantifier nestedQuantifier = (TmaRhsQuantifier) part;

			RhsSequence inner;
			ITmaRhsPart innerSymRef = nestedQuantifier.getInner();
			if (isGroupPart(innerSymRef)) {
				List<ITmaRhsPart> groupPart = getGroupPart(innerSymRef);
				inner = convertGroup(outer, groupPart, innerSymRef);
			} else {
				RhsSymbol symref = convertPrimary(outer, innerSymRef);
				if (symref == null) {
					return null;
				}
				inner = builder.sequence(null, Arrays.<RhsPart>asList(symref), innerSymRef);
			}
			int quantifier = nestedQuantifier.getQuantifier();
			if (quantifier == TmaRhsQuantifier.KIND_OPTIONAL) {
				error(part, "? cannot be a child of another quantifier");
				return null;
			}
			return createList(inner, quantifier == TmaRhsQuantifier.KIND_ONEORMORE, null, part);
		}

		error(part, "internal error: unknown right-hand side part found");
		return null;
	}

	private RhsPart convertChoice(Symbol outer, List<TmaRule0> rules, SourceElement origin) {
		Collection<RhsPart> result = new ArrayList<RhsPart>(rules.size());
		for (TmaRule0 rule : rules) {
			RhsPart abstractRulePart = convertGroup(outer, rule.getList(), rule);
			if (abstractRulePart == null) {
				return null;
			}
			result.add(abstractRulePart);
		}
		return builder.choice(result, origin);
	}

	private RhsSequence convertGroup(Symbol outer, List<ITmaRhsPart> groupPart, SourceElement origin) {
		List<RhsPart> groupResult = new ArrayList<RhsPart>();
		if (groupPart == null) {
			return null;
		}
		for (ITmaRhsPart innerPart : groupPart) {
			RhsPart rulePart = convertPart(outer, innerPart);
			if (rulePart != null) {
				groupResult.add(rulePart);
			}
		}
		return groupResult.isEmpty() ? null : builder.sequence(null, groupResult, origin);
	}

	private RhsSymbol createList(RhsSequence inner, boolean nonEmpty, RhsPart separator, ITmaRhsPart origin) {
		RhsList list = builder.list(inner, separator, (separator != null && !nonEmpty) || nonEmpty, origin);
		String listName = list.getProvisionalName();
		Nonterminal result = builder.addShared(list, listName);

		if (separator != null && !nonEmpty) {
			// (a separator ',')*  => alistopt ::= alist | ; alist ::= a | alist ',' a ;
			result = builder.addShared(builder.optional(builder.symbol(result, origin), origin), listName + "_opt");
		}
		return builder.symbol(result, origin);

	}

	private boolean isGroupPart(ITmaRhsPart symbolRef) {
		if (!(symbolRef instanceof TmaRhsNested)) {
			return false;
		}
		List<TmaRule0> innerRules = ((TmaRhsNested) symbolRef).getRules();
		if (innerRules.size() == 1) {
			TmaRule0 first = innerRules.get(0);
			return isSimpleNonEmpty(first);
		}
		return false;
	}

	private boolean isChoicePart(ITmaRhsPart symbolRef) {
		if (!(symbolRef instanceof TmaRhsNested)) {
			return false;
		}
		List<TmaRule0> innerRules = ((TmaRhsNested) symbolRef).getRules();
		if (innerRules.size() < 2) {
			return false;
		}
		for (TmaRule0 rule : innerRules) {
			if (!(isSimpleNonEmpty(rule))) {
				return false;
			}
		}
		return true;
	}

	private boolean isSimpleNonEmpty(TmaRule0 rule) {
		return rule != null
				&& rule.getPrefix() == null
				&& rule.getSuffix() == null
				&& rule.getList() != null
				&& !rule.getList().isEmpty()
				&& !rule.hasSyntaxError();
	}

	private List<ITmaRhsPart> getGroupPart(ITmaRhsPart symbolRef) {
		return ((TmaRhsNested) symbolRef).getRules().get(0).getList();
	}

	private void extractUnorderedParts(ITmaRhsPart unorderedRulePart, List<ITmaRhsPart> result) {
		if (unorderedRulePart instanceof TmaRhsUnordered) {
			extractUnorderedParts(((TmaRhsUnordered) unorderedRulePart).getLeft(), result);
			extractUnorderedParts(((TmaRhsUnordered) unorderedRulePart).getRight(), result);
		} else if (unorderedRulePart instanceof TmaCommand) {
			error(unorderedRulePart, "semantic action cannot be used as a part of unordered group");
		} else if (!(unorderedRulePart instanceof TmaSyntaxProblem)) {
			result.add(unorderedRulePart);
		}
	}

	private List<Terminal> resolveTerminals(List<TmaSymref> input) {
		List<Terminal> result = new ArrayList<Terminal>(input.size());
		for (TmaSymref id : input) {
			Symbol sym = resolver.resolve(id);
			if (sym instanceof Terminal) {
				result.add((Terminal) sym);
			} else if (sym != null) {
				error(id, "terminal is expected");
			}
		}
		return result;
	}

	private void error(ITmaNode n, String message) {
		resolver.error(n, message);
	}
}
