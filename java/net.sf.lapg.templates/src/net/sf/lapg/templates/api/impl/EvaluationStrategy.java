package net.sf.lapg.templates.api.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INamedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.ast.AstParser;
import net.sf.lapg.templates.ast.ExpressionNode;

public abstract class EvaluationStrategy implements IEvaluationStrategy {

	private final TemplatesRegistry registry;

	private final INavigationStrategy.Factory strategies;

	public EvaluationStrategy(INavigationStrategy.Factory factory, ITemplateLoader... loaders) {
		this.strategies = factory;
		factory.setTemplatesFacade(this);
		registry = new TemplatesRegistry(this, loaders);
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

	public String getTitle(Object object) {
		if( object == null ) {
			return "<unknown>";
		}
		if( object instanceof INamedEntity ) {
			return ((INamedEntity)object).getTitle();
		}
		return object.getClass().getCanonicalName();
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		ITemplate t = null;
		boolean isBase = false;
		if (name.equals("base")) {
			ITemplate current = context.getCurrentTemplate();
			if (current != null) {
				isBase = true;
				t = current.getBase();
				if (t == null) {
					fireError(referer, "Cannot find base template for `" + current.getName() + "`");
				}
			}
		}
		if(!isBase) {
			t = registry.getTemplate(referer, name);
		}
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public String evaluateTemplate(ILocatedEntity referer, String template, String templateId, EvaluationContext context) {
		AstParser p = new AstParser() {
			@Override
			public void error(String s) {
				EvaluationStrategy.this.fireError(null, inputName + ":" + s);
			}
		};
		ITemplate[] loaded = null;
		if (!p.parseBody(template, "syntax", templateId != null ? templateId : referer.getLocation())) {
			loaded = new ITemplate[0];
		} else {
			loaded = p.getResult();
		}

		ITemplate t = loaded != null && loaded.length == 1 && loaded[0].getName().equals("inline") ? loaded[0] : null;
		if (t == null) {
			return "";
		}
		try {
			return t.apply(context, this, null);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public void createFile(String name, String contents) {
		// do nothing by default
	}

	public Iterator<?> getCollectionIterator(Object o) {
		if (o instanceof Collection<?>) {
			return ((Collection<?>) o).iterator();
		}
		if (o instanceof Object[]) {
			return new ArrayIterator((Object[]) o);
		}
		return null;
	}

	private static class ArrayIterator implements Iterator<Object> {

		Object[] elements;

		int index;

		int lastElement;

		public ArrayIterator(Object[] elements) {
			this(elements, 0, elements.length - 1);
		}

		public ArrayIterator(Object[] elements, int firstElement, int lastElement) {
			this.elements = elements;
			index = firstElement;
			this.lastElement = lastElement;
		}

		public boolean hasNext() {
			return elements != null && index <= lastElement;
		}

		public Object next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return elements[index++];
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
