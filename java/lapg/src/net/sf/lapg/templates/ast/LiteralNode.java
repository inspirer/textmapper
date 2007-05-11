package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class LiteralNode extends ExpressionNode {
	
	Object literal;

	public LiteralNode(Object literal) {
		this.literal = literal;
	}

	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		return literal;
	}

	public String toString() {
		if( literal instanceof String)
			return "'" + literal.toString() + "'";
		return literal.toString();
	}
}
