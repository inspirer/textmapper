package net.sf.lapg.templates.api.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.ast.AstParser;

public abstract class TemplatesFacade extends AbstractTemplateFacade {

	private final TemplatesRegistry registry;

	public TemplatesFacade(INavigationStrategy.Factory strategy, ITemplateLoader... loaders) {
		super(strategy);
		registry = new TemplatesRegistry(this, loaders);
	}

	@Override
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
				TemplatesFacade.this.fireError(null, inputName + ":" + s);
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
