package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.ast.AstClass;
import org.textmapper.lapg.api.ast.AstEnum;
import org.textmapper.lapg.api.ast.AstEnumMember;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.*;

import java.util.*;

/**
 * evgeny, 1/29/13
 */
public class TMMapper {

	private final Grammar grammar;
	private final GrammarMapper mapper;
	private final AstBuilder builder;
	private final Map<Symbol, Symbol> decorators = new HashMap<Symbol, Symbol>();
	private final Map<Symbol, List<Runnable>> typeListeners = new HashMap<Symbol, List<Runnable>>();
	private final Queue<Runnable> postProcessors = new LinkedList<Runnable>();
	private final Map<AstClass, List<RhsPart>> classContent = new LinkedHashMap<AstClass, List<RhsPart>>();
	private List<Nonterminal> unmapped;

	public TMMapper(Grammar grammar) {
		this.grammar = grammar;
		this.mapper = LapgCore.createMapper(grammar);
		this.builder = LapgCore.createAstBuilder();
	}

	public void deriveAST() {
		collectUnmapped();

/*
		mapEnums();
		mapDecorators();
		mapInterfaces();
		mapClasses();
		mapLists();
		mapFields();

		assert typeListeners.isEmpty();
		for (Runnable pp : postProcessors) {
			pp.run();
		}
*/
	}

	private void mapNonterm(Nonterminal n, AstType type) {
		if (type == null || n == null) {
			throw new NullPointerException();
		}
		final List<Runnable> runnables = typeListeners.remove(n);
		mapper.map(n, type);
		if (runnables != null) {
			for (Runnable r : runnables) {
				r.run();
			}
		}
	}

	private void whenMapped(Symbol s, Runnable r) {
		if (s.getType() != null || !(s instanceof Nonterminal)) {
			r.run();
		} else {
			List<Runnable> runnables = typeListeners.get(s);
			if (runnables == null) {
				runnables = new ArrayList<Runnable>();
				typeListeners.put(s, runnables);
			}
			runnables.add(r);
		}
	}

	private void collectUnmapped() {
		unmapped = new LinkedList<Nonterminal>();
		for (Symbol sym : grammar.getSymbols()) {
			if (!(sym instanceof Nonterminal) || sym.getType() != null) continue;
			if (((Nonterminal) sym).getDefinition() instanceof RhsList) continue;
			unmapped.add((Nonterminal) sym);
		}
	}

