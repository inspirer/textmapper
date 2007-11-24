package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public abstract class ExpressionNode extends Node {

	protected ExpressionNode(int line) {
		super(line);
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationEnvironment env) {
		try {
			sb.append(env.evaluate(this, context, false).toString());
		} catch( EvaluationException ex ) {
		}
	}

	public abstract Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException;

	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	public abstract void toString(StringBuffer sb);
}