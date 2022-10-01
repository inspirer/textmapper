/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
import org.textmapper.lapg.api.ast.*;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.builder.GrammarFacade;
import org.textmapper.lapg.util.NonterminalUtil;
import org.textmapper.lapg.util.RhsUtil;
import org.textmapper.lapg.util.TypesUtil;
import org.textmapper.tool.compiler.TMTypeHint.Kind;

import java.util.*;
import java.util.Map.Entry;

/**
 * evgeny, 1/29/13
 */
public class TMMapper {

	private final Grammar grammar;
	private final ProcessingStatus status;
	private final boolean allowTypeAny;

	private final GrammarMapper mapper;
	private final AstBuilder builder;
	private final Map<Symbol, Symbol> decorators = new HashMap<>();
	private final Map<Symbol, List<Runnable>> typeListeners = new HashMap<>();
	private final Queue<Runnable> postProcessors = new LinkedList<>();
	private final Map<AstClass, List<RhsPart>> classContent = new LinkedHashMap<>();
	private final Map<String, AstClass> aliasToClass = new HashMap<>();
	private List<Nonterminal> unmapped;

	public TMMapper(Grammar grammar, ProcessingStatus status, boolean allowTypeAny) {
		this.grammar = grammar;
		this.status = status;
		this.allowTypeAny = allowTypeAny;
		this.mapper = LapgCore.createMapper(grammar);
		this.builder = LapgCore.createAstBuilder();
	}

	public AstModel deriveAST() {
		collectUnmapped();
		rewriteLists();

		mapVoid();
		mapEnums();
		mapDecorators();
		mapInterfaces();
		mapClasses();
		mapCustomTypeClasses();
		mapLists();
		mapFields();

		assert unmapped.isEmpty();
		if (!typeListeners.isEmpty()) {
			for (Symbol s : typeListeners.keySet()) {
				error(s, "cannot compute AST type (detected a cycle in type dependencies)");
			}
		} else {
			postProcessors.forEach(Runnable::run);
		}

		return builder.create();
	}

	public void detectListsOnly() {
		collectUnmapped();
		rewriteLists();
	}

	private void rewriteLists() {
		for (Nonterminal n : unmapped) {
			if (hasInterfaceHint(n) || hasClassHint(n) || TMDataUtil.getCustomType(n) != null) {
				continue;
			}
			GrammarFacade.rewriteAsList(n);
		}
	}

