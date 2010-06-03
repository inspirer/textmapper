package net.sf.lapg.gen.options.ast;

// literal_expression ::= scon (normal)
// literal_expression ::= icon (normal)
public class LiteralExpression implements IExpression {

	private String scon;
	private Integer icon;

	public String getScon() {
		return scon;
	}
	public Integer getIcon() {
		return icon;
	}
}
