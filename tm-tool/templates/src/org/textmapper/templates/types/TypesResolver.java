/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.templates.types;

import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.api.types.*;
import org.textmapper.templates.api.types.IDataType.Constraint;
import org.textmapper.templates.api.types.IDataType.ConstraintKind;
import org.textmapper.templates.api.types.IDataType.DataTypeKind;
import org.textmapper.templates.storage.Resource;
import org.textmapper.templates.types.TiFeature.TiMultiplicity;
import org.textmapper.templates.types.TypesTree.TypesProblem;
import org.textmapper.templates.types.ast.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Two-pass types model loader.
 */
class TypesResolver {

	private final String myPackage;
	private final Resource myResource;
	private final Map<String, TiClass> myRegistryClasses;
	private final TemplatesStatus myStatus;

	// 1-st stage
	private TypesTree<AstInput> myTree;
	private Set<String> requiredPackages = new HashSet<>();

	// 2-nd stage
	List<StageWorker> myResolveTypes = new ArrayList<>();

	// 3-d stage
	List<StageWorker> myResolveDefaultValues = new ArrayList<>();

	public TypesResolver(String package_, Resource resource, Map<String, TiClass> registryClasses,
						 TemplatesStatus status) {
		this.myPackage = package_;
		this.myResource = resource;
		this.myRegistryClasses = registryClasses;
		this.myStatus = status;
	}

	public void build() {
		final TypesTree<AstInput> tree = TypesTree.parse(
				new TypesTree.TextSource(myPackage, myResource.getContents(), 1));

		if (tree.hasErrors()) {
			for (final TypesProblem s : tree.getErrors()) {
				myStatus.report(TemplatesStatus.KIND_ERROR, s.getMessage(), new SourceElement() {
					@Override
					public String getResourceName() {
						return myResource.getUri().toString();
					}

					@Override
					public int getOffset() {
						return s.getOffset();
					}

					@Override
					public int getEndOffset() {
						return s.getEndoffset();
					}

					@Override
					public int getLine() {
						return tree.getSource().lineForOffset(s.getOffset());
					}
				});
			}
			return;
		}

		myTree = tree;
		AstInput input = tree.getRoot();
		Set<String> myFoundClasses = new HashSet<>();
		for (AstTypeDeclaration td : input.getDeclarations()) {
			TiClass cl = convertClass(td);
			String fqName = myPackage + "." + cl.getName();
			if (myRegistryClasses.containsKey(fqName)) {
				myStatus.report(TemplatesStatus.KIND_ERROR,
						"class is declared twice: " + fqName
								+ (myFoundClasses.contains(fqName) ? " (in one file)" : ""),
						new LocatedNodeAdapter(td));
			} else {
				myRegistryClasses.put(fqName, cl);
			}
			myFoundClasses.add(fqName);
		}
	}

	private TiClass convertClass(final AstTypeDeclaration td) {
		List<IFeature> features = new ArrayList<>();
		List<IMethod> methods = new ArrayList<>();
		if (td.getMembers() != null) {
			for (IAstMemberDeclaration memberDeclaration : td.getMembers()) {
				if (memberDeclaration instanceof AstFeatureDeclaration) {
					features.add(convertFeature((AstFeatureDeclaration) memberDeclaration));
				} else if (memberDeclaration instanceof AstMethodDeclaration) {
					methods.add(convertMethod((AstMethodDeclaration) memberDeclaration));
				}
			}
		}
		final TiClass result = new TiClass(
				td.getName(), myPackage, new ArrayList<>(), features, methods);

		if (td.getSuper() == null) return result;

		final List<String> superNames = new ArrayList<>();
		for (List<String> className : td.getSuper()) {
			String s = getQualifiedName(className);
			if (s.indexOf('.') == -1) {
				s = myPackage + "." + s;
			} else {
				String targetPackage = s.substring(0, s.lastIndexOf('.'));
				requiredPackages.add(targetPackage);
			}
			superNames.add(s);
		}
		myResolveTypes.add(() -> {
			for (String ref : superNames) {
				TiClass target = myRegistryClasses.get(ref);
				if (target == null) {
					myStatus.report(TemplatesStatus.KIND_ERROR,
							"cannot resolve super type: " + ref + " for " + result.getName(),
							new LocatedNodeAdapter(td));
				} else {
					result.getExtends().add(target);
				}
			}
		});
		return result;
	}

