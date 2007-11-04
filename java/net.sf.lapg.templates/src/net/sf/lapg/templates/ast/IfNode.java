package net.sf.lapg.templates.ast;

import java.util.ArrayList;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class IfNode extends CompoundNode {
	private ExpressionNode select;
	private ArrayList<Node> elseInstructions;

	public IfNode(ExpressionNode select) {
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}

	public void setElseInstructions(ArrayList<Node> elseInstructions) {
		this.elseInstructions = elseInstructions;
	}

	@Override
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		try {
			ArrayList<Node> execute = env.toBoolean(env.evaluate(select, context, true)) ? instructions
					: elseInstructions;
			if (execute != null) {
				for (Node n : execute) {
					n.emit(sb, context, env);
				}
			}
		} catch (EvaluationException ex) {
			/* ignore, skip if */
		}
	}
}