	private void mapEnums() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			RhsPart definition = RhsUtil.unwrap(n.getDefinition());
			if (definition instanceof RhsChoice) {
				RhsChoice alt = (RhsChoice) n.getDefinition();
				boolean isEnum = true;
				for (RhsPart part : alt.getParts()) {
					if (!(RhsUtil.isConstant(part))) {
						isEnum = false;
						break;
					}
				}
				if (isEnum) {
					AstEnum astEnum = builder.addEnum(getNonterminalTypeName(n), n);
					mapNonterm(n, astEnum);
					for (RhsPart part : alt.getParts()) {
						RhsPart p = RhsUtil.unwrap(part);
						String memberName = null;
						if (p instanceof RhsAssignment) {
							memberName = ((RhsAssignment) p).getName();
							p = ((RhsAssignment) p).getPart();
						}
						RhsSymbol term = (RhsSymbol) p;
						memberName = builder.uniqueName(astEnum, memberName != null ? memberName : TMDataUtil.getId(term.getTarget()), true);
						AstEnumMember member = builder.addMember(memberName, astEnum, part);
						mapper.map(term, null, member, false);
					}
					i.remove();
				}

			} else if (RhsUtil.isConstant(definition)) {
				mapNonterm(n, AstType.BOOL);
				RhsSymbol term = (RhsSymbol) RhsUtil.unwrapEx(definition, false, false, true);
				mapper.map(term, null, Boolean.TRUE, false);
				i.remove();
			}
		}
	}

	private String getNonterminalTypeName(Nonterminal n) {
		return builder.uniqueName(null, TMDataUtil.getId(n), false);
	}

	private void mapDecorators() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			final Nonterminal n = i.next();
			RhsPart definition = RhsUtil.unwrap(n.getDefinition());
			Iterable<RhsPart> rules = definition instanceof RhsChoice
					? Arrays.asList(((RhsChoice) definition).getParts())
					: Collections.singleton(definition);
			RhsPart r = null;
			for (RhsPart part : rules) {
				if (RhsUtil.isEmpty(part)) {
					continue;
				}
				if (r == null) {
					r = RhsUtil.unwrapOpt(RhsUtil.unwrap(part));
				} else {
					// more than one non-empty rule => not a decorator
					r = null;
					break;
				}
			}
			if (r != null) {
				final RhsPart master = withoutConstants(r);
				if (master instanceof RhsSymbol && (master == r || hasProperty(master, "pass"))) {
					final Symbol target = ((RhsSymbol) master).getTarget();
					if (target instanceof Terminal && target.getType() == null) {
						// cannot map a terminal without a type, ignoring decorator
						continue;
					}
					decorators.put(n, target);
					whenMapped(target, new Runnable() {
						@Override
						public void run() {
							mapNonterm(n, target.getType());
						}
					});
					postProcessors.add(new Runnable() {
						@Override
						public void run() {
							mapper.map((RhsSymbol) master, null, null, false);
						}
					});
					i.remove();
				}
			}
		}
	}

	private static RhsPart withoutConstants(RhsPart part) {
		if (part instanceof RhsSequence) {
			RhsPart varPart = null;
			for (RhsPart p_ : ((RhsSequence) part).getParts()) {
				RhsPart p = RhsUtil.unwrap(p_);
				if (p instanceof RhsSymbol && RhsUtil.isConstant((RhsSymbol) p)) continue;
				if (varPart != null) return null;
				varPart = p;
			}
			return varPart == null ? null : withoutConstants(varPart);
		} else if (part instanceof RhsUnordered || part instanceof RhsChoice || part instanceof RhsList) {
			return null;
		}
		return part;
	}

	private void addExtends(final Nonterminal n, final AstClass baseClass) {
		whenMapped(n, new Runnable() {
			@Override
			public void run() {
				AstClass cl = (AstClass) n.getType();
				builder.addExtends(cl, baseClass);
			}
		});
	}

	private void mapInterfaces() {
		List<RhsSymbol> passSymbols = new ArrayList<RhsSymbol>();
		Set<Nonterminal> extList = new LinkedHashSet<Nonterminal>();
		List<RhsPart> customRuleList = new ArrayList<RhsPart>();

		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			RhsPart definition = RhsUtil.unwrap(n.getDefinition());
			Iterable<RhsPart> rules = definition instanceof RhsChoice
					? Arrays.asList(((RhsChoice) definition).getParts())
					: Collections.singleton(definition);

			extList.clear();
			customRuleList.clear();
			passSymbols.clear();

			boolean isInterface = false;
			for (RhsPart part : rules) {
				RhsPart r = RhsUtil.unwrap(part);
				final RhsPart master = withoutConstants(r);
				if (master instanceof RhsSymbol && (master == r || hasProperty(master, "pass"))) {
					final Symbol target = unwrapDecorators(((RhsSymbol) master).getTarget());
					if (supportsExtending(target)) {
						isInterface = true;
						if (master != r) {
							passSymbols.add((RhsSymbol) master);
						}
						extList.add((Nonterminal) target);
						continue;
					}
				}
				final String ruleAlias = (String) part.getUserData(RhsPart.RULE_ALIAS);
				if (ruleAlias == null) {
					isInterface = false;
					break;
				}
				customRuleList.add(part);
			}
			if (isInterface) {
				AstClass interfaceClass = builder.addClass(getNonterminalTypeName(n), null, n);
				mapNonterm(n, interfaceClass);
				for (Nonterminal nonterminal : extList) {
					addExtends(nonterminal, interfaceClass);
				}
				for (RhsSymbol sym : passSymbols) {
					mapper.map(sym, null, null, false);
				}
				for (RhsPart rulePart : customRuleList) {
					String ruleAlias = (String) rulePart.getUserData(RhsPart.RULE_ALIAS);
					AstClass ruleClass = builder.addClass(builder.uniqueName(null, TMDataUtil.getId(n) + "_" + ruleAlias, false), null, n);
					builder.addExtends(ruleClass, interfaceClass);
					for (Rule rule : n.getRules()) {
						if (rule.getSource() == rulePart) {
							mapper.map(rule, ruleClass);
						}
					}
					mapClass(ruleClass, rulePart);
				}
				i.remove();
			}
		}
	}

	private boolean supportsExtending(Symbol sym) {
		sym = unwrapDecorators(sym);
		if (!(sym instanceof Nonterminal)) return false;
		Nonterminal n = (Nonterminal) sym;
		if (n.getDefinition() instanceof RhsList) return false;
		AstType type = sym.getType();
		if (type instanceof AstEnum) return false;
		return true;
	}

	private Symbol unwrapDecorators(Symbol sym) {
		Symbol curr = sym;
		Symbol next = decorators.get(curr);
		if (next == null) {
			return sym;
		}
		Set<Symbol> seen = new HashSet<Symbol>();
		while (next != null) {
			if (!seen.add(next)) {
				// TODO handle, report etc.
				throw new IllegalStateException("cycle in decorators");
			}
			curr = next;
			next = decorators.get(sym);
		}
		return curr;
	}

	private void mapClasses() {
		for (Nonterminal n : unmapped) {
			// TODO handle baseRule
			//final Symbol baseRule = getExtends(n);

			AstClass cl = builder.addClass(getNonterminalTypeName(n), null, n);
			mapNonterm(n, cl);
			mapClass(cl, RhsUtil.unwrap(n.getDefinition()));
		}
		unmapped.clear();
	}

	private void mapLists() {
		for (Symbol symbol : grammar.getSymbols()) {
			if (!(symbol instanceof Nonterminal) || symbol.getType() != null) continue;
			Nonterminal n = (Nonterminal) symbol;
			if (!(n.getDefinition() instanceof RhsList)) continue;
			RhsList list = (RhsList) n.getDefinition();

			AstType elementType = getRhsType(list.getElement());
			if (elementType != null && list.getCustomInitialElement() != null) {
				AstType initialElemType = getRhsType(list.getCustomInitialElement());
				elementType = initialElemType != null ? getJoinType(null, elementType, initialElemType) : null;
			}
			if (elementType == null) {
				AstClass elementClass = builder.addClass(builder.uniqueName(null, TMDataUtil.getId(n) + "_element", false), null, n);
				mapClass(elementClass, list.getElement(), list.getCustomInitialElement());
				elementType = elementClass;
			}
			mapNonterm(n, builder.list(elementType, list.isNonEmpty(), n));
		}
	}

	private void mapClass(AstClass cl, RhsPart... parts) {
		List<RhsPart> rhsParts = classContent.get(cl);
		if (rhsParts == null) {
			rhsParts = new ArrayList<RhsPart>();
			classContent.put(cl, rhsParts);
		}
		for (RhsPart part : parts) {
			if (part != null) {
				rhsParts.add(part);
			}
		}
	}

	private void mapFields() {
//		TODO implement
//		for (Entry<AstClass, List<RhsPart>> entry : classContent.entrySet()) {
//		}
	}

	private static AstType getRhsType(RhsPart part) {
		part = withoutConstants(RhsUtil.unwrap(part));
		if (part instanceof RhsCast) {
			return ((RhsCast) part).getTarget().getType();
		}
		if (part instanceof RhsSymbol) {
			return ((RhsSymbol) part).getTarget().getType();
		}
		return null;
	}

	private static AstClass getJoinClass(String contextId, AstClass c1, AstClass c2) {
		// TODO implement
		return null;
	}

	private static AstType getJoinType(String contextId, AstType t1, AstType t2) {
		if (t1.isSubtypeOf(t2)) {
			return t2;
		}
		if (t2.isSubtypeOf(t1)) {
			return t1;
		}
		if (t1 instanceof AstClass && t2 instanceof AstClass) {
			return getJoinClass(contextId, (AstClass) t1, (AstClass) t2);
		}
		return null;
	}

	private static boolean hasProperty(UserDataHolder o, String name) {
		final Map<String, Object> annotations = TMDataUtil.getAnnotations(o);
		if (annotations == null) {
			return false;
		}
		final Object o1 = annotations.get(name);
		return o1 instanceof Boolean ? (Boolean) o1 : false;
	}

//	private static Symbol getExtends(Symbol s) {
//		final Map<String, Object> annotations = TMDataUtil.getAnnotations(s);
//		if (annotations == null) {
//			return null;
//		}
//		final Object o1 = annotations.get("extends");
//		return o1 instanceof Symbol ? (Symbol) o1 : null;
//	}

//	private static void mark(Rule rule, String templateName) {
//		rule.putUserData("codeTemplate", templateName);
//	}

//	private static class FieldDescriptor {
//		private RhsSymbol sym;
//		private AstType type;
//		private String alias;
//
//		private RhsSymbol next;
//
//
//
//	}
}
