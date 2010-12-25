/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.templates.types;

import org.textway.templates.api.IProblemCollector;
import org.textway.templates.api.types.*;
import org.textway.templates.api.types.IDataType.Constraint;
import org.textway.templates.api.types.IDataType.ConstraintKind;
import org.textway.templates.api.types.IDataType.DataTypeKind;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.storage.Resource;
import org.textway.templates.types.TypesTree.TypesProblem;
import org.textway.templates.types.ast.*;

import java.util.*;

/**
 * Two-pass types model loader.
 */
class TypesResolver {

	private final String myPackage;
	private final Resource myResource;
	private final Map<String, TiClass> myRegistryClasses;
	private final IProblemCollector myStatus;

	// 1-st stage
	private TypesTree<Input> myTree;
	private Set<String> requiredPackages = new HashSet<String>();

	// 2-nd stage
	List<ResolveBean> myResolveFeatureTypes = new ArrayList<ResolveBean>();
	List<ResolveSuperBean> myResolveSuperTypes = new ArrayList<ResolveSuperBean>();

	// 3-d stage
	List<ResolveDefaultValue> myResolveDefaultValues = new ArrayList<ResolveDefaultValue>();

	public TypesResolver(String package_, Resource resource, Map<String, TiClass> registryClasses,
					  IProblemCollector problemCollector) {
		this.myPackage = package_;
		this.myResource = resource;
		this.myRegistryClasses = registryClasses;
		this.myStatus = problemCollector;
	}

	public void build() {
		final TypesTree<Input> tree = TypesTree.parse(new TypesTree.TextSource(myPackage, myResource.getContents().toCharArray(), 1));
		if (tree.hasErrors()) {
			myStatus.fireError(null, "Problem(s) in type definitions:");
			for (final TypesProblem s : tree.getErrors()) {
				myStatus.fireError(new ILocatedEntity() {
					public String getLocation() {
						return myResource.getUri().getPath() + "," + tree.getSource().lineForOffset(s.getOffset());
					}
				}, s.getMessage());
			}
			return;
		}

		myTree = tree;
		Input input = tree.getRoot();
		Set<String> myFoundClasses = new HashSet<String>();
		for (TypeDeclaration td : input.getDeclarations()) {
			TiClass cl = convertClass(td);
			String fqName = myPackage + "." + cl.getName();
			if (myRegistryClasses.containsKey(fqName)) {
				myStatus.fireError(new LocatedNodeAdapter(td), "class is declared twice: " + fqName
						+ (myFoundClasses.contains(fqName) ? " (in one file)" : ""));
			} else {
				myRegistryClasses.put(fqName, cl);
			}
			myFoundClasses.add(fqName);
		}
	}

	private TiClass convertClass(TypeDeclaration td) {
		List<IFeature> features = new ArrayList<IFeature>();
		if(td.getFeatureDeclarationsopt() != null) {
			for (FeatureDeclaration fd : td.getFeatureDeclarationsopt()) {
				features.add(convertFeature(fd));
			}
		}
		TiClass result = new TiClass(td.getName(), myPackage, new ArrayList<IClass>(), features);
		if (td.getExtends() != null) {
			List<String> superNames = new ArrayList<String>();
			for (List<String> className : td.getExtends()) {
				String s = getQualifiedName(className);
				if(s.indexOf('.') == -1) {
					s = myPackage + "." + s;
				}
				superNames.add(s);
			}
			myResolveSuperTypes.add(new ResolveSuperBean(result, td, superNames));
		}
		return result;
	}

