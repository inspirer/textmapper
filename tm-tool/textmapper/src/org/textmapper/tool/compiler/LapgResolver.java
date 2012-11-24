/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
import org.textmapper.lapg.api.builder.RuleBuilder;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexMatcher;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.common.FormatUtil;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IType;
import org.textmapper.templates.types.TiExpressionBuilder;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.templates.types.TypesUtil;
import org.textmapper.tool.compiler.LapgRuleBuilder.*;
import org.textmapper.tool.gen.TemplateStaticMethods;
import org.textmapper.tool.parser.LapgLexer;
import org.textmapper.tool.parser.LapgLexer.ErrorReporter;
import org.textmapper.tool.parser.LapgLexer.LapgSymbol;
import org.textmapper.tool.parser.LapgLexer.Lexems;
import org.textmapper.tool.parser.LapgTree;
import org.textmapper.tool.parser.LapgTree.LapgProblem;
import org.textmapper.tool.parser.LapgTree.TextSource;
import org.textmapper.tool.parser.ast.*;

import java.io.IOException;
import java.util.*;

public class LapgResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver"; //$NON-NLS-1$
	private static final String INITIAL_STATE = "initial";

	private final LapgTree<AstRoot> tree;
	private final TypesRegistry types;
	private String myTypesPackage;

	private final Map<String, Symbol> symbolsMap = new HashMap<String, Symbol>();
	private final Map<Symbol, String> identifierMap = new HashMap<Symbol, String>();
	private final Map<String, LexerState> statesMap = new HashMap<String, LexerState>();
	private final Map<AstLexeme, LapgLexicalRule> lexemeMap = new HashMap<AstLexeme, LapgLexicalRule>();

	private final Map<SourceElement, Map<String, Object>> annotationsMap = new HashMap<SourceElement, Map<String, Object>>();
	private final Map<SourceElement, TextSourceElement> codeMap = new HashMap<SourceElement, TextSourceElement>();
	private final Map<ListDescriptor, Nonterminal> listsMap = new HashMap<ListDescriptor, Nonterminal>();

	private Map<String, Object> options;
	private GrammarBuilder builder;
	private boolean hasInputs = false;

	public LapgResolver(LapgTree<AstRoot> tree, TypesRegistry types) {
		this.tree = tree;
		this.types = types;
	}

	public LapgGrammar resolve() {
		if (tree.getRoot() == null) {
			return null;
		}
		myTypesPackage = getTypesPackage();

		builder = LapgCore.createBuilder();
		symbolsMap.put(Symbol.EOI, builder.getEoi());
		collectLexerStates();
		collectLexems();

		if (tree.getRoot().getGrammar() != null) {
			collectNonTerminals();
			collectRules();
			collectDirectives();

			if (!hasInputs) {
				Symbol input = symbolsMap.get("input");
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
		String copyrightHeader = extractCopyright();

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
			identifierMap.put(sym, helper.generateId(sym.getName(), i));
		}

		Map<LexicalRule, LapgStateTransitionSwitch> transitionMap = new HashMap<LexicalRule, LapgStateTransitionSwitch>();
		for (LexicalRule rule : g.getLexicalRules()) {
			AstLexeme astLexeme = (AstLexeme) ((DerivedSourceElement) rule).getOrigin();
			LapgLexicalRule lapgRule = lexemeMap.get(astLexeme);
			transitionMap.put(rule, lapgRule.getTransitions());
		}

		return new LapgGrammar(g, templates, !tree.getErrors().isEmpty(), options, copyrightHeader,
				identifierMap, annotationsMap, codeMap, transitionMap);
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

	private Symbol create(AstIdentifier id, String type, int kind, Terminal softClass) {
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

	private Symbol createNested(int kind, String type, Symbol outer, Terminal softClass, IAstNode source) {
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

	private Nonterminal createDerived(Symbol element, String suffix, IAstNode source) {
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

	private Symbol resolve(AstReference id) {
		String name = id.getName();
		Symbol sym = symbolsMap.get(name);
		if (sym == null) {
			if (name.length() > 3 && name.endsWith("opt")) {
				sym = symbolsMap.get(name.substring(0, name.length() - 3));
				if (sym != null) {
					Nonterminal symopt = (Nonterminal) create(
							new AstIdentifier(id.getName(), id.getInput(), id.getOffset(), id.getEndOffset()),
							sym.getType(), Symbol.KIND_NONTERM, null);
					RuleBuilder rb = builder.rule(null, symopt, id);
					rb.create();
					rb.addPart(null, sym, null, id);
					rb.create();
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	private List<LexerState> convertApplicableStates(AstStateSelector selector) {
		List<LexerState> result = new ArrayList<LexerState>();
		for (AstLexerState state : selector.getStates()) {
			LexerState applicable = statesMap.get(state.getName().getName());
			result.add(applicable);
		}
		return result;
	}

	private LapgStateTransitionSwitch convertTransitions(AstStateSelector selector) {
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
			LexerState target = statesMap.get(targetName);
			if (target == null) {
				error(state.getDefaultTransition(), targetName + " cannot be resolved");
				continue;
			}

			if (defaultTransition == null && !(noDefault)) {
				defaultTransition = target;
			} else if (defaultTransition != target) {
				LexerState source = statesMap.get(state.getName().getName());
				stateSwitch.put(source, target);
			}
		}
		return stateSwitch.isEmpty() && defaultTransition == null ? null
				: new LapgStateTransitionSwitch(stateSwitch.isEmpty() ? null : stateSwitch, defaultTransition);
	}

	private LapgStateTransitionSwitch getTransition(AstLexeme lexeme, LapgStateTransitionSwitch active) {
		AstReference transition = lexeme.getTransition();
		if (transition != null) {
			String targetName = transition.getName();
			LexerState target = statesMap.get(targetName);
			if (target == null) {
				error(transition, targetName + " cannot be resolved");
			} else {
				return new LapgStateTransitionSwitch(target);
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
				if (!lexemeMap.get(astClassLexeme).canBeClassFor(lexemeMap.get(l))) {
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

	private void collectLexems() {
		List<LexicalRule> classRules = new LinkedList<LexicalRule>();
		Map<String, RegexPart> namedPatternsMap = new HashMap<String, RegexPart>();

		// Step 1. Process class lexems, named patterns & groups.

		LapgStateTransitionSwitch activeTransitions = null;
		List<LexerState> activeStates = Collections.singletonList(statesMap.get(INITIAL_STATE));

		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				lexemeMap.put(lexeme, new LapgLexicalRule(lexeme, getTransition(lexeme, activeTransitions), activeStates));
				AstLexemAttrs attrs = lexeme.getAttrs();
				if (attrs == null || attrs.getKind() != LexicalRule.KIND_CLASS) {
					continue;
				}
				if (lexeme.getRegexp() == null) {
					error(lexeme, "class lexeme rule without regular expression, ignored");
					continue;
				}

				Terminal s = (Terminal) create(lexeme.getName(), lexeme.getType(), Symbol.KIND_TERM, null);
				RegexPart regex;
				try {
					regex = LapgCore.parse(s.getName(), lexeme.getRegexp().getRegexp());
				} catch (RegexParseException e) {
					error(lexeme.getRegexp(), e.getMessage());
					continue;
				}

				LexicalRule liLexicalRule = builder.addLexicalRule(LexicalRule.KIND_CLASS, s, regex, lexemeMap.get(lexeme).getApplicableInStates(), lexeme.getPriority(),
						null, lexeme);
				classRules.add(liLexicalRule);
				codeMap.put(liLexicalRule, lexeme.getCode());
			} else if (clause instanceof AstStateSelector) {
				activeStates = convertApplicableStates((AstStateSelector) clause);
				activeTransitions = convertTransitions((AstStateSelector) clause);
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
					error(astpattern, "redeclaration of named pattern `" + name + "'");
				} else {
					builder.addPattern(name, regex, astpattern);
					namedPatternsMap.put(name, regex);
				}
			}
		}

		// Step 2. Process other lexems. Match soft lexems with their classes.

		RegexContext context = LapgCore.createContext(namedPatternsMap);
		Map<LexicalRule, RegexMatcher> classMatchers = new LinkedHashMap<LexicalRule, RegexMatcher>();
		for (LexicalRule clRule : classRules) {
			classMatchers.put(clRule, LapgCore.createMatcher(clRule.getRegexp(), context));
		}

		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				AstLexemAttrs attrs = lexeme.getAttrs();
				int kind = attrs == null ? LexicalRule.KIND_NONE : attrs.getKind();
				if (kind == LexicalRule.KIND_CLASS) {
					continue;
				}
				if (lexeme.getRegexp() != null) {
					String name = lexeme.getName().getName();
					RegexPart regex;
					try {
						regex = LapgCore.parse(name, lexeme.getRegexp().getRegexp());
					} catch (RegexParseException e) {
						error(lexeme.getRegexp(), e.getMessage());
						continue;
					}

					if (kind == LexicalRule.KIND_SOFT && lexeme.getCode() != null) {
						error(lexeme.getCode(), "soft lexeme rule `" + lexeme.getName().getName()
								+ "' cannot have a semantic action");
					}
					LexicalRule classRule = getClassRule(classMatchers, lexeme, regex);
					Terminal softClass = null;
					if (kind == LexicalRule.KIND_SOFT) {
						if (classRule == null) {
							if (!regex.isConstant()) {
								error(lexeme, "soft lexeme rule `" + name + "' should have a constant regexp");
							} else {
								error(lexeme, "soft lexeme rule `" + name + "' doesn't match any class rule");
							}
							kind = LexicalRule.KIND_NONE;
						} else {
							softClass = classRule.getSymbol();

							String type = lexeme.getType();
							String classtype = softClass.getType();
							if (type != null && !type.equals(classtype)) {
								if (classtype == null) {
									classtype = "<no type>";
								}
								error(lexeme, "soft terminal `" + name + "' overrides base type: expected `"
										+ classtype + "', found `" + type + "'");
							}
						}
					}

					Terminal s = (Terminal) create(lexeme.getName(), lexeme.getType(),
							kind == LexicalRule.KIND_SOFT ? Symbol.KIND_SOFTTERM : Symbol.KIND_TERM, softClass);
					LexicalRule liLexicalRule = builder.addLexicalRule(kind, s, regex, lexemeMap.get(lexeme).getApplicableInStates(), lexeme.getPriority(),
							classRule, lexeme);
					codeMap.put(liLexicalRule, lexeme.getCode());
				} else {
					if (kind == LexicalRule.KIND_SOFT) {
						error(lexeme, "soft lexeme rule `" + lexeme.getName().getName() + "' should have a regular expression");
					}
					create(lexeme.getName(), lexeme.getType(), Symbol.KIND_TERM, null);
				}
			}
		}
	}

	private void addSymbolAnnotations(AstIdentifier id, Map<String, Object> annotations) {
		if (annotations != null) {
			Symbol sym = symbolsMap.get(id.getName());
			Map<String, Object> symAnnotations = annotationsMap.get(sym);
			if (symAnnotations == null) {
				symAnnotations = new HashMap<String, Object>();
				annotationsMap.put(sym, symAnnotations);
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

	private void collectNonTerminals() {
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				create(nonterm.getName(), nonterm.getType(), Symbol.KIND_NONTERM, null);
			}
		}
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				addSymbolAnnotations(nonterm.getName(), convert(nonterm.getAnnotations(), "AnnotateSymbol"));
			}
		}
	}

	private void createRule(Nonterminal left, AstRule right) {
		LapgRuleBuilder ruleBuilder = new LapgRuleBuilder(builder, right.getAlias(), left, right, annotationsMap);
		List<AstRulePart> list = right.getList();
		AstCode lastAction = null;
		if (list != null) {
			AstRulePart last = list.size() > 0 ? list.get(list.size() - 1) : null;
			if (last instanceof AstCode) {
				lastAction = (AstCode) last;
				list = list.subList(0, list.size() - 1);
			}

			for (AstRulePart part : list) {
				AbstractRulePart rulePart = convertRulePart(left, part);
				if (rulePart != null) {
					ruleBuilder.add(rulePart);
				}
			}
		}
		AstRuleAttribute ruleAttribute = right.getAttribute();
		AstReference rulePrio = ruleAttribute instanceof AstPrioClause ? ((AstPrioClause) ruleAttribute).getReference()
				: null;
		if (rulePrio != null) {
			Symbol prio = resolve(rulePrio);
			if (prio instanceof Terminal) {
				ruleBuilder.setPriority((Terminal) prio);
			} else if (prio != null) {
				error(rulePrio, "symbol `" + prio.getName() + "' is not a terminal");
			}
		}

		// TODO store %shift attribute
		// TODO check right.getAnnotations().getNegativeLA() == null
		Rule[] result = ruleBuilder.create();
		Map<String, Object> annotations = convert(right.getAnnotations(), "AnnotateRule");
		for (Rule r : result) {
			annotationsMap.put(r, annotations);
			codeMap.put(r, lastAction);
		}
	}

	private AbstractRulePart convertRulePart(Symbol outer, AstRulePart part) {
		if (part instanceof AstCode) {
			AstCode astCode = (AstCode) part;
			Nonterminal codeSym = (Nonterminal) createNested(Symbol.KIND_NONTERM, null, outer, null, astCode);
			Rule actionRule = builder.rule(null, codeSym, astCode).create();
			codeMap.put(actionRule, astCode);
			return new RulePart(null, codeSym, null, null, astCode);

		} else if (part instanceof AstUnorderedRulePart) {
			List<AstRefRulePart> refParts = new ArrayList<AstRefRulePart>();
			extractUnorderedParts(part, refParts);
			if (refParts.size() < 2 || refParts.size() > 5) {
				error(part, "max 5 elements are allowed for permutation");
				return null;
			}
			AbstractRulePart[] resolved = new AbstractRulePart[refParts.size()];
			int index = 0;
			for (AstRefRulePart refPart : refParts) {
				AbstractRulePart rulePart = convertRulePart(outer, refPart);
				if (rulePart == null) {
					return null;
				}
				resolved[index++] = rulePart;
			}
			return new UnorderedRulePart(resolved);

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
					return convertGroup(outer, groupPart, true);
				} else if (isChoicePart(optionalPart)) {
					List<AstRule> rules = ((AstRuleNestedNonTerm) optionalPart).getRules();
					return convertChoice(outer, rules, true);
				}
			}

			Symbol optsym = resolve(outer, optionalPart);
			return new CompositeRulePart(true, new RulePart(alias, optsym, nla, annotations, refPart.getReference()));
		}

		// inline (...)
		if (isGroupPart(refPart)) {
			List<AstRulePart> groupPart = getGroupPart(refPart.getReference());
			return convertGroup(outer, groupPart, false);

			// inline (...|...|...)
		} else if (isChoicePart(refPart)) {
			List<AstRule> rules = ((AstRuleNestedNonTerm) refPart.getReference()).getRules();
			return convertChoice(outer, rules, false);
		}

		Symbol sym = resolve(outer, refPart.getReference());
		return new RulePart(alias, sym, nla, annotations, refPart.getReference());
	}

	private AbstractRulePart convertChoice(Symbol outer, List<AstRule> rules, boolean isOptional) {
		AbstractRulePart[] result = new AbstractRulePart[rules.size()];
		int index = 0;
		for (AstRule rule : rules) {
			AbstractRulePart abstractRulePart = convertGroup(outer, rule.getList(), false);
			if (abstractRulePart == null) {
				return null;
			}
			result[index++] = abstractRulePart;
		}
		if (isOptional) {
			return new CompositeRulePart(true, new ChoiceRulePart(result));
		}
		return new ChoiceRulePart(result);
	}

	private AbstractRulePart convertGroup(Symbol outer, List<AstRulePart> groupPart, boolean isOptional) {
		List<AbstractRulePart> groupResult = new ArrayList<AbstractRulePart>();
		for (AstRulePart innerPart : groupPart) {
			AbstractRulePart rulePart = convertRulePart(outer, innerPart);
			if (rulePart != null) {
				groupResult.add(rulePart);
			}
		}
		return groupResult.size() > 0
				? new CompositeRulePart(isOptional, groupResult.toArray(new AbstractRulePart[groupResult.size()]))
				: null;
	}

	private Symbol resolve(Symbol outer, AstRuleSymbolRef rulesymref) {
		if (rulesymref instanceof AstRuleDefaultSymbolRef) {
			return resolve(((AstRuleDefaultSymbolRef) rulesymref).getReference());

		} else if (rulesymref instanceof AstRuleNestedNonTerm) {
			Nonterminal nested = (Nonterminal) createNested(Symbol.KIND_NONTERM, null, outer, null, rulesymref);
			List<AstRule> rules = ((AstRuleNestedNonTerm) rulesymref).getRules();
			for (AstRule right : rules) {
				if (!right.hasSyntaxError()) {
					createRule(nested, right);
				}
			}
			return nested;

		} else if (rulesymref instanceof AstRuleNestedListWithSeparator) {
			AstRuleNestedListWithSeparator listWithSeparator = (AstRuleNestedListWithSeparator) rulesymref;

			AbstractRulePart inner = convertGroup(outer, listWithSeparator.getRuleParts(), false);
			List<AbstractRulePart> sep = new ArrayList<AbstractRulePart>();
			for (AstReference ref : listWithSeparator.getSeparator()) {
				Symbol s = resolve(ref);
				if (s == null) {
					continue;
				}
				if (s instanceof Terminal) {
					sep.add(new RulePart(s, ref));
				} else {
					error(ref, "separator should be terminal symbol");
				}
			}
			AbstractRulePart separator = new CompositeRulePart(false, sep.toArray(new AbstractRulePart[sep.size()]));
			return createList(outer, inner, listWithSeparator.isAtLeastOne(), separator, rulesymref);

		} else if (rulesymref instanceof AstRuleNestedQuantifier) {
			AstRuleNestedQuantifier nestedQuantifier = (AstRuleNestedQuantifier) rulesymref;

			AbstractRulePart inner;
			AstRuleSymbolRef innerSymRef = nestedQuantifier.getInner();
			if (isGroupPart(innerSymRef)) {
				List<AstRulePart> groupPart = getGroupPart(innerSymRef);
				inner = convertGroup(outer, groupPart, false);
			} else {
				Symbol innerTarget = resolve(outer, innerSymRef);
				inner = new RulePart(innerTarget, innerSymRef);
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

	private Symbol createList(Symbol outer, AbstractRulePart inner, boolean atLeastOne, AbstractRulePart separator, AstRuleSymbolRef origin) {
		ListDescriptor descr = new ListDescriptor(inner, separator, atLeastOne);
		Nonterminal listSymbol = listsMap.get(descr);
		if (listSymbol != null) {
			return listSymbol;
		}

		Symbol representative = inner.getRepresentative();
		listSymbol = representative != null
				? createDerived(representative, atLeastOne || separator != null ? "_list" : "_optlist", origin) /* TODO type? */
				: (Nonterminal) createNested(Symbol.KIND_NONTERM, null, outer, null, origin);

		LapgRuleBuilder rb = new LapgRuleBuilder(builder, null, listSymbol, origin, annotationsMap);
		// list
		rb.add(new RulePart(listSymbol, origin));
		// separator
		if (separator != null) {
			rb.add(separator);
		}
		rb.add(inner);
		rb.create();
		rb = new LapgRuleBuilder(builder, null, listSymbol, origin, annotationsMap);
		if (atLeastOne || separator != null) {
			// one or more list
			rb.add(inner);
		}
		rb.create();

		if (separator != null && !atLeastOne) {
			// (a separator ',')*   => alistopt ::= alist | ; alist ::= a | alist ',' a ;
			Nonterminal symopt = createDerived(listSymbol, "_opt", origin);
			RuleBuilder b = builder.rule(null, symopt, origin);
			b.create();
			b.addPart(null, listSymbol, null, origin);
			b.create();
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
				Symbol left = symbolsMap.get(nonterm.getName().getName());
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
			Symbol sym = resolve(id);
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
					Symbol sym = resolve(inputRef.getReference());
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
		tree.getErrors().add(new LapgResolverProblem(LapgTree.KIND_ERROR, n.getOffset(), n.getEndOffset(), message));
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
					return LapgResolver.this.resolve((AstReference) expression);
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

	private String extractCopyright() {
		TextSource source = tree.getSource();
		final boolean[] hasErrors = new boolean[]{false};
		ErrorReporter reporter = new ErrorReporter() {
			@Override
			public void error(int start, int end, int line, String s) {
				hasErrors[0] = true;
			}
		};

		try {
			LapgLexer lexer = new LapgLexer(source.getStream(), reporter);
			lexer.setSkipComments(false);
			List<String> headers = new LinkedList<String>();

			LapgSymbol sym = lexer.next();
			int lastline = 0;
			StringBuilder sb = new StringBuilder();
			while (sym.symbol == Lexems._skip_comment && source.columnForOffset(sym.offset) == 0) {
				String val = lexer.current().substring(1);
				if (val.endsWith("\n")) {
					val = val.substring(0, val.length() - (val.endsWith("\r\n") ? 2 : 1));
				}
				if (sym.line > lastline + 1 && sb.length() > 0) {
					headers.add(sb.toString());
					sb.setLength(0);
				}
				lastline = sym.line;
				if (!(sym.line == 1 && val.startsWith("!"))) {
					sb.append(val).append('\n');
				}
				sym = lexer.next();
			}
			if (hasErrors[0]) {
				return null;
			}
			if (sb.length() > 0) {
				headers.add(sb.toString());
			}
			for (String s : headers) {
				if (s.toLowerCase().contains("license")) {
					return new TemplateStaticMethods().shiftLeft(s);
				}
			}

		} catch (IOException e) {
			/* ignore */
		}

		return null;
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

	private static class ListDescriptor {
		private final AbstractRulePart inner;
		private final AbstractRulePart separator;
		private final boolean atLeastOne;

		private ListDescriptor(AbstractRulePart inner, AbstractRulePart separator, boolean atLeastOne) {
			this.inner = inner;
			this.separator = separator;
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
