package org.textway.templates.ast;

import java.util.List;

import org.textway.templates.ast.TemplatesTree.TextSource;

public class ElseIfNode extends CompoundNode {

	private final ExpressionNode condition;
	private final ElseIfNode next;

	protected ElseIfNode(ExpressionNode condition, List<Node> instructions, ElseIfNode next, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.condition = condition;
		this.next = next;
		setInstructions(instructions);
	}

	public ExpressionNode getCondition() {
		return condition;
	}

	public ElseIfNode getNext() {
		return next;
	}
}
