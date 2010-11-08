package org.textway.templates.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.textway.templates.api.ILocatedEntity;
import org.textway.templates.api.IProblemCollector;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IFeature;
import org.textway.templates.types.TypesTree.TypesProblem;
import org.textway.templates.types.ast.IAstNode;
import org.textway.templates.types.ast.Input;
import org.textway.templates.types.ast.TypeDeclaration;

public class TiResolver {

	private final String myPackage;
	private final String myContent;
	private final Map<String, TiClass> myRegistryClasses;
	private final IProblemCollector myStatus;

	// 1-st stage
	private TypesTree myTree;

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
			myStatus.fireError(null, "Problems in templates bundle found:");
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
				myStatus.fireError(new LocatedNodeAdapter(td),
						"duplicate class declaration found: " + fqName + (myFoundClasses.contains(fqName) ? " (in one file)" : ""));
			} else {
				myRegistryClasses.put(fqName, cl);
			}
			myFoundClasses.add(fqName);
		}
	}

	private TiClass convertClass(TypeDeclaration td) {
		List<IFeature> features = new ArrayList<IFeature>();
		// TODO convert features
		return new TiClass(td.getName(), new ArrayList<IClass>(), features);
	}

	public void resolve() {

	}

	public Collection<String> getRequired() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
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
}
