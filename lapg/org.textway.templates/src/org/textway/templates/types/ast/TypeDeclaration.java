package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class TypeDeclaration extends AstNode {

	private String name;
	private List<List<String>> _extends;
	private List<IMemberDeclaration> memberDeclarationsopt;

	public TypeDeclaration(String name, List<List<String>> _extends, List<IMemberDeclaration> memberDeclarationsopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this._extends = _extends;
		this.memberDeclarationsopt = memberDeclarationsopt;
	}

	public String getName() {
		return name;
	}
	public List<List<String>> getExtends() {
		return _extends;
	}
	public List<IMemberDeclaration> getMemberDeclarationsopt() {
		return memberDeclarationsopt;
	}
}
