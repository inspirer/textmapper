package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public abstract class ExpressionNode extends Node {
	
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		try {
			sb.append(env.evaluate(this, context, false).toString());
		} catch( EvaluationException ex ) {
		}
	}
	
	public abstract Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException;
}