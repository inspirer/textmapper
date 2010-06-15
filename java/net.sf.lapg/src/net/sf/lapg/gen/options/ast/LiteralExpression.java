package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class LiteralExpression extends AstOptNode implements IExpression {

	private String scon;
	private Integer icon;

	public LiteralExpression(String scon, Integer icon, TextSource input, int start, int end) {
		super(input, start, end);
		this.scon = scon;
		this.icon = icon;
	}

	public String getScon() {
		return scon;
	}
	public Integer getIcon() {
		return icon;
	}
}