	private TiFeature convertFeature(FeatureDeclaration fd) {
		// constraints
		int loBound = 0;
		int hiBound = 1;
		List<StringConstraint> stringConstraints = null;
		if (fd.getModifiersopt() != null) {
			int multiplicityCount = 0;
			for (IConstraint c : fd.getModifiersopt()) {
				if (c instanceof Multiplicity) {
					Multiplicity multiplicity = (Multiplicity) c;
					loBound = multiplicity.getLo();
					hiBound = multiplicity.getHasNoUpperBound() ? -1 : multiplicity.getHi() != null ? multiplicity
							.getHi() : loBound;
					multiplicityCount++;
				} else if (c instanceof StringConstraint) {
					if (stringConstraints == null) {
						stringConstraints = new ArrayList<StringConstraint>();
					}
					stringConstraints.add((StringConstraint) c);
				}
			}
			if (multiplicityCount > 1) {
				myStatus.fireError(new LocatedNodeAdapter(fd), "two multiplicity constraints found (feature `" + fd.getName() + "`)");
			}
			if (stringConstraints != null && fd.getType().getKind() != Type.LSTRING) {
				myStatus.fireError(new LocatedNodeAdapter(fd), "only string type can have constraints (feature `" + fd.getName() + "`)");
			}
		}
		TiFeature feature = new TiFeature(fd.getName(), loBound, hiBound, fd.getType().getIsReference());
		convertType(feature, fd.getType(), stringConstraints);
		convertDefautVal(feature, fd.getDefaultvalopt());
		return feature;
	}

	private void convertDefautVal(TiFeature feature, IExpression defaultValue) {
		if (defaultValue == null) {
			return;
		}
		scanRequiredClasses(defaultValue);
		myResolveDefaultValues.add(new ResolveDefaultValue(feature, defaultValue));
	}

