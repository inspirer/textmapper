package net.sf.lapg.templates.ast;

import java.util.ArrayList;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class CompoundNode extends Node {

	protected CompoundNode(String input, int line) {
		super(input, line);
	}

	protected ArrayList<Node> instructions;

	public ArrayList<Node> getInstructions() {
		return instructions;
	}

	public void setInstructions(ArrayList<Node> instructions) {
		this.instructions = instructions;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		if (instructions != null) {
			for (Node n : instructions) {
				n.emit(sb, context, env);
			}
		}
	}
}
