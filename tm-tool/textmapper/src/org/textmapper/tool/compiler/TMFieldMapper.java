/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.*;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.util.RhsUtil;
import org.textmapper.lapg.util.TypesUtil;

import java.util.*;

public class TMFieldMapper {

	private static final MarkerType BOOL_OR_ENUM = new MarkerType();

	private final ProcessingStatus status;
	private final AstBuilder builder;
	private final GrammarMapper mapper;
	private final boolean allowTypeAny;

	public TMFieldMapper(ProcessingStatus status, AstBuilder builder,
						 GrammarMapper mapper, boolean allowTypeAny) {
		this.status = status;
		this.builder = builder;
		this.mapper = mapper;
		this.allowTypeAny = allowTypeAny;
	}

	private void error(SourceElement element, String message) {
		status.report(ProcessingStatus.KIND_ERROR, message, element);
	}

	public void mapFields(AstClass cl, RhsPart def) {
		DefaultMappingContext context = new DefaultMappingContext();
		traverseFields(def, context, new int[]{0}, true);
		traverseFields(def, context, new int[]{0}, false);

		Collections.sort(context.result);
		for (FieldDescriptor fd : context.result) {
			AstType type = fd.type;
			Map<Symbol, AstEnumMember> members = new LinkedHashMap<>();
			if (type == BOOL_OR_ENUM) {
				for (FieldMapping m = fd.firstMapping; m != null; m = m.next) {
					Symbol target = m.sym.getTarget();
					members.put(target, null);
				}
				if (members.size() > 1) {
					AstEnum enum_ = builder.addEnum(
							builder.uniqueName(cl, fd.baseName + "_kind", false),
							cl, fd.firstMapping.origin);
					final Symbol[] enumMembers =
							members.keySet().toArray(new Symbol[members.size()]);
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

			AstField field = builder.addField(builder.uniqueName(cl, fd.baseName, true), type, fd
					.nullable, cl, fd.firstMapping.origin);
			for (FieldMapping m = fd.firstMapping; m != null; m = m.next) {
				Object value = TMDataUtil.getLiteral(m.sym);
				if (fd.type == BOOL_OR_ENUM) {
					if (type == AstType.BOOL) {
						value = Boolean.TRUE;
					} else {
						// TODO handle template vars
						value = members.get(m.sym.getTarget());
					}
				}

				mapper.map(m.sym, field, value, m.addition);
			}
		}

	}

	private void traverseFields(RhsPart part, MappingContext context,
								int[] index, boolean withAlias) {
		if (part instanceof RhsOptional) {
			traverseFields(((RhsOptional) part).getPart(), context, index, withAlias);

		} else if (part instanceof RhsSet) {
			// sets do not contain any useful information for ASTs

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

		} else if (part instanceof RhsAssignment || part instanceof RhsCast ||
				part instanceof RhsSymbol) {
			// field is almost here
			RhsAssignment assignment = RhsUtil.getAssignment(part);
			RhsPart unwrapped = RhsUtil.unwrapEx(part, true, true, true);
			if (!(unwrapped instanceof RhsSymbol)) {
				error(part, (part instanceof RhsAssignment ? "assignment" : "cast") +
						" is not expected here");
				return;
			}

			index[0]++;
			if (withAlias && assignment == null || !withAlias && assignment != null) {
				return;
			}

			RhsSymbol ref = (RhsSymbol) unwrapped;
			// TODO create an interface if this is a TemplateVar

			AstType type = RhsUtil.getCastType(part);
			if (type == null) {
				final Object literal = TMDataUtil.getLiteral(ref);
				if (literal != null) {
					type = literal instanceof String ? AstType.STRING :
							literal instanceof Integer ? AstType.INT : AstType.BOOL;
				}
			}
			if (type == null) {
				if (!TMMapper.isConstantOrVoid(ref)) {
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

	private static String getFieldBaseName(RhsSymbol sym) {
		// TODO handle template vars
		final String id = TMDataUtil.getId(sym.getTarget());
		return id.endsWith("opt") && id.length() > 3 ? id.substring(0, id.length() - 3) : id;
	}

	private interface MappingContext {
		FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, int symIndex,
								   boolean isAddition, SourceElement origin);
	}

	private class DefaultMappingContext implements MappingContext {

		private List<FieldDescriptor> result = new ArrayList<>();
		private Map<FieldId, Collection<FieldDescriptor>> fieldsMap = new HashMap<>();

		@Override
		public FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, int symIndex,
										  boolean isAddition, SourceElement origin) {
			// TODO handle template vars
			FieldId id = alias != null ? new FieldId(alias) :
					new FieldId(isAddition, sym.getTarget(), type);
			Collection<FieldDescriptor> fields = fieldsMap.get(id);
			if (fields == null) {
				fields = new ArrayList<>();
				fieldsMap.put(id, fields);
			}
			FieldDescriptor fd = new FieldDescriptor(alias != null ? alias : getFieldBaseName(sym));
			fd.addMapping(new FieldMapping(sym, symIndex, isAddition, origin), type);
			result.add(fd);
			fields.add(fd);
			return fd;
		}
	}

	private class ChoiceMappingContext implements MappingContext {

		private final MappingContext parent;
		private Map<FieldId, Collection<FieldDescriptor>> localMap = new HashMap<>();
		private Set<FieldDescriptor> used;

		private ChoiceMappingContext(MappingContext parent) {
			this.parent = parent;
		}

		@Override
		public FieldDescriptor addMapping(String alias, AstType type, RhsSymbol sym, int symIndex,
										  boolean isAddition, SourceElement origin) {
			// TODO handle template vars
			FieldId id = alias != null ? new FieldId(alias) :
					new FieldId(isAddition, sym.getTarget(), type);
			Collection<FieldDescriptor> fds = localMap.get(id);
			if (used == null) {
				used = new HashSet<>();
			}
			if (fds != null) {
				for (FieldDescriptor fd : fds) {
					if (used.add(fd)) {
						// reusing field from a previous alternative
						fd.addMapping(new FieldMapping(sym, symIndex, isAddition, origin), type);
						return fd;
					}
				}
			} else {
				fds = new ArrayList<>();
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

	private class FieldDescriptor implements Comparable {
		private AstType type;
		private final String baseName;
		private FieldMapping firstMapping;
		private boolean nullable = true;

		private FieldDescriptor(String baseName) {
			this.baseName = baseName;
		}

		private void addMapping(FieldMapping mapping, AstType type) {
			if (firstMapping == null) {
				firstMapping = mapping;
				this.type = type;
				return;
			}
			if (this.type != type) {
				AstType commonType = TypesUtil.getJoinType(this.type, type);
				if (commonType == null) {
					if (allowTypeAny) {
						commonType = AstType.ANY;
					} else {
						error(mapping.origin, "cannot deduce type for `" + baseName +
								"': no common type for " + this.type.toString() + " and " +
								type.toString());
						// Ignore mapping.
						return;
					}
				}
				this.type = commonType;
			}
			mapping.next = firstMapping.next;
			firstMapping.next = mapping;
		}

		@Override
		public int compareTo(Object o) {
			return new Integer(firstMapping.symbolIndex).compareTo(((FieldDescriptor) o)
					.firstMapping.symbolIndex);
		}
	}

	private static class FieldMapping {
		private final RhsSymbol sym;
		private final int symbolIndex;
		private final boolean addition;
		private final SourceElement origin;
		private FieldMapping next;

		private FieldMapping(RhsSymbol sym, int symbolIndex, boolean isAddition,
							 SourceElement origin) {
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

		private FieldId(String alias) {
			this.alias = alias;
			this.type = null;
			this.addList = false;
			this.sym = null;
		}

		private FieldId(boolean isAddition, Symbol ref, AstType type) {
			this.alias = null;
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

			if (alias != null ? !alias.equals(fieldId.alias) : fieldId.alias != null) return false;
			if (addList != fieldId.addList) return false;
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
}
