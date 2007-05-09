package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;
import net.sf.lapg.templates.EvaluationException;

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
	
	int kind;
	ExpressionNode leftExpr, rightExpr;
	
	public ConditionalNode(int kind, ExpressionNode left, ExpressionNode right) {
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	public Object evaluate(Object context, ExecutionEnvironment env) throws EvaluationException {
		Object right = env.evaluate(rightExpr, context);
		Object left = env.evaluate(leftExpr, context);

		switch( kind ) {
		case EQ:
			return safeEqual(left,right);
		case NE:
			return !safeEqual(left,right);
		case AND:
			return env.toBoolean(left) && env.toBoolean(right);
		case OR:
			return env.toBoolean(left) || env.toBoolean(right);
		}
		
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
			throw new RuntimeException("relational arguments should be integer");
		}
		
		return null;
	}
	
	private static boolean safeEqual(Object a, Object b) {
		if( a == null )
			return b == null;
		if( b == null )
			return false;
		
		return a.equals(b);
	}

	public String toString() {
		return leftExpr.toString() + operators[kind] + rightExpr.toString();
	}
}
