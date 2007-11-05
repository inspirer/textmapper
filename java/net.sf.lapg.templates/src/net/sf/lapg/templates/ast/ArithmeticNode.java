package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class ArithmeticNode extends ExpressionNode {

	public static final int PLUS = 1;
	public static final int MINUS = 2;
	public static final int MULT = 3;
	public static final int DIV = 4;
	public static final int REM = 5;

	private static String[] operators = new String[] { "", " + ", " - ", " * ", " / ", " % " };

	private int kind;
	private ExpressionNode leftExpr;
	private ExpressionNode rightExpr;

	public ArithmeticNode(int kind, ExpressionNode left, ExpressionNode right, int line) {
		super(line);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	private Object convertToInteger(Object s) {
		if( s instanceof String ) {
			return new Integer((String)s);
		}
		return s;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object left = convertToInteger(env.evaluate(leftExpr, context, false));
		Object right = convertToInteger(env.evaluate(rightExpr, context, false));

		if( left instanceof Integer && right instanceof Integer ) {
			int l = ((Integer)left).intValue(), r = ((Integer)right).intValue();
			switch( kind ) {
			case PLUS:
				return new Integer(l+r);
			case MINUS:
				return new Integer(l-r);
			case MULT:
				return new Integer(l*r);
			case DIV:
				return new Integer(l/r);
			case REM:
				return new Integer(l%r);
			}
		} else {
			throw new EvaluationException("arithmetic arguments should be integer");
		}

		return null;
	}

	@Override
	public void toString(StringBuffer sb) {
		leftExpr.toString(sb);
		sb.append(operators[kind]);
		rightExpr.toString(sb);
	}
}
