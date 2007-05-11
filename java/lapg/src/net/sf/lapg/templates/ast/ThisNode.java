package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ThisNode extends ExpressionNode {

	public Object evaluate(Object context, IEvaluationEnvironment env) {
		return context;
	}

	public String toString() {
		return "this";
	}
}
