package net.sf.lapg.templates.ast;

public class IfNode extends CompoundNode {
	ExpressionNode select;

	public IfNode(ExpressionNode select) {
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}
}
