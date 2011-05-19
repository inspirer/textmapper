/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.parser;

import org.textway.lapg.api.*;
import org.textway.lapg.common.FormatUtil;
import org.textway.lapg.lex.RegexMatcher;
import org.textway.lapg.lex.RegexpParseException;
import org.textway.lapg.parser.LapgLexer.ErrorReporter;
import org.textway.lapg.parser.LapgLexer.LapgSymbol;
import org.textway.lapg.parser.LapgLexer.Lexems;
import org.textway.lapg.parser.LapgTree.LapgProblem;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.parser.ast.*;
import org.textway.lapg.regex.RegexPart;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IFeature;
import org.textway.templates.api.types.IType;
import org.textway.templates.types.TiExpressionBuilder;
import org.textway.templates.types.TypesRegistry;
import org.textway.templates.types.TypesUtil;

import java.io.IOException;
import java.util.*;

public class LapgResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver"; //$NON-NLS-1$

	private final LapgTree<AstRoot> tree;
	private final TypesRegistry types;
	private String myTypesPackage;

	private final Map<String, LiSymbol> symbolsMap = new HashMap<String, LiSymbol>();

	private final List<LiSymbol> symbols = new ArrayList<LiSymbol>();
	private List<LiLexem> lexems;
	private List<LiRule> rules;
	private List<LiPrio> priorities;

	private List<LiInputRef> inputs;
	private LiSymbol eoi;

	private Map<String, Object> options;
	private LapgResolverHelper helper = new LapgResolverHelper();

	public LapgResolver(LapgTree<AstRoot> tree, TypesRegistry types) {
		this.tree = tree;
		this.types = types;
	}

	public Grammar resolve() {
		if (tree.getRoot() == null) {
			return null;
		}
		myTypesPackage = getTypesPackage();

		collectLexems();
		int terminals = symbols.size();

		collectNonTerminals();
		collectRules();
		collectDirectives();
		collectOptions();
		SourceElement templates = getTemplates();

		if (inputs.size() == 0) {
			LiSymbol input = symbolsMap.get("input");
			if (input == null) {
				error(tree.getRoot(), "no input non-terminal");
			} else if (input.isTerm()) {
				error(tree.getRoot(), "input should be non-terminal");
			} else {
				inputs.add(new LiInputRef(null, input, true));
			}
		}

		LiRule[] ruleArr = rules.toArray(new LiRule[rules.size()]);
		for (int i = 0; i < ruleArr.length; i++) {
			ruleArr[i].setIndex(i);
		}
		LiSymbol[] symbolArr = symbols.toArray(new LiSymbol[symbols.size()]);
		for (LiSymbol s : symbolArr) {
			String name = s.getName();
			if (FormatUtil.isIdentifier(name)) {
				helper.markUsed(name);
			}
		}
		for (int i = 0; i < symbolArr.length; i++) {
			symbolArr[i].setId(i, helper.generateId(symbolArr[i].getName(), i));
		}
		LiLexem[] lexemArr = lexems.toArray(new LiLexem[lexems.size()]);
		LiPrio[] prioArr = priorities.toArray(new LiPrio[priorities.size()]);
		InputRef[] inputArr = inputs.toArray(new InputRef[inputs.size()]);

		LiSymbol error = symbolsMap.get("error");
		String copyrightHeader = extractCopyright();

		return new LiGrammar(symbolArr, ruleArr, prioArr, lexemArr,
				inputArr, eoi, error, options,
				templates, terminals,
				!tree.getErrors().isEmpty(), copyrightHeader);
	}

	private SourceElement getTemplates() {
		int offset = tree.getRoot() != null ? tree.getRoot().getTemplatesStart() : -1;
		char[] text = tree.getSource().getContents();
		if (offset < text.length && offset != -1) {
			IAstNode n = new AstNode(tree.getSource(), offset, text.length) {
				public void accept(AbstractVisitor v) {
				}
			};
			return new LiEntity(n);
		}
		return null;
	}

	private LiSymbol create(AstIdentifier id, String type, boolean isTerm, boolean isSoft) {
		String name = id.getName();
		if (symbolsMap.containsKey(name)) {
			LiSymbol sym = symbolsMap.get(name);
			if (sym.isTerm() != isTerm) {
				error(id, "redeclaration of " + (isTerm ? "non-terminal" : "terminal") + ": " + name);
			} else if (sym.isSoft() != isSoft) {
				error(id, "redeclaration of " + (isSoft ? "non-soft" : "soft") + " terminal: " + name);
			} else if (!LapgResolverHelper.safeEquals(sym.getType(), type)) {
				error(id, "redeclaration of type: " + (type == null ? "<empty>" : type) + " instead of " + (sym.getType() == null ? "<empty>" : sym.getType()));
			}
			return sym;
		} else {
			LiSymbol sym = new LiSymbol(name, type, isTerm, isSoft, id);
			symbolsMap.put(name, sym);
			symbols.add(sym);
			return sym;
		}
	}

	private LiSymbol resolve(AstReference id) {
		String name = id.getName();
		LiSymbol sym = symbolsMap.get(name);
		if (sym == null) {
			if (name.length() > 3 && name.endsWith("opt")) {
				sym = symbolsMap.get(name.substring(0, name.length() - 3));
				if (sym != null) {
					LiSymbol symopt = create(new AstIdentifier(id.getName(), id.getInput(), id.getOffset(), id.getEndOffset()), sym.getType(), false, false);
					rules.add(new LiRule(null, symopt, new LiSymbolRef[0], null, null, null, id));
					rules.add(new LiRule(null, symopt, new LiSymbolRef[]{new LiSymbolRef(sym, null, null, null)}, null, null, null, id));
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	private int convert(AstGroupsSelector selector) {
		int result = 0;
		for (Integer i : selector.getGroups()) {
			if (i == null || i < 0 || i > 31) {
				error(selector, "group id should be in range 0..31");
				return 1;
			} else if ((result & (1 << i)) != 0) {
				error(selector, "duplicate group id: " + i);
				return 1;
			} else {
				result |= (1 << i);
			}
		}
		if (result == 0) {
			error(selector, "empty group set");
			return 1;
		}
		return result;
	}

	private SourceElement convert(final AstCode code) {
		if (code == null) {
			return null;
		}
		return new LiEntity(code);
	}

	private void collectLexems() {
		eoi = new LiSymbol("eoi", null, true, false, null);
		symbolsMap.put(eoi.getName(), eoi);
		symbols.add(eoi);
		int groups = 1;

		List<AstLexerPart> lexerParts = tree.getRoot().getLexer();
		lexems = new ArrayList<LiLexem>(lexerParts.size());

		List<LiLexem> classLexems = new LinkedList<LiLexem>();

		int lexemIndex = 0;
		for (AstLexerPart clause : tree.getRoot().getLexer()) {
			if (clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				AstLexemAttrs attrs = lexeme.getAttrs();
				int kind = attrs == null ? Lexem.KIND_NONE : attrs.getKind();
				LiSymbol s = create(lexeme.getName(), lexeme.getType(), true, kind == Lexem.KIND_SOFT);
				if (lexeme.getRegexp() != null) {
					RegexPart regex;
					try {
						regex = RegexMatcher.parse(lexeme.getRegexp().getRegexp());
					} catch (RegexpParseException e) {
						error(lexeme.getRegexp(), e.getMessage());
						continue;
					}

					LiLexem liLexem = new LiLexem(kind, lexemIndex++, s,
							regex,
							groups, lexeme.getPriority(),
							convert(lexeme.getCode()),
							lexeme);
					lexems.add(liLexem);
					if (kind == Lexem.KIND_CLASS) {
						classLexems.add(liLexem);
					}
				}
			} else if (clause instanceof AstGroupsSelector) {
				groups = convert((AstGroupsSelector) clause);
			}
		}

		for (LiLexem clLexem : classLexems) {
			RegexMatcher m = new RegexMatcher(clLexem.getParsedRegexp());
			for (LiLexem l : lexems) {
				RegexPart lRegex = l.getParsedRegexp();
				if (lRegex.isConstant() && l.getKind() != Lexem.KIND_CLASS) {
					if (clLexem.getGroups() == l.getGroups() && m.matches(lRegex.getConstantValue())) {
						if (l.getClassLexem() != null) {
							error(l, "lexem matches two classes `" + l.getClassLexem().getSymbol().getName()
									+ "' and `" + clLexem.getSymbol().getName() + "', using first");
						} else {
							l.setClassLexem(clLexem);
						}
					}
				}
			}
		}

		for (LiLexem l : lexems) {
			if (l.getKind() != Lexem.KIND_SOFT) {
				continue;
			}
			if (l.getClassLexem() == null) {
				String name = l.getSymbol().getName();
				if (!l.getParsedRegexp().isConstant()) {
					error(l, "soft lexem `" + name + "' should have a constant regexp");
				} else {
					error(l, "soft lexem `" + name + "' doesn't match any class lexem");
				}
			} else {
				Symbol softClass = l.getClassLexem().getSymbol();
				Symbol existingClass = l.getSymbol().getSoftClass();
				if (existingClass != null && existingClass != softClass) {
					String name = l.getSymbol().getName();
					error(l, "soft terminal `" + name + "' class ambiguity: " + softClass.getName() + " or " + existingClass.getName());
				} else if(existingClass == null) {
					((LiSymbol)l.getSymbol()).setSoftClass(softClass);
				}
			}
		}
	}

	private void addSymbolAnnotations(AstIdentifier id, Map<String, Object> annotations) {
		if (annotations != null) {
			LiSymbol sym = symbolsMap.get(id.getName());
			for (Map.Entry<String, Object> ann : annotations.entrySet()) {
				if (sym.getAnnotation(ann.getKey()) != null) {
					error(id, "redeclaration of annotation `" + ann.getKey() + "' for non-terminal: " + id.getName() + ", skipped");
				} else {
					sym.addAnnotation(ann.getKey(), ann.getValue());
				}
			}
		}
	}

	private void collectNonTerminals() {
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				create(nonterm.getName(), nonterm.getType(), false, false);
			}
		}
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				addSymbolAnnotations(nonterm.getName(), convert(nonterm.getAnnotations(), "AnnotateSymbol"));
			}
		}
	}

	private void createRule(LiSymbol left, AstRule right, List<LiSymbolRef> rightPart) {
		List<AstRuleSymbol> list = right.getList();
		rightPart.clear();
		if (list != null) {
			for (AstRuleSymbol rs : list) {
				if (rs.hasSyntaxError()) {
					continue;
				}
				AstCode astCode = rs.getCode();
				if (astCode != null) {
					LiSymbol codeSym = new LiSymbol("{}", null, false, false, astCode);
					symbols.add(codeSym);
					rightPart.add(new LiSymbolRef(codeSym, null, null, null));
					rules.add(new LiRule(null, codeSym, null, convert(astCode), null, null, astCode));
				}
				LiSymbol sym = resolve(rs.getSymbol());
				if (sym != null) {
					// TODO check duplicate alias
					rightPart.add(new LiSymbolRef(sym, rs.getAlias(), convert(rs.getAnnotations(), "AnnotateReference"), rs.getSymbol()));
				}
			}
		}
		AstRuleAttribute ruleAttribute = right.getAttribute();
		AstReference rulePrio = ruleAttribute instanceof AstPrioClause ? ((AstPrioClause) ruleAttribute).getReference() : null;
		LiSymbol prio = rulePrio != null ? resolve(rulePrio) : null;
		// TODO store %shift attribute
		// TODO check prio is term
		rules.add(
				new LiRule(
						right.getAlias(),
						left,
						rightPart.toArray(new LiSymbolRef[rightPart.size()]),
						convert(right.getAction()),
						prio,
						convert(right.getAnnotations(), "AnnotateRule"),
						right));
	}

	private void collectRules() {
		rules = new ArrayList<LiRule>();
		List<LiSymbolRef> rightPart = new ArrayList<LiSymbolRef>(32);
		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				LiSymbol left = symbolsMap.get(nonterm.getName().getName());
				if (left == null) {
					continue; /* error is already reported */
				}
				for (AstRule right : nonterm.getRules()) {
					if (!right.hasSyntaxError()) {
						createRule(left, right, rightPart);
					}
				}
			}
		}
	}

	private List<LiSymbol> resolve(List<AstReference> input) {
		List<LiSymbol> result = new ArrayList<LiSymbol>(input.size());
		for (AstReference id : input) {
			LiSymbol sym = resolve(id);
			if (sym != null) {
				result.add(sym);
			}
		}
		return result;
	}

	private void collectDirectives() {
		priorities = new ArrayList<LiPrio>();
		inputs = new ArrayList<LiInputRef>();

		for (AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if (clause instanceof AstDirective) {
				AstDirective directive = (AstDirective) clause;
				String key = directive.getKey();
				List<LiSymbol> val = resolve(directive.getSymbols());
				if (key.equals("input")) {
					// TODO handle no-eoi attribute
					for (LiSymbol s : val) {
						boolean hasEoi = !s.getName().toLowerCase().endsWith("noeoi"); // FIXME
						inputs.add(new LiInputRef(null, s, hasEoi));
					}
				} else if (key.equals("left")) {
					priorities.add(new LiPrio(Prio.LEFT, val.toArray(new LiSymbol[val.size()]), directive));
				} else if (key.equals("right")) {
					priorities.add(new LiPrio(Prio.RIGHT, val.toArray(new LiSymbol[val.size()]), directive));
				} else if (key.equals("nonassoc")) {
					priorities.add(new LiPrio(Prio.NONASSOC, val.toArray(new LiSymbol[val.size()]), directive));
				} else {
					error(directive, "unknown directive identifier used: `" + key + "`");
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

	private void error(LiEntity n, String message) {
		tree.getErrors().add(new LapgResolverProblem(LapgTree.KIND_ERROR, n.getOffset(), n.getEndOffset(), message));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convert(AstAnnotations astAnnotations, String kind) {
		if (astAnnotations == null) {
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
						report(expression, "`" + symbolClass.toString() + "` is not a subtype of `" + type.toString() + "`");
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
			while (sym.lexem == Lexems._skip_comment && source.columnForOffset(sym.offset) == 0) {
				String val = lexer.current().substring(1);
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
					return s;
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
}
