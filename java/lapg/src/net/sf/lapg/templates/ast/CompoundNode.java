package net.sf.lapg.templates.ast;

import java.util.ArrayList;

public class CompoundNode extends Node {

	protected ArrayList<Node> instructions;

	public ArrayList<Node> getInstructions() {
		return instructions;
	}

	public void setInstructions(ArrayList<Node> instructions) {
		this.instructions = instructions;
	}

	protected void emit(StringBuffer sb, Object context) {
		for( Node n : instructions ) {
			n.emit(sb, context);
		}
	}
}
