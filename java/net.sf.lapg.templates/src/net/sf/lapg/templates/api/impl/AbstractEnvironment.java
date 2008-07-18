package net.sf.lapg.templates.api.impl;

import java.util.Collection;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INamedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.ast.ExpressionNode;

public abstract class AbstractEnvironment implements IEvaluationEnvironment {

	private final INavigationStrategy.Factory strategies;

	public AbstractEnvironment(INavigationStrategy.Factory factory) {
		this.strategies = factory;
		factory.setEnvironment(this);
	}

	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.callMethod(obj, methodName, args);
	}

	public Object getByIndex(Object obj, Object index) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.getByIndex(obj, index);
	}

	public Object getProperty(Object obj, String id) throws EvaluationException {
		INavigationStrategy strategy = strategies.getStrategy(obj);
		return strategy.getProperty(obj, id);
	}

	public boolean toBoolean(Object o) {
		if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		} else if( o instanceof String ) {
			return ((String)o).trim().length() > 0;
		}
		return o != null;
	}

	public String toString(Object o, ExpressionNode referer) throws EvaluationException {
		if( o instanceof Collection || o instanceof Object[] ) {
			String message = "Evaluation of `"+referer.toString()+"` results in collection, cannot convert to String";
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(referer, message);
			throw ex;
		}
		return o.toString();
	}

	public Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if( result == null && !permitNull ) {
				String message = "Evaluation of `"+expr.toString()+"` failed for " + getTitle(context.getThisObject()) + ": null";
				EvaluationException ex = new HandledEvaluationException(message);
				fireError(expr, message);
				throw ex;
			}
			return result;
		} catch( HandledEvaluationException ex ) {
			throw ex;
		} catch( Throwable th ) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String message = "Evaluation of `"+expr.toString()+"` failed for " + getTitle(context.getThisObject()) + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(expr, message);
			throw ex;
		}
	}

	public String executeTemplate(ILocatedEntity referer, String name, EvaluationContext context, Object[] arguments) {
		return "";
	}

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments) {
		return executeTemplate(null, name, context, arguments);
	}

	public String getTitle(Object object) {
		if( object == null ) {
			return "<unknown>";
		}
		if( object instanceof INamedEntity ) {
			return ((INamedEntity)object).getTitle();
		}
		return object.getClass().getCanonicalName();
	}

	public void fireError(ILocatedEntity referer, String error) {
		if( referer != null ) {
			System.err.print(referer.getLocation() + ": ");   // FIXME
		}
		System.err.println(error);
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}
}
