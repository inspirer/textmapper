/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
import org.textmapper.templates.api.ITemplate;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.bundle.IBundleEntity;

import java.util.List;

public class TemplateNode extends CompoundNode implements ITemplate {
	private final String name;
	private final ParameterNode[] parameters;
	private final String templatePackage;
	private ITemplate base;

	public TemplateNode(String name, List<ParameterNode> parameters,
						String templatePackage, TextSource source, int offset, int endoffset) {
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
	}

	@Override
	public int getKind() {
		return KIND_TEMPLATE;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String apply(EvaluationContext context, IEvaluationStrategy env, Object[] arguments)
			throws EvaluationException {
		int paramCount = parameters != null ? parameters.length : 0;
		int argsCount = arguments != null ? arguments.length : 0;

		if (paramCount != argsCount) {
			throw new EvaluationException("Wrong number of arguments used while calling `"
					+ toString() + "`: should be " + paramCount + " instead of " + argsCount);
		}

		StringBuilder sb = new StringBuilder();
		if (paramCount > 0) {
			for (int i = 0; i < paramCount; i++) {
				context.setVariable(parameters[i].getName(),
						arguments[i] != null ? arguments[i] : EvaluationContext.NULL_VALUE);
			}
		}
		emit(sb, context, env);
		return sb.toString();
	}

	@Override
	public String toString() {
		return getSignature();
	}

	@Override
	public void toJavascript(StringBuilder sb) {
		sb.append("function ").append(getSignature()).append(" {\n");
		if (instructions == null || instructions.isEmpty()) {
			sb.append("  return '';");
		} else {
			sb.append("  return ");
			for (int i = 0; i < instructions.size(); i++) {
				if (i > 0) {
					sb.append(" + \n\t\t");
				}
				instructions.get(i).toJavascript(sb);
			}
			sb.append(";");
		}
		sb.append("}\n\n");
	}

	@Override
	public String getPackage() {
		return templatePackage;
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
	public IBundleEntity getBase() {
		return base;
	}

	@Override
	public void setBase(IBundleEntity template) {
		this.base = (ITemplate) template;
	}
}
