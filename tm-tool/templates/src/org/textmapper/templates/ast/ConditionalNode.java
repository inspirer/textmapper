/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
package org.textmapper.templates.ast;

import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.ast.TemplatesTree.TextSource;

public class ConditionalNode extends ExpressionNode {

	public static final int LT = 1;
	public static final int GT = 2;
	public static final int LE = 3;
	public static final int GE = 4;
	public static final int EQ = 5;
	public static final int NE = 6;
	public static final int AND = 7;
	public static final int OR = 8;

	private static String[] operators = new String[]{
			"", " < ", " > ", " <= ", " >= ", " == ", " != ", " && ", " || "};

	private final int kind;
	private final ExpressionNode leftExpr;
	private final ExpressionNode rightExpr;

	public ConditionalNode(int kind, ExpressionNode left, ExpressionNode right,
						   TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		Object leftVal = env.evaluate(leftExpr, context, kind == AND || kind == OR);
		switch (kind) {
			case LT:
			case GE:
			case GT:
			case LE: {
				int result = env.asOperand(leftVal).compareTo(
						env.evaluate(rightExpr, context, false));
				switch (kind) {
					case LT:
						return result < 0;
					case LE:
						return result <= 0;
					case GT:
						return result > 0;
					case GE:
						return result >= 0;
				}
			}
			case EQ:
			case NE: {
				boolean equals = env.asOperand(leftVal).equalsTo(
						env.evaluate(rightExpr, context, false));
				return kind == EQ ? equals : !equals;
			}
			case AND:
				return env.asAdaptable(leftVal).asBoolean()
						&& env.asAdaptable(env.evaluate(rightExpr, context, true)).asBoolean();
			case OR:
				return env.asAdaptable(leftVal).asBoolean()
						|| env.asAdaptable(env.evaluate(rightExpr, context, true)).asBoolean();
		}
		throw new EvaluationException("internal error: unknown kind");
	}

	public static boolean safeEqual(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}

		return a.equals(b);
	}

	@Override
	public void toString(StringBuilder sb) {
		leftExpr.toString(sb);
		sb.append(operators[kind]);
		rightExpr.toString(sb);
	}
}
