package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ThisNode extends ExpressionNode {

	protected ThisNode(int line) {
		super(line);
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) {
		return context;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append("this");
	}
}
