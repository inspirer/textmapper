/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IType;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.types.TiClosure;
import org.textmapper.templates.types.TiExpressionBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CreateClassNode extends ExpressionNode {

	private final String className;
	private final Map<String, ExpressionNode> fieldInitializers;

	protected CreateClassNode(String className, Map<String, ExpressionNode> fieldInitializers, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.className = className;
		this.fieldInitializers = fieldInitializers;
	}

	public String getClassName() {
		return className;
	}

	public Map<String, ExpressionNode> getFieldInitializers() {
		return fieldInitializers;
	}

	@Override
	public Object evaluate(final EvaluationContext context, final IEvaluationStrategy env) throws EvaluationException {
		return new TiExpressionBuilder<ExpressionNode>() {
			@Override
			public IClass resolveType(String className) {
				if (className.indexOf('.') == -1) {
					className = context.getCurrent().getPackage() + "." + className;
				}
				return env.getTypesRegistry().getClass(className, CreateClassNode.this);
			}

			@Override
			public Object resolve(ExpressionNode expression, IType type) {
				if (expression instanceof CreateClassNode) {
					CreateClassNode newexpr = (CreateClassNode) expression;
					return convertNew(newexpr, newexpr.getClassName(), newexpr.getFieldInitializers(), type);
				}
				if (expression instanceof ListNode) {
					ExpressionNode[] exprlist = ((ListNode) expression).getExpressions();
					List<ExpressionNode> content = exprlist == null ? Collections.<ExpressionNode>emptyList() : Arrays.asList(exprlist);
					return convertArray(expression, content, type);
				}
				if (expression instanceof LiteralNode) {
					Object literal = ((LiteralNode) expression).getLiteral();
					return convertLiteral(expression, literal, type);
				}

				Object result = null;
				try {
					result = env.evaluate(expression, context, true);
					if (result instanceof Boolean || result instanceof Number || result instanceof String) {
						return convertLiteral(expression, result, type);
					} else if (result instanceof TiClosure) {
						return convertClosure(expression, (TiClosure) result, type);
					}

					// TODO check type
				} catch (EvaluationException e) {
					/* ignore, error is already produced */
				}
				return result;
			}

			@Override
			public void report(ExpressionNode expression, String message) {
				env.report(TemplatesStatus.KIND_ERROR, message, expression);
			}
		}.resolve(this, null);
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("new ");
		sb.append(className);
		sb.append("(");
		int index = sb.length();
		if (fieldInitializers != null) {
			for (Entry<String, ExpressionNode> entry : fieldInitializers.entrySet()) {
				if (sb.length() > index) {
					sb.append(", ");
				}
				sb.append(entry.getKey());
				sb.append("=");
				entry.getValue().toString(sb);
			}
		}
		sb.append(")");
	}
}
