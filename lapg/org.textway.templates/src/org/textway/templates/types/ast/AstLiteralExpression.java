package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class AstLiteralExpression extends AstNode implements IAstExpression {

	private String scon;
	private Integer icon;
	private Boolean bcon;

	public AstLiteralExpression(String scon, Integer icon, Boolean bcon, TextSource input, int start, int end) {
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
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for scon
		// TODO for icon
		// TODO for bcon
	}
}
