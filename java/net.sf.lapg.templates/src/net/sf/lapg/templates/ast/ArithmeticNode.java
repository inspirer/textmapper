package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
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

	public ArithmeticNode(int kind, ExpressionNode left, ExpressionNode right, String input, int line) {
		super(input, line);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	private Object convertToInteger(Object s) {
		if( s instanceof String ) {
			try {
				return new Integer((String)s);
			} catch(NumberFormatException ex) {
				/* ignore */
			}
		}
		return s;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
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
		} else if( kind == PLUS ) {
			if( left instanceof Object[] && right instanceof Object[] ) {
				return concatenate((Object[])left,(Object[])right);
			} else if( left instanceof String && right instanceof String ) {
				return ((String)left) + ((String)right);
			} else {
				throw new EvaluationException("arguments of plus should be arrays, strings or integers");
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

	private static Object[] concatenate(Object[] a1, Object[] a2) {
		int a1Len= a1.length;
		int a2Len= a2.length;
		if (a1Len == 0) {
			return a2;
		}
		if (a2Len == 0) {
			return a1;
		}
		Object[] res= new Object[a1Len + a2Len];
		System.arraycopy(a1, 0, res, 0, a1Len);
		System.arraycopy(a2, 0, res, a1Len, a2Len);
		return res;
	}
}
