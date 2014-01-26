package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.ast.*;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.builder.GrammarFacade;
import org.textmapper.lapg.util.RhsUtil;
import org.textmapper.tool.compiler.TMTypeHint.Kind;

import java.util.*;
import java.util.Map.Entry;

/**
 * evgeny, 1/29/13
 */
public class TMMapper {

	private static final MarkerType BOOL_OR_ENUM = new MarkerType();

	private final ProcessingStatus status;
	private final Grammar grammar;
	private final GrammarMapper mapper;
	private final AstBuilder builder;
	private final Map<Symbol, Symbol> decorators = new HashMap<Symbol, Symbol>();
	private final Map<Symbol, List<Runnable>> typeListeners = new HashMap<Symbol, List<Runnable>>();
	private final Queue<Runnable> postProcessors = new LinkedList<Runnable>();
	private final Map<AstClass, List<RhsPart>> classContent = new LinkedHashMap<AstClass, List<RhsPart>>();
	private final Map<String, AstClass> aliasToClass = new HashMap<String, AstClass>();
	private List<Nonterminal> unmapped;

	public TMMapper(Grammar grammar, ProcessingStatus status) {
		this.grammar = grammar;
		this.status = status;
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
			for (Runnable pp : postProcessors) {
				pp.run();
			}
		}

