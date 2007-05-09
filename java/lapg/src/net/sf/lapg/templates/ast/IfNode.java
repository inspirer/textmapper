package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;
import net.sf.lapg.templates.EvaluationException;

public class IfNode extends CompoundNode {
	ExpressionNode select;

	public IfNode(ExpressionNode select) {
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}

	protected void emit(StringBuffer sb, Object context, ExecutionEnvironment env) {
		try {
			if( env.toBoolean(env.evaluate(select, context)) ) {
				for( Node n : instructions ) {
					n.emit(sb, context, env);
				}
			}
		} catch( EvaluationException ex ) {
			/* ignore, skip if */
		}
	}
}
