/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

import org.textmapper.templates.api.*;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.bundle.IBundleEntity;

import java.util.LinkedList;
import java.util.List;

public class QueryNode extends Node implements IQuery {

	private final String name;
	private final ParameterNode[] parameters;
	private final String templatePackage;
	private final ExpressionNode expr;
	private final boolean isCached;
	private IQuery base;

	public QueryNode(String name, List<ParameterNode> parameters,
					 String templatePackage, ExpressionNode expr, boolean cache,
					 TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		int dot = name.lastIndexOf('.');
		this.name = dot > 0 ? name.substring(dot + 1) : name;
		if (templatePackage == null) {
			this.templatePackage = dot > 0 ? name.substring(0, dot) : "";
		} else {
			this.templatePackage = templatePackage;
		}
		this.parameters = parameters != null ?
				parameters.toArray(new ParameterNode[parameters.size()]) : null;
		this.expr = expr;
		this.isCached = cache;
	}

	@Override
	public int getKind() {
		return KIND_QUERY;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPackage() {
		return templatePackage;
	}

	@Override
	public Object invoke(EvaluationContext context, IEvaluationStrategy env, Object[] arguments)
			throws EvaluationException {
		int paramCount = parameters != null ? parameters.length : 0, argsCount = arguments != null
				? arguments.length
				: 0;

		if (paramCount != argsCount) {
			throw new EvaluationException("Wrong number of arguments used while calling `"
					+ toString() + "`: should be " + paramCount + " instead of " + argsCount);
		}

		Object result;
		if (isCached) {
			result = env.getCache().lookup(this, context.getThisObject(), arguments);
			if (result != IEvaluationCache.MISSED) {
				return result;
			}
		}

		if (paramCount > 0) {
			for (int i = 0; i < paramCount; i++) {
				context.setVariable(parameters[i].getName(),
						arguments[i] != null ? arguments[i] : EvaluationContext.NULL_VALUE);
			}
		}
		result = env.evaluate(expr, context, true);
		if (isCached) {
			env.getCache().cache(result, this, context.getThisObject(), arguments);
		}
		return result;
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append('(');
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				parameters[i].toString(sb);
			}
		}
		sb.append(')');
		return sb.toString();
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		/* declaration statement, nothing to emit */
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getSignature());
		sb.append(" = ");
		expr.toString(sb);
		return sb.toString();
	}

	@Override
	public void toJavascript(StringBuilder sb) {
		sb.append("function ").append(getSignature()).append(" {\n");
		LinkedList<Node> queue = new LinkedList<>();
		queue.add(expr);

		Node head;
		while ((head = queue.poll()) != null) {
			if (head instanceof CommaNode) {
				queue.addFirst(((CommaNode) head).rightExpr);
				queue.addFirst(((CommaNode) head).leftExpr);
				continue;
			}
			sb.append(queue.isEmpty() ? "  return " : "  ");
			head.toJavascript(sb);
			sb.append(";\n");
		}
		sb.append("}\n\n");
	}

	@Override
	public IBundleEntity getBase() {
		return base;
	}

	@Override
	public void setBase(IBundleEntity template) {
		this.base = (IQuery) template;
	}
}
