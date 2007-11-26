package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class DollarNode extends Node {

	protected DollarNode(String input, int line) {
		super(input, line);
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationEnvironment env) {
		Object value = context.getVariable("$");
		if( value != null ) {
			sb.append(value.toString());
		} else {
			sb.append("$");
		}
	}
}
