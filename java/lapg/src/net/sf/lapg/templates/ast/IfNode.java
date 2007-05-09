package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;

public class IfNode extends CompoundNode {
	ExpressionNode select;

	public IfNode(ExpressionNode select) {
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}

	protected void emit(StringBuffer sb, Object context, ExecutionEnvironment env) {
		Object value = select.resolve(context, env);
		if( env.toBoolean(value) ) {
			for( Node n : instructions ) {
				n.emit(sb, context, env);
			}
		}
	}
}
