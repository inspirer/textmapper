package net.sf.lapg.gen.options.ast;

import net.sf.lapg.gen.options.OptdefTree.TextSource;

// defaultval ::= Ldefault expression (normal)
public class Defaultval extends AstOptNode {

	private IExpression expression;

	public Defaultval(IExpression expression, TextSource input, int start, int end) {
		super(input, start, end);
		this.expression = expression;
	}

	public IExpression getExpression() {
		return expression;
	}
}
