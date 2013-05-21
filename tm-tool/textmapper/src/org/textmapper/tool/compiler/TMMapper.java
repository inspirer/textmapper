package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.ast.*;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.*;

import java.util.*;

/**
 * evgeny, 1/29/13
 */
public class TMMapper {

	private final ProcessingStatus status;
	private final Grammar grammar;
	private final GrammarMapper mapper;
	private final AstBuilder builder;
	private final Map<Symbol, Symbol> decorators = new HashMap<Symbol, Symbol>();
	private final Map<Symbol, List<Runnable>> typeListeners = new HashMap<Symbol, List<Runnable>>();
	private final Queue<Runnable> postProcessors = new LinkedList<Runnable>();
	private final Map<AstClass, List<RhsPart>> classContent = new LinkedHashMap<AstClass, List<RhsPart>>();
	private List<Nonterminal> unmapped;

	public TMMapper(Grammar grammar, ProcessingStatus status) {
		this.grammar = grammar;
		this.status = status;
		this.mapper = LapgCore.createMapper(grammar);
		this.builder = LapgCore.createAstBuilder();
	}

	public AstModel deriveAST() {
		collectUnmapped();

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

		return builder.create();
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
					AstEnum astEnum = builder.addEnum(getNonterminalTypeName(n), null, n);
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
						AstEnumMember member = builder.addMember(builder.uniqueName(astEnum, memberName, true), astEnum, part);
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
		List<RhsSequence> customRuleList = new ArrayList<RhsSequence>();

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
				final String ruleAlias = part instanceof RhsSequence ? ((RhsSequence) part).getName() : null;
				if (ruleAlias == null) {
					isInterface = false;
					break;
				}
				customRuleList.add((RhsSequence) part);
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
				Map<String, AstClass> aliasToClass = new HashMap<String, AstClass>();
				for (RhsSequence rulePart : customRuleList) {
					String ruleAlias = rulePart.getName();
					AstClass ruleClass = aliasToClass.get(ruleAlias);
					if (ruleClass == null) {
						ruleClass = builder.addClass(builder.uniqueName(null, TMDataUtil.getId(n) + "_" + ruleAlias, false), null, n);
						builder.addExtends(ruleClass, interfaceClass);
						aliasToClass.put(ruleAlias, ruleClass);
					}
					mapClass(ruleClass, rulePart);
					for (Rule rule : n.getRules()) {
						if (rule.getSource() == rulePart) {
							mapper.map(rule, ruleClass);
						}
					}
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

			// TODO map RhsSymbols....

			if (elementType == null) {
				AstClass elementClass = builder.addClass(builder.uniqueName(null, TMDataUtil.getId(n) + "_element", false), null, n);
				//mapClass(elementClass, list.getElement(), list.getCustomInitialElement());
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
		for (AstClass cl : classContent.keySet()) {
			List<RhsPart> rhsParts = classContent.get(cl);
			if (rhsParts == null || rhsParts.isEmpty()) continue;

			RhsPart def = rhsParts.size() == 1
					? rhsParts.get(0)
					: RhsUtil.asChoice(rhsParts.toArray(new RhsPart[rhsParts.size()]));
			DefaultMappingContext context = new DefaultMappingContext();
			traverseFields(def, context);

			for (FieldDescriptor fd : context.result) {
				AstField field = builder.addField(builder.uniqueName(cl, fd.baseName, true), fd.type, fd.nullable, cl, fd.firstMapping.origin);
				for (FieldMapping m = fd.firstMapping; m != null; m = m.next) {
					mapper.map(m.sym, field, null, m.addition);
				}
			}
		}
	}

	private void traverseFields(RhsPart part, MappingContext context) {
		if (part instanceof RhsOptional) {
			traverseFields(((RhsOptional) part).getPart(), context);

		} else if (part instanceof RhsUnordered || part instanceof RhsSequence) {
			RhsPart[] parts = part instanceof RhsUnordered ? ((RhsUnordered) part).getParts() : ((RhsSequence) part).getParts();
			for (RhsPart p : parts) {
				traverseFields(p, context);
			}

		} else if (part instanceof RhsChoice) {
			ChoiceMappingContext choiceContext = new ChoiceMappingContext(context);
			RhsPart[] parts = ((RhsChoice) part).getParts();
			for (RhsPart p : parts) {
				traverseFields(p, choiceContext);
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

			RhsSymbol ref = (RhsSymbol) unwrapped;
			AstType type = RhsUtil.getCastType(part);
			if (type == null) {
				type = ref.getTarget().getType();
				if (type == null && assignment != null) {
					type = AstType.BOOL;
				}
			}

			if (type != null) {
				context.addMapping(assignment != null ? assignment.getName() : null, type, ref,
						assignment != null && assignment.isAddition(), part);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void error(SourceElement element, String message) {
		status.report(ProcessingStatus.KIND_ERROR, message, element);
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

	private interface MappingContext {
		FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, boolean isAddition, SourceElement origin);
	}

	private static class DefaultMappingContext implements MappingContext {

		private List<FieldDescriptor> result = new ArrayList<FieldDescriptor>();
		private Map<FieldId, Collection<FieldDescriptor>> fieldsMap = new HashMap<FieldId, Collection<FieldDescriptor>>();

		public FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, boolean isAddition, SourceElement origin) {
			FieldId id = new FieldId(alias, isAddition, sym, type);
			Collection<FieldDescriptor> fields = fieldsMap.get(id);
			if (fields == null) {
				fields = new ArrayList<FieldDescriptor>();
				fieldsMap.put(id, fields);
			}
			FieldDescriptor fd = new FieldDescriptor(alias != null ? alias : TMDataUtil.getId(sym.getTarget()), type);
			fd.addMapping(new FieldMapping(sym, isAddition, origin));
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
		public FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, boolean isAddition, SourceElement origin) {
			FieldId id = new FieldId(alias, isAddition, sym, type);
			Collection<FieldDescriptor> fds = localMap.get(id);
			if (used == null) {
				used = new HashSet<FieldDescriptor>();
			}
			if (fds != null) {
				for (FieldDescriptor fd : fds) {
					if (used.add(fd)) {
						// reusing field from a previous alternative
						fd.addMapping(new FieldMapping(sym, isAddition, origin));
						return fd;
					}
				}
			} else {
				fds = new ArrayList<FieldDescriptor>();
				localMap.put(id, fds);
			}

			FieldDescriptor fd = parent.addMapping(alias, type, sym, isAddition, origin);
			fds.add(fd);
			used.add(fd);
			return fd;
		}

		private void reset() {
			// TODO detect nullables
			used = null;
		}
	}

	private static class FieldDescriptor {
		private final AstType type;
		private final String baseName;
		private FieldMapping firstMapping;
		private boolean nullable = true;

		private FieldDescriptor(String baseName, AstType type) {
			this.baseName = baseName;
			this.type = type;
		}

		private void addMapping(FieldMapping mapping) {
			mapping.next = firstMapping;
			firstMapping = mapping;
		}
	}

	private static class FieldMapping {
		private final RhsSymbol sym;
		private final boolean addition;
		private final SourceElement origin;
		private FieldMapping next;

		private FieldMapping(RhsSymbol sym, boolean isAddition, SourceElement origin) {
			this.sym = sym;
			addition = isAddition;
			this.origin = origin;
		}
	}

	private static class FieldId {
		private final RhsSymbol sym;
		private final AstType type;
		private final boolean addList;
		private final String alias;

		private FieldId(String alias, boolean isAddition, RhsSymbol ref, AstType type) {
			this.alias = alias;
			this.sym = ref;
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
}
