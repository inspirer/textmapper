package net.sf.lapg.templates.ast;

import java.util.ArrayList;

public class CaseNode extends CompoundNode {
	ExpressionNode caseExpr;

	public CaseNode(ExpressionNode caseExpr, int line) {
		super(line);
		this.caseExpr = caseExpr;
	}

	public ExpressionNode getExpression() {
		return caseExpr;
	}

	public void addInstruction(Node node) {
		if( instructions == null ) {
			instructions = new ArrayList<Node>();
		}
		instructions.add(node);
	}

	public static void add(ArrayList<CaseNode> cases, Node instruction ) {
		CaseNode node = cases.get(cases.size()-1);
		node.addInstruction(instruction);
	}
}