	private IMethod convertMethod(final AstMethodDeclaration memberDeclaration) {
		final TiMethod result = new TiMethod(memberDeclaration.getName());
		convertType(new TypeHandler() {
			@Override
			public void typeResolved(IType type) {
				result.setType(type);
			}

			@Override
			public String containerName() {
				return memberDeclaration.getName();
			}
		}, memberDeclaration.getReturnType(), true);
		List<AstTypeEx> parametersopt = memberDeclaration.getParameters();
		final IType[] params = parametersopt == null || parametersopt.size() == 0
				? null : new IType[parametersopt.size()];
		if (params == null) return result;

		for (int i = 0; i < params.length; i++) {
			final int boundI = i;
			AstTypeEx astTypeEx = parametersopt.get(i);
			convertType(new TypeHandler() {
				@Override
				public void typeResolved(IType type) {
					params[boundI] = type;
					if (boundI == params.length - 1) {
						result.setParameterTypes(params);
					}
				}

				@Override
				public String containerName() {
					return memberDeclaration.getName();
				}
			}, astTypeEx, true);
		}
		return result;
	}

	private TiFeature convertFeature(AstFeatureDeclaration fd) {
		// constraints
		List<AstStringConstraint> stringConstraints = null;
		List<IMultiplicity> multiplicities = null;
		if (fd.getTypeEx().getMultiplicityListCommaSeparated() != null) {
			myStatus.report(TemplatesStatus.KIND_ERROR,
					"feature cannot have multiplicity in type (feature `" + fd.getName() + "`)",
					new LocatedNodeAdapter(fd.getTypeEx()));
		}
		if (fd.getModifiers() != null) {
			for (AstConstraint c : fd.getModifiers()) {
				if (c.getMultiplicityListCommaSeparated() != null) {
					if (multiplicities != null) {
						myStatus.report(TemplatesStatus.KIND_ERROR,
								"several multiplicity constraints found (feature `"
										+ fd.getName() + "`)",
								new LocatedNodeAdapter(fd));
					} else {
						multiplicities = convertMultiplicities(
								c.getMultiplicityListCommaSeparated());
					}
				} else if (c.getStringConstraint() != null) {
					if (stringConstraints == null) {
						stringConstraints = new ArrayList<>();
					}
					stringConstraints.add(c.getStringConstraint());
				}
			}
			if (stringConstraints != null
					&& fd.getTypeEx().getType().getKind() != AstType.AstKindKind.LSTRING) {
				myStatus.report(TemplatesStatus.KIND_ERROR,
						"only string type can have constraints (feature `" + fd.getName() + "`)",
						new LocatedNodeAdapter(fd));
			}
		}
		final TiFeature feature = new TiFeature(fd.getName(),
				fd.getTypeEx().getType().isReference(), multiplicities == null
				? new IMultiplicity[0]
				: multiplicities.toArray(new IMultiplicity[multiplicities.size()]));
		convertType(new TypeHandler() {
			@Override
			public void typeResolved(IType type) {
				feature.setType(type);
			}

			@Override
			public String containerName() {
				return feature.getName();
			}
		}, fd.getTypeEx().getType(), stringConstraints, true);
		convertDefautVal(feature, fd.getDefaultval());
		return feature;
	}

	private List<IMultiplicity> convertMultiplicities(List<AstMultiplicity> list) {
		List<IMultiplicity> multiplicities = new ArrayList<>();
		for (AstMultiplicity multiplicity : list) {
			int loBound = multiplicity.getLo();
			int hiBound = multiplicity.hasNoUpperBound() ? -1
					: multiplicity.getHi() != null ? multiplicity.getHi()
					: loBound;
			TiMultiplicity new_ = new TiMultiplicity(loBound, hiBound);
			if (list.size() > 1 && !new_.isMultiple()) {
				myStatus.report(TemplatesStatus.KIND_ERROR,
						"cannot combine 1 or 0..1 with other multiplicities",
						new LocatedNodeAdapter(multiplicity));
				return null;
			}
			multiplicities.add(new_);
		}
		return multiplicities;
	}

