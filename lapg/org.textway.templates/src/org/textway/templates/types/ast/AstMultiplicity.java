package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class AstMultiplicity extends AstNode {

	private Integer lo;
	private boolean hasNoUpperBound;
	private Integer hi;

	public AstMultiplicity(Integer lo, boolean hasNoUpperBound, Integer hi, TextSource input, int start, int end) {
		super(input, start, end);
		this.lo = lo;
		this.hasNoUpperBound = hasNoUpperBound;
		this.hi = hi;
	}

	public Integer getLo() {
		return lo;
	}
	public boolean getHasNoUpperBound() {
		return hasNoUpperBound;
	}
	public Integer getHi() {
		return hi;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for lo
		// TODO for hi
	}
}
