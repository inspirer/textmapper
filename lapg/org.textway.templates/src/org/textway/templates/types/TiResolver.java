package org.textway.templates.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.textway.templates.api.ILocatedEntity;
import org.textway.templates.api.IProblemCollector;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IDataType.Constraint;
import org.textway.templates.api.types.IDataType.ConstraintKind;
import org.textway.templates.api.types.IDataType.DataTypeKind;
import org.textway.templates.api.types.IFeature;
import org.textway.templates.types.TypesTree.TypesProblem;
import org.textway.templates.types.ast.FeatureDeclaration;
import org.textway.templates.types.ast.IAstNode;
import org.textway.templates.types.ast.IConstraint;
import org.textway.templates.types.ast.Input;
import org.textway.templates.types.ast.Multiplicity;
import org.textway.templates.types.ast.StringConstraint;
import org.textway.templates.types.ast.Type;
import org.textway.templates.types.ast.TypeDeclaration;
import org.textway.templates.types.ast._String;

/**
 *  Two-pass types model loader. 
 */
class TiResolver {

	private final String myPackage;
	private final String myContent;
	private final Map<String, TiClass> myRegistryClasses;
	private final IProblemCollector myStatus;

	// 1-st stage
	private TypesTree<Input> myTree;
	private Set<String> requiredPackages = new HashSet<String>();

	public TiResolver(String package_, String content, Map<String, TiClass> registryClasses,
			IProblemCollector problemCollector) {
		this.myPackage = package_;
		this.myContent = content;
		this.myRegistryClasses = registryClasses;
		this.myStatus = problemCollector;
	}

	public void build() {
		final TypesTree<Input> tree = TypesTree.parse(new TypesTree.TextSource(myPackage, myContent.toCharArray(), 1));
		if (tree.hasErrors()) {
			myStatus.fireError(null, "Problem(s) in type definitions:");
			for (final TypesProblem s : tree.getErrors()) {
				myStatus.fireError(new ILocatedEntity() {
					@Override
					public String getLocation() {
						return myPackage + "," + tree.getSource().lineForOffset(s.getOffset());
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
		for (FeatureDeclaration fd : td.getFeatureDeclarations()) {
			features.add(convertFeature(fd));
		}
		// TODO extends
		return new TiClass(td.getName(), new ArrayList<IClass>(), features);
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
				myStatus.fireError(new LocatedNodeAdapter(fd), "two multiplicity constraints found (feature " + fd.getName() + ")");
			}
			if (stringConstraints != null && fd.getType().getKind() != Type.LSTRING) {
				myStatus.fireError(new LocatedNodeAdapter(fd), "only string type can have constraints (feature " + fd.getName() + ")");
			}
		}
		TiFeature feature = new TiFeature(fd.getName(), loBound, hiBound, fd.getType().getIsReference());
		convertType(feature, fd.getType(), stringConstraints);
		// TODO default value
		return feature;
	}

	private void convertType(TiFeature feature, Type type, List<StringConstraint> constraints) {
		if (type.getKind() == 0) {
			// reference
			resolveLater(feature, type, type.getIdentifier());
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

	List<ResolveBean> myResolveFeatureTypes = new ArrayList<ResolveBean>();

	public void resolve() {
		for (ResolveBean entry : myResolveFeatureTypes) {
			TiClass tiClass = myRegistryClasses.get(entry.getReference());
			if (tiClass == null) {
				myStatus.fireError(new LocatedNodeAdapter(entry.getNode()),
						"cannot resolve type: " + entry.getReference() + " in " + entry.getFeature().getName());
			} else {
				entry.getFeature().setType(tiClass);
			}
		}
	}

	public Collection<String> getRequired() {
		return requiredPackages;
	}

	private class LocatedNodeAdapter implements ILocatedEntity {

		IAstNode node;

		public LocatedNodeAdapter(IAstNode node) {
			this.node = node;
		}

		@Override
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
}
