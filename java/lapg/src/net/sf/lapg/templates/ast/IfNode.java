package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class IfNode extends CompoundNode {
	ExpressionNode select;

	public IfNode(ExpressionNode select) {
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}

	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		try {
			if( env.toBoolean(env.evaluate(select, context, true)) ) {
				for( Node n : instructions ) {
					n.emit(sb, context, env);
				}
			}
		} catch( EvaluationException ex ) {
			/* ignore, skip if */
		}
	}
}