	private void scanRequiredClasses(IExpression expression) {
		if (expression instanceof StructuralExpression) {
			StructuralExpression expr = (StructuralExpression) expression;
			if (expr.getExpressionListopt() != null) {
				for (IExpression inner : expr.getExpressionListopt()) {
					scanRequiredClasses(inner);
				}
			}
			if (expr.getMapEntriesopt() != null) {
				for (MapEntriesItem item : expr.getMapEntriesopt()) {
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

	private void convertType(TiFeature feature, Type type, List<StringConstraint> constraints) {
		if (type.getKind() == 0) {
			// reference
			resolveLater(feature, type, getQualifiedName(type.getName()));
		} else {
			// datatype
			DataTypeKind kind = DataTypeKind.STRING;
			if (type.getKind() == Type.LINT) {
				kind = DataTypeKind.INT;
			} else if (type.getKind() == Type.LBOOL) {
				kind = DataTypeKind.BOOL;
			}

			List<Constraint> convertedConstraints = new ArrayList<Constraint>();
			if (constraints != null) {
				for (StringConstraint c : constraints) {
					Constraint convertConstraint = convertConstraint(c);
					if (convertConstraint != null) {
						convertedConstraints.add(convertConstraint);
					}
				}
			}
			feature.setType(new TiDataType(kind, convertedConstraints));
		}
	}

	private Constraint convertConstraint(StringConstraint c) {
		if (c.getKind() != 0) {
			List<String> strings = new ArrayList<String>();
			for (_String s : c.getStrings()) {
				strings.add(s.getIdentifier() != null ? s.getIdentifier() : s.getScon());
			}
			return new TiDataType.TiConstraint(c.getKind() == StringConstraint.LCHOICE ? ConstraintKind.CHOICE
					: ConstraintKind.SET, strings);
		}
		String constraintId = c.getIdentifier();
		if (constraintId.equals("notempty")) {
			return new TiDataType.TiConstraint(ConstraintKind.NOTEMPTY, null);
		} else if (constraintId.equals("qualified")) {
			return new TiDataType.TiConstraint(ConstraintKind.QUALIFIED_IDENTIFIER, null);
		} else if (constraintId.equals("identifier")) {
			return new TiDataType.TiConstraint(ConstraintKind.IDENTIFIER, null);
		} else {
			myStatus.fireError(new LocatedNodeAdapter(c), "unknown string constraint: " + constraintId);
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

	private void resolveLater(TiFeature feature, Type decl, String reference) {
		int lastDot = reference.lastIndexOf('.');
		if (lastDot == -1) {
			reference = myPackage + "." + reference;
		} else {
			String targetPackage = reference.substring(0, lastDot);
			requiredPackages.add(targetPackage);
		}
		myResolveFeatureTypes.add(new ResolveBean(feature, decl, reference));
	}

	void resolve() {
		for (ResolveBean entry : myResolveFeatureTypes) {
			TiClass tiClass = myRegistryClasses.get(entry.getReference());
			if (tiClass == null) {
				myStatus.fireError(new LocatedNodeAdapter(entry.getNode()),
						"cannot resolve type: " + entry.getReference() + " in " + entry.getFeature().getName());
			} else {
				entry.getFeature().setType(tiClass);
			}
		}

		for (ResolveSuperBean entry : myResolveSuperTypes) {
			TiClass source = entry.getClassifier();
			for (String ref : entry.getReferences()) {
				TiClass target = myRegistryClasses.get(ref);
				if (target == null) {
					myStatus.fireError(new LocatedNodeAdapter(entry.getNode()),
							"cannot resolve super type: " + ref + " for " + entry.getClassifier().getName());
				} else {
					source.getExtends().add(target);
				}
			}
		}
	}

	void resolveExpressions() {
		for (ResolveDefaultValue entry : myResolveDefaultValues) {
			entry.getFeature_().setDefaultValue(convertExpression(entry.getDefaultValue(), TypesUtil.getType(entry.getFeature_())));
		}
	}

	private Object convertExpression(IExpression expression, IType type) {
		return new TiExpressionBuilder<IExpression>() {

			@Override
			public IClass resolveType(String className) {
				return myRegistryClasses.get(className);
			}

			@Override
			public Object resolve(IExpression expression, IType type) {
				if (expression instanceof LiteralExpression) {
					LiteralExpression literal = (LiteralExpression) expression;
					Object val = literal.getBcon() != null ? literal.getBcon()
							: literal.getIcon() != null ? literal.getIcon()
							: literal.getScon();
					return convertLiteral(expression, val, type);
				}
				if (expression instanceof StructuralExpression) {
					StructuralExpression expr = (StructuralExpression) expression;
					if (expr.getName() != null) {
						String qualifiedName = getQualifiedName(expr.getName());
						if (qualifiedName.indexOf('.') == -1) {
							qualifiedName = myPackage + "." + qualifiedName;
						}
						Map<String, IExpression> props = null;
						if(expr.getMapEntriesopt() != null) {
							props = new HashMap<String, IExpression>();
							for(MapEntriesItem i : expr.getMapEntriesopt()) {
								props.put(i.getIdentifier(), i.getExpression());
							}
						}
						return convertNew(expr, qualifiedName, props, type);
					} else {
						return convertArray(expr, expr.getExpressionListopt(), type);
					}
				}
				return null;
			}

			@Override
			public void report(IExpression expression, String message) {
				myStatus.fireError(new LocatedNodeAdapter((IAstNode) expression), message);
			}
		}.resolve(expression, type);
	}

	public Collection<String> getRequired() {
		return requiredPackages;
	}

	private class LocatedNodeAdapter implements ILocatedEntity {

		IAstNode node;

		public LocatedNodeAdapter(IAstNode node) {
			this.node = node;
		}

		public String getLocation() {
			return myPackage + "," + myTree.getSource().lineForOffset(node.getOffset());
		}
	}

	private static class ResolveBean {
		private TiFeature feature;
		private IAstNode node;
		private String reference;

		public ResolveBean(TiFeature feature, IAstNode node, String reference) {
			this.feature = feature;
			this.node = node;
			this.reference = reference;
		}

		public TiFeature getFeature() {
			return feature;
		}

		public String getReference() {
			return reference;
		}

		public IAstNode getNode() {
			return node;
		}
	}

	private static class ResolveSuperBean {
		private TiClass class_;
		private IAstNode node;
		private Collection<String> references;

		public ResolveSuperBean(TiClass tiClass, IAstNode node, Collection<String> references) {
			this.class_ = tiClass;
			this.node = node;
			this.references = references;
		}

		public TiClass getClassifier() {
			return class_;
		}

		public Collection<String> getReferences() {
			return references;
		}

		public IAstNode getNode() {
			return node;
		}
	}

	private static class ResolveDefaultValue {
		private final TiFeature feature_;
		private final IExpression defaultValue;

		public ResolveDefaultValue(TiFeature feature, IExpression defaultValue) {
			this.feature_ = feature;
			this.defaultValue = defaultValue;
		}

		public TiFeature getFeature_() {
			return feature_;
		}

		public IExpression getDefaultValue() {
			return defaultValue;
		}
	}
}
