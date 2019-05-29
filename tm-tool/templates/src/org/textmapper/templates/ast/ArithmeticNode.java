/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
import org.textmapper.templates.objects.IxOperand;


public class ArithmeticNode extends ExpressionNode {

	public static final int PLUS = 1;
	public static final int MINUS = 2;
	public static final int MULT = 3;
	public static final int DIV = 4;
	public static final int REM = 5;

	private static String[] operators = new String[]{"", " + ", " - ", " * ", " / ", " % "};

	private final int kind;
	private final ExpressionNode leftExpr;
	private final ExpressionNode rightExpr;

	public ArithmeticNode(int kind, ExpressionNode left, ExpressionNode right,
						  TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.kind = kind;
		this.leftExpr = left;
		this.rightExpr = right;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		IxOperand left = env.asOperand(env.evaluate(leftExpr, context, false));
		Object right = env.evaluate(rightExpr, context, false);
		switch (kind) {
			case PLUS:
				return left.plus(right);
			case MINUS:
				return left.minus(right);
			case MULT:
				return left.multiply(right);
			case DIV:
				return left.div(right);
			case REM:
				return left.mod(right);
		}
		throw new IllegalStateException();
	}

	@Override
	public void toString(StringBuilder sb) {
		leftExpr.toString(sb);
		sb.append(operators[kind]);
		rightExpr.toString(sb);
	}
}
