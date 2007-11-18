package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class DollarNode extends Node {

	protected DollarNode(int line) {
		super(line);
	}

	@Override
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		sb.append("$");
	}
}
