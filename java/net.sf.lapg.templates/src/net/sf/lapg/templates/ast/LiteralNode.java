package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class LiteralNode extends ExpressionNode {

	private Object literal;

	public LiteralNode(Object literal, String input, int line) {
		super(input, line);
		this.literal = literal;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
		return literal;
	}

	@Override
	public void toString(StringBuffer sb) {
		if( literal == null ) {
			sb.append("null");
		} else if( literal instanceof String) {
			sb.append("'");
			sb.append(literal.toString());
			sb.append("'");
		} else {
			sb.append(literal.toString());
		}
	}
}
