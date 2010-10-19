/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.ast.AstTree.TextSource;


public class ArithmeticNode extends ExpressionNode {

	public static final int PLUS = 1;
	public static final int MINUS = 2;
	public static final int MULT = 3;
	public static final int DIV = 4;
	public static final int REM = 5;

	private static String[] operators = new String[] { "", " + ", " - ", " * ", " / ", " % " };

	private final int kind;
	private final ExpressionNode leftExpr;
	private final ExpressionNode rightExpr;

	public ArithmeticNode(int kind, ExpressionNode left, ExpressionNode right, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	private Object convertToInteger(Object obj) {
		if( obj instanceof String ) {
            String s = (String) obj;
            if(s.length() > 0
                    && Character.isDigit(s.charAt(s.length()-1)) 
                    && (Character.isDigit(s.charAt(0)) || s.charAt(0) == '-')) {
                try {
                    return new Integer(s);
                } catch(NumberFormatException ex) {
                    /* ignore */
                }
            }
		}
		return obj;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
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
			} else if( left instanceof String || right instanceof String ) {
				return left.toString() + right.toString();
			} else {
				throw new EvaluationException("arguments of plus should be arrays, strings or integers ("+left.getClass().getCanonicalName() + " + "+right.getClass().getCanonicalName()+")");
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
