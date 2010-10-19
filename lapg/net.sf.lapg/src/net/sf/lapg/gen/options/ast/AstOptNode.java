package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

public abstract class AstOptNode implements IAstOptNode {
	
	protected TextSource fInput;
	protected int fStart;
	protected int fEnd;

	public AstOptNode(TextSource input, int start, int end) {
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

