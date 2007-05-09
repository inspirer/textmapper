package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;
import net.sf.lapg.templates.EvaluationException;

public abstract class ExpressionNode extends Node {
	
	protected void emit(StringBuffer sb, Object context, ExecutionEnvironment env) {
		try {
			sb.append(env.evaluate(this, context).toString());
		} catch( EvaluationException ex ) {
		}
	}
	
	public abstract Object evaluate(Object context, ExecutionEnvironment env) throws EvaluationException;
}