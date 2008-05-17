package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ConditionalNode extends ExpressionNode {

	public static final int LT = 1;
	public static final int GT = 2;
	public static final int LE = 3;
	public static final int GE = 4;
	public static final int EQ = 5;
	public static final int NE = 6;
	public static final int AND = 7;
	public static final int OR = 8;

	private static String[] operators = new String[] { "", " < ", " > ", " <= ", " >= ", " == ", " != ", " && ", " || " };

	private int kind;
	private ExpressionNode leftExpr;
	private ExpressionNode rightExpr;

	public ConditionalNode(int kind, ExpressionNode left, ExpressionNode right, String input, int line) {
		super(input, line);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
		Object left = env.evaluate(leftExpr, context, false);

		switch( kind ) {
		case EQ:
			return safeEqual(left,env.evaluate(rightExpr, context, false));
		case NE:
			return !safeEqual(left,env.evaluate(rightExpr, context, false));
		case AND:
			return env.toBoolean(left) && env.toBoolean(env.evaluate(rightExpr, context, false));
		case OR:
			return env.toBoolean(left) || env.toBoolean(env.evaluate(rightExpr, context, false));
		}

		Object right = env.evaluate(rightExpr, context, false);

		if( left instanceof Integer && right instanceof Integer ) {
			int l = ((Integer)left).intValue(), r = ((Integer)right).intValue();
			switch( kind ) {
			case LT:
				return l < r;
			case LE:
				return l <= r;
			case GE:
				return l >= r;
			case GT:
				return l > r;
			}
		} else {
			throw new EvaluationException("relational arguments should be integer");
		}

		throw new EvaluationException("internal error: unknown kind");
	}

	public static boolean safeEqual(Object a, Object b) {
		if( a == null ) {
			return b == null;
		}
		if( b == null ) {
			return false;
		}

		return a.equals(b);
	}

	@Override
	public void toString(StringBuffer sb) {
		leftExpr.toString(sb);
		sb.append(operators[kind]);
		rightExpr.toString(sb);
	}
}
