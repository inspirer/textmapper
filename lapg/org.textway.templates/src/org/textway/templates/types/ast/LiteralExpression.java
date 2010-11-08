package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class LiteralExpression extends AstNode implements IExpression {

	private String scon;
	private Integer icon;
	private Boolean bcon;

	public LiteralExpression(String scon, Integer icon, Boolean bcon, TextSource input, int start, int end) {
		super(input, start, end);
		this.scon = scon;
		this.icon = icon;
		this.bcon = bcon;
	}

	public String getScon() {
		return scon;
	}
	public Integer getIcon() {
		return icon;
	}
	public Boolean getBcon() {
		return bcon;
	}
}
