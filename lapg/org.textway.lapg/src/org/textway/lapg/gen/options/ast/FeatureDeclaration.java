package org.textway.lapg.gen.options.ast;

import java.util.List;
import org.textway.lapg.gen.options.OptdefTree.TextSource;

public class FeatureDeclaration extends AstOptNode {

	private String name;
	private Type type;
	private List<IConstraint> modifiersopt;
	private IExpression defaultvalopt;

	public FeatureDeclaration(String name, Type type, List<IConstraint> modifiersopt, IExpression defaultvalopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.type = type;
		this.modifiersopt = modifiersopt;
		this.defaultvalopt = defaultvalopt;
	}

	public String getName() {
		return name;
	}
	public Type getType() {
		return type;
	}
	public List<IConstraint> getModifiersopt() {
		return modifiersopt;
	}
	public IExpression getDefaultvalopt() {
		return defaultvalopt;
	}
}
