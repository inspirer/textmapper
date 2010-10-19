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

	private final int kind;
	private final ExpressionNode leftExpr;
	private final ExpressionNode rightExpr;

	public ConditionalNode(int kind, ExpressionNode left, ExpressionNode right, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object left = env.evaluate(leftExpr, context, kind == AND || kind == OR);

		switch( kind ) {
		case EQ:
			return safeEqual(left,env.evaluate(rightExpr, context, false));
		case NE:
			return !safeEqual(left,env.evaluate(rightExpr, context, false));
		case AND:
			return env.toBoolean(left) && env.toBoolean(env.evaluate(rightExpr, context, true));
		case OR:
			return env.toBoolean(left) || env.toBoolean(env.evaluate(rightExpr, context, true));
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