	private void mapVoid() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (isVoid(n)) {
				mapper.map(n, VoidType.INSTANCE);
				i.remove();
			}
		}
	}

	public static boolean isVoid(Nonterminal n) {
		if (hasVoidHint(n)) return true;
		if (hasClassHint(n)) return false;
		RhsPart def = RhsUtil.unwrap(n.getDefinition());
		if (def instanceof RhsSymbol) {
			Symbol sym = ((RhsSymbol) def).getTarget();
			if (sym instanceof Terminal) {
				return ((Terminal) sym).isConstant();
			}
			// TODO infinite recursion?
			return isVoid((Nonterminal) sym);
		}
		return false;
	}

	private void mapNonterm(Nonterminal n, AstType type) {
		if (type == null || n == null) {
			throw new NullPointerException();
		}
		List<Runnable> listeners = typeListeners.remove(n);
		mapper.map(n, type);
		if (listeners != null) {
			listeners.forEach(Runnable::run);
		}
	}

	private void whenMapped(Symbol s, Runnable r) {
		if (s.getType() != null || !(s instanceof Nonterminal)) {
			r.run();
		} else {
			List<Runnable> listeners = typeListeners.get(s);
			if (listeners == null) {
				listeners = new ArrayList<>();
				typeListeners.put(s, listeners);
			}
			listeners.add(r);
		}
	}

	private void collectUnmapped() {
		unmapped = new LinkedList<>();
		for (Symbol sym : grammar.getSymbols()) {
			if (!(sym instanceof Nonterminal) || sym.getType() != null) continue;
			unmapped.add((Nonterminal) sym);
		}
	}

	private void mapEnums() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (hasClassHint(n) || TMDataUtil.getCustomType(n) != null ||
					n.getDefinition() instanceof RhsList) {
				continue;
			}
			RhsPart definition = RhsUtil.unwrap(n.getDefinition());
			if (definition instanceof RhsChoice) {
				RhsChoice alt = (RhsChoice) n.getDefinition();
				boolean isEnum = true;
				for (RhsPart part : alt.getParts()) {
					if (!(isConstantOrVoid(part))) {
						isEnum = false;
						break;
					}
				}
				if (isEnum) {
					AstEnum astEnum = builder.addEnum(getNonterminalTypeName(n, null), null, n);
					mapNonterm(n, astEnum);
					for (RhsPart part : alt.getParts()) {
						RhsPart p = RhsUtil.unwrap(part);
						String memberName = null;
						if (p instanceof RhsAssignment) {
							memberName = ((RhsAssignment) p).getName();
							p = ((RhsAssignment) p).getPart();
						}
						RhsSymbol term = (RhsSymbol) p;
						if (memberName == null) {
							memberName = TMDataUtil.getId(term.getTarget());
						}
						AstEnumMember member = builder.addMember(
								builder.uniqueName(astEnum, memberName, true), astEnum, part);
						mapper.map(term, null, member, false);
					}
					i.remove();
					continue;
				}
			}

			RhsPart single = RhsUtil.unwrapEx(definition, true, false, false);
			if (single instanceof RhsSymbol && isConstantOrVoid(single)) {
				mapNonterm(n, AstType.BOOL);
				mapper.map((RhsSymbol) single, null, Boolean.TRUE, false);
				i.remove();
			}
		}
	}

	private String getAliasId(Nonterminal n, String ruleAlias) {
		if (ruleAlias != null && !ruleAlias.startsWith("_")) {
			return ruleAlias;
		}
		String id = TMDataUtil.getId(n);
		return id + "#" + (ruleAlias == null ? "" : ruleAlias);
	}

	private String getNonterminalTypeName(Nonterminal n, String suffix) {
		String id = TMDataUtil.getId(n);
		if (suffix != null) {
			id = suffix.startsWith("_") ? id + suffix : suffix;
		}
		return builder.uniqueName(null, id, false);
	}

	private void mapDecorators() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList) continue;
			if (hasClassHint(n) || TMDataUtil.getCustomType(n) != null || hasInterfaceHint(n)) {
				continue;
			}

			RhsPart r = null;
			for (RhsSequence rule : NonterminalUtil.getRules(n)) {
				if (RhsUtil.isEmpty(rule)) continue;

				if (r == null) {
					r = RhsUtil.unwrapOpt(RhsUtil.unwrap(rule));
				} else {
					// more than one non-empty rule => not a decorator
					r = null;
					break;
				}
			}
			if (r != null) {
				RhsSymbol master = getMasterSymbol(r);
				if (master != null) {
					Symbol target = master.getTarget();
					assert target != null;
					if (target instanceof Terminal && target.getType() == null) {
						// cannot map a terminal without a type, ignoring decorator
						continue;
					}
					decorators.put(n, target);
					whenMapped(target, () -> mapNonterm(n, target.getType()));
					postProcessors.add(() -> mapper.map(master, null, null, false));
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
				if (p instanceof RhsSymbol && isConstantOrVoid(p)) continue;
				if (varPart != null) return null;
				varPart = p;
			}
			return varPart == null ? null : withoutConstants(varPart);
		} else if (part instanceof RhsUnordered || part instanceof RhsChoice ||
				part instanceof RhsList) {
			return null;
		}
		return part;
	}

	static boolean isConstantOrVoid(RhsPart part) {
		part = RhsUtil.unwrapEx(part, false, false, true);
		if (part instanceof RhsSymbol) {
			RhsSymbol sym = (RhsSymbol) part;
			return RhsUtil.isConstant(sym) || sym.getTarget() instanceof Nonterminal &&
					sym.getTarget().getType() instanceof VoidType;
		}
		return false;
	}

	private void addExtends(Nonterminal n, AstClass baseClass) {
		whenMapped(n, () -> {
			AstClass cl = (AstClass) n.getType();
			try {
				builder.addExtends(cl, baseClass);
			} catch (IllegalArgumentException ex) {
				error(n, n.getNameText() + ": " + cl.getName() + " cannot extend " +
						baseClass.getName() +
						" (would introduce a cycle in the inheritance hierarchy)");
			}
		});
	}

	private void addInterface(AstClass cl, Nonterminal baseInterface,
							  SourceElement origin) {
		whenMapped(baseInterface, () -> {
			boolean isInterface = baseInterface.getType() instanceof AstClass &&
					((AstClass)baseInterface.getType()).isInterface();
			if (!isInterface) {
				error(origin, cl.getName() + " cannot extend " + baseInterface.getNameText() +
						" (interface is expected)");
				return;
			}
			AstClass superInterface = (AstClass) baseInterface.getType();
			try {
				builder.addExtends(cl, superInterface);
			} catch (IllegalArgumentException ex) {
				error(origin, cl.getName() + " cannot extend " + superInterface.getName() +
						" (would introduce a cycle in the inheritance hierarchy)");
			}
		});
	}

	private void mapInterfaces() {
		List<RhsSymbol> passSymbols = new ArrayList<>();
		Set<Nonterminal> extList = new LinkedHashSet<>();
		List<RhsSequence> customRuleList = new ArrayList<>();

		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList || TMDataUtil.getCustomType(n) != null ||
					hasClassHint(n)) {
				continue;
			}

			extList.clear();
			customRuleList.clear();
			passSymbols.clear();

			boolean hasNamedRules = false;
			boolean isInterface = true;
			for (RhsSequence rule : NonterminalUtil.getRules(n)) {
				RhsSymbol master = getMasterSymbol(rule);
				if (master != null) {
					Symbol target = unwrapDecorators(master.getTarget());
					if (supportsExtending(target)) {
						passSymbols.add(master);
						extList.add((Nonterminal) target);
						continue;
					}
				}

				String ruleAlias = rule.getName();
				if (ruleAlias == null && !hasInterfaceHint(n)) {
					isInterface = false;
					break;
				}
				hasNamedRules |= (ruleAlias != null);
				customRuleList.add(rule);
			}
			if (isInterface && (!passSymbols.isEmpty() || hasInterfaceHint(n) && hasNamedRules)) {
				AstClass interfaceClass = builder.addInterface(
						getNonterminalTypeName(n, null), null, n);
				List<Nonterminal> superInterfaces = TMDataUtil.getImplements(n);
				if (superInterfaces != null) {
					for (Nonterminal si : superInterfaces) {
						addInterface(interfaceClass, si, n);
					}
				}

				mapNonterm(n, interfaceClass);
				for (Nonterminal nonterminal : extList) {
					addExtends(nonterminal, interfaceClass);
				}
				for (RhsSymbol sym : passSymbols) {
					mapper.map(sym, null, null, false);
				}
				for (RhsSequence rulePart : customRuleList) {
					String ruleAlias = rulePart.getName() == null ? "_Impl" : rulePart.getName();
					String aliasId = getAliasId(n, ruleAlias);
					AstClass ruleClass = aliasToClass.get(aliasId);
					if (ruleClass == null) {
						ruleClass = builder.addClass(getNonterminalTypeName(n, ruleAlias), null, n);
						builder.addExtends(ruleClass, interfaceClass);
						aliasToClass.put(aliasId, ruleClass);
					}
					mapClass(ruleClass, rulePart);
					mapper.map(rulePart, null, ruleClass, false);
				}
				i.remove();
			} else if (hasInterfaceHint(n)) {
				status.report(ProcessingStatus.KIND_ERROR, "interface hint was ignored", n);
			}
		}
	}

	private boolean supportsExtending(Symbol sym) {
		sym = unwrapDecorators(sym);
		if (!(sym instanceof Nonterminal)) return false;
		Nonterminal n = (Nonterminal) sym;
		if (n.getDefinition() instanceof RhsList) return false;
		AstType type = sym.getType();
		return type == null || type instanceof AstClass /* TODO && !((AstClass) type).isSealed()*/;
	}

	private Symbol unwrapDecorators(Symbol sym) {
		assert sym != null;
		Symbol curr = sym;
		Symbol next = decorators.get(curr);
		if (next == null) {
			return sym;
		}
		Set<Symbol> seen = new HashSet<>();
		while (next != null) {
			if (!seen.add(next)) {
				// TODO handle, report etc.
				throw new IllegalStateException("cycle in decorators: " + seen.toString());
			}
			curr = next;
			next = decorators.get(curr);
		}
		return curr;
	}

	private RhsSymbol getMasterSymbol(RhsPart rule) {
		RhsPart r = RhsUtil.unwrap(rule);
		RhsPart master = withoutConstants(r);
		if (master instanceof RhsSymbol &&
				(master == r || TMDataUtil.hasProperty(master, "pass"))) {
			return (RhsSymbol) master;
		}
		return null;
	}

	private void mapClasses() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList || TMDataUtil.getCustomType(n) != null) {
				continue;
			}
			AstClass cl = builder.addClass(getNonterminalTypeName(n, null), null, n);
			List<Nonterminal> superInterfaces = TMDataUtil.getImplements(n);
			if (superInterfaces != null) {
				for (Nonterminal si : superInterfaces) {
					addInterface(cl, si, n);
				}
			}
			mapNonterm(n, cl);
			mapClass(cl, RhsUtil.unwrap(n.getDefinition()));
			i.remove();
		}
	}

	/**
	 * Maps `A returns B` nonterminals.
	 */
	private void mapCustomTypeClasses() {
		Iterator<Nonterminal> i = unmapped.iterator();
		Map<Nonterminal, AstClass> customTypes = new LinkedHashMap<>();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList) continue;
			Nonterminal customType = TMDataUtil.getCustomType(n);
			assert customType != null;
			if (decorators.containsKey(customType)) {
				i.remove();
				error(n, "custom type cannot refer a decorator nonterminal");
				mapNonterm(n, builder.addClass(getNonterminalTypeName(n, null), null, n));
				continue;
			}
			AstType mappedType = customType.getType();
			if (!(mappedType instanceof AstClass)) {
				i.remove();
				error(n, "type for `" + n.getNameText() + "' is not a classifier");
				mapNonterm(n, builder.addClass(getNonterminalTypeName(n, null), null, n));
				continue;
			}
			AstClass cl = (AstClass) mappedType;
			i.remove();
			customTypes.put(n, cl);
		}
		for (Entry<Nonterminal, AstClass> e : customTypes.entrySet()) {
			mapCustomTypeNonterm(e.getKey(), e.getValue(), customTypes);
		}
	}

	private void mapCustomTypeNonterm(Nonterminal n, AstClass cl,
									  Map<Nonterminal, AstClass> customTypes) {
		if (!cl.isInterface()) {
			mapNonterm(n, cl);
			mapClass(cl, RhsUtil.unwrap(n.getDefinition()));
			return;
		}

		boolean mapped = false;
		Set<AstClass> usedClasses = new HashSet<>();
		Map<RhsSequence, AstClass> mappedRules = new HashMap<>();

		for (RhsSequence rule : NonterminalUtil.getRules(n)) {
			RhsSymbol master = getMasterSymbol(rule);
			if (master != null) {
				Symbol target = unwrapDecorators(master.getTarget());
				AstType masterType = target.getType();
				if (masterType == null && target instanceof Nonterminal) {
					masterType = customTypes.get(target);
				}

				if (masterType != null && masterType.isSubtypeOf(cl)) {
					if (!mapped) {
						mapNonterm(n, cl);
						mapped = true;
					}
					mapper.map(master, null, null, false);
					continue;
				}
			}

			String ruleAlias = rule.getName();
			String aliasId = getAliasId(n, ruleAlias);
			AstClass ruleClass = aliasToClass.get(aliasId);
			if (ruleClass == null) {
				ruleClass = builder.addClass(getNonterminalTypeName(n, ruleAlias), null, n);
				aliasToClass.put(aliasId, ruleClass);
			}
			if (usedClasses.add(ruleClass)) {
				builder.addExtends(ruleClass, cl);
			}
			mapClass(ruleClass, rule);
			mappedRules.put(rule, ruleClass);
		}
		if (!mapped) {
			if (usedClasses.size() == 1) {
				cl = usedClasses.iterator().next();
			}
			mapNonterm(n, cl);
		}
		for (Entry<RhsSequence, AstClass> e : mappedRules.entrySet()) {
			mapper.map(e.getKey(), null, e.getValue(), false);
		}
	}

	private void mapLists() {
		List<RhsSymbol> rhsSymbols = new ArrayList<>();
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (!(n.getDefinition() instanceof RhsList)) continue;
			RhsList list = (RhsList) n.getDefinition();

			// Is there already exists a type for the list element?
			rhsSymbols.clear();
			TypeOrSymbolHandle typeOrSymbol = getTypeOrUnresolvedSymbol(
					list.getElement(), rhsSymbols);
			if (typeOrSymbol != null && list.getCustomInitialElement() != null) {
				typeOrSymbol = typeOrSymbol.merge(getTypeOrUnresolvedSymbol(
						list.getCustomInitialElement(), rhsSymbols));
			}

			// Can we proceed without a separate class?
			if (typeOrSymbol != null) {
				if (typeOrSymbol.getType() != null) {
					// Symbol elements have a common type.
					if (typeOrSymbol.getType() instanceof VoidType) {
						mapper.map(n, VoidType.INSTANCE);
					} else {
						mapNonterm(n, builder.list(typeOrSymbol.getType(), list.isNonEmpty(), n));
						for (RhsSymbol sym : rhsSymbols) {
							mapper.map(sym, null, null, true);
						}
					}
				} else {
					// We got some unresolved symbol, which usually means list<list<...>>
					Symbol listElement = typeOrSymbol.getUnresolvedSymbol();
					if (listElement instanceof Terminal && listElement.getType() == null) {
						error(listElement, "terminal symbol must have a type");
					} else {
						whenMapped(listElement, () -> {
							if (listElement.getType() instanceof VoidType) {
								mapper.map(n, VoidType.INSTANCE);
								return;
							}
							mapNonterm(n, builder.list(
									listElement.getType(), list.isNonEmpty(), n));
							for (RhsSymbol sym : rhsSymbols) {
								mapper.map(sym, null, null, true);
							}
						});
					}
				}
			} else {
				AstClass elementClass = builder.addClass(
						getNonterminalTypeName(n, "_item"), null, n);
				mapNonterm(n, builder.list(elementClass, list.isNonEmpty(), n));
				mapper.map(list.getElement(), null, elementClass, true);
				if (list.getCustomInitialElement() != null) {
					mapper.map(list.getCustomInitialElement(), null, elementClass, true);
				}
				mapClass(elementClass, list.getElement(), list.getCustomInitialElement());
			}
			i.remove();
		}
	}

	private void mapClass(AstClass cl, RhsPart... parts) {
		List<RhsPart> rhsParts = classContent.get(cl);
		if (rhsParts == null) {
			rhsParts = new ArrayList<>();
			classContent.put(cl, rhsParts);
		}
		for (RhsPart part : parts) {
			if (part != null) {
				rhsParts.add(part);
			}
		}
	}

	private void mapFields() {
		TMFieldMapper fieldMapper = new TMFieldMapper(status, builder, mapper, allowTypeAny);
		for (AstClass cl : classContent.keySet()) {
			List<RhsPart> rhsParts = classContent.get(cl);
			if (rhsParts == null || rhsParts.isEmpty()) continue;

			RhsPart def = rhsParts.size() == 1
					? rhsParts.get(0)
					: RhsUtil.asChoice(rhsParts.toArray(new RhsPart[rhsParts.size()]));

			fieldMapper.mapFields(cl, def);
		}
	}

	private void error(SourceElement element, String message) {
		status.report(ProcessingStatus.KIND_ERROR, message, element);
	}

	/**
	 * Returns AST type of the given part (if exists). Builds a list of all RhsSymbols that
	 * contribute to the returned type.
	 */
	private TypeOrSymbolHandle getTypeOrUnresolvedSymbol(RhsPart part, List<RhsSymbol> out) {
		part = RhsUtil.unwrap(part);
		RhsCast cast = null;
		boolean optional = false;
		while (part != null) {
			if (part instanceof RhsSequence) {
				part = RhsUtil.unwrap(withoutConstants(part));
			} else if (part instanceof RhsOptional) {
				part = RhsUtil.unwrap(((RhsOptional) part).getPart());
				optional = true;
			} else if (part instanceof RhsChoice) {
				TypeOrSymbolHandle result = null;
				for (RhsPart p : ((RhsChoice) part).getParts()) {
					TypeOrSymbolHandle update = getTypeOrUnresolvedSymbol(p, out);
					if (update == null) {
						if (!RhsUtil.isEmpty(RhsUtil.unwrap(p))) return null;

						optional = true;
						continue;
					}

					result = result == null ? update : result.merge(update);
					if (result == null) break;
				}
				if (optional && result != null) {
					result = result.toOptional();
				}
				return result;
			} else if (part instanceof RhsCast) {
				cast = (RhsCast) part;
				part = RhsUtil.unwrap(cast.getPart());
			} else if (part instanceof RhsSymbol) {
				// TODO handle template vars (create an AST type for it)
				Symbol sym = cast != null ? cast.getTarget() : ((RhsSymbol) part).getTarget();
				out.add((RhsSymbol) part);
				return new TypeOrSymbolHandle(sym, optional);
			} else {
				part = null;
			}
		}
		return null;
	}

	private static boolean hasClassHint(Nonterminal n) {
		TMTypeHint typeHint = TMDataUtil.getTypeHint(n);
		return typeHint != null && typeHint.getKind() == Kind.CLASS ||
				TMDataUtil.hasProperty(n, "_class");
	}

	private static boolean hasInterfaceHint(Nonterminal n) {
		TMTypeHint typeHint = TMDataUtil.getTypeHint(n);
		return typeHint != null && typeHint.getKind() == Kind.INTERFACE ||
				TMDataUtil.hasProperty(n, "_interface");
	}

	private static boolean hasVoidHint(Nonterminal n) {
		TMTypeHint typeHint = TMDataUtil.getTypeHint(n);
		return typeHint != null && typeHint.getKind() == Kind.VOID ||
				TMDataUtil.hasProperty(n, "noast");
	}

	private final class TypeOrSymbolHandle {

		// One of the following two is not null.
		private final Symbol unresolvedSymbol;
		private final AstType type;

		private final boolean isNullable;

		private TypeOrSymbolHandle(Symbol symbol, boolean optional) {
			this(symbol.getType() == null ? symbol : null, symbol.getType(), optional);
		}

		private TypeOrSymbolHandle(Symbol symbol, AstType type, boolean optional) {
			assert (symbol == null) ^ (type == null);
			this.unresolvedSymbol = symbol;
			this.type = type;
			this.isNullable = optional;
		}

		public TypeOrSymbolHandle merge(TypeOrSymbolHandle other) {
			if (other == null) return null;

			if (unresolvedSymbol != null && other.unresolvedSymbol == unresolvedSymbol) {
				return isNullable ? this : other;
			}
			if (type != null && other.type != null) {
				AstType common = TypesUtil.getJoinType(type, other.type);
				if (common != null) {
					return new TypeOrSymbolHandle(null, common, isNullable || other.isNullable);
				}
			}
			return null;
		}

		public TypeOrSymbolHandle toOptional() {
			return new TypeOrSymbolHandle(unresolvedSymbol, type, true);
		}

		public boolean isNullable() {
			return isNullable;
		}

		public Symbol getUnresolvedSymbol() {
			return unresolvedSymbol;
		}

		public AstType getType() {
			return type;
		}
	}
}
