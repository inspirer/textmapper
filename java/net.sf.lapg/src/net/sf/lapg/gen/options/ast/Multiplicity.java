package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Multiplicity extends AstOptNode implements IConstraint {

	private Integer icon;
	private Integer icon2;

	public Multiplicity(Integer icon, Integer icon2, TextSource input, int start, int end) {
		super(input, start, end);
		this.icon = icon;
		this.icon2 = icon2;
	}

	public Integer getIcon() {
		return icon;
	}
	public Integer getIcon2() {
		return icon2;
	}
}
