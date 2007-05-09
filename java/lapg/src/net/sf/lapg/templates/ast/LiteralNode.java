package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;

public class LiteralNode extends ExpressionNode {
	
	Object literal;

	public LiteralNode(Object literal) {
		this.literal = literal;
	}

	public Object resolve(Object context, ExecutionEnvironment env) {
		return literal;
	}
}
