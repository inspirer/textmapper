package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class ErrorNode extends ExpressionNode {

	protected ErrorNode(String input, int line) {
		super(input, line);
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
		throw new EvaluationException("illegal expression");
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append("<error>");
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationEnvironment env) {
		/* ignore, errors are emited on parser stage */
	}
}
