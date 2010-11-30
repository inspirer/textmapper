package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class TypeDeclaration extends AstNode {

	private String name;
	private List<List<String>> _extends;
	private List<FeatureDeclaration> featureDeclarationsopt;

	public TypeDeclaration(String name, List<List<String>> _extends, List<FeatureDeclaration> featureDeclarationsopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this._extends = _extends;
		this.featureDeclarationsopt = featureDeclarationsopt;
	}

	public String getName() {
		return name;
	}
	public List<List<String>> getExtends() {
		return _extends;
	}
	public List<FeatureDeclaration> getFeatureDeclarationsopt() {
		return featureDeclarationsopt;
	}
}
