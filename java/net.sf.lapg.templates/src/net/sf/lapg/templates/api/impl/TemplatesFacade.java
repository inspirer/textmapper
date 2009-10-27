package net.sf.lapg.templates.api.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.TemplateSource;
import net.sf.lapg.templates.ast.AstParser;

public abstract class TemplatesFacade extends AbstractTemplateFacade {

	private HashSet<String> loadedPackages;

	private HashMap<String, ITemplate> templates;

	private ITemplateLoader[] loaders;

	public TemplatesFacade(INavigationStrategy.Factory strategy, ITemplateLoader... loaders) {
		super(strategy);
		this.templates = new HashMap<String, ITemplate>();
		this.loadedPackages = new HashSet<String>();
		this.loaders = loaders;

		if (loaders == null || loaders.length < 1) {
			throw new IllegalArgumentException("no loaders provided");
		}
	}

	private TemplateSource[] getContainerContent(String containerName) {
		List<TemplateSource> result = new LinkedList<TemplateSource>();
		for (ITemplateLoader loader : loaders) {
			TemplateSource source = loader.load(containerName, this);
			if (source != null) {
				result.add(source);
			}
		}
		return result.size() > 0 ? result.toArray(new TemplateSource[result.size()]) : null;
	}

	public void loadPackage(ILocatedEntity referer, String packageName) {
		if (loadedPackages.contains(packageName)) {
			return;
		}

		TemplateSource[] contents = getContainerContent(packageName);
		if (contents == null) {
			fireError(referer, "Couldn't load template package `" + packageName + "`");
			return;
		}

		for(int i = contents.length-1; i >= 0; i--) {
			ITemplate[] loaded = contents[i].getTemplates();
			if (loaded == null || loaded.length == 0) {
				fireError(referer, "Couldn't get templates from " + contents[i].getName());
				return;
			}

			for (ITemplate t : loaded) {
				// TODO
				String templateId = packageName + "." + t.getName();
				if (templates.containsKey(templateId)) {
					fireError(t, "Template `" + templateId + "` was already defined");
				}
				templates.put(templateId, t);
			}
		}
		loadedPackages.add(packageName);
	}

	private ITemplate getTemplate(ILocatedEntity referer, String qualifiedName) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			fireError(referer, "Fully qualified template name should contain dot.");
			return null;
		}

		String templatePackage = qualifiedName.substring(0, lastDot);
		loadPackage(referer, templatePackage);

		String resolvedName = qualifiedName;

		if (!templates.containsKey(resolvedName)) {
			fireError(referer, "Template `" + resolvedName + "` was not found in package `" + templatePackage + "`");
		}
		return templates.get(resolvedName);
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
			t = getTemplate(referer, name);
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

		ITemplate t = (loaded != null && loaded.length == 1 && loaded[0].getName().equals("inline")) ? loaded[0] : null;
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
