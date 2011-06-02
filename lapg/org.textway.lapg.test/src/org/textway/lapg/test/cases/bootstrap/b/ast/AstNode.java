package org.textway.lapg.test.cases.bootstrap.b.ast;

import org.textway.lapg.test.cases.bootstrap.b.SampleBTree.TextSource;

public abstract class AstNode implements IAstNode {
	
	protected TextSource fInput;
	protected int fStart;
	protected int fEnd;

	public AstNode(TextSource input, int start, int end) {
		this.fStart = start;
		this.fEnd = end;
		this.fInput = input;
	}

	public int getOffset() {
		return fStart;
	}

	public int getEndOffset() {
		return fEnd;
	}

	public TextSource getInput() {
		return fInput;
	}

	public String toString() {
		return fInput == null ? "" : fInput.getText(fStart, fEnd);
	}

	//public abstract void accept(Visitor v);
}

