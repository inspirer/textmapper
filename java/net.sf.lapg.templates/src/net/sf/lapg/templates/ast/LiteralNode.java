package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class LiteralNode extends ExpressionNode {

	private Object literal;

	public LiteralNode(Object literal) {
		this.literal = literal;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		return literal;
	}

	@Override
	public void toString(StringBuffer sb) {
		if( literal instanceof String) {
			sb.append("'");
			sb.append(literal.toString());
			sb.append("'");
		} else {
			sb.append(literal.toString());
		}
	}
}
