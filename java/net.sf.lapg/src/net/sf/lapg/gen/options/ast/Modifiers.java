package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Modifiers extends AstOptNode {

	private List<IConstraint> constraints;

	public Modifiers(List<IConstraint> constraints, TextSource input, int start, int end) {
		super(input, start, end);
		this.constraints = constraints;
	}

	public List<IConstraint> getConstraints() {
		return constraints;
	}
}
