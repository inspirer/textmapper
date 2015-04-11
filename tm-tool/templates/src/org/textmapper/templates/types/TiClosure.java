/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.templates.types;

import org.textmapper.templates.api.*;
import org.textmapper.templates.api.types.IClosureType;
import org.textmapper.templates.api.types.IType;
import org.textmapper.templates.ast.ExpressionNode;
import org.textmapper.templates.ast.ParameterNode;
import org.textmapper.templates.objects.DefaultIxObject;

import java.util.Collection;

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

	public int getParametersCount() {
		return parameters == null ? 0 : parameters.length;
	}

	@Override
	public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
		if ("invoke".equals(methodName)) {
			int paramCount = getParametersCount(), argsCount = args != null ? args.length : 0;

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

			EvaluationContext context = new EvaluationContext(boundContext.getThisObject(), caller, boundContext);
			for (int i = 0; i < paramCount; i++) {
				context.setVariable(parameters[i].getName(), args[i]);
			}
			result = env.evaluate(expression, context, true);
			if (isCached) {
				env.getCache().cache(result, this, boundContext.getThisObject(), args);
			}
			return result;
		}
		return super.callMethod(caller, methodName, args);
	}

	@Override
	public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
		if (parameters == null && "value".equals(propertyName)) {
			return callMethod(caller, "invoke");
		}
		throw new EvaluationException("property `" + propertyName + "` is absent in `" + getType() + "`: o");
	}

	@Override
	public boolean is(String qualifiedName) throws EvaluationException {
		return "closure".equals(qualifiedName.toLowerCase());
	}

	public boolean matches(IClosureType type) {
		Collection<IType> parameterTypes = type.getParameterTypes();
		if(getParametersCount() != parameterTypes.size()) {
			return false;
		}

		// FIXME check types

		return true;
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
