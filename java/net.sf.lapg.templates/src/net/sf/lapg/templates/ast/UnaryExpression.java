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

public class UnaryExpression extends ExpressionNode {

	public static final int NOT = 1;
	public static final int MINUS = 2;

	private static String[] operators = new String[] { "", "! ", "- " };

	private final int kind;
	private final ExpressionNode expr;

	public UnaryExpression(int kind, ExpressionNode expr, String input, int line) {
		super(input, line);
		this.kind = kind;
		this.expr = expr;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		if (kind == NOT) {
			Object value = env.evaluate(expr, context, true);
			return !env.toBoolean(value);
		}

		if (kind == MINUS) {
			Object value = env.evaluate(expr, context, false);
			if (value instanceof Integer) {
				return -((Integer) value).intValue();
			} else {
				throw new EvaluationException("unary minus expression should be Integer");
			}
		}

		throw new EvaluationException("internal error: unknown kind");
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append(operators[kind]);
		expr.toString(sb);
	}
}
