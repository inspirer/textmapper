package net.sf.lapg.templates.api;

import java.util.HashMap;

import net.sf.lapg.templates.ast.ExpressionNode;

public abstract class AbstractEnvironment implements IEvaluationEnvironment {

	HashMap<String,Object> vars = new HashMap<String,Object>();
	NavigationStrategyFactory strategies = new NavigationStrategyFactory();

	public Object getVariable(String id) {
		return vars.get(id);
	}

	public void setVariable(String id, Object value) {
		vars.put(id, value);
	}

	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.callMethod(obj, methodName, args);
	}

	public Object getByIndex(Object obj, Object index) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.getByIndex(obj, index);
	}

	public Object getByQuery(Object obj, String query) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.getByQuery(obj, query);
	}

	public Object getProperty(Object obj, String id) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.getProperty(obj, id);
	}

	public boolean toBoolean(Object o) {
		if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		} else if( o instanceof String ) {
			return ((String)o).length() > 0;
		}
		return o != null;
	}

	public Object evaluate(ExpressionNode expr, Object context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if( result == null && !permitNull ) {
				String message = "Evaluation of `"+expr.toString()+"` failed for " + getTitle(context) + ": null";
				EvaluationException ex = new HandledEvaluationException(message);
				fireError(expr, message);
				throw ex;
			}
			return result;
		} catch( HandledEvaluationException ex ) {
			throw ex;
		} catch( Throwable th ) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String message = "Evaluation of `"+expr.toString()+"` failed for " + getTitle(context) + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(expr, message);
			throw ex;
		}
	}

	public String executeTemplate(ILocatedEntity referer, String name, Object context, Object[] arguments) {
		return "";
	}

	public String executeTemplate(String name, Object context, Object[] arguments) {
		return executeTemplate(null, name, context, arguments);
	}

	public String getTitle(Object context) {
		if( context == null ) {
			return "<unknown>";
		}
		if( context instanceof INamedEntity ) {
			return ((INamedEntity)context).getTitle();
		}
		return context.getClass().getCanonicalName();
	}

	public void fireError(ILocatedEntity referer, String error) {
		if( referer != null ) {
			System.err.print(referer.getLocation() + ": ");
		}
		System.err.println(error);
	}

	public IStaticMethods getStaticMethods() {
		return null;
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}
}
