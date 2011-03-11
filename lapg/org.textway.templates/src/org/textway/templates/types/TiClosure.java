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
package org.textway.templates.types;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationCache;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.ExpressionNode;
import org.textway.templates.ast.ParameterNode;
import org.textway.templates.objects.DefaultIxObject;

/**
 * Gryaznov Evgeny, 3/11/11
 */
public class TiClosure extends DefaultIxObject {

	private final boolean isCached;
	private final ParameterNode[] parameters;
	private final ExpressionNode expression;
	private final EvaluationContext boundContext;
	private final IEvaluationStrategy env;

	public TiClosure(boolean cached, ParameterNode[] parameters, ExpressionNode expression, EvaluationContext boundContext, IEvaluationStrategy env) {
		this.isCached = cached;
		this.parameters = parameters;
		this.expression = expression;
		this.boundContext = boundContext;
		this.env = env;
	}

	@Override
	public Object callMethod(String methodName, Object[] args) throws EvaluationException {
		if ("invoke".equals(methodName)) {
			int paramCount = parameters != null ? parameters.length : 0, argsCount = args != null ? args.length : 0;

			if (paramCount != argsCount) {
				throw new EvaluationException("Wrong number of arguments used while calling closure `" + getType()
						+ "`: should be " + paramCount + " instead of " + argsCount);
			}

			Object result;
			if (isCached) {
				result = env.getCache().lookup(this, boundContext.getThisObject(), args);
				if (result != IEvaluationCache.MISSED) {
					return result;
				}
			}

			EvaluationContext context = new EvaluationContext(boundContext.getThisObject(), boundContext);
			for (int i = 0; i < paramCount; i++) {
				context.setVariable(parameters[i].getName(), args[i]);
			}
			result = env.evaluate(expression, context, true);
			if (isCached) {
				env.getCache().cache(result, this, boundContext.getThisObject(), args);
			}
			return result;
		}
		return super.callMethod(methodName, args);
	}

	@Override
	public Object getProperty(String propertyName) throws EvaluationException {
		if (parameters == null && "value".equals(propertyName)) {
			return callMethod("invoke", new Object[0]);
		}
		throw new EvaluationException("property `" + propertyName + "` is absent in `" + getType() + "`: o");
	}

	@Override
	public boolean is(String qualifiedName) throws EvaluationException {
		return "closure".equals(qualifiedName.toLowerCase());
	}

	@Override
	protected String getType() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				parameters[i].toString(sb);
			}
			sb.append(" ");
		}
		sb.append("=> .. }");
		return sb.toString();
	}
}
