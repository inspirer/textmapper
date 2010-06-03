package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

// literal_expression ::= scon (normal)
// literal_expression ::= icon (normal)
public class LiteralExpression extends AstOptNode implements IExpression {

	private String scon;
	private Integer icon;

	public LiteralExpression(TextSource input, int start, int end) {
		super(input, start, end);
	}

	public String getScon() {
		return scon;
	}
	public Integer getIcon() {
		return icon;
	}
}
