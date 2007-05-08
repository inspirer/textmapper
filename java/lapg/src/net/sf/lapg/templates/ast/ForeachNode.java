package net.sf.lapg.templates.ast;

public class ForeachNode extends CompoundNode {
	String var;
	ExpressionNode select;
	
	public ForeachNode(String var, ExpressionNode select) {
		this.var = var;
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}

	public String getVar() {
		return var;
	}
}