	private void convertDefautVal(final TiFeature feature, final IAstExpression defaultValue) {
		if (defaultValue == null) {
			return;
		}
		scanRequiredClasses(defaultValue);
		myResolveDefaultValues.add(() ->
				feature.setDefaultValue(convertExpression(defaultValue, feature.getType())));
	}

	private void scanRequiredClasses(IAstExpression expression) {
		if (expression instanceof AstStructuralExpression) {
			AstStructuralExpression expr = (AstStructuralExpression) expression;
			if (expr.getExpressionList() != null) {
				expr.getExpressionList().forEach(this::scanRequiredClasses);
			}
			if (expr.getMapEntries() != null) {
				for (AstListOfIdentifierAnd2ElementsCommaSeparatedItem item
						: expr.getMapEntries()) {
					scanRequiredClasses(item.getExpression());
				}
			}
			if (expr.getName() != null) {
				if (expr.getName().size() > 1) {
					String reference = getQualifiedName(expr.getName());
					int lastDot = reference.lastIndexOf('.');
					if (lastDot >= 0) {
						requiredPackages.add(reference.substring(0, lastDot));
					}
				}
			}
		}
	}

	private void convertType(TypeHandler handler, final AstTypeEx type, boolean async) {
		convertType(handler, type.getType(), null, async);
		// FIXME handle list
	}

	private void convertType(final TypeHandler handler, final AstType type,
							 List<AstStringConstraint> constraints, boolean async) {
		if (type.getKind() == null) {
			// reference or closure
			if (type.isClosure()) {
				final List<AstTypeEx> params = type.getParameters();
				if (params == null) {
					handler.typeResolved(new TiClosureType());
					return;
				}
				StageWorker worker = () -> {
					final IType[] types = new IType[params.size()];
					for (int i = 0; i < params.size(); i++) {
						final int boundI = i;
						convertType(new TypeHandler() {
							@Override
							public void typeResolved(IType type1) {
								types[boundI] = type1;
							}

							@Override
							public String containerName() {
								return "closure";
							}
						}, params.get(i), false);
					}
					handler.typeResolved(new TiClosureType(types));
				};
				if (async) {
					myResolveTypes.add(worker);
				} else {
					worker.resolve();
				}
			} else {
				String reference = getQualifiedName(type.getName());
				int lastDot = reference.lastIndexOf('.');
				if (lastDot == -1) {
					reference = myPackage + "." + reference;
				} else {
					String targetPackage = reference.substring(0, lastDot);
					requiredPackages.add(targetPackage);
				}
				final String className = reference;
				StageWorker worker = () -> {
					TiClass tiClass = myRegistryClasses.get(className);
					if (tiClass == null) {
						myStatus.report(TemplatesStatus.KIND_ERROR,
								"cannot resolve type: " + className + " in "
										+ handler.containerName(),
								new LocatedNodeAdapter(type));
					} else {
						handler.typeResolved(tiClass);
					}
				};
				if (async) {
					myResolveTypes.add(worker);
				} else {
					worker.resolve();
				}
			}
		} else {
			// datatype
			DataTypeKind kind = DataTypeKind.STRING;
			if (type.getKind() == AstType.AstKindKind.LINT) {
				kind = DataTypeKind.INT;
			} else if (type.getKind() == AstType.AstKindKind.LBOOL) {
				kind = DataTypeKind.BOOL;
			}

			List<Constraint> convertedConstraints = new ArrayList<>();
			if (constraints != null) {
				for (AstStringConstraint c : constraints) {
					Constraint convertConstraint = convertConstraint(c);
					if (convertConstraint != null) {
						convertedConstraints.add(convertConstraint);
					}
				}
			}
			handler.typeResolved(new TiDataType(kind, convertedConstraints));
		}
	}

