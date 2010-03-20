package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IQuery;

public class QueryNode extends Node implements IQuery {

	private final String name;
	private final String[] parameters;
	private final String templatePackage;
	private final ExpressionNode expr;
	private final boolean isCached;
	private IQuery base;

	public QueryNode(String name, List<String> parameters, String templatePackage, ExpressionNode expr, boolean cache, String input,
			int line) {
		super(input, line);
		int dot = name.lastIndexOf('.');
		this.name = dot > 0 ? name.substring(dot + 1) : name;
		if (templatePackage == null) {
			this.templatePackage = dot > 0 ? name.substring(0, dot) : "";
		} else {
			this.templatePackage = templatePackage;
		}
		this.parameters = parameters != null ? parameters.toArray(new String[parameters.size()]) : null;
		this.expr = expr;
		this.isCached = cache;
	}

	public int getKind() {
		return KIND_QUERY;
	}

	public String getName() {
		return name;
	}

	public String getPackage() {
		return templatePackage;
	}

	public Object invoke(EvaluationContext context, IEvaluationStrategy env, Object[] arguments)
			throws EvaluationException {
		int paramCount = parameters != null ? parameters.length : 0, argsCount = arguments != null ? arguments.length
				: 0;

		if (paramCount != argsCount) {
			throw new EvaluationException("Wrong number of arguments used while calling `" + toString()
					+ "`: should be " + paramCount + " instead of " + argsCount);
		}

		Object result;
		if(isCached) {
			result = env.getCache().lookup(this, context.getThisObject(), arguments);
			if(result != null) {
				return result;
			}
		}

		if (paramCount > 0) {
			int i;
			Object[] old = new Object[paramCount];
			for (i = 0; i < paramCount; i++) {
				old[i] = context.getVariable(parameters[i]);
			}
			try {
				for (i = 0; i < paramCount; i++) {
					context.setVariable(parameters[i], arguments[i]);
				}

				result = env.evaluate(expr, context, false);
			} finally {
				for (i = 0; i < paramCount; i++) {
					context.setVariable(parameters[i], old[i]);
				}
			}
		} else {
			result = env.evaluate(expr, context, false);
		}
		if(isCached) {
			env.getCache().cache(result, this, context.getThisObject(), arguments);
		}
		return result;
	}

	public String getSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		if (parameters != null) {
			sb.append('(');
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(parameters[i]);
			}
			sb.append(')');
		}
		return sb.toString();
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		/* declaration statement, nothing to emit */
	}

	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getSignature());
		sb.append(" = ");
		expr.toString(sb);
		return sb.toString();
	}

	public IBundleEntity getBase() {
		return base;
	}

	public void setBase(IBundleEntity template) {
		this.base = (IQuery) template;
	}
}