		return builder.create();
	}

	private void rewriteLists() {
		for (Nonterminal n : unmapped) {
			if (hasInterfaceHint(n) || hasClassHint(n) || TMDataUtil.getCustomType(n) != null) continue;
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
		final List<Runnable> listeners = typeListeners.remove(n);
		mapper.map(n, type);
		if (listeners != null) {
			for (Runnable r : listeners) {
				r.run();
			}
		}
	}

	private void whenMapped(Symbol s, Runnable r) {
		if (s.getType() != null || !(s instanceof Nonterminal)) {
			r.run();
		} else {
			List<Runnable> listeners = typeListeners.get(s);
			if (listeners == null) {
				listeners = new ArrayList<Runnable>();
				typeListeners.put(s, listeners);
			}
			listeners.add(r);
		}
	}

	private void collectUnmapped() {
		unmapped = new LinkedList<Nonterminal>();
		for (Symbol sym : grammar.getSymbols()) {
			if (!(sym instanceof Nonterminal) || sym.getType() != null) continue;
			unmapped.add((Nonterminal) sym);
		}
	}

	private void mapEnums() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (hasClassHint(n) || TMDataUtil.getCustomType(n) != null || n.getDefinition() instanceof RhsList) {
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
						AstEnumMember member = builder.addMember(builder.uniqueName(astEnum, memberName, true),
								astEnum, part);
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
			final Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList) continue;
			if (hasClassHint(n) || TMDataUtil.getCustomType(n) != null || hasInterfaceHint(n)) {
				continue;
			}

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
					if (target instanceof Terminal && (target.getType() == null || ((Terminal) target).isSoft())) {
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
				if (p instanceof RhsSymbol && isConstantOrVoid(p)) continue;
				if (varPart != null) return null;
				varPart = p;
			}
			return varPart == null ? null : withoutConstants(varPart);
		} else if (part instanceof RhsUnordered || part instanceof RhsChoice || part instanceof RhsList) {
			return null;
		}
		return part;
	}

	private static boolean isConstantOrVoid(RhsPart part) {
		part = RhsUtil.unwrapEx(part, false, false, true);
		if (part instanceof RhsSymbol) {
			final RhsSymbol sym = (RhsSymbol) part;
			return RhsUtil.isConstant(sym) ||
					sym.getTarget() instanceof Nonterminal && sym.getTarget().getType() instanceof VoidType;
		}
		return false;
	}

	private void addExtends(final Nonterminal n, final AstClass baseClass) {
		whenMapped(n, new Runnable() {
			@Override
			public void run() {
				AstClass cl = (AstClass) n.getType();
				try {
					builder.addExtends(cl, baseClass);
				} catch (IllegalArgumentException ex) {
					error(n, n.getName() + ": " + cl.getName() + " cannot extends " + baseClass.getName() +
							" (introduces a cycle in the inheritance hierarchy)");
				}
			}
		});
	}

	private void mapInterfaces() {
		List<RhsSymbol> passSymbols = new ArrayList<RhsSymbol>();
		Set<Nonterminal> extList = new LinkedHashSet<Nonterminal>();
		List<RhsSequence> customRuleList = new ArrayList<RhsSequence>();

		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList || TMDataUtil.getCustomType(n) != null || hasClassHint(n)) {
				continue;
			}

			RhsPart definition = n.getDefinition();
			Iterable<RhsPart> rules = definition instanceof RhsChoice
					? Arrays.asList(((RhsChoice) definition).getParts())
					: Collections.singleton(definition);

			extList.clear();
			customRuleList.clear();
			passSymbols.clear();

			boolean hasNamedRules = false;
			boolean isInterface = true;
			for (RhsPart part : rules) {
				RhsPart r = RhsUtil.unwrap(part);
				final RhsPart master = withoutConstants(r);
				if (master instanceof RhsSymbol && (master == r || hasProperty(master, "pass"))) {
					final Symbol target = unwrapDecorators(((RhsSymbol) master).getTarget());
					if (supportsExtending(target)) {
						passSymbols.add((RhsSymbol) master);
						extList.add((Nonterminal) target);
						continue;
					}
				}

				if (!(part instanceof RhsSequence)) {
					error(part, "internal error: every rule must start with a sequence");
					continue;
				}

				final String ruleAlias = ((RhsSequence) part).getName();
				if (ruleAlias == null && !hasInterfaceHint(n)) {
					isInterface = false;
					break;
				}
				hasNamedRules |= (ruleAlias != null);
				customRuleList.add((RhsSequence) part);
			}
			if (isInterface && (!passSymbols.isEmpty() || hasInterfaceHint(n) && hasNamedRules)) {
				AstClass interfaceClass = builder.addInterface(getNonterminalTypeName(n, null), null, n);
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
		Symbol curr = sym;
		Symbol next = decorators.get(curr);
		if (next == null) {
			return sym;
		}
		Set<Symbol> seen = new HashSet<Symbol>();
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

	private void mapClasses() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList || TMDataUtil.getCustomType(n) != null) continue;
			AstClass cl = builder.addClass(getNonterminalTypeName(n, null), null, n);
			mapNonterm(n, cl);
			mapClass(cl, RhsUtil.unwrap(n.getDefinition()));
			i.remove();
		}
	}

	private void mapCustomTypeClasses() {
		Iterator<Nonterminal> i = unmapped.iterator();
		Map<Nonterminal, AstClass> customTypes = new LinkedHashMap<Nonterminal, AstClass>();
		while (i.hasNext()) {
			final Nonterminal n = i.next();
			if (n.getDefinition() instanceof RhsList) continue;
			final Nonterminal customType = TMDataUtil.getCustomType(n);
			assert customType != null;
			if (decorators.containsKey(customType)) {
				i.remove();
				error(n, "custom type cannot refer a decorator nonterminal");
				mapNonterm(n, builder.addClass(getNonterminalTypeName(n, null), null, n));
				continue;
			}
			final AstType mappedType = customType.getType();
			if (!(mappedType instanceof AstClass)) {
				i.remove();
				error(n, "type for `" + n.getName() + "' is not a classifier");
				mapNonterm(n, builder.addClass(getNonterminalTypeName(n, null), null, n));
				continue;
			}
			final AstClass cl = (AstClass) mappedType;
			i.remove();
			mapNonterm(n, cl);
			customTypes.put(n, cl);
		}
		for (Entry<Nonterminal, AstClass> e : customTypes.entrySet()) {
			mapCustomTypeNonterm(e.getKey(), e.getValue());
		}
	}

	private void mapCustomTypeNonterm(Nonterminal n, AstClass cl) {
		if (!cl.isInterface()) {
			mapClass(cl, RhsUtil.unwrap(n.getDefinition()));
			return;
		}

		RhsPart definition = n.getDefinition();
		Iterable<RhsPart> rules = definition instanceof RhsChoice
				? Arrays.asList(((RhsChoice) definition).getParts())
				: Collections.singleton(definition);

		for (RhsPart part : rules) {
			RhsPart r = RhsUtil.unwrap(part);
			final RhsPart master = withoutConstants(r);
			if (master instanceof RhsSymbol && (master == r || hasProperty(master, "pass"))) {
				final Symbol target = unwrapDecorators(((RhsSymbol) master).getTarget());
				if (target.getType() != null && target.getType().isSubtypeOf(cl)) {
					mapper.map((RhsSymbol) master, null, null, false);
					continue;
				}
			}
			if (!(part instanceof RhsSequence)) {
				error(part, "internal error: every rule must start with a sequence");
				continue;
			}

			final String ruleAlias = ((RhsSequence) part).getName();
			final String aliasId = getAliasId(n, ruleAlias);
			AstClass ruleClass = aliasToClass.get(aliasId);
			if (ruleClass == null) {
				ruleClass = builder.addClass(getNonterminalTypeName(n, ruleAlias), null, n);
				builder.addExtends(ruleClass, cl);
				aliasToClass.put(aliasId, ruleClass);
			}
			mapClass(ruleClass, part);
			mapper.map((RhsSequence) part, null, ruleClass, false);

		}
	}

	private void mapLists() {
		Iterator<Nonterminal> i = unmapped.iterator();
		while (i.hasNext()) {
			final Nonterminal n = i.next();
			if (!(n.getDefinition() instanceof RhsList)) continue;
			final RhsList list = (RhsList) n.getDefinition();

			final RhsSymbolHandle elementHandle = getSingleSymbol(list.getElement());
			final RhsSymbolHandle initialElemHandle =
					list.getCustomInitialElement() != null ? getSingleSymbol(list.getCustomInitialElement()) : null;

			boolean requiresClass = elementHandle == null
					|| list.getCustomInitialElement() != null && initialElemHandle == null
					|| initialElemHandle != null && initialElemHandle.getSymbol() != elementHandle.getSymbol();

			if (!requiresClass) {
				final Symbol listElement = elementHandle.getSymbol();
				if (listElement instanceof Terminal && listElement.getType() == null) {
					error(listElement, "terminal symbol must have a type");
				} else {
					whenMapped(listElement, new Runnable() {
						@Override
						public void run() {
							if (listElement.getType() instanceof VoidType) {
								mapper.map(n, VoidType.INSTANCE);
								return;
							}
							mapNonterm(n, builder.list(listElement.getType(), list.isNonEmpty(), n));
							mapper.map(elementHandle.symbol, null, null, true);
							if (initialElemHandle != null) {
								mapper.map(initialElemHandle.symbol, null, null, true);
							}
						}
					});
				}
			} else {
				AstClass elementClass = builder.addClass(getNonterminalTypeName(n, "_item"), null, n);
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
		for (AstClass cl : classContent.keySet()) {
			List<RhsPart> rhsParts = classContent.get(cl);
			if (rhsParts == null || rhsParts.isEmpty()) continue;

			RhsPart def = rhsParts.size() == 1
					? rhsParts.get(0)
					: RhsUtil.asChoice(rhsParts.toArray(new RhsPart[rhsParts.size()]));
			DefaultMappingContext context = new DefaultMappingContext();
			traverseFields(def, context, new int[]{0}, true);
			traverseFields(def, context, new int[]{0}, false);

			Collections.sort(context.result);
			for (FieldDescriptor fd : context.result) {
				AstType type = fd.type;
				Map<Symbol, AstEnumMember> members = new LinkedHashMap<Symbol, AstEnumMember>();
				if (type == BOOL_OR_ENUM) {
					for (FieldMapping m = fd.firstMapping; m != null; m = m.next) {
						Symbol target = m.sym.getTarget();
						members.put(target, null);
					}
					if (members.size() > 1) {
						AstEnum enum_ = builder.addEnum(builder.uniqueName(cl, fd.baseName + "_kind", false), cl,
								fd.firstMapping.origin);
						final Symbol[] enumMembers = members.keySet().toArray(new Symbol[members.size()]);
						for (Symbol enumMember : enumMembers) {
							final AstEnumMember astEnumMember = builder.addMember(
									builder.uniqueName(enum_, TMDataUtil.getId(enumMember), true),
									enum_, null /* TODO ??? */);
							members.put(enumMember, astEnumMember);
						}
						type = enum_;
					} else {
						type = AstType.BOOL;
					}
				}

				AstField field = builder.addField(builder.uniqueName(cl, fd.baseName, true),
						type, fd.nullable, cl, fd.firstMapping.origin);
				for (FieldMapping m = fd.firstMapping; m != null; m = m.next) {
					Object value = TMDataUtil.getLiteral(m.sym);
					if (fd.type == BOOL_OR_ENUM) {
						if (type == AstType.BOOL) {
							value = Boolean.TRUE;
						} else {
							value = members.get(m.sym.getTarget());
						}
					}

					mapper.map(m.sym, field, value, m.addition);
				}
			}
		}
	}

	private void traverseFields(RhsPart part, MappingContext context, int[] index, boolean withAlias) {
		if (part instanceof RhsOptional) {
			traverseFields(((RhsOptional) part).getPart(), context, index, withAlias);

		} else if (part instanceof RhsUnordered || part instanceof RhsSequence) {
			RhsPart[] parts = part instanceof RhsUnordered
					? ((RhsUnordered) part).getParts()
					: ((RhsSequence) part).getParts();
			for (RhsPart p : parts) {
				traverseFields(p, context, index, withAlias);
			}

		} else if (part instanceof RhsChoice) {
			ChoiceMappingContext choiceContext = new ChoiceMappingContext(context);
			RhsPart[] parts = ((RhsChoice) part).getParts();
			for (RhsPart p : parts) {
				traverseFields(p, choiceContext, index, withAlias);
				choiceContext.reset();
			}

		} else if (part instanceof RhsAssignment || part instanceof RhsCast || part instanceof RhsSymbol) {
			// field is almost here
			RhsAssignment assignment = RhsUtil.getAssignment(part);
			RhsPart unwrapped = RhsUtil.unwrapEx(part, true, true, true);
			if (!(unwrapped instanceof RhsSymbol)) {
				error(part, (part instanceof RhsAssignment ? "assignment" : "cast") + " is not expected here");
				return;
			}

			index[0]++;
			if (withAlias && assignment == null || !withAlias && assignment != null) {
				return;
			}

			RhsSymbol ref = (RhsSymbol) unwrapped;
			AstType type = RhsUtil.getCastType(part);
			if (type == null) {
				final Object literal = TMDataUtil.getLiteral(ref);
				if (literal != null) {
					type = literal instanceof String ? AstType.STRING :
							literal instanceof Integer ? AstType.INT : AstType.BOOL;
				}
			}
			if (type == null) {
				if (!isConstantOrVoid(ref)) {
					type = ref.getTarget().getType();
				}
				if (type == null && assignment != null) {
					type = BOOL_OR_ENUM;
				}
			}

			if (type != null) {
				context.addMapping(assignment != null ? assignment.getName() : null, type, ref,
						index[0], assignment != null && assignment.isAddition(), part);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void error(SourceElement element, String message) {
		status.report(ProcessingStatus.KIND_ERROR, message, element);
	}


	private static RhsSymbolHandle getSingleSymbol(RhsPart part) {
		part = RhsUtil.unwrap(part);
		RhsCast cast = null;
		boolean optional = false;
		while (part != null) {
			if (part instanceof RhsSequence) {
				part = RhsUtil.unwrap(withoutConstants(part));
			} else if (part instanceof RhsOptional) {
				part = RhsUtil.unwrap(((RhsOptional) part).getPart());
				optional = true;
			} else if (part instanceof RhsCast) {
				cast = (RhsCast) part;
				part = RhsUtil.unwrap(cast.getPart());
			} else if (part instanceof RhsSymbol) {
				return new RhsSymbolHandle((RhsSymbol) part, cast, optional);
			} else {
				part = null;
			}
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

	private static boolean hasClassHint(Nonterminal n) {
		final TMTypeHint typeHint = TMDataUtil.getTypeHint(n);
		return typeHint != null && typeHint.getKind() == Kind.CLASS || hasProperty(n, "_class");
	}

	private static boolean hasInterfaceHint(Nonterminal n) {
		final TMTypeHint typeHint = TMDataUtil.getTypeHint(n);
		return typeHint != null && typeHint.getKind() == Kind.INTERFACE || hasProperty(n, "_interface");
	}

	private static boolean hasVoidHint(Nonterminal n) {
		final TMTypeHint typeHint = TMDataUtil.getTypeHint(n);
		return typeHint != null && typeHint.getKind() == Kind.VOID || hasProperty(n, "noast");
	}

	private static String getFieldBaseName(RhsSymbol sym) {
		final String id = TMDataUtil.getId(sym.getTarget());
		return id.endsWith("opt") && id.length() > 3 ? id.substring(0, id.length() - 3) : id;
	}

	private interface MappingContext {
		FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, int symIndex,
								   boolean isAddition, SourceElement origin);
	}

	private static class DefaultMappingContext implements MappingContext {

		private List<FieldDescriptor> result = new ArrayList<FieldDescriptor>();
		private Map<FieldId, Collection<FieldDescriptor>> fieldsMap = new HashMap<FieldId, Collection<FieldDescriptor>>();

		@Override
		public FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, int symIndex,
										  boolean isAddition, SourceElement origin) {
			FieldId id = new FieldId(alias, isAddition, sym.getTarget(), type);
			Collection<FieldDescriptor> fields = fieldsMap.get(id);
			if (fields == null) {
				fields = new ArrayList<FieldDescriptor>();
				fieldsMap.put(id, fields);
			}
			FieldDescriptor fd = new FieldDescriptor(alias != null ? alias : getFieldBaseName(sym), type);
			fd.addMapping(new FieldMapping(sym, symIndex, isAddition, origin));
			result.add(fd);
			fields.add(fd);
			return fd;
		}
	}

	private static class ChoiceMappingContext implements MappingContext {

		private final MappingContext parent;
		private Map<FieldId, Collection<FieldDescriptor>> localMap = new HashMap<FieldId, Collection<FieldDescriptor>>();
		private Set<FieldDescriptor> used;

		private ChoiceMappingContext(MappingContext parent) {
			this.parent = parent;
		}

		@Override
		public FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, int symIndex,
										  boolean isAddition, SourceElement origin) {
			FieldId id = new FieldId(alias, isAddition, sym.getTarget(), type);
			Collection<FieldDescriptor> fds = localMap.get(id);
			if (used == null) {
				used = new HashSet<FieldDescriptor>();
			}
			if (fds != null) {
				for (FieldDescriptor fd : fds) {
					if (used.add(fd)) {
						// reusing field from a previous alternative
						fd.addMapping(new FieldMapping(sym, symIndex, isAddition, origin));
						return fd;
					}
				}
			} else {
				fds = new ArrayList<FieldDescriptor>();
				localMap.put(id, fds);
			}

			FieldDescriptor fd = parent.addMapping(alias, type, sym, symIndex, isAddition, origin);
			fds.add(fd);
			used.add(fd);
			return fd;
		}

		private void reset() {
			// TODO detect nullables
			used = null;
		}
	}

	private static class FieldDescriptor implements Comparable {
		private final AstType type;
		private final String baseName;
		private FieldMapping firstMapping;
		private boolean nullable = true;

		private FieldDescriptor(String baseName, AstType type) {
			this.baseName = baseName;
			this.type = type;
		}

		private void addMapping(FieldMapping mapping) {
			if (firstMapping == null) {
				firstMapping = mapping;
				return;
			}
			mapping.next = firstMapping.next;
			firstMapping.next = mapping;
		}

		@Override
		public int compareTo(Object o) {
			return new Integer(firstMapping.symbolIndex).compareTo(((FieldDescriptor) o).firstMapping.symbolIndex);
		}
	}

	private static class FieldMapping {
		private final RhsSymbol sym;
		private final int symbolIndex;
		private final boolean addition;
		private final SourceElement origin;
		private FieldMapping next;

		private FieldMapping(RhsSymbol sym, int symbolIndex, boolean isAddition, SourceElement origin) {
			this.sym = sym;
			this.symbolIndex = symbolIndex;
			this.addition = isAddition;
			this.origin = origin;
		}
	}

	private static class FieldId {
		private final Symbol sym;
		private final AstType type;
		private final boolean addList;
		private final String alias;

		private FieldId(String alias, boolean isAddition, Symbol ref, AstType type) {
			this.alias = alias;
			this.sym = alias != null ? null : ref;
			if (!isAddition && type instanceof AstList) {
				this.addList = true;
				this.type = ((AstList) type).getInner();
			} else {
				this.addList = isAddition;
				this.type = type;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			FieldId fieldId = (FieldId) o;

			if (addList != fieldId.addList) return false;
			if (alias != null ? !alias.equals(fieldId.alias) : fieldId.alias != null) return false;
			if (sym != null ? !sym.equals(fieldId.sym) : fieldId.sym != null) return false;
			if (type != null ? !type.equals(fieldId.type) : fieldId.type != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = sym != null ? sym.hashCode() : 0;
			result = 31 * result + (type != null ? type.hashCode() : 0);
			result = 31 * result + (addList ? 1 : 0);
			result = 31 * result + (alias != null ? alias.hashCode() : 0);
			return result;
		}
	}

	private static final class MarkerType implements AstType {
		public MarkerType() {
		}

		@Override
		public boolean isSubtypeOf(AstType another) {
			return another == this;
		}

		@Override
		public String toString() {
			return "marker type";
		}
	}

	private static final class RhsSymbolHandle {

		private final RhsSymbol symbol;
		private final RhsCast cast;
		private final boolean isOptional;

		private RhsSymbolHandle(RhsSymbol symbol, RhsCast cast, boolean optional) {
			this.symbol = symbol;
			this.cast = cast;
			this.isOptional = optional;
		}

		public Symbol getSymbol() {
			return cast != null ? cast.getTarget() : symbol.getTarget();
		}
	}
}