	private Constraint convertConstraint(AstStringConstraint c) {
		if (c.getKind() != null) {
			List<String> strings = c.getStrings().stream().map(s -> s.getIdentifier() != null ? s
					.getIdentifier() : s.getScon()).collect(Collectors.toList());
			return new TiDataType.TiConstraint(
					c.getKind() == AstStringConstraint.AstKindKind.LCHOICE
							? ConstraintKind.CHOICE
							: ConstraintKind.SET,
					strings);
		}
		String constraintId = c.getIdentifier();
		switch (constraintId) {
			case "notempty":
				return new TiDataType.TiConstraint(ConstraintKind.NOTEMPTY, null);
			case "qualified":
				return new TiDataType.TiConstraint(ConstraintKind.QUALIFIED_IDENTIFIER, null);
			case "identifier":
				return new TiDataType.TiConstraint(ConstraintKind.IDENTIFIER, null);
			default:
				myStatus.report(TemplatesStatus.KIND_ERROR,
						"unknown string constraint: " + constraintId,
						new LocatedNodeAdapter(c));
				return null;
		}
	}

	private String getQualifiedName(List<String> name) {
		StringBuilder sb = new StringBuilder();
		for (String s : name) {
			if (sb.length() > 0) {
				sb.append('.');
			}
			sb.append(s);
		}
		return sb.toString();
	}

	void resolve() {
		myResolveTypes.forEach(StageWorker::resolve);
	}

	void resolveExpressions() {
		myResolveDefaultValues.forEach(StageWorker::resolve);
	}

	private Object convertExpression(IAstExpression expression, IType type) {
		return new TiExpressionBuilder<IAstExpression>() {

			@Override
			public IClass resolveType(String className) {
				return myRegistryClasses.get(className);
			}

			@Override
			public Object resolve(IAstExpression expression, IType type) {
				if (expression instanceof AstLiteralExpression) {
					AstLiteralExpression literal = (AstLiteralExpression) expression;
					Object val = literal.getBcon() != null ? literal.getBcon()
							: literal.getIcon() != null ? literal.getIcon()
							: literal.getScon();
					return convertLiteral(expression, val, type);
				}
				if (expression instanceof AstStructuralExpression) {
					AstStructuralExpression expr = (AstStructuralExpression) expression;
					if (expr.getName() != null) {
						String qualifiedName = getQualifiedName(expr.getName());
						if (qualifiedName.indexOf('.') == -1) {
							qualifiedName = myPackage + "." + qualifiedName;
						}
						Map<String, IAstExpression> props = null;
						if (expr.getMapEntries() != null) {
							props = new HashMap<>();
							for (AstListOfIdentifierAnd2ElementsCommaSeparatedItem i
									: expr.getMapEntries()) {
								props.put(i.getIdentifier(), i.getExpression());
							}
						}
						return convertNew(expr, qualifiedName, props, type);
					} else {
						return convertArray(expr, expr.getExpressionList(), type);
					}
				}
				return null;
			}

			@Override
			public void report(IAstExpression expression, String message) {
				myStatus.report(TemplatesStatus.KIND_ERROR, message,
						new LocatedNodeAdapter(expression));
			}
		}.resolve(expression, type);
	}

	public Collection<String> getRequired() {
		return requiredPackages;
	}

	private class LocatedNodeAdapter implements SourceElement {

		IAstNode node;

		public LocatedNodeAdapter(IAstNode node) {
			this.node = node;
		}

		@Override
		public String getResourceName() {
			return myResource.getUri().toString();
		}

		@Override
		public int getOffset() {
			return node.getOffset();
		}

		@Override
		public int getEndOffset() {
			return node.getEndoffset();
		}

		@Override
		public int getLine() {
			return myTree.getSource().lineForOffset(node.getOffset());
		}
	}

	private interface StageWorker {
		void resolve();
	}

	private interface TypeHandler {
		void typeResolved(IType type);

		String containerName();
	}
}
