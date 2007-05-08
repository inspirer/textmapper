package net.sf.lapg.templates.ast;

public class ThisNode extends ExpressionNode {

	public Object resolve(Object context) {
		return context;
	}
}
