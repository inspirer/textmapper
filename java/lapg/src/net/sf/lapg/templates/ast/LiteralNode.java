package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;
import net.sf.lapg.templates.EvaluationException;

public class LiteralNode extends ExpressionNode {
	
	Object literal;

	public LiteralNode(Object literal) {
		this.literal = literal;
	}

	public Object evaluate(Object context, ExecutionEnvironment env) throws EvaluationException {
		return literal;
	}

	public String toString() {
		if( literal instanceof String)
			return "'" + literal.toString() + "'";
		return literal.toString();
	}
}
