package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.api.ITemplate;

public class TemplateNode extends CompoundNode implements ITemplate {
	private String name;
	private String[] parameters;
	private String templatePackage;

	public TemplateNode(String name, List<String> parameters, String templatePackage) {
		this.name = name;
		this.parameters = parameters != null ? parameters.toArray(new String[parameters.size()]) : null;
		this.templatePackage = templatePackage;
	}

	public String getName() {
		return name;
	}

	public String apply(Object context, IEvaluationEnvironment env, Object[] arguments) throws EvaluationException {
		int paramCount = parameters != null ? parameters.length : 0,
			argsCount = arguments != null ? arguments.length : 0;

		if( paramCount != argsCount ) {
			throw new EvaluationException("Wrong number of arguments used while calling `"+toString()+"`: should be " + paramCount + " instead of " + argsCount);
		}

		StringBuffer sb = new StringBuffer();
		if( paramCount > 0 ) {
			int i;
			Object[] old = new Object[paramCount];
			for( i = 0; i < paramCount; i++ ) {
				old[i] = env.getVariable(parameters[i]);
			}
			try {
				for( i = 0; i < paramCount; i++ ) {
					env.setVariable(parameters[i], arguments[i]);
				}

				emit(sb, context, env);
			} finally {
				for( i = 0; i < paramCount; i++ ) {
					env.setVariable(parameters[i], old[i]);
				}
			}
		} else {
			emit(sb, context, env);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		if( parameters != null ) {
			sb.append('(');
			for( int i = 0; i < parameters.length; i++ ) {
				if( i > 0 ) {
					sb.append(',');
				}
				sb.append(parameters[i]);
			}
			sb.append(')');
		}
		return sb.toString();
	}

	public String getPackage() {
		return templatePackage;
	}
}
