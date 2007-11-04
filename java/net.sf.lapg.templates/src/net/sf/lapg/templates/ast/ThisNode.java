package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ThisNode extends ExpressionNode {

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) {
		return context;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append("this");
	}
}
