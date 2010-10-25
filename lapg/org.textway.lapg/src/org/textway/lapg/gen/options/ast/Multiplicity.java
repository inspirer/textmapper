package org.textway.lapg.gen.options.ast;

import org.textway.lapg.gen.options.OptdefTree.TextSource;

public class Multiplicity extends AstOptNode implements IConstraint {

	private boolean hasNoUpperBound;
	private Integer icon;
	private Integer icon2;

	public Multiplicity(boolean hasNoUpperBound, Integer icon, Integer icon2, TextSource input, int start, int end) {
		super(input, start, end);
		this.hasNoUpperBound = hasNoUpperBound;
		this.icon = icon;
		this.icon2 = icon2;
	}

	public boolean getHasNoUpperBound() {
		return hasNoUpperBound;
	}
	public Integer getIcon() {
		return icon;
	}
	public Integer getIcon2() {
		return icon2;
	}
}
